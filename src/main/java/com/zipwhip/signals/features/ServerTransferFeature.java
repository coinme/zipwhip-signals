package com.zipwhip.signals.features;

import com.corundumstudio.socketio.SocketIOClient;
import com.zipwhip.framework.Feature;
import com.zipwhip.framework.pubsub.Callback;
import com.zipwhip.framework.pubsub.EventData;
import com.zipwhip.framework.pubsub.EventDataUtil;
import com.zipwhip.signals.ClientRegistry;
import com.zipwhip.signals.SignalServerConfiguration;
import com.zipwhip.signals.address.ClientAddress;
import com.zipwhip.signals.message.Message;
import com.zipwhip.signals.message.TransferredMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Date: 5/14/13
 * Time: 5:46 PM
 *
 * Servers can talk to each other via message-transfer.
 *
 * This feature will not append to the mailbox. It will simply deliver the message.
 *
 * There is a possibility that the client is no longer connected. In the time it took the message to deliver to this feature
 * the client disconnected. We are not tracking the deliverable state on the server. It's the client's job to notice the
 * discrepancy in versions and ask the server for the missing signals. Additionally, on reconnect the client will issue
 * backfill requests to cover this scenario preemptively.
 *
 * @author Michael
 * @version 1
 */
public class ServerTransferFeature extends Feature<SignalServerConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerTransferFeature.class);

    @Autowired
    ClientRegistry clientRegistry;

    @Override
    protected void onInit() {
        subscribe("/server/transfer/" + parent.getConfiguration().getServerAddress().getName(), CALLBACK);
    }

    private final Callback CALLBACK = new Callback() {
        @Override
        public void notify(String uri, EventData eventData) throws Exception {
            Message message = EventDataUtil.getExtra(Message.class, eventData);

            ClientAddress clientAddress;

            if (message == null) {
                throw new IllegalArgumentException("message null : " + eventData);
            } else if (!(message.getAddress() instanceof ClientAddress)) {
                throw new IllegalArgumentException("Wrong type of address : " + message.getAddress());
            } else if (message instanceof TransferredMessage) {
                message = ((TransferredMessage) message).getMessage();
            }

            clientAddress = (ClientAddress) message.getAddress();

            if (clientAddress == null) {
                throw new IllegalArgumentException("clientAddress is null");
            }

            transfer(clientAddress, message);
        }
    };

    private void transfer(ClientAddress recipient, Message message) {
        SocketIOClient client = clientRegistry.get(recipient.getClientId());

        if (client == null) {
            throw new IllegalStateException("SocketIOClient not found for clientId: " + recipient.getClientId());
        }

        client.sendJsonObject(message);
    }

}
