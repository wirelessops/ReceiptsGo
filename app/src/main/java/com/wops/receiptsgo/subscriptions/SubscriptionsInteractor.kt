package com.wops.receiptsgo.subscriptions

import com.wops.receiptsgo.purchases.PurchaseEventsListener
import com.wops.receiptsgo.purchases.PurchaseManager
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.model.PurchaseFamily
import com.wops.receiptsgo.purchases.model.Subscription
import com.wops.receiptsgo.purchases.source.PurchaseSource
import com.wops.core.di.scopes.ApplicationScope
import com.android.billingclient.api.ProductDetails
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ApplicationScope
class SubscriptionsInteractor(
    private val purchaseManager: PurchaseManager,
    private val subscribeOnScheduler: Scheduler = Schedulers.io(),
    private val observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
) {

    @Inject
    constructor(purchaseManager: PurchaseManager) : this(
        purchaseManager,
        Schedulers.io(),
        AndroidSchedulers.mainThread()
    )

    fun addSubscriptionListener(listener: PurchaseEventsListener) {
        purchaseManager.addEventListener(listener)
    }

    fun removeSubscriptionListener(listener: PurchaseEventsListener) {
        purchaseManager.removeEventListener(listener)
    }

    fun getPlansWithOwnership(): Single<Map<ProductDetails, Boolean>> {
        return Single.zip(
            getAvailablePlansInfo(),
            getOwnedPlans(),
            BiFunction<List<ProductDetails>, List<InAppPurchase>, Map<ProductDetails, Boolean>> { plansInfo: List<ProductDetails>, ownedPlans: List<InAppPurchase> ->
                val result = HashMap<ProductDetails, Boolean>()
                for (plan in plansInfo) {
                    for (ownedPlan in ownedPlans) {
                        if (ownedPlan.sku == plan.productId) {
                            result[plan] = true
                        }
                    }

                    if (!result.contains(plan)) {
                        result[plan] = false
                    }
                }

                return@BiFunction result
            })
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    fun purchaseStandardPlan() {
        purchaseManager.initiatePurchase(InAppPurchase.StandardSubscriptionPlan, PurchaseSource.SubscriptionsScreen)
    }

    fun purchasePremiumPlan() {
        purchaseManager.initiatePurchase(InAppPurchase.PremiumSubscriptionPlan, PurchaseSource.SubscriptionsScreen)
    }

    private fun getOwnedPlans(): Single<List<InAppPurchase>> {

        return purchaseManager.allOwnedPurchasesAndSync
            .map { set ->
                set.filter { managedProduct ->
                    val inAppPurchase = managedProduct.inAppPurchase
                    inAppPurchase.type == Subscription::class.java && inAppPurchase.purchaseFamilies.contains(
                        PurchaseFamily.SubscriptionPlans
                    )
                }
            }
            .map { managedProducts -> managedProducts.map { managedProduct -> managedProduct.inAppPurchase } }
    }

    private fun getAvailablePlansInfo(): Single<List<ProductDetails>> {

        return purchaseManager.allAvailablePurchaseSkus
            .map { set ->
                set.filter { skuDetails ->
                    val inAppPurchase = InAppPurchase.from(skuDetails.productId)
                    inAppPurchase != null && inAppPurchase.type == Subscription::class.java
                            && inAppPurchase.purchaseFamilies.contains(PurchaseFamily.SubscriptionPlans)
                }
            }
    }
}