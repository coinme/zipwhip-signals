package com.zipwhip.signals;

import com.zipwhip.jms.SimpleQueueSender;
import com.zipwhip.signals.address.ServerAddress;
import com.zipwhip.signals.message.Message;

/**
 * Date: 5/7/13
 * Time: 5:48 PM
 *
 * @author Michael
 * @version 1
 */
public class ServerConnection implements Connection {

    private final SimpleQueueSender simpleQueueSender;
    private final String name;

    public ServerConnection(SimpleQueueSender simpleQueueSender, String name) {
        this.simpleQueueSender = simpleQueueSender;
        this.name = name;
    }

    public ServerConnection(SimpleQueueSender simpleQueueSender, ServerAddress address) {
        this.simpleQueueSender = simpleQueueSender;
        this.name = address.getName();
    }

    @Override
    public void send(Message message) {
        simpleQueueSender.sendQueueJMSMessage("/server/transfer/" + name, message);
    }

    public SimpleQueueSender getSimpleQueueSender() {
        return simpleQueueSender;
    }

    public String getName() {
        return name;
    }
}
