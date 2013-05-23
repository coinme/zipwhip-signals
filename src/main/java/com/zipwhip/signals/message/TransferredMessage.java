package com.zipwhip.signals.message;

import com.zipwhip.signals.address.Address;

/**
 * Date: 5/14/13
 * Time: 5:59 PM
 *
 * This message is being transferred.
 *
 * @author Michael
 * @version 1
 */
public class TransferredMessage implements Message {

    private static final long serialVersionUID = -3013371841698285592L;

    private Address address;
    private Message message;

    public TransferredMessage(Address address, Message message) {
        this.address = address;
        this.message = message;
    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public String getContent() {
        return message.getContent();
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
