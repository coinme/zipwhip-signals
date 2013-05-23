package com.zipwhip.signals.message;

import com.zipwhip.signals.address.Address;

/**
 * Date: 5/14/13
 * Time: 7:52 PM
 *
 * This message is typically delivered to the clients. It tells them the version of this message and if it is for
 * a backfill request.
 *
 * @author Michael
 * @version 1
 */
public class DeliveredMessage extends DefaultMessage {

    private static final long serialVersionUID = -972039147127688314L;

    private final long version;
    private final boolean backfill;

    public DeliveredMessage(Address address, String content, long version) {
        this(address, content, version, false);
    }

    public DeliveredMessage(Address address, String content, long version, boolean backfill) {
        super(address, content);

        this.version = version;
        this.backfill = backfill;
    }

    public long getVersion() {
        return version;
    }

    public boolean isBackfill() {
        return backfill;
    }
}
