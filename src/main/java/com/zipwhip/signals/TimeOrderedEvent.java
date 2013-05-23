package com.zipwhip.signals;

import java.io.Serializable;

/**
 * Date: 5/14/13
 * Time: 5:16 PM
 *
 * We need to be able to coordinate events in time.
 *
 * @author Michael
 * @version 1
 */
public class TimeOrderedEvent implements Serializable {

    private static final long serialVersionUID = 111189907124779503L;

    private long time;
    private Object event;
    private String sender;

    public TimeOrderedEvent(String sender, Object event, long time) {
        this.time = time;
        this.event = event;
        this.sender = sender;
    }

    public TimeOrderedEvent(String sender, Object event) {
        this(sender, event, System.currentTimeMillis());
    }

    public TimeOrderedEvent() {

    }

    public long getTime() {
        return time;
    }

    public Object getEvent() {
        return event;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setEvent(Object event) {
        this.event = event;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
