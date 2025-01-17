package com.wops.receiptsgo.purchases;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.google.common.base.Preconditions;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import com.wops.analytics.Analytics;
import com.wops.analytics.events.DataPoint;
import com.wops.analytics.events.DefaultDataPointEvent;
import com.wops.analytics.events.Events;
import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.purchases.lifecycle.PurchaseManagerActivityLifecycleCallbacks;
import com.wops.receiptsgo.purchases.model.ConsumablePurchase;
import com.wops.receiptsgo.purchases.model.InAppPurchase;
import com.wops.receiptsgo.purchases.model.ManagedProduct;
import com.wops.receiptsgo.purchases.source.PurchaseSource;
import com.wops.core.di.scopes.ApplicationScope;
import io.reactivex.Completable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


@ApplicationScope
public class DefaultPurchaseManager implements PurchaseManager {

    private final Analytics analytics;
    private final Scheduler subscribeOnScheduler;
    private final AtomicReference<WeakReference<Activity>> activityReference = new AtomicReference<>(new WeakReference<>(null));


    private final BillingClientManager billingClientManager;

    @Inject
    public DefaultPurchaseManager(@NonNull BillingClientManager billingClientManager,
                           @NonNull Analytics analytics) {
        this(billingClientManager, analytics, Schedulers.io());
    }

    @VisibleForTesting
    DefaultPurchaseManager(@NonNull BillingClientManager billingClientManager,
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
    @Override
    public void addEventListener(@NonNull PurchaseEventsListener listener) {
        billingClientManager.addPurchaseEventListener(listener);
    }

    /**
     * Removes an event listener from our stack in order to stop receiving callbacks
     *
     * @param listener the listener to unregister
     */
    @Override
    public void removeEventListener(@NonNull PurchaseEventsListener listener) {
        billingClientManager.removePurchaseEventListener(listener);
    }


    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void initialize(@NonNull Application application) {
        Logger.debug("DefaultPurchaseManager", "Initializing the purchase manager");

        application.registerActivityLifecycleCallbacks(new PurchaseManagerActivityLifecycleCallbacks(this));

        // Initialize our purchase set to update our wallet
        getAllOwnedPurchasesAndSync()
                .subscribeOn(subscribeOnScheduler)
                .subscribe(managedProducts -> Logger.debug("DefaultPurchaseManager", "Successfully initialized all user owned purchases {}.", managedProducts),
                        throwable -> Logger.error("DefaultPurchaseManager", "Failed to initialize all user owned purchases.", throwable));
    }

    /**
     * Should be called whenever we resume a new activity in order to allow us to use it for initiating
     * purchases
     *
     * @param activity the current {@link Activity}
     */
    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        final Activity existingActivity = activityReference.get().get();
        if (!activity.equals(existingActivity)) {
            activityReference.set(new WeakReference<>(activity));
        }
    }

    @Override
    public Single<Set<ManagedProduct>> getAllOwnedPurchasesAndSync() {
        return billingClientManager.queryAllOwnedPurchasesAndSync()
                .doOnSuccess(managedProducts -> Logger.debug(this, "Found owned purchases: "
                        + managedProducts + " , synced with local purchase wallet"))
                .subscribeOn(subscribeOnScheduler);
    }

    @Override
    public Single<Set<ProductDetails>> getAllAvailablePurchaseSkus() {
        return billingClientManager.queryAllAvailablePurchases()
                .subscribeOn(subscribeOnScheduler);
    }

    @NonNull
    @Override
    public Single<Set<InAppPurchase>> getAllAvailablePurchases() {
        return getAllAvailablePurchaseSkus()
                .map(availablePurchases -> {
                    final Set<InAppPurchase> inAppPurchases = new HashSet<>();
                    for (ProductDetails purchase : availablePurchases) {
                        inAppPurchases.add(InAppPurchase.from(purchase.getProductId()));
                    }
                    return inAppPurchases;
                });
    }


    @Override
    public void initiatePurchase(@NonNull final ProductDetails skuDetails, @NonNull final PurchaseSource purchaseSource) {
        Logger.info("DefaultPurchaseManager", "Initiating purchase of {} from {}.", skuDetails, purchaseSource);
        analytics.record(new DefaultDataPointEvent(Events.Purchases.ShowPurchaseIntent).addDataPoint(new DataPoint("sku", skuDetails.getProductId())).addDataPoint(new DataPoint("source", purchaseSource)));

        billingClientManager.initiatePurchase(skuDetails, activityReference.get().get())
                .subscribe(() -> {
                }, throwable -> {
                });
    }

    @Override
    public void initiatePurchase(@NonNull final InAppPurchase inAppPurchase, @NonNull final PurchaseSource purchaseSource) {
        Logger.info("DefaultPurchaseManager", "Initiating purchase of {} from {}.", inAppPurchase, purchaseSource);
        analytics.record(new DefaultDataPointEvent(Events.Purchases.ShowPurchaseIntent).addDataPoint(new DataPoint("sku", inAppPurchase.getSku())).addDataPoint(new DataPoint("source", purchaseSource)));

        billingClientManager.querySkuDetails(inAppPurchase)
                .flatMapCompletable(skuDetails -> billingClientManager.initiatePurchase(skuDetails, activityReference.get().get()))
                .subscribe(() -> {
                }, throwable -> {
                });
    }

    @Override
    public Single<List<Purchase>> queryUnacknowledgedSubscriptions() {
        return billingClientManager.queryUnacknowledgedSubscriptions()
                .subscribeOn(subscribeOnScheduler);
    }

    @Override
    public Completable acknowledgePurchase(Purchase purchase) {
        return billingClientManager.acknowledgePurchase(purchase)
                .subscribeOn(subscribeOnScheduler);

    }


    /**
     * Attempts to consume the purchase of a given {@link ConsumablePurchase}
     *
     * @param consumablePurchase the product to consume
     * @return an {@link io.reactivex.Completable} with the success/error result
     */
    @NonNull
    @Override
    public Completable consumePurchase(@NonNull final ConsumablePurchase consumablePurchase) {
        Logger.info("DefaultPurchaseManager", "Consuming the purchase of {}", consumablePurchase.getInAppPurchase());
        String sku = consumablePurchase.getInAppPurchase().getSku();

        return billingClientManager.consumePurchase(consumablePurchase)
                .doOnError(throwable ->
                        Logger.warn(this,
                                "Received an unexpected response code for the consumption of this product {}", sku, throwable))
                .doOnComplete(() -> Logger.info("DefaultPurchaseManager", "Successfully consumed the purchase of {}", sku));
    }

}
