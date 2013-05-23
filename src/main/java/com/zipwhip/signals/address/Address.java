package com.zipwhip.signals.address;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Michael
 * Date: Dec 11, 2010
 * Time: 4:34:01 PM
 *
 * For routing. You can send a message with an Address, and our Topology figures out
 * how to get it there. The "toString()" method of this interface is how you compare
 * addresses.
 */
public interface Address extends Serializable, Comparable<Address> {

}
