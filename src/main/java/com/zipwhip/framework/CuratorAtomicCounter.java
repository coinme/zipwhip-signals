package com.zipwhip.framework;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.atomic.PromotedToLock;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

/**
 * Date: 5/19/13
 * Time: 12:26 AM
 *
 * @author Michael
 * @version 1
 */
public class CuratorAtomicCounter implements AtomicCounter {

    private static final RetryPolicy RETRY_POLICY = new BoundedExponentialBackoffRetry(10, 10000, 10);

    private DistributedAtomicLong operation;

    public CuratorAtomicCounter(CuratorFramework client, String counterId) {
        this.operation = new DistributedAtomicLong(client, "/counters/" + counterId,
                RETRY_POLICY,
                PromotedToLock.builder()
                        .lockPath("/locks/counters/" + counterId)
                        .retryPolicy(RETRY_POLICY)
                        .timeout(1, TimeUnit.SECONDS)
                        .build());
    }

    @Override
    public Long incrementAndGet() throws Exception {
        AtomicValue<Long> value = operation.increment();

        if (!value.succeeded()) {
            throw new Exception("Not successful! (Unknown reason)");
        }

        return value.postValue();
    }
}
