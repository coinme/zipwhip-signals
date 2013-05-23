package com.zipwhip.cache;

import com.zipwhip.util.InputCallable;

/**
 * Date: 5/14/13
 * Time: 3:07 PM
 *
 * @author Michael
 * @version 1
 */
public interface Cache<K,V> {

    V get(K key, InputCallable<K, V> callable) throws Exception;

}
