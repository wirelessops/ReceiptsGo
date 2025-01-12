package com.wops.receiptsgo.purchases

import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.source.PurchaseSource

interface PurchaseEventsListener {
    /**
     * Called if we successfully completed a purchase
     *
     * @param inAppPurchase the new subscription that we purchased
     * @param purchaseSource where the purchase flow was initiated
     */
    fun onPurchaseSuccess(inAppPurchase: InAppPurchase, purchaseSource: PurchaseSource)

    /**
     * Called if we failed to complete a purchase
     *
     * @param purchaseSource where the purchase flow was initiated
     */
    fun onPurchaseFailed(purchaseSource: PurchaseSource)

    /**
     * Called when we get the purchase in a pending state
     */
    fun onPurchasePending()
}