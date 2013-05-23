package com.zipwhip.signals.address;

/**
 * Date: 5/7/13
 * Time: 5:47 PM
 *
 * @author Michael
 * @version 1
 */
public class ServerAddress extends AddressBase{

    private static final long serialVersionUID = 3280231462877285009L;

    private String name;

    public ServerAddress() {
    }

    public ServerAddress(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerAddress)) return false;

        ServerAddress that = (ServerAddress) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    public String toString() {
        return "server:" + name;
    }
}
