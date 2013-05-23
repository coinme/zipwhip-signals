package com.zipwhip.signals.presence;

import com.zipwhip.signals.address.ClientAddress;
import com.zipwhip.signals.address.ServerAddress;

/**
 * Created by IntelliJ IDEA.
 * User: Michael
 * Date: 6/28/11
 * Time: 3:11 PM
 *
 * Controls who is connected to the SignalServer
 *
 * Recommendation for implementation is a JMS enqueue of a command.
 *
 */
public interface PresenceController {

    void connected(ServerAddress serverAddress, ClientAddress clientAddress, long secret);

    void disconnected(ServerAddress serverAddress, ClientAddress clientAddress, long secret);

    void presence(ClientAddress clientAddress, Presence presence);

}
