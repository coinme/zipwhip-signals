package com.zipwhip.signals;

import com.zipwhip.framework.Configuration;
import com.zipwhip.signals.address.ServerAddress;

/**
 * Date: 5/14/13
 * Time: 5:53 PM
 *
 * @author Michael
 * @version 1
 */
public class SignalServerConfiguration extends Configuration {

    private ServerAddress serverAddress;

    public ServerAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(ServerAddress serverAddress) {
        this.serverAddress = serverAddress;
    }
}
