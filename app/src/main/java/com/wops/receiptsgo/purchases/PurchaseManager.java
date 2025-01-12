package com.wops.receiptsgo.purchases;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;

import androidx.annotation.NonNull;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.wops.receiptsgo.purchases.model.ConsumablePurchase;
import com.wops.receiptsgo.purchases.model.InAppPurchase;
import com.wops.receiptsgo.purchases.model.ManagedProduct;
import com.wops.receiptsgo.purchases.source.PurchaseSource;

import java.util.List;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface PurchaseManager {
    void addEventListener(@NonNull PurchaseEventsListener listener);

    void removeEventListener(@NonNull PurchaseEventsListener listener);

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void initialize(@NonNull Application application);

    void onActivityResumed(@NonNull Activity activity);

    Single<Set<ManagedProduct>> getAllOwnedPurchasesAndSync();

    Single<Set<ProductDetails>> getAllAvailablePurchaseSkus();

    @NonNull
    Single<Set<InAppPurchase>> getAllAvailablePurchases();

    void initiatePurchase(@NonNull ProductDetails skuDetails, @NonNull PurchaseSource purchaseSource);

    void initiatePurchase(@NonNull InAppPurchase inAppPurchase, @NonNull PurchaseSource purchaseSource);

    Single<List<Purchase>> queryUnacknowledgedSubscriptions();

    Completable acknowledgePurchase(Purchase purchase);

    @NonNull
    Completable consumePurchase(@NonNull ConsumablePurchase consumablePurchase);
}
