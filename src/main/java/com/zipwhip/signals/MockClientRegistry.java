package com.zipwhip.signals;

import com.corundumstudio.socketio.SocketIOClient;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Date: 5/7/13
 * Time: 6:30 PM
 *
 * @author Michael
 * @version 1
 */
public class MockClientRegistry implements ClientRegistry {

    private final Map<String, SocketIOClient> map = Collections.synchronizedMap(new TreeMap<String, SocketIOClient>());

    @Override
    public void put(SocketIOClient client) {
        map.put(client.getSessionId().toString(), client);
    }

    @Override
    public void remove(SocketIOClient client) {
        map.remove(client.getSessionId().toString());
    }

    @Override
    public SocketIOClient get(String uuid) {
        return map.get(uuid);
    }
}
