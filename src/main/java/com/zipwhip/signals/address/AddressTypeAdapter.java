package com.zipwhip.signals.address;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Date: 5/18/13
 * Time: 9:50 PM
 *
 * @author Michael
 * @version 1
 */
public class AddressTypeAdapter implements JsonSerializer<Address>, JsonDeserializer<Address> {

    @Override
    public Address deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = (JsonObject)json;

        String classString =  object.get("class").getAsString();

        if ("ClientAddress".equals(classString)) {
            ClientAddress result = new ClientAddress();

            result.setClientId(object.get("clientId").getAsString());

            return result;
        } else if ("ServerAddress".equals(classString)) {
            ServerAddress result = new ServerAddress();

            result.setName(object.get("name").getAsString());

            return result;
        } else if ("ChannelAddress".equals(classString)) {
            ChannelAddress result = new ChannelAddress();

            result.setChannel(object.get("channel").getAsString());

            return result;
        } else {
            throw new JsonParseException("Unknown type: " + classString);
        }
    }

    @Override
    public JsonElement serialize(Address src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject envelope = new JsonObject();

        envelope.addProperty("class", src.getClass().getSimpleName());

        if (src instanceof ClientAddress) {
            envelope.addProperty("clientId", ((ClientAddress) src).getClientId());
        } else if (src instanceof ServerAddress) {
            envelope.addProperty("name", ((ServerAddress) src).getName());
        } else if (src instanceof ChannelAddress) {
            envelope.addProperty("channel", ((ChannelAddress) src).getChannel());
        }

        return envelope;
    }
}
