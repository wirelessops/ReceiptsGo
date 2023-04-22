package co.smartreceipts.android.purchases.consumption

import co.smartreceipts.android.purchases.model.ManagedProduct
import co.smartreceipts.android.purchases.model.PurchaseFamily
import io.reactivex.Completable

interface InAppPurchaseConsumer<T : ManagedProduct> {
    /**
     * Checks if a particular [T] was consumed for a specific [PurchaseFamily]
     *
     * @param managedProduct the [T] to check
     * @param purchaseFamily the [PurchaseFamily] that we're checking for
     *
     * @return `true` if it is consumed, or `false` if not
     */
    fun isConsumed(managedProduct: T, purchaseFamily: PurchaseFamily): Boolean

    /**
     * Consumes a purchase for a particular [T] was consumed for a specific
     * [PurchaseFamily]
     *
     * @param managedProduct the [T] to check
     * @param purchaseFamily the [PurchaseFamily] that we're checking for
     *
     * @return a [Completable] that will complete if this consumption is handled properly or
     * an error if not
     */
    fun consumePurchase(managedProduct: T, purchaseFamily: PurchaseFamily): Completable
}