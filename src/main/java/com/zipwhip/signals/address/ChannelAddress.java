package com.zipwhip.signals.address;

/**
 * Created by IntelliJ IDEA.
 * User: Michael
 * Date: Dec 11, 2010
 * Time: 7:53:59 PM
 * <p/>
 * To all consumers of a given channel.
 */
@OneToManyAddress
public class ChannelAddress extends AddressBase {

    private static final long serialVersionUID = 1L;
    private static final String CHANNEL_KEY = "channel";

    private String channel;

    public ChannelAddress() {
    }

    public ChannelAddress(String channel) {
        this.channel = channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        ChannelAddress that = (ChannelAddress) o;

        if (channel != null ? !channel.equals(that.channel) : that.channel != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return channel != null ? channel.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "channel:" + channel;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
