package com.wops.receiptsgo;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class SameThreadExecutorService extends AbstractExecutorService {

    private volatile boolean isShutdown = false;

    @Override
    public void shutdown() {
        isShutdown = true;
    }

    @NonNull
    @Override
    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return isShutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        shutdown();
        return isTerminated();
    }

    @Override
    public void execute(@NonNull Runnable command) {
        command.run();
    }
}
