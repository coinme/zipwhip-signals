package com.zipwhip.signals.address;

/**
 * Created by IntelliJ IDEA.
 * User: Michael
 * Date: Dec 11, 2010
 * Time: 5:50:25 PM
 */
@OneToOneAddress
public class ClientAddress extends AddressBase {

    private static final long serialVersionUID = 6712566321988288130L;

    private String clientId;

    public ClientAddress() {
    }

    public ClientAddress(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        ClientAddress that = (ClientAddress) o;

        if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return clientId != null ? clientId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "client:" + clientId;
    }

}
