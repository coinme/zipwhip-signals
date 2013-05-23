package com.zipwhip.framework;

import java.io.Serializable;

/**
 * Date: 5/19/13
 * Time: 12:33 AM
 *
 * @author Michael
 * @version 1
 */
public class AtomicEvent<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 5003518136511578426L;

    private final long eventId;
    private final T data;

    public AtomicEvent(long eventId, T data) {
        this.eventId = eventId;
        this.data = data;
    }

    public long getEventId() {
        return eventId;
    }

    public T getData() {
        return data;
    }
}
