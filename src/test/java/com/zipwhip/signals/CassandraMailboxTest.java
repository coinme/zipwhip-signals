package com.zipwhip.signals;

import com.zipwhip.signals.address.ClientAddress;
import com.zipwhip.signals.mailbox.CassandraMailbox;
import com.zipwhip.signals.mailbox.Mail;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Date: 5/19/13
 * Time: 12:17 PM
 *
 * @author Michael
 * @version 1
 */
public class CassandraMailboxTest extends FrameworkTestBase {

    @Autowired
    CassandraMailbox mailbox;

    @Test
    public void testEnqueue() throws Exception {
        ClientAddress clientAddress = new ClientAddress(UUID.randomUUID().toString());

        long version0 = mailbox.append(clientAddress, "Test0");
        assertEquals(0, version0);

        long version1 = mailbox.append(clientAddress, "Test1");
        assertEquals(1, version1);

        long version2 = mailbox.append(clientAddress, "Test2");
        assertEquals(2, version2);

        Mail mail = mailbox.getLast(clientAddress);

        assertNotNull(mail);
        assertEquals(2, mail.getVersion());
        assertEquals(mail, mailbox.getAt(clientAddress, 2));
        assertEquals("Test2", mail.getContent());

        assertEquals("Test0", mailbox.getAt(clientAddress, 0).getContent());
        assertEquals("Test1", mailbox.getAt(clientAddress, 1).getContent());
        assertEquals("Test2", mailbox.getAt(clientAddress, 2).getContent());

        List<Mail> collection = mailbox.getAfter(clientAddress, -1);
        assertEquals(3, collection.size());

        Map<Long, String> map = sort(collection);

        assertEquals("Test0", map.get(0L));
        assertEquals("Test1", map.get(1L));
        assertEquals("Test2", map.get(2L));

        map = sort(mailbox.get(clientAddress, 0, 2));
        assertEquals(3, collection.size());

        assertEquals("Test0", map.get(0L));
        assertEquals("Test1", map.get(1L));
        assertEquals("Test2", map.get(2L));

        assertEquals((Long)2L, mailbox.getMaxVersion(clientAddress));
    }

    private Map<Long, String> sort(List<Mail> collection) {
        Map<Long, String> result = new HashMap<Long, String>();

        for (Mail mail : collection) {
            result.put(mail.getVersion(), mail.getContent());
        }

        return result;
    }
}
