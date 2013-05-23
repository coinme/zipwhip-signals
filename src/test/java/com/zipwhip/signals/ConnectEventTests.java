package com.zipwhip.signals;

import com.corundumstudio.socketio.SocketIOClient;
import com.zipwhip.framework.AtomicCounter;
import com.zipwhip.framework.AtomicEvent;
import com.zipwhip.framework.pubsub.TestCallback;
import com.zipwhip.signals.address.ClientAddress;
import com.zipwhip.signals.address.ServerAddress;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static junit.framework.Assert.*;

/**
 * Date: 5/19/13
 * Time: 12:46 AM
 *
 * @author Michael
 * @version 1
 */

public class ConnectEventTests extends FrameworkTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectEventTests.class);

    @Autowired
    Topology topology;

    @Autowired
    AtomicCounter atomicCounter;

    @Autowired
    ClientRegistry clientRegistry;

    ClientAddress clientAddress = new ClientAddress(UUID.randomUUID().toString());
    ServerAddress serverAddress;

    @Before
    public void setUp() throws Exception {
        serverAddress = application.getConfiguration().getServerAddress();
        topology.remove(clientAddress, serverAddress).await();
    }

    @Test
    public void testCounter() throws Exception {
        Long value1 = atomicCounter.incrementAndGet();
        assertNotNull(value1);

        Long value2 = atomicCounter.incrementAndGet();
        assertNotNull(value2);

        assertTrue(value2 > value1);
    }

    @Test
    public void testConnect() throws Exception {
        TestCallback callback1 = subscribe("/client/connected");
        TestCallback callback2 = subscribe("/client/disconnected");

        SocketIOClient client = new MockSocketIOClient(UUID.fromString(clientAddress.getClientId()));
        clientRegistry.put(client);

        application.getBroker().publish("/sockets/connection/changed", clientAddress);

        callback1.getLatch().await();

        assertEquals(1, callback1.getHitCount());
        assertNotNull(callback1.getLastItem());
        AtomicEvent event = (AtomicEvent) callback1.getLastItem()[0];
        assertNotNull(event);
        assertEquals(clientAddress, event.getData());

        long eventId = event.getEventId();
        assertTrue(eventId > 0);
        LOGGER.debug("EventId1: " + eventId);

        // do the remove locally
        clientRegistry.remove(client);
        // attempt to process it.
        application.getBroker().publish("/sockets/connection/changed", clientAddress);

        callback2.getLatch().await();

        assertEquals(1, callback2.getHitCount());
        assertNotNull(callback2.getLastItem());
        event = (AtomicEvent) callback2.getLastItem()[0];
        assertNotNull(event);
        assertEquals(clientAddress, event.getData());

        long eventId2 = event.getEventId();
        assertTrue(eventId2 > eventId);
        LOGGER.debug("EventId2: " + eventId);
    }
}
