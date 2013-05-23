package com.zipwhip.signals.discovery;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Date: 5/19/13
 * Time: 12:38 AM
 *
 * @author Michael
 * @version 1
 */
public class SignalServerCuratorFactoryBean {

    public static CuratorFramework create(String connectString) {
        CuratorFramework framework = CuratorFrameworkFactory.newClient(connectString, new ExponentialBackoffRetry(1000, 3));

        framework.start();

        return framework;
    }

}
