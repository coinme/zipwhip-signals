zipwhip-signals
===============

Clustered signal delivery system to connected clients.

What it does
------

* Clients connect to this server via **Socket.IO**
* The server keeps track of whos-connected-where via a cluster-thread-safe **Topology**
* It supports horizontal scaling and can handle an infinite amount of connections and throughput. 
* Messages are persistent and so clients can connect up to 30 days later to get messages.
* Messaging is one-way (towards clients only).
* Your central infrastructure is separate from this server and injects messages via JMS.

Core Benefits
------

* Clients connect to the Signal Server via the standard **Socket.IO** protocol.
* The server keeps accurate **Presence** information for each connection cluster-wide.
* Your central infrastructure can talk to connected clients via the **Address** metaphor. 

How you use it
------

Your central infrastructure -> JMS -> zipwhip-services -> connected client(s)

Routing decisions
------

When your central server (Something

```java
public class ServerEnqueueFeature {

}
```
