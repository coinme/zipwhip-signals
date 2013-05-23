package com.zipwhip.signals.mailbox;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.exceptions.NotFoundException;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.model.Rows;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.util.RangeBuilder;
import com.zipwhip.signals.address.Address;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Date: 5/19/13
 * Time: 12:30 PM
 *
 * @author Michael
 * @version 1
 */
public class CassandraMailbox implements Mailbox {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraMailbox.class);
    private static final Integer THREE_MONTHS_IN_SECONDS = 60 * 60 * 24 * 30 * 3;

    private static final ColumnFamily<String, String> CF_MAIL_VERSIONS =
            new ColumnFamily<String, String>(
                    "MailVersions",           // Column Family Name
                    StringSerializer.get(),   // Key Serializer
                    StringSerializer.get());  // Column Serializer

    private static final ColumnFamily<String, String> CF_MAIL =
            new ColumnFamily<String, String>(
                    "Mail",              // Column Family Name
                    StringSerializer.get(),   // Key Serializer
                    StringSerializer.get());    // Column Serializer

    private final CuratorFramework curator;
    private final Keyspace keyspace;

    public CassandraMailbox(CuratorFramework curator, Keyspace keyspace) {
        this.curator = curator;
        this.keyspace = keyspace;
    }

    @Override
    public Mail getLast(Address address) throws Exception {
        String rowKey = getRowKey(address);
        Long version = getCurrentVersion(rowKey);

        if (version == null) {
            return null;
        }

        return getAt(rowKey, version);
    }

    @Override
    public Mail getAt(Address address, long version) throws Exception {
        return getAt(getRowKey(address), version);
    }

    @Override
    public List<Mail> get(Address address, long startVersion, long endVersion) throws Exception {
        // we need to do a key-slice range thing.
        Rows<String, String> rows = keyspace.prepareQuery(CF_MAIL)
                .getKeySlice(getKeySlice(address, startVersion, endVersion))
                .withColumnRange(new RangeBuilder()
                        .setLimit(2)
                        .build())
                .execute()
                .getResult();

        if (rows.isEmpty()) {
            LOGGER.error("Result from Cassandra was empty. Possibly bad data?");
            return null;
        }

        return mailify(rows);
    }

    @Override
    public List<Mail> getAfter(Address address, long version) throws Exception {
        Long currentVersion = getMaxVersion(address);

        if (currentVersion == null || currentVersion <= version) {
            LOGGER.error(String.format("The version requested was higher than the current version: %s/%s", currentVersion, version));
            return null;
        }

        return get(address, version + 1, currentVersion);
    }

    @Override
    public long append(Address address, String content) throws Exception {
        InterProcessSemaphoreMutex mutex = new InterProcessSemaphoreMutex(curator, "/locks/mail/" + address);

        if (!mutex.acquire(10, TimeUnit.SECONDS)) {
            throw new Exception("Unable to lock mutex");
        }

        // we need to declare the version after it's been successfully saved. Otherwise we'd declare the version
        // via incr and then potentially create a hole if we crashed. What if Cassandra can't save the mail?
        // A dropped signal is potentially better than a hole. A hole would freak the clients out?
        // by crashing in this fashion, we're effectively allowing the caller to retry later. (without us having to
        // roll back).

        try {
            String rowKey = getRowKey(address);
            long version = getNextVersion(rowKey);

            putAtVersion(rowKey, content, version);

            setVersionActive(rowKey, version);

            return version;
        } finally {
            mutex.release();
        }
    }

    @Override
    public Long getMaxVersion(Address address) throws Exception {
        return getCurrentVersion(getRowKey(address));
    }

    private Mail getAt(String rowKey, long version) throws ConnectionException {

        try {
            Column<String> column = keyspace.prepareQuery(CF_MAIL)
                    .getRow(getRowKeyForVersion(rowKey, version))
                    .getColumn("data")
                    .execute()
                    .getResult();

            if (!column.hasValue()) {
                return null;
            }

            return new Mail(column.getStringValue(), version);
        } catch (NotFoundException e) {
            return null;
        }
    }

    private List<Mail> mailify(Rows<String, String> rows) {
        List<Mail> result = new LinkedList<Mail>();

        for (Row<String, String > row : rows) {
            if (row == null || row.getColumns() == null || row.getColumns().isEmpty()) {
                continue;
            }

            result.add(mailify(row));
        }

        return result;
    }

    private Mail mailify(Row<String, String> row) {
        String content = row.getColumns().getStringValue("data", null);
        Long version = row.getColumns().getLongValue("version", null);

        return new Mail(content, version);
    }

    private Collection<String> getKeySlice(Address address, long startVersion, long endVersion) {
        Collection<String> keys = new LinkedList<String>();
        String rowKey = getRowKey(address);

        for(long index = startVersion; index <= endVersion; index++){
            keys.add(getRowKeyForVersion(rowKey, index));
        }

        return keys;
    }

    private String getRowKeyForVersion(String rowKey, long version) {
        if (rowKey.contains("_")) {
            throw new IllegalArgumentException("The rowKey has a security hole");
        }

        return rowKey + "_" + version;
    }

    private String getRowKey(Address address) {
        return address.toString();
    }

    private void setVersionActive(String rowKey, long version) throws ConnectionException {
        keyspace.prepareColumnMutation(CF_MAIL_VERSIONS, rowKey, "version")
                // null TTL to never expire.
                .putValue(version, null)
                .execute();
    }

    private void putAtVersion(String rowKey, String content, long version) throws ConnectionException {
        MutationBatch m = keyspace.prepareMutationBatch();

        rowKey = getRowKeyForVersion(rowKey, version);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("Saving %s to %s", content, rowKey));
        }

        m.withRow(CF_MAIL, rowKey)
                .setDefaultTtl(THREE_MONTHS_IN_SECONDS)
                .putColumn("data", content)
                .putColumn("version", version);

        m.execute();
    }

    private long getNextVersion(String rowKey) throws ConnectionException {
        Long version = getCurrentVersion(rowKey);

        // set the new version.
        if (version == null) {
            return 0;
        } else {
            return version + 1;
        }
    }

    private Long getCurrentVersion(String rowKey) throws ConnectionException {
        Column<String> query = null;
        try {
            query = keyspace.prepareQuery(CF_MAIL_VERSIONS)
                    .getKey(rowKey)
                    .getColumn("version")
                    .execute()
                    .getResult();
        } catch (NotFoundException e) {
            return null;
        }

        if (!query.hasValue()) {
            return null;
        }

        return query.getLongValue();
    }

    public CuratorFramework getCurator() {
        return curator;
    }

    public Keyspace getKeyspace() {
        return keyspace;
    }
}
