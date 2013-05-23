package com.zipwhip.framework;

/**
 * Date: 5/19/13
 * Time: 12:26 AM
 *
 * Cluster-wide counter. Always increasing. No 2 threads across the entire group will have the same value.
 *
 * @author Michael
 * @version 1
 */
public interface AtomicCounter {

    Long incrementAndGet() throws Exception;

}
