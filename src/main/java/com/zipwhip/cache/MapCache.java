package com.zipwhip.cache;

import com.zipwhip.util.InputCallable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 5/14/13
 * Time: 3:08 PM
 *
 * @author Michael
 * @version 1
 */
public class MapCache<K,V> implements Cache<K, V> {

    private final Map<K, V> map = Collections.synchronizedMap(new HashMap<K, V>());

    public V get(K key, InputCallable<K, V> callable) throws Exception {
        V value = map.get(key);

        if (value == null) {
            synchronized (this) {
                value = map.get(key);

                if (value == null) {
                    value = callable.call(key);

                    map.put(key, value);
                }
            }
        }

        return value;
    }
}
