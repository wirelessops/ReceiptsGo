package co.smartreceipts.android.purchases;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.android.billingclient.api.SkuDetails;
import com.google.common.base.Preconditions;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.DataPoint;
import co.smartreceipts.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.purchases.lifecycle.PurchaseManagerActivityLifecycleCallbacks;
import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


@ApplicationScope
public class PurchaseManager {


    private static final int BILLING_RESPONSE_CODE_OK = 0;

    private final Analytics analytics;
    private final Scheduler subscribeOnScheduler;
    private final AtomicReference<WeakReference<Activity>> activityReference = new AtomicReference<>(new WeakReference<>(null));


    private final BillingClientManager billingClientManager;

    @Inject
    public PurchaseManager(@NonNull BillingClientManager billingClientManager,
                           @NonNull Analytics analytics) {
        this(billingClientManager, analytics, Schedulers.io());
    }

    @VisibleForTesting
    PurchaseManager(@NonNull BillingClientManager billingClientManager,
                    @NonNull Analytics analytics,
                    @NonNull Scheduler subscribeOnScheduler) {
        this.billingClientManager = billingClientManager;
        this.analytics = analytics;
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    /**
     * Adds an event listener to our stack in order to start receiving callbacks
     *
     * @param listener the listener to register
     */
    public void addEventListener(@NonNull PurchaseEventsListener listener) {
        billingClientManager.addPurchaseEventListener(listener);
    }

    /**
     * Removes an event listener from our stack in order to stop receiving callbacks
     *
     * @param listener the listener to unregister
     */
    public void removeEventListener(@NonNull PurchaseEventsListener listener) {
        billingClientManager.removePurchaseEventListener(listener);
    }


    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void initialize(@NonNull Application application) {
        Logger.debug(PurchaseManager.this, "Initializing the purchase manager");

        application.registerActivityLifecycleCallbacks(new PurchaseManagerActivityLifecycleCallbacks(this));

        // Initialize our purchase set to update our wallet
        getAllOwnedPurchases()
                .subscribeOn(subscribeOnScheduler)
                .subscribe(managedProducts -> Logger.debug(PurchaseManager.this, "Successfully initialized all user owned purchases {}.", managedProducts),
                        throwable -> Logger.error(PurchaseManager.this, "Failed to initialize all user owned purchases.", throwable));
    }

    /**
     * Should be called whenever we resume a new activity in order to allow us to use it for initiating
     * purchases
     *
     * @param activity the current {@link Activity}
     */
    public void onActivityResumed(@NonNull Activity activity) {
        final Activity existingActivity = activityReference.get().get();
        if (!activity.equals(existingActivity)) {
            activityReference.set(new WeakReference<>(activity));
        }
    }

    public Single<Set<ManagedProduct>> getAllOwnedPurchases() {
        return billingClientManager.queryAllOwnedPurchases()
                .doOnSuccess(managedProducts -> Logger.debug(this, "Found owned purchases: " + managedProducts))
                .subscribeOn(subscribeOnScheduler);
    }

    public Observable<Set<SkuDetails>> getAllAvailablePurchaseSkus() {
        return billingClientManager.queryAllAvailablePurchases()
                .toObservable()
                .subscribeOn(subscribeOnScheduler);
    }

    @NonNull
    public Observable<Set<InAppPurchase>> getAllAvailablePurchases() {
        return getAllAvailablePurchaseSkus()
                .map(availablePurchases -> {
                    final Set<InAppPurchase> inAppPurchases = new HashSet<>();
                    for (SkuDetails purchase : availablePurchases) {
                        inAppPurchases.add(InAppPurchase.from(purchase.getSku()));
                    }
                    return inAppPurchases;
                });
    }


    public void initiatePurchase(@NonNull final SkuDetails skuDetails, @NonNull final PurchaseSource purchaseSource) {
        Logger.info(PurchaseManager.this, "Initiating purchase of {} from {}.", skuDetails, purchaseSource);
        analytics.record(new DefaultDataPointEvent(Events.Purchases.ShowPurchaseIntent).addDataPoint(new DataPoint("sku", skuDetails.getSku())).addDataPoint(new DataPoint("source", purchaseSource)));

        billingClientManager.initiatePurchase(skuDetails, activityReference.get().get())
                .subscribe(() -> {
                }, throwable -> {
                });
    }

    public void initiatePurchase(@NonNull final InAppPurchase inAppPurchase, @NonNull final PurchaseSource purchaseSource) {
        Logger.info(PurchaseManager.this, "Initiating purchase of {} from {}.", inAppPurchase, purchaseSource);
        analytics.record(new DefaultDataPointEvent(Events.Purchases.ShowPurchaseIntent).addDataPoint(new DataPoint("sku", inAppPurchase.getSku())).addDataPoint(new DataPoint("source", purchaseSource)));

        billingClientManager.querySkuDetails(inAppPurchase)
                .flatMapCompletable(skuDetails -> billingClientManager.initiatePurchase(skuDetails, activityReference.get().get()))
                .subscribe(() -> {
                }, throwable -> {
                });
    }


    /**
     * Attempts to consume the purchase of a given {@link ConsumablePurchase}
     *
     * @param consumablePurchase the product to consume
     * @return an {@link io.reactivex.Completable} with the success/error result
     */
    @NonNull
    public Completable consumePurchase(@NonNull final ConsumablePurchase consumablePurchase) {
        Logger.info(PurchaseManager.this, "Consuming the purchase of {}", consumablePurchase.getInAppPurchase());
        String sku = consumablePurchase.getInAppPurchase().getSku();

        return billingClientManager.consumePurchase(consumablePurchase)
                .doOnError(throwable ->
                        Logger.warn(this,
                                "Received an unexpected response code for the consumption of this product {}", sku, throwable))
                .doOnComplete(() -> Logger.info(PurchaseManager.this, "Successfully consumed the purchase of {}", sku));
    }

}
