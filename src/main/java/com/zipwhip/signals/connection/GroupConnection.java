package com.zipwhip.signals.connection;

import com.zipwhip.signals.Connection;
import com.zipwhip.signals.message.Message;

import java.util.Collection;

/**
 * Date: 5/7/13
 * Time: 6:01 PM
 *
 * @author Michael
 * @version 1
 */
public class GroupConnection implements Connection {

    private final Collection<Connection> connections;

    public GroupConnection(Collection<Connection> connections) {
        this.connections = connections;
    }

    @Override
    public void send(Message message) {
        for (Connection connection : connections) {
            connection.send(message);
        }
    }
}
