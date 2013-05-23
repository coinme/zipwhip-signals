package com.zipwhip.signals.features;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.zipwhip.framework.Feature;
import com.zipwhip.framework.pubsub.Callback;
import com.zipwhip.framework.pubsub.EventData;
import com.zipwhip.framework.pubsub.EventDataUtil;
import com.zipwhip.framework.pubsub.UriAgent;
import com.zipwhip.signals.ClientRegistry;
import com.zipwhip.signals.SignalServerConfiguration;
import com.zipwhip.signals.address.ClientAddress;
import com.zipwhip.signals.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Date: 5/7/13
 * Time: 10:48 AM
 *
 * The SocketIoFeature is very simple.
 *
 * The job is to integrate the SocketIoServer into the Application space so it can participate in PubSub.
 *
 * @author Michael
 * @version 1
 */
public class SocketIoFeature extends Feature<SignalServerConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketIoFeature.class);

    private static final UriAgent URI_SOCKETS_SEND = new UriAgent("/sockets/send");

    @Autowired
    ClientRegistry clientRegistry;

    // this is not autowired because i want it to be manually injected.
    // the reason is that this is not a commonly available service.
    private SocketIOServer socketIOServer;

    @Override
    protected void onInit() {
        socketIOServer.addMessageListener(DATA_LISTENER);

        socketIOServer.addConnectListener(CONNECT_LISTENER);

        socketIOServer.addDisconnectListener(DISCONNECT_LISTENER);

        socketIOServer.start();

        subscribe(URI_SOCKETS_SEND, SOCKETS_SEND);
    }

    private final Callback SOCKETS_SEND = new Callback() {
        @Override
        public void notify(String uri, EventData eventData) throws Exception {
            ClientAddress clientAddress = EventDataUtil.getExtra(ClientAddress.class, eventData, 0);
            Message message = EventDataUtil.getExtra(Message.class, eventData, 1);
            SocketIOClient client = clientRegistry.get(clientAddress.getClientId());

            if (client == null) {
                EventDataUtil.fail(parent.getBroker(), uri, eventData, URI_SOCKETS_SEND, new IllegalStateException("No client found for : " + clientAddress));
                return;
            }

            // transmit message
            client.sendJsonObject(message);
        }
    };

    private final ConnectListener CONNECT_LISTENER = new ConnectListener() {
        @Override
        public void onConnect(SocketIOClient client) {
            String clientId = client.getSessionId().toString();

            synchronized (SocketIoFeature.this) {
                SocketIOClient existingClient = clientRegistry.get(clientId);

                if (existingClient == null) {
                    // they are already attached. We must have a situation of:
                    //    [connect] ---------- [disconnect]
                    //            [connect] ---[disconnect]

                    // Don't process this event. It was already handled.
                    return;
                }

                // announce locally
                clientRegistry.put(client);
            }

            // announce to the cluster that this occurred.
            publish("/sockets/connection/changed", clientId);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Connected %s", clientId));
            }

        }
    };

    private final DisconnectListener DISCONNECT_LISTENER = new DisconnectListener() {
        @Override
        public void onDisconnect(SocketIOClient client) {
            String clientId = client.getSessionId().toString();

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Disconnected %s", clientId));
            }

            synchronized (SocketIoFeature.this) {
                SocketIOClient existingClient = clientRegistry.get(clientId);

                if (existingClient == null) {
                    // they have already been processed??
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.warn(String.format("ClientId %s is already in the clientRegistry. NOT announcing disconnect.", clientId));
                    }

                    return;
                }

                // process locally
                //  - remove them from our in-memory lookup.
                //    * Not a huge deal. Just a memory leak otherwise.
                clientRegistry.remove(client);
            }

            // announce sometime later that this has occurred.
            publish("/sockets/connection/changed", clientId);
        }
    };

    private final DataListener<String> DATA_LISTENER = new DataListener<String>() {
        @Override
        public void onData(SocketIOClient client, String message, AckRequest ackRequest) {
            LOGGER.debug("data listener!");
        }
    };

    @Override
    protected void onDestroy() {
        socketIOServer.stop();
    }

    public SocketIOServer getSocketIOServer() {
        return socketIOServer;
    }

    public void setSocketIOServer(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

}
