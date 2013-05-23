package com.zipwhip.signals.features;

import com.zipwhip.concurrent.ObservableFuture;
import com.zipwhip.framework.Feature;
import com.zipwhip.framework.pubsub.EventData;
import com.zipwhip.framework.pubsub.Subscribe;
import com.zipwhip.signals.Topology;
import com.zipwhip.signals.address.Address;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Date: 5/23/13
 * Time: 4:16 PM
 *
 * Clients will subscribe.
 *
 * @author Michael
 * @version 1
 */
public class ServerSubscribeFeature extends Feature {

    @Autowired
    Topology topology;

    @SuppressWarnings("unchecked")
    @Subscribe(uri = "/server/topology/add")
    public void process(EventData eventData) throws InterruptedException {
        Address address = (Address) eventData.getExtras()[0];
        Set<Address> addresses = (Set<Address>) eventData.getExtras()[1];

        process(address, addresses);
    }

    private void process(Address address, Set<Address> addresses) throws InterruptedException {
        ObservableFuture<Void> future = topology.add(address, addresses);

        future.await(5, TimeUnit.SECONDS);
    }
}
