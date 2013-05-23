package com.zipwhip.signals;

import com.zipwhip.framework.pubsub.EventData;
import com.zipwhip.signals.message.Message;
import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.Converter;
import com.zipwhip.util.DataConversionException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Date: 5/7/13
 * Time: 5:25 PM
 *
 * @author Michael
 * @version 1
 */
public class MessageConverter implements Converter<EventData, Collection<Message>> {

    @Override
    public Collection<Message> convert(EventData eventData) throws DataConversionException {
        Object[] extras = eventData.getExtras();

        if (CollectionUtil.isNullOrEmpty(extras)) {
            return  null;
        }

        Collection<Message> result = new ArrayList<Message>(extras.length);

        for (Object extra : extras) {
            if (!(extra instanceof Message)) {
                throw new DataConversionException("Wrong type of extra. Expected message, was: " + extra.getClass());
            }

            result.add((Message)extra);
        }

        return result;
    }
}
