package com.zipwhip.signals;

import com.corundumstudio.socketio.SocketIOClient;
import com.zipwhip.cache.Cache;
import com.zipwhip.cache.MapCache;
import com.zipwhip.concurrent.ObservableFuture;
import com.zipwhip.framework.pubsub.Broker;
import com.zipwhip.jms.SimpleQueueSender;
import com.zipwhip.signals.address.Address;
import com.zipwhip.signals.address.ClientAddress;
import com.zipwhip.signals.address.ServerAddress;
import com.zipwhip.signals.connection.Connections;
import com.zipwhip.signals.message.Message;
import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.InputCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Date: 5/7/13
 * Time: 5:44 PM
 *
 * @author Michael
 * @version 1
 */
public class ConnectionManagerImpl implements ConnectionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManagerImpl.class);

    private final Cache<String, Connection> serverConnectionCache = new MapCache<String, Connection>();

    private ServerAddress serverAddress;
    private ClientRegistry clientRegistry;
    private Topology topology;
    private Broker broker;
    private SimpleQueueSender simpleQueueSender;

    @Override
    public Connection get(final Address address) throws Exception {
        if (address instanceof ServerAddress) {
            if (address.equals(serverAddress)) {
                // We're the one it wants to connect to!
                return selfConnection;
            }

            // They want to send to a server. We send to servers via PubSub.
            return serverConnectionCache.get(((ServerAddress) address).getName(), serverConnectionBuilder);
        }

        if (address instanceof ClientAddress) {
            // we talk to ClientAddresses through servers. We need to find which server
            // this client is connected to. It should generally only be 1 (even though its a list)
            String clientId = ((ClientAddress) address).getClientId();
            SocketIOClient client = clientRegistry.get(clientId);

            if (client != null) {
                // they are connected to us!!
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(String.format("ClientId (%s) is connected to us by %s", clientId, client));
                }

                return new Connection() {
                    @Override
                    public void send(Message message) {
                        broker.publish("/sockets/send", address, message);
                    }
                };
            } else {
                // they are not connected.
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(String.format("ClientId (%s) is NOT connected to us. Maybe another server.", clientId));
                }
            }
        }

        // this handles the cases of ChannelAddress and (remote) ClientAddress.

        final ObservableFuture<Set<Address>> topologyFuture = topology.get(address);

        Collection<Address> addresses = topologyFuture.get(5, TimeUnit.SECONDS);

        if (CollectionUtil.isNullOrEmpty(addresses)) {
            LOGGER.error("No addresses found for " + address);

            return null;
        }

        synchronized (addresses) {
            List<Connection> connections = new ArrayList<Connection>(addresses.size());

            for (final Address listeningAddress : addresses) {
                if (listeningAddress == null) {
                    continue;
                } else if (listeningAddress.equals(address)) {
                    LOGGER.error("This address is the same as the other address. Infinite loop detected. " + address);
                    continue;
                }

                Connection connection = get(listeningAddress);

                if (connection != null) {
                    connections.add(connection);
                }
            }

            if (CollectionUtil.isNullOrEmpty(connections)) {
                throw new IllegalStateException("No connections for : " + addresses);
            }

            if (connections.size() == 1) {
                return connections.get(0);
            }

            return Connections.asConnection(connections);
        }
    }

    private Connection selfConnection = new Connection() {

        @Override
        public void send(Message message) {
            broker.publish("/sockets/send", message.getAddress(), message);
        }
    };

    private final InputCallable<String, Connection> serverConnectionBuilder = new InputCallable<String, Connection>() {

        @Override
        public Connection call(String serverName) {
            return new ServerConnection(simpleQueueSender, serverName);
        }
    };

    public SimpleQueueSender getSimpleQueueSender() {
        return simpleQueueSender;
    }

    public void setSimpleQueueSender(SimpleQueueSender simpleQueueSender) {
        this.simpleQueueSender = simpleQueueSender;
    }

    public Topology getTopology() {
        return topology;
    }

    public void setTopology(Topology topology) {
        this.topology = topology;
    }

    public Broker getBroker() {
        return broker;
    }

    public void setBroker(Broker broker) {
        this.broker = broker;
    }

    public ClientRegistry getClientRegistry() {
        return clientRegistry;
    }

    public void setClientRegistry(ClientRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
    }

    public ServerAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(ServerAddress serverAddress) {
        this.serverAddress = serverAddress;
    }
}
