package com.wops.receiptsgo.utils;

import androidx.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory with the ability to set the thread name prefix.
 * This class is exactly similar to
 * {@link java.util.concurrent.Executors#defaultThreadFactory()}
 * from JDK8, except for the thread naming feature.
 *
 * <p>
 * The factory creates threads that have names on the form
 * <i>prefix-N-thread-M</i>, where <i>prefix</i>
 * is a string provided in the constructor, <i>N</i> is the sequence number of
 * this factory, and <i>M</i> is the sequence number of the thread created
 * by this factory.
 *
 * <a href="http://stackoverflow.com/questions/6113746/naming-threads-and-thread-pools-of-executorservice/6113794">
 * See guide here</a>
 */
public class PreFixedThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    /**
     * Creates a new ThreadFactory where threads are created with a name prefix
     * of <code>prefix</code>.
     *
     * @param prefix Thread name prefix. Never use a value of "pool" as in that
     *      case you might as well have used
     *      {@link java.util.concurrent.Executors#defaultThreadFactory()}.
     */
    public PreFixedThreadFactory(@NonNull String prefix) {
        final SecurityManager securityManager = System.getSecurityManager();
        group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = prefix + "-" + poolNumber.getAndIncrement() + "-thread-";
    }


    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        final Thread thread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
