package com.zipwhip.signals;

import com.zipwhip.concurrent.DefaultObservableFuture;
import com.zipwhip.concurrent.ObservableFuture;
import com.zipwhip.executors.SimpleExecutor;
import com.zipwhip.signals.address.Address;
import com.zipwhip.signals.address.AddressPersister;
import com.zipwhip.signals.address.Persister;
import com.zipwhip.util.CollectionUtil;
import com.zipwhip.util.InputCallable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * Date: 5/18/13
 * Time: 9:07 PM
 *
 * @author Michael
 * @version 1
 */
public class RedisTopology implements Topology {

    private static final String KEY_BASE = "topology:";

    private static final Long SUCCESS = 1L;
    private static final Long FAILURE = 0L;

    private JedisPool jedisPool;
    private Persister<Address> persister = new AddressPersister();
    private Executor networkExecutor = SimpleExecutor.getInstance();
    private Executor eventExecutor = SimpleExecutor.getInstance();

    @Override
    public ObservableFuture<Set<Address>> get(final Address client) {
        return run(new InputCallable<Jedis, Set<Address>>() {
            @Override
            public Set<Address> call(Jedis jedis) {
                final String key = KEY_BASE + client.toString();

                Set<String> strings = jedis.smembers(key);

                if (CollectionUtil.isNullOrEmpty(strings)) {
                    return null;
                }

                Set<Address> result = new HashSet<Address>();
                for (String string : strings) {
                    Address address = persister.parse(string);

                    result.add(address);
                }

                return result;
            }
        });
    }


    @Override
    public ObservableFuture<Void> add(final Address client, final Address server) {
        return run(new InputCallable<Jedis, Void>() {
            @Override
            public Void call(Jedis jedis) throws Exception {
                final String key = getKey(client);

                Long reply = jedis.sadd(key, persister.serialize(server));

                validateOrFail(reply);

                return null;
            }
        });
    }

    @Override
    public ObservableFuture<Void> remove(final Address client, final Address server) {
        return run(new InputCallable<Jedis, Void>() {
            @Override
            public Void call(Jedis jedis) throws Exception {
                final String key = getKey(client);

                Long reply = jedis.srem(key, persister.serialize(server));

                validateOrFail(reply);

                return null;
            }

        });
    }

    private void validateOrFail(Long reply) throws Exception {
        if (reply == null || failure(reply) || !success(reply)) {
            throw new Exception("Failure: " + reply);
        }
    }

    private boolean failure(Long reply) {
        return FAILURE.equals(reply);
    }

    private boolean success(Long reply) {
        return SUCCESS.equals(reply);
    }

    private <T> ObservableFuture<T> run(final InputCallable<Jedis, T> callable) {
        final ObservableFuture<T> future = new DefaultObservableFuture<T>(this, eventExecutor);

        try {
            networkExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    final Jedis jedis = jedisPool.getResource();

                    try {
                        T result = callable.call(jedis);

                        future.setSuccess(result);
                    } catch (Exception e) {
                        future.setFailure(e);
                    } finally {
                        jedisPool.returnResource(jedis);
                    }
                }
            });
        } catch (Exception e) {
            future.setFailure(e);
        }

        return future;
    }

    private String getKey(Address address) {
        return KEY_BASE + address.toString();
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
}
