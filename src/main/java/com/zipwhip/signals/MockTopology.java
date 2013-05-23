package com.zipwhip.signals;

import com.zipwhip.concurrent.FakeObservableFuture;
import com.zipwhip.concurrent.ObservableFuture;
import com.zipwhip.signals.address.Address;
import com.zipwhip.util.LocalDirectory;
import com.zipwhip.util.SetDirectory;

import java.util.Set;

/**
 * Date: 5/7/13
 * Time: 5:51 PM
 *
 * @author Michael
 * @version 1
 */
public class MockTopology implements Topology {

    private LocalDirectory<Address, Address> directory = new SetDirectory<Address, Address>();

    @Override
    @SuppressWarnings("unchecked")
    public ObservableFuture<Set<Address>> get(Address client) {
        return new FakeObservableFuture<Set<Address>>(this, (Set)directory.get(client));
    }

    @Override
    public ObservableFuture<Void> add(Address client, Address server) {
        directory.add(client, server);

        return new FakeObservableFuture<Void>(this, null);
    }

    @Override
    public ObservableFuture<Void> remove(Address client, Address server) {
        directory.remove(client, server);

        return new FakeObservableFuture<Void>(this, null);
    }

    public void clear() {
        directory.clear();
    }
}
