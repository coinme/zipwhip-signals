package com.zipwhip.signals;

import com.google.common.util.concurrent.ListenableFuture;
import com.netflix.astyanax.Execution;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.model.ConsistencyLevel;
import com.netflix.astyanax.query.RowQuery;
import com.netflix.astyanax.retry.RetryNTimes;
import com.netflix.astyanax.retry.RetryPolicy;
import com.netflix.astyanax.serializers.StringSerializer;
import com.zipwhip.concurrent.DefaultObservableFuture;
import com.zipwhip.concurrent.ObservableFuture;
import com.zipwhip.executors.SimpleExecutor;
import com.zipwhip.signals.address.Address;
import com.zipwhip.signals.address.AddressPersister;
import com.zipwhip.signals.address.Persister;
import com.zipwhip.util.InputCallable;
import com.zipwhip.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executor;

/**
 * Date: 5/19/13
 * Time: 4:09 PM
 *
 * @author Michael
 * @version 1
 */
public class CassandraTopology implements Topology {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraTopology.class);

    private static final ColumnFamily<String, String> CF_TOPOLOGY =
            new ColumnFamily<String, String>(
                    "Topology",              // Column Family Name
                    StringSerializer.get(),   // Key Serializer
                    StringSerializer.get());    // Column Serializer

    private static final RetryPolicy RETRY_POLICY = new RetryNTimes(10);

    private final Keyspace keyspace;
    private final Persister<Address> persister = new AddressPersister();

    private Executor executor = SimpleExecutor.getInstance();

    public CassandraTopology(Keyspace keyspace) {
        this.keyspace = keyspace;
    }

    @Override
    public ObservableFuture<Void> add(final Address client, final Address server) {
        MutationBatch m = keyspace.prepareMutationBatch()
                .withRetryPolicy(RETRY_POLICY)
                .setConsistencyLevel(ConsistencyLevel.CL_ALL);

        m.withRow(CF_TOPOLOGY, getRowKey(client))
                .putColumn(persister.serialize(server), 0);

        return run(m, new InputCallable<OperationResult<Void>, Void>() {
            @Override
            public Void call(OperationResult<Void> result) throws Exception {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("Column added %s -> %s", client, server));
                }

                return null;
            }
        });
    }

    @Override
    public ObservableFuture<Set<Address>> get(Address client) {
        RowQuery<String, String> query = keyspace.prepareQuery(CF_TOPOLOGY)
                .withRetryPolicy(RETRY_POLICY)
                .setConsistencyLevel(ConsistencyLevel.CL_ONE)
                .getRow(getRowKey(client));

        return run(query, new InputCallable<OperationResult<ColumnList<String>>, Set<Address>>() {
            @Override
            public Set<Address> call(OperationResult<ColumnList<String>> result) throws Exception {
                ColumnList<String> columns = result.getResult();

                if (columns.isEmpty()) {
                    return null;
                }

                Set<Address> set = new TreeSet<Address>();

                for (Column<String> column : columns) {
                    String addressString = column.getName();
                    if (StringUtil.isNullOrEmpty(addressString)) {
                        LOGGER.error("Empty entry in column list! " + column);
                        // TODO: nuke it?
                        continue;
                    }

                    Address address = persister.parse(addressString);

                    if (address == null) {
                        throw new Exception("Address was null out of parse! Data integrity check failure for: " + addressString);
                    }

                    set.add(address);
                }

                return set;
            }
        });
    }

    @Override
    public ObservableFuture<Void> remove(final Address client, final Address server) {
        String column = persister.serialize(server);

        Execution<Void> query = keyspace.prepareColumnMutation(CF_TOPOLOGY, getRowKey(client), column)
                .setConsistencyLevel(ConsistencyLevel.CL_ALL)
                .deleteColumn();

        return run(query, new InputCallable<OperationResult<Void>, Void>() {

            @Override
            public Void call(OperationResult<Void> result) throws Exception {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("Column deleted %s -> %s", client, server));
                }

                return null;
            }
        });
    }

    private String getRowKey(Address client) {
        return client.toString();
    }

    private <T, R> ObservableFuture<T> run(Execution<R> execution, final InputCallable<OperationResult<R>, T> callable) {
        final ObservableFuture<T> future = new DefaultObservableFuture<T>(this, executor);

        try {
            final ListenableFuture<OperationResult<R>> listenableFuture = execution.executeAsync();

            listenableFuture.addListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!listenableFuture.isDone() || listenableFuture.isCancelled()) {
                            future.setFailure(new Exception("The future was not done, or was cancelled."));
                            return;
                        }

                        T result = callable.call(listenableFuture.get());

                        future.setSuccess(result);
                    } catch (Exception e) {
                        future.setFailure(e);
                    }
                }
            }, executor);

        } catch (ConnectionException e) {
            future.setFailure(e);
        }

        return future;
    }
}
