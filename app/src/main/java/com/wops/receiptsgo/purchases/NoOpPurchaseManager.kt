package com.wops.receiptsgo.purchases

import android.app.Activity
import android.app.Application
import android.util.Log
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.wops.receiptsgo.purchases.model.ConsumablePurchase
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.model.ManagedProduct
import com.wops.receiptsgo.purchases.source.PurchaseSource
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class NoOpPurchaseManager @Inject constructor(): PurchaseManager {
    override fun addEventListener(listener: PurchaseEventsListener) {
        // Nothing
    }

    override fun removeEventListener(listener: PurchaseEventsListener) {
       // Nothing
    }

    override fun initialize(application: Application) {
        Log.d("NoOpPurchaseManager", "Initializing the purchase manager");

    }

    override fun onActivityResumed(activity: Activity) {
        // Nothing
    }

    override fun getAllOwnedPurchasesAndSync(): Single<Set<ManagedProduct?>?>? {
        return null
    }

    override fun getAllAvailablePurchaseSkus(): Single<Set<ProductDetails?>?>? {
        return null
    }

    override fun getAllAvailablePurchases(): Single<Set<InAppPurchase?>?> {
        return Single.just(emptySet<InAppPurchase>())
    }

    override fun initiatePurchase(
        skuDetails: ProductDetails,
        purchaseSource: PurchaseSource
    ) {
        // do nothing
    }

    override fun initiatePurchase(
        inAppPurchase: InAppPurchase,
        purchaseSource: PurchaseSource
    ) {
        // do nothing
    }

    override fun queryUnacknowledgedSubscriptions(): Single<List<Purchase?>?>? {
        return null
    }

    override fun acknowledgePurchase(purchase: Purchase?): Completable? {
        return null
    }

    override fun consumePurchase(consumablePurchase: ConsumablePurchase): Completable {
        return Completable.complete()
    }
}
