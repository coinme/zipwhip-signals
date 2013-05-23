package com.zipwhip.signals;

import com.corundumstudio.socketio.SocketIOClient;

/**
 * Date: 5/7/13
 * Time: 6:11 PM
 *
 * This is a registry for the currently connected clients.
 *
 * This is a private data structure that is not shared between instances of the server.
 *
 * @author Michael
 * @version 1
 */
public interface ClientRegistry {

    void put(SocketIOClient client);

    void remove(SocketIOClient client);

    SocketIOClient get(String uuid);

}
