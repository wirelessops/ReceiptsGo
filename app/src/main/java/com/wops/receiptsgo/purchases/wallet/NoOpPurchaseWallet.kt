package com.wops.receiptsgo.purchases.wallet


import com.wops.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.model.ManagedProduct
import com.wops.receiptsgo.purchases.subscriptions.RemoteSubscription
import javax.inject.Inject

@ApplicationScope
class NoOpPurchaseWallet @Inject constructor() : PurchaseWallet {

    override fun hasActivePurchase(inAppPurchase: InAppPurchase): Boolean {
       return false
    }

    override fun getActiveLocalInAppPurchases(): Set<ManagedProduct?> {
        return emptySet()
    }

    override fun getLocalInAppManagedProduct(inAppPurchase: InAppPurchase): ManagedProduct? {
        return null
    }

    override fun addLocalInAppPurchaseToWallet(managedProduct: ManagedProduct) {
        TODO("Not yet implemented")
    }

    override fun updateLocalInAppPurchasesInWallet(managedProducts: Set<ManagedProduct?>) {
        TODO("Not yet implemented")
    }

    override fun updateRemotePurchases(remoteSubscriptions: Set<RemoteSubscription?>) {
        TODO("Not yet implemented")
    }
}

