package com.zipwhip.signals;

import com.zipwhip.framework.pubsub.Broker;
import com.zipwhip.jms.MockSimpleQueueSender;
import com.zipwhip.signals.address.Address;
import com.zipwhip.signals.address.ChannelAddress;
import com.zipwhip.signals.address.ClientAddress;
import com.zipwhip.signals.address.ServerAddress;
import com.zipwhip.signals.mailbox.Mailbox;
import com.zipwhip.signals.message.DefaultMessage;
import com.zipwhip.signals.message.DeliveredMessage;
import com.zipwhip.signals.message.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.*;

/**
 * Date: 5/7/13
 * Time: 6:08 PM
 *
 * @author Michael
 * @version 1
 */
public class NetworkImplTest extends FrameworkTestBase {

    @Autowired
    Topology topology;

    @Autowired
    Broker broker;

    @Autowired
    ConnectionManagerImpl network;

    @Autowired
    Mailbox mailbox;

    @Autowired
    ClientRegistry clientRegistry;

    @Autowired
    MockSimpleQueueSender simpleQueueSender;

    UUID uuid = UUID.randomUUID();
    ClientAddress clientAddress = new ClientAddress(uuid.toString());
    ServerAddress serverAddress2 = new ServerAddress("server:" + UUID.randomUUID().toString());
    ServerAddress serverAddress1;
    ChannelAddress channelAddress = new ChannelAddress("/channel/23423434");

    @Before
    public void setUp() throws Exception {
        serverAddress1 = application.getConfiguration().getServerAddress();

        network.setTopology(topology);

        // store that this clientAddress is currently located on that serverAddress
        topology.add(clientAddress, serverAddress1);
        topology.add(channelAddress, clientAddress);
    }

    @After
    public void tearDown() throws Exception {
        topology.remove(clientAddress, serverAddress1);
    }

    @Test
    public void testTopology() throws Exception {
        topology.remove(clientAddress, serverAddress1).get();
        assertNull(topology.get(serverAddress1).get());

        topology.add(clientAddress, serverAddress1).get();

        Collection<Address> addresses = topology.get(clientAddress).get();

        assertNotNull(addresses);
        assertEquals(1, addresses.size());
        assertEquals(serverAddress1, addresses.iterator().next());
    }

    @Test
    public void testTransferLoop() throws Exception {
        final ClientAddress clientAddress1 = new ClientAddress(UUID.randomUUID().toString());

        topology.add(clientAddress1, clientAddress1);

        try {
            network.get(clientAddress1);
            fail("Should have crashed bc of the loop");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testTransfer() throws Exception {
        final ClientAddress clientAddress1 = new ClientAddress(UUID.randomUUID().toString());

        topology.add(clientAddress1, serverAddress2);

        Connection connection = network.get(clientAddress1);

        assertNotNull(connection);
        // Make sure that clientAddress is found on a different server
        assertTrue(connection instanceof ServerConnection);
        assertEquals(serverAddress2.getName(), ((ServerConnection) connection).getName());

        final Message message = new DefaultMessage(clientAddress1, null);

        connection.send(message);

        List<Object> messages = simpleQueueSender.getQueue().get("/server/transfer/" + serverAddress2.getName());
        simpleQueueSender.getQueue().clear();
        Message message1 = (Message) messages.get(0);

        assertEquals(message1, message);
        assertEquals(message1.getAddress(), clientAddress1);
    }

    @Test
    public void testLocalConnectionClientAddressedMessage() throws Exception {
        // store that this clientAddress is currently located on that serverAddress
        MockSocketIOClient client = new MockSocketIOClient(uuid);

        // tell the environment that this client is connected locally.
        clientRegistry.put(client);

        String content = "{payload:true}";
        Message message = new DefaultMessage(clientAddress, content);

        try {
            // this should be transferred to another server
            broker.publish("/server/message/enqueue", message);

            assertTrue(client.getLatch().await(5, TimeUnit.SECONDS));
            assertNotNull(client.getPacket());
            assertTrue(client.getPacket().getData() instanceof DeliveredMessage);

            DeliveredMessage deliveredMessage = (DeliveredMessage) client.getPacket().getData();

            assertEquals(0, deliveredMessage.getVersion());
            assertEquals(clientAddress, deliveredMessage.getAddress());
            assertEquals(content, deliveredMessage.getContent());
        } finally {
            clientRegistry.remove(client);
        }
    }

    @Test
    public void testLocalConnectionChannelAddressedMessage() throws Exception {
        // lets subscribe the clientAddress to our given channelAddress
        assertNotNull(topology.get(channelAddress).get());
        assertEquals(clientAddress, topology.get(channelAddress).get().iterator().next());

        // store that this clientAddress is currently located on that serverAddress
        MockSocketIOClient client = new MockSocketIOClient(uuid);

        // tell the environment that this client is connected locally.
        clientRegistry.put(client);

        try {
            String content = "{payload:true}";
            Message message = new DefaultMessage(channelAddress, content);

            {
                broker.publish("/server/message/enqueue", message);

                assertTrue(client.getLatch().await(5, TimeUnit.SECONDS));
                assertNotNull(client.getPacket());
                assertTrue(client.getPacket().getData() instanceof DeliveredMessage);

                DeliveredMessage deliveredMessage = (DeliveredMessage) client.getPacket().getData();

                assertEquals(0, deliveredMessage.getVersion());
                assertEquals(channelAddress, deliveredMessage.getAddress());
                assertEquals(content, deliveredMessage.getContent());
            }

            client.reset();

            {
                broker.publish("/server/message/enqueue", message);

                assertTrue(client.getLatch().await(5, TimeUnit.SECONDS));
                assertNotNull(client.getPacket());
                assertTrue(client.getPacket().getData() instanceof DeliveredMessage);

                DeliveredMessage deliveredMessage = (DeliveredMessage) client.getPacket().getData();

                assertEquals(1, deliveredMessage.getVersion());
                assertEquals(channelAddress, deliveredMessage.getAddress());
                assertEquals(content, deliveredMessage.getContent());
            }
        } finally {
            clientRegistry.remove(client);
        }
    }
}
