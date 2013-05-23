package com.zipwhip.signals.features;

import com.zipwhip.framework.Configuration;
import com.zipwhip.framework.Feature;
import com.zipwhip.framework.pubsub.AnnotationManager;
import com.zipwhip.framework.pubsub.Subscribe;
import com.zipwhip.signals.Connection;
import com.zipwhip.signals.ConnectionManager;
import com.zipwhip.signals.MessageConverter;
import com.zipwhip.signals.mailbox.Mailbox;
import com.zipwhip.signals.message.DeliveredMessage;
import com.zipwhip.signals.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * Date: 5/7/13
 * Time: 5:11 PM
 *
 * This class receives messages via pubsub. (Typically injected via JMS listener).
 *
 * It injects messages into the mailbox and then forwards them to any subscribers (if present).
 *
 * @author Michael
 * @version 1
 */
public class ServerEnqueueFeature extends Feature<Configuration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerEnqueueFeature.class);
    private static final String MESSAGE_CONVERTER = "com.zipwhip.signals.MessageConverter";

    static {
        AnnotationManager.register(MESSAGE_CONVERTER, new MessageConverter());
    }

    @Autowired
    Mailbox mailbox;

    @Autowired
    ConnectionManager connectionManager;

    /**
     * This method is wired up via PubSub. The Annotation will automatically convert the EventData into our type.
     *
     * Our job is to append each message to the mailbox and forward to any active listeners.
     *
     * @param messages
     * @throws Exception
     */
    @Subscribe(uri = "/server/message/enqueue", converter = MESSAGE_CONVERTER)
    public void enqueue(Collection<Message> messages) throws Exception {
        for (Message message : messages) {
            enqueue(message);
        }
    }

    // this is called via JMS
    public void enqueue(final Message message) throws Exception {
        if (message == null || message.getAddress() == null) {
            throw new NullPointerException("address");
        }

        long version = mailbox.append(message.getAddress(), message.getContent());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Saved message %d to address %s", version, message.getAddress()));
        }

        Connection connection = connectionManager.get(message.getAddress());

        if (connection == null) {
            LOGGER.error(String.format("No active subscribers found for %s. They must not be connected anywhere right now. " +
                    "Don't worry. It's in their mailbox for later.", message.getAddress()));

            return;
        }

        // This connection might be forwarding to another server.
        // It also might be sending to a locally connected client.
        // It might be broadcasting to all in a given channel (if the address is of type ChannelAddress)
        // Or, it might be all 3 of those combined.
        connection.send(new DeliveredMessage(message.getAddress(), message.getContent(), version));
    }
}
