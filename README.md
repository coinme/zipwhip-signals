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
* It is active-active datacenter.
* All nodes in the cluster are clones of each other.

Technologies
-----

* Zookeeper provides leader election.
* Leaders clean up after downed nodes. (New leaders clean up after old leaders)
* We store mail and subscriptions in Cassandra (providing active-active datacenter support)
* JMS provides round-robin load balancing of data processing.
* Zookeeper provides cluster-wide mutexes.
* Zookeeper provides a global ordering of events.

How clients subscribe
------

* Client connects and gets a **clientId** from **Socket.IO**
* Client makes HTTP POST to your central server. We use **/signals/connect**
* Your central server sends a command to **zipwhip-signals** via the JMS Queue **/server/connect**

```java
  public class SubscriptionFeature extends Feature {
    
    @Autowired
    Topology topology;
    
    @Subscribe(uri = "/server/connect", converter = "SubscribeConverter")
    public void process(Address client, Set<Address> addresses) throws Exception {
      
      ObservableFuture<Void> future = topology.add(client, addresses);
      
      // Because we're in the pubsub thread (usually a JMS consumer). We don't want to overwhelm
      // our JVM memory with pending requests. We need to block.
      
      // By blocking here, we're pooling waiting requests in JMS rather than in our local JVM.
      
      if (!future.await(5, TimeUnit.SECONDS)) {
        throw new Exception("Future did not complete within 5 seconds. Is our topology down?");
      }    
    }
  }
```


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
 * A global map of Address to Address subscriptions. For example, a ClientAddress can bind into a 
 * ChannelAddress. This means that whenever a message is sent to the ChannelAddress, the ClientAddress
 * will receive a copy.
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

This code is running on all nodes of the cluster. Each server is aware of the full **Topology**. The **ConnectionManager** is responsible for figuring out how to send messages to various types of **Address**.

```java
public class ServerEnqueueFeature extends Feature {

  @Autowired
  Network network;
  
  @Autowired
  ConnectionManager connectionManager;
  
  @Subscribe(uri = "/server/enqueue", converter = "MessageConverter")
  public void process(Message message) {
    long version = mailbox.append(message);
    
    Connection connection = connectionManager.get(message.getAddress());
    
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
