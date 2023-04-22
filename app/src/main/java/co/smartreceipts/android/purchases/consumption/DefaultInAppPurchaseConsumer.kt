package co.smartreceipts.android.purchases.consumption

import co.smartreceipts.android.purchases.model.ConsumablePurchase
import co.smartreceipts.android.purchases.model.ManagedProduct
import co.smartreceipts.android.purchases.model.PurchaseFamily
import co.smartreceipts.android.purchases.model.Subscription
import co.smartreceipts.core.di.scopes.ApplicationScope
import io.reactivex.Completable
import javax.inject.Inject

@ApplicationScope
class DefaultInAppPurchaseConsumer @Inject constructor(
    private val consumableInAppPurchaseConsumer: ConsumableInAppPurchaseConsumer,
    private val subscriptionInAppPurchaseConsumer: SubscriptionInAppPurchaseConsumer
) : InAppPurchaseConsumer<ManagedProduct> {

    override fun isConsumed(
        managedProduct: ManagedProduct, purchaseFamily: PurchaseFamily
    ): Boolean {

        return when (managedProduct) {
            is ConsumablePurchase -> consumableInAppPurchaseConsumer.isConsumed(
                managedProduct,
                purchaseFamily
            )
            is Subscription -> subscriptionInAppPurchaseConsumer.isConsumed(
                managedProduct,
                purchaseFamily
            )
            else -> throw IllegalArgumentException("Unsupported managed product type: $managedProduct")
        }
    }

    override fun consumePurchase(
        managedProduct: ManagedProduct, purchaseFamily: PurchaseFamily
    ): Completable {
        return when (managedProduct) {
            is ConsumablePurchase -> consumableInAppPurchaseConsumer.consumePurchase(
                managedProduct,
                purchaseFamily
            )
            is Subscription -> subscriptionInAppPurchaseConsumer.consumePurchase(
                managedProduct,
                purchaseFamily
            )
            else -> throw IllegalArgumentException("Unsupported managed product type: $managedProduct")
        }
    }
}