package co.smartreceipts.android.ad;

import android.app.Activity;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ActivityScope;

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
