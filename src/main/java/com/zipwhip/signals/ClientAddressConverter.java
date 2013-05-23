package com.zipwhip.signals;

import com.zipwhip.framework.pubsub.EventData;
import com.zipwhip.signals.address.ClientAddress;
import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.Converter;
import com.zipwhip.util.DataConversionException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Date: 5/18/13
 * Time: 10:57 PM
 *
 * @author Michael
 * @version 1
 */
public class ClientAddressConverter implements Converter<EventData, Collection<ClientAddress>> {

    @Override
    public Collection<ClientAddress> convert(EventData eventData) throws DataConversionException {
        Object[] extras = eventData.getExtras();

        if (CollectionUtil.isNullOrEmpty(extras)) {
            return  null;
        }

        Collection<ClientAddress> result = new ArrayList<ClientAddress>(extras.length);

        for (Object extra : extras) {
            if (extra instanceof ClientAddress) {
                result.add((ClientAddress)extra);
            } else if (extra instanceof String) {
                result.add(new ClientAddress((String)extra));
            } else {
                throw new DataConversionException("Wrong type of extra. Expected message, was: " + extra.getClass());
            }
        }

        return result;
    }
}