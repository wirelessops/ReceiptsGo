package com.wops.receiptsgo.ad;

import android.app.Activity;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import com.wops.core.di.scopes.ActivityScope;


@ActivityScope
public class EmptyBannerAdPresenter implements AdPresenter {

    @Inject
    EmptyBannerAdPresenter() {
        /* no-op */
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity) {
        /* no-op */
    }

    @Override
    public void onResume() {
        /* no-op */
    }

    @Override
    public void onPause() {
        /* no-op */
    }

    @Override
    public void onDestroy() {
        /* no-op */
    }

    @Override
    public void onSuccessPlusPurchase() {
        /* no-op */
    }
}
