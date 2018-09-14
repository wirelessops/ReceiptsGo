package co.smartreceipts.android.purchases.subscriptions

import co.smartreceipts.android.apis.hosts.ServiceManager
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.purchases.apis.subscriptions.SubscriptionsApiService
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import javax.inject.Inject

@ApplicationScope
class RemoteSubscriptionManager @Inject constructor(private val purchaseWallet: PurchaseWallet,
                                                    private val serviceManager: ServiceManager,
                                                    private val subscriptionApiResponseValidator: SubscriptionApiResponseValidator) {

    fun initialize() {
        serviceManager.getService(SubscriptionsApiService::class.java)
                .getSubscriptions()
                .map {
                    subscriptionApiResponseValidator.getActiveSubscriptions(it)
                }
                .subscribe {
                    purchaseWallet.updateRemotePurchases(it)
                }
    }


}