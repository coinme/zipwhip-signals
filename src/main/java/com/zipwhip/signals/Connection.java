package com.zipwhip.signals;

import com.zipwhip.signals.message.Message;

/**
 * Date: 5/7/13
 * Time: 5:43 PM
 *
 * A connection can send messages to endpoints. There are many use-cases for this:
 *
 *    - Server talking to 1 client.
 *    - Server talking to many clients (some of which are on other servers).
 *    - Message to a channel, forwarded to listening clients
 *    - Message to a server, telling it to do something (like shut down).
 *
 * @author Michael
 * @version 1
 */
public interface Connection {

    /**
     * Asynchronously send a message.
     *
     * @param message
     */
    void send(Message message);

}
