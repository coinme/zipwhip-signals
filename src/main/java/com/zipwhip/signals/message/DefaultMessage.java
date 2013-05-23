package com.zipwhip.signals.message;

import com.zipwhip.signals.address.Address;

/**
 * Date: 5/7/13
 * Time: 6:13 PM
 *
 * @author Michael
 * @version 1
 */
public class DefaultMessage implements Message {

    private static final long serialVersionUID = 8111225347753594028L;

    private final Address address;
    private final String content;

    public DefaultMessage(Address address, String content) {
        this.address = address;
        this.content = content;
    }

    public Address getAddress() {
        return address;
    }

    public String getContent() {
        return content;
    }
}
