package com.zipwhip.signals;

import com.zipwhip.signals.address.Address;

/**
 * Date: 5/7/13
 * Time: 5:43 PM
 *
 * The ConnectionManager decides how to best reach a given Address.
 *
 * Example use-cases:
 *
 *    - a ClientAddress that is connected locally already is just routed over a Netty channel.
 *    - a ServerAddress is routed over JMS via the queue: /server/{serverName}
 *    - a ChannelAddress is resolved to the underlying ClientAddress(es). This forms a 1-many connection.
 *
 * @author Michael
 * @version 1
 */
public interface ConnectionManager {

    /**
     * For a given Address, how do we talk to it.
     *
     * @param address
     * @return
     */
    Connection get(Address address) throws Exception;

}
