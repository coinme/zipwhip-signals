package com.zipwhip.signals.connection;

import com.zipwhip.signals.Connection;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Date: 5/7/13
 * Time: 6:01 PM
 *
 * @author Michael
 * @version 1
 */
public class Connections {

    public static Connection asConnection(Connection... connections) {
        ArrayList<Connection> list = new ArrayList<Connection>(connections.length);

        for (Connection connection : connections) {
            list.add(connection);
        }

        return asConnection(list);
    }

    public static Connection asConnection(Collection<Connection> connections) {
        return new GroupConnection(connections);
    }

}
