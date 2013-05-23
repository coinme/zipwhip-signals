package com.zipwhip.signals.address;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Date: 5/18/13
 * Time: 10:02 PM
 *
 * @author Michael
 * @version 1
 */
public class AddressPersister implements Persister<Address> {

    private Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Address.class, new AddressTypeAdapter())
            .create();

    @Override
    public Address parse(String data) {
        return gson.fromJson(data, Address.class);
    }

    @Override
    public String serialize(Address data) {
        return gson.toJson(data);
    }
}
