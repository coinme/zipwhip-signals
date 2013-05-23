package com.zipwhip.signals.features;

import com.zipwhip.concurrent.ObservableFuture;
import com.zipwhip.events.Observer;
import com.zipwhip.framework.Feature;
import com.zipwhip.framework.pubsub.AnnotationManager;
import com.zipwhip.framework.pubsub.Subscribe;
import com.zipwhip.signals.*;
import com.zipwhip.signals.address.Address;
import com.zipwhip.signals.address.ClientAddress;
import com.zipwhip.signals.address.ServerAddress;
import com.zipwhip.signals.discovery.ClusterDiscoveryService;
import com.zipwhip.util.CollectionUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Date: 5/18/13
 * Time: 10:55 PM
 *
 * This feature watches the events from SocketIo and determines if they truly connect|disconnect events. It's possible
 * for a clientId to be shared across multiple servers, so we need to only announce the events if we're the
 * first-on-connect or the last-on-disconnect to fire.
 *
 * The SocketIoFeature runs the onConnect/onDisconnect logic inside the Netty worker thread pool. It's a very tight/fast
 * process. We're a little slower, so we're running in the Application thread pool. If Zookeeper is down, we're going to
 * block for a while per event.
 *
 * TODO: We need to figure out how to retry later instead of just quitting on an error.
 *
 * @author Michael
 * @version 1
 */
public class ClientConnectionChangedFeature extends Feature<SignalServerConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnectionChangedFeature.class);
    private static final String MESSAGE_CONVERTER = "com.zipwhip.signals.ClientAddressConverter";

    static {
        AnnotationManager.register(MESSAGE_CONVERTER, new ClientAddressConverter());
    }

    @Autowired
    ClientRegistry clientRegistry;

    @Autowired
    CuratorFramework curator;

    @Autowired
    Topology topology;

    @Autowired
    AtomicEventProvider atomicEventProvider;

    @Autowired
    ClusterDiscoveryService clusterDiscoveryService;

    @Subscribe(uri = "/sockets/connection/changed", converter = MESSAGE_CONVERTER)
    public void onConnectionChanged(Collection<ClientAddress> addresses) {
        if (CollectionUtil.isNullOrEmpty(addresses)) {
            LOGGER.warn("Empty address set");
            return;
        }

        for (ClientAddress address : addresses) {
            try {
                onConnectionChanged(address);
            } catch (Exception e) {
                LOGGER.error("Exception processing change event " + address, e);
            }
        }
    }

    private void onConnectionChanged(final ClientAddress clientAddress) throws Exception {
        final ServerAddress serverAddress = parent.getConfiguration().getServerAddress();

        // we need to cluster lock on the clientId
        // the system will not let us unlock it from a different thread.
        final InterProcessMutex mutex = new InterProcessMutex(curator, "/locks/clients/" + clientAddress.getClientId());

        try {
            if (!mutex.acquire(10, TimeUnit.SECONDS)) {
                LOGGER.error("Unable to acquire lock in 10 seconds. Giving up???!");
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Exception with mutex", e);
        }

        try {
            // we need to determine if this is a connect or disconnect.
            // we're going to look at the ClientRegistry to see if they are currently connected.
            // If they are currently connected, it was a connect event.
            // If not, it was a disconnect.
            boolean connectEvent = isConnectedToRegistry(clientAddress);

            if (connectEvent) {
                // block for 5 seconds
                // will crash if no modification made.
                awaitOrFail(
                        topology.add(clientAddress, serverAddress)
                );
            } else {
                // block for 5 seconds
                // will crash if no modification made.
                awaitOrFail(
                        topology.remove(clientAddress, serverAddress)
                );
            }

            // now we need to determine if we're the ones to notify.
            // block for 5 seconds
            Set<Address> servers = awaitOrFail(
                    topology.get(clientAddress)
            );

            if (connectEvent) {
                if (CollectionUtil.isNullOrEmpty(servers)) {
                    LOGGER.error("Server list was null/empty. Sanity check failure. We should have been in the list.");
                    return;
                }
            }

            // clean the list (remove dead servers)
            // modifies the server and the local set.
            cleanDeadServers(clientAddress, servers);

            if (connectEvent) {
                if (CollectionUtil.isNullOrEmpty(servers)) {
                    LOGGER.error("Server list was null/empty. Sanity check failure. The cleaner cleaned us out!");
                    return;
                }

                if (!servers.contains(serverAddress)) {
                    LOGGER.error("We should have been in the list. The cleaner must have cleaned us out.");
                    return;
                }

                if (servers.size() != 1) {
                    LOGGER.debug("Server list contained more than 1 entry. We're not alone. Ignoring the event. Not announcing to group.");
                    return;
                }

                // Announce to the group. We attach the eventId so clients can 100% trust ordering of events.
                publish("/client/connected",
                        atomicEventProvider.create(clientAddress));
            } else {
                // it should be empty.
                if (CollectionUtil.exists(servers)) {
                    LOGGER.debug("Server list was not empty. Must still be connected somewhere else.");
                    return;
                }

                // Announce to the group. We attach the eventId so clients can 100% trust ordering of events.
                publish("/client/disconnected",
                        atomicEventProvider.create(clientAddress));
            }
        } catch (Throwable throwable) {
            LOGGER.error("Exception processing work", throwable);
        } finally {
            try {
                mutex.release();
            } catch (Exception e) {
                LOGGER.error("Failed to release mutex!", e);
            }
        }
    }

    private boolean isConnectedToRegistry(ClientAddress clientAddress) {
        return null != clientRegistry.get(clientAddress.getClientId());
    }

    /**
     * We need to use Zookeeper discovery to determine if each of these servers are still online.
     * If they are not online, we need to clean the topology by removing them.
     *
     * @param servers
     */
    private void cleanDeadServers(ClientAddress client, Set<Address> servers) throws Exception {
        if (CollectionUtil.isNullOrEmpty(servers)) {
            return;
        }

        final CountDownLatch latch = new CountDownLatch(servers.size());
        Iterator<Address> iterator = servers.iterator();

        while(iterator.hasNext()) {
            Address address = iterator.next();

            if (!(address instanceof ServerAddress)) {
                LOGGER.warn("address not an instance of ServerAddress?? " + address);

                iterator.remove();
                continue;
            }

            if (!clusterDiscoveryService.isActive((ServerAddress)address)) {
                // we need to make sure it is done before releasing the mutex.
                // let's not block though, let's just use a latch.
                topology.remove(client, address).addObserver(new Observer<ObservableFuture<Void>>() {
                    @Override
                    public void notify(Object sender, ObservableFuture<Void> item) {
                        latch.countDown();
                    }
                });

                iterator.remove();
                continue;
            } else {
                latch.countDown();
            }

            // this is ok.

        }

        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("The latch didn't complete within 10 seconds!");
        }

        // we don't care about latch success/failure. We just needed to wait until the IO was complete.
        // technically it could be still going on, though Redis is atomic, the other servers will process behind it.
        // All we're doing by delaying 10 seconds is reducing the likelihood that race conditions occur.

    }

    private <T> T awaitOrFail(ObservableFuture<T> future) throws Throwable {
        if (!future.await(5, TimeUnit.SECONDS)) {
            future.cancel();
            throw new Exception("Not finished");
        } else if (future.isFailed()) {
            throw future.getCause();
        } else if (future.isCancelled()) {
            throw new Exception("Cancelled");
        }

        return future.getResult();
    }

}
