package com.wops.receiptsgo.ocr.purchases;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import org.reactivestreams.Subscriber;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import com.wops.core.di.scopes.ApplicationScope;
import com.wops.analytics.log.Logger;
import dagger.Lazy;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

@ApplicationScope
public class LocalOcrScansTracker {

    private static final String KEY_AVAILABLE_SCANS = "key_int_available_ocr_scans";

    private final Lazy<SharedPreferences> sharedPreferences;
    private final Scheduler firstLocalScanReadScheduler;
    private final BehaviorSubject<Integer> remainingScansSubject = BehaviorSubject.create();
    private final AtomicBoolean haveWeCalledTheLocalScansStreamYet = new AtomicBoolean(false);

    @Inject
    public LocalOcrScansTracker(@NonNull Lazy<SharedPreferences> sharedPreferences) {
        this(sharedPreferences, Schedulers.io());
    }

    @VisibleForTesting
    LocalOcrScansTracker(@NonNull Lazy<SharedPreferences> sharedPreferences,
                         @NonNull Scheduler firstLocalScanReadScheduler) {
        this.sharedPreferences = Preconditions.checkNotNull(sharedPreferences);
        this.firstLocalScanReadScheduler = Preconditions.checkNotNull(firstLocalScanReadScheduler);
    }

    /**
     * @return the remaining Ocr scan count that is allowed for this user. Please note that is
     * this not the authority for this (ie it's not the server), this may not be fully accurate, so we
     * may still get a remote error after a scan. Additionally, please note that this {@link Observable}
     * will only call {@link Subscriber#onNext(Object)} with the latest value (and never onComplete or
     * onError) to allow us to continually get the updated value
     *
     * TODO: This whole thing is super weird. We should check if we can make this NOT a stream and instead a normal Observable
     */
    @NonNull
    public Observable<Integer> getRemainingScansStream() {
        if (!haveWeCalledTheLocalScansStreamYet.getAndSet(true)) {
            // The first time we call this method, supply the remaining count to it
            Single.fromCallable(this::getRemainingScans)
                    .doOnSuccess(remainingScansSubject::onNext)
                    .subscribeOn(firstLocalScanReadScheduler)
                    .subscribe();
        }
        return remainingScansSubject;
    }

    /**
     * @return the locally tracked (ie possibly inaccurate) remaining scans count
     */
    public int getRemainingScans() {
        return sharedPreferences.get().getInt(KEY_AVAILABLE_SCANS, 0);
    }

    public void setRemainingScans(int remainingScans) {
        Logger.info(this, "Setting scans remaining as {}.", remainingScans);
        sharedPreferences.get().edit().putInt(KEY_AVAILABLE_SCANS, remainingScans).apply();
        remainingScansSubject.onNext(remainingScans);
    }

    public void decrementRemainingScans() {
        if (getRemainingScans() > 0) {
            sharedPreferences.get().edit().putInt(KEY_AVAILABLE_SCANS, getRemainingScans() - 1).apply();
            remainingScansSubject.onNext(getRemainingScans());
        }
    }
}
