package com.zipwhip.util;

import com.zipwhip.concurrent.ObservableFuture;
import com.zipwhip.events.Observer;
import org.slf4j.Logger;

/**
 * Date: 5/19/13
 * Time: 12:14 AM
 *
 * @author Michael
 * @version 1
 */
public class LogObserver<T> implements Observer<ObservableFuture<T>> {

    private final Logger logger;

    public LogObserver(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void notify(Object sender, ObservableFuture<T> future) {
        if (future == null) {
            logger.warn("The future was null");
        } else if (future.isCancelled()) {
            logger.error("The future was cancelled.");
        } else if (future.isFailed()) {
            logger.error("The future was failed", future.getCause());
        } else if (future.isSuccess()) {
            if (logger.isDebugEnabled()) {
                logger.debug("The future succeeded with result: " + future.getResult());
            }
        }

    }
}
