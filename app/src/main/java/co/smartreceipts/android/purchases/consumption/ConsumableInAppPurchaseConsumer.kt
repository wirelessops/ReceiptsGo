package co.smartreceipts.android.purchases.consumption

import co.smartreceipts.android.purchases.PurchaseManager
import co.smartreceipts.android.purchases.model.ConsumablePurchase
import co.smartreceipts.android.purchases.model.PurchaseFamily
import com.google.common.base.Preconditions
import io.reactivex.Completable
import javax.inject.Inject

internal class ConsumableInAppPurchaseConsumer @Inject constructor(purchaseManager: PurchaseManager) :
    InAppPurchaseConsumer<ConsumablePurchase?> {
    private val purchaseManager: PurchaseManager

    init {
        this.purchaseManager = Preconditions.checkNotNull(purchaseManager)
    }

    override fun isConsumed(managedProduct: ConsumablePurchase, purchaseFamily: PurchaseFamily) =
        false

    override fun consumePurchase(
        managedProduct: ConsumablePurchase, purchaseFamily: PurchaseFamily
    ): Completable = purchaseManager.consumePurchase(managedProduct)

}