package com.wops.receiptsgo.purchases.consumption

import com.wops.receiptsgo.purchases.PurchaseManager
import com.wops.receiptsgo.purchases.model.ConsumablePurchase
import com.wops.receiptsgo.purchases.model.PurchaseFamily
import co.smartreceipts.core.di.scopes.ApplicationScope
import io.reactivex.Completable
import javax.inject.Inject

@ApplicationScope
class ConsumableInAppPurchaseConsumer @Inject constructor(private val purchaseManager: PurchaseManager) :
    InAppPurchaseConsumer<ConsumablePurchase> {

    override fun isConsumed(managedProduct: ConsumablePurchase, purchaseFamily: PurchaseFamily) =
        false

    override fun consumePurchase(
        managedProduct: ConsumablePurchase, purchaseFamily: PurchaseFamily
    ): Completable = purchaseManager.consumePurchase(managedProduct)

}