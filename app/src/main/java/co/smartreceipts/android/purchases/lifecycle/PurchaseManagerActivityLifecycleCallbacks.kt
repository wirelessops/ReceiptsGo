package co.smartreceipts.android.purchases.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.purchases.PurchaseManager;

public class PurchaseManagerActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private final PurchaseManager purchaseManager;

    public PurchaseManagerActivityLifecycleCallbacks(@NonNull PurchaseManager purchaseManager) {
        this.purchaseManager = Preconditions.checkNotNull(purchaseManager);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        this.purchaseManager.onActivityResumed(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        this.purchaseManager.onActivityPaused();
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
