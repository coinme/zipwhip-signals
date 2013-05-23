package com.zipwhip.signals.address;

/**
 * Date: 5/18/13
 * Time: 9:37 PM
 *
 * @author Michael
 * @version 1
 */
public interface Persister<T> {

    T parse(String data);

    String serialize(T data);

}
