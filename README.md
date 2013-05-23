zipwhip-signals
===============

Clustered signal delivery system to connected clients. 

What it does
------

* Clients connect to this server via **Socket.IO**
* The server keeps track of whos-connected-where via a cluster-thread-safe **Topology**
* The server keeps accurate **Presence** information for each connection cluster-wide.
* It supports horizontal scaling and can handle an infinite amount of connections and throughput. 
* Messages are persistent and so clients can connect up to 30 days later to get messages.
* Messaging is one-way (towards clients only). Similar to distributed pub-sub in nature.
* Your central infrastructure is separate from this server and injects messages via JMS.

How you use it
------

Your central infrastructure -> JMS -> zipwhip-services -> connected client(s)

```java

/**
 * Send an arbitrary payload to a channel. All clients subscribed to this channel will receive it.
 *
 * @param channel Where to send the message.
 * @param payload Arbitrary content. 
 */
public void send(String channel, Object payload) {
  DefaultMessage message = new DefaultMessage();
  
  message.setAddress(new ChannelAddress(channel));
  message.setContent(gson.toJson(payload));
  
  jms.enqueue("/server/enqueue", message);
}
```

Topology 
------

```java
/**
 * A global map of Address to Address subscriptions. For example, a ClientAddress can bind into a ChannelAddress. This
 * means that whenever a message is sent to the ChannelAddress, the ClientAddress will receive a copy.
 *
 * Here are the common use-cases:
 *   Client -> Server
 *   Server -> Client(s)
 *   Channel -> Client(s)
 *   Client -> Channel(s)
 *
 * Implementations must be 100% cluster-atomic. 
 * 
 * @author Michael
 * @version 1
 */
public interface Topology {

    ObservableFuture<Set<Address>> get(Address address);

    ObservableFuture<Void> add(Address address, Address address);

    ObservableFuture<Void> remove(Address address, Address address);

}

```

Routing decisions
------

This code is running on all nodes of your server. Each server is aware of the full **Topology**. The **Network** is responsible for figuring out how to send messages to various types of **Address**.

```java
public class ServerEnqueueFeature extends Feature {

  @Autowired
  Network network;
  
  @Autowired
  Mailbox mailbox;
  
  @Subscribe(uri = "/server/enqueue", converter = "MessageConverter")
  public void process(Message message) {
    long version = mailbox.append(message);
    
    Connection connection = network.get(message.getAddress());
    
    if (connection == null) {
      // There are no active connections that care about this in the entire cluster.    
      return;
    }
    
    // This might be a 1-many connection. 
    // It could be a locally connected client.
    // It could be a client on another server.
    connection.send(new DeliveredMessage(message, version));
  }
}
```
