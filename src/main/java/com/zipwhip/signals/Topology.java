package com.zipwhip.signals;

import com.zipwhip.concurrent.ObservableFuture;
import com.zipwhip.signals.address.Address;

import java.util.Set;

/**
 * Date: 5/7/13
 * Time: 5:42 PM
 *
 * A global map of Address to Address subscriptions. For example, a ClientAddress can bind into a ChannelAddress. This
 * means that whenever a message is sent to the ChannelAddress, the ClientAddress will receive a copy.
 *
 * Here are the common use-cases:
 *   Client -> Server
 *   Server -> Client(s)
 *   Channel -> Client(s)
 *   Client -> Channel(s)
 *
 * @author Michael
 * @version 1
 */
public interface Topology {

    ObservableFuture<Set<Address>> get(Address client);

    ObservableFuture<Void> add(Address client, Address server);

    ObservableFuture<Void> add(Address client, Set<Address> servers);

    ObservableFuture<Void> remove(Address client, Address server);

}
