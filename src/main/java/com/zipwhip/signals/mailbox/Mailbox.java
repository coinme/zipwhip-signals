/**
 *
 */
package com.zipwhip.signals.mailbox;

import com.zipwhip.signals.address.Address;

import java.util.List;

/**
 * Manages the mailbox for a given connection (a connection is known by it's
 * clientId)
 *
 * @author msmyers
 */
public interface Mailbox {

    /**
     * Returns the most recent message for a given address.
     *
     * @param address
     * @return
     */
    Mail getLast(Address address) throws Exception;

    /**
     *
     * @param address
     * @param version Version index. Inclusive.
     * @return
     */
    Mail getAt(Address address, long version) throws Exception;

    /**
     *
     * @param address
     * @param startVersion
     *            INCLUSIVE
     * @param endVersion
     *            INCLUSIVE
     * @return
     */
    List<Mail> get(Address address, long startVersion, long endVersion) throws Exception;

    /**
     * Get everything
     * @param address
     * @param version
     *            NON-INCLUSIVE
     * @return
     */
    List<Mail> getAfter(Address address, long version) throws Exception;

    /**
     *
     * @param address
     * @param content
     *            The message you want to store.
     * @return The version of the given address.
     */
    long append(Address address, String content) throws Exception;

    /**
     * Gets the current version for a given address.
     *
     * Returns NULL if there is no max version.
     *
     * @param address
     * @return
     */
    Long getMaxVersion(Address address) throws Exception;

}
