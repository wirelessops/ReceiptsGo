package com.wops.receiptsgo;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import com.wops.receiptsgo.ad.AdStatusTracker;

public class ExtraInitializerFreeImpl implements ExtraInitializer {

    private final Executor executor;
    private final AdStatusTracker adStatusTracker;

    @Inject
    public ExtraInitializerFreeImpl(@NonNull Executor executor,
                                    @NonNull AdStatusTracker adStatusTracker) {
        this.executor = Preconditions.checkNotNull(executor);
        this.adStatusTracker = Preconditions.checkNotNull(adStatusTracker);
    }

    @Override
    public void init() {
        // Note: We call this in the background to pre-fetch and cache the results to avoid disk read violations
        this.executor.execute(adStatusTracker::shouldShowAds);
    }
}
