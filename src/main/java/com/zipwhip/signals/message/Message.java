package com.zipwhip.signals.message;

import com.zipwhip.signals.address.Address;

import java.io.Serializable;

/**
 * Date: 5/7/13
 * Time: 4:41 PM
 *
 * @author Michael
 * @version 1
 */
public interface Message extends Serializable {

    /**
     * Indicates who this message is to be delivered to.
     *
     * @return
     */
    Address getAddress();

    /**
     * The content of the message.
     *
     * @return
     */
    String getContent();

}
