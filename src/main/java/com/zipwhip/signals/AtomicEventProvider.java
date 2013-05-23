package com.zipwhip.signals;

import com.zipwhip.framework.AtomicCounter;
import com.zipwhip.framework.AtomicEvent;

import java.io.Serializable;

/**
 * Date: 5/19/13
 * Time: 11:33 AM
 *
 * This class gives a cluster-wide event ordering system. An event that has a lower "eventId" is _always before_ an
 * event with a larger id. This is how we'll coordinate presence as "disconnect occurred 'before' a connect"
 *
 * @author Michael
 * @version 1
 */
public class AtomicEventProvider {

    /**
     * The AtomicCounter is cluster-wide guaranteed to increment. No 2 calls will ever have the same value.
     */
    private AtomicCounter atomicCounter;

    public <T extends Serializable> AtomicEvent<T> create(T data) throws Exception {
        Long eventId = atomicCounter.incrementAndGet();

        return new AtomicEvent<T>(eventId, data);
    }

    public AtomicCounter getAtomicCounter() {
        return atomicCounter;
    }

    public void setAtomicCounter(AtomicCounter atomicCounter) {
        this.atomicCounter = atomicCounter;
    }
}
