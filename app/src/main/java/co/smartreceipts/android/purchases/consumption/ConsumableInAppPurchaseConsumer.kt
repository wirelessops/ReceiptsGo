package co.smartreceipts.android.purchases.consumption

import co.smartreceipts.android.purchases.PurchaseManager
import co.smartreceipts.android.purchases.model.ConsumablePurchase
import co.smartreceipts.android.purchases.model.PurchaseFamily
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