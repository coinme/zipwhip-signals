package com.zipwhip.signals.mailbox;

import com.zipwhip.signals.address.Address;

import java.util.List;

/**
 * Date: 5/7/13
 * Time: 5:40 PM
 *
 * @author Michael
 * @version 1
 */
public class MockMailbox implements Mailbox {

    private long version;

    @Override
    public Mail getLast(Address address) {
        return null;
    }

    @Override
    public Mail getAt(Address address, long version) {
        return null;
    }

    @Override
    public List<Mail> get(Address address, long startVersion, long endVersion) {
        return null;
    }

    @Override
    public List<Mail> getAfter(Address address, long version) {
        return null;
    }

    @Override
    public long append(Address address, String content) {
        return ++version;
    }

    @Override
    public Long getMaxVersion(Address address) {
        return version;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
