package co.smartreceipts.android.sync.utils;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * Defines our executor threads that we can use for must sync-based operations. The main benefit of
 * using this vs {@link Schedulers#io()} is that is allows us access the underlying executor pool,
 * which we use for Google Drive (which defaults its callbacks on the main thread)
 */
public class SyncSchedulers {

    private static final int POOL_THREADS = 3;
    private static final Executor DRIVE_EXECUTORS = Executors.newFixedThreadPool(POOL_THREADS);
    private static final Scheduler DRIVE_SCHEDULER = Schedulers.from(DRIVE_EXECUTORS);

    @NonNull
    public static Scheduler io() {
        return DRIVE_SCHEDULER;
    }

    @NonNull
    public static Executor executor() {
        return DRIVE_EXECUTORS;
    }

}
