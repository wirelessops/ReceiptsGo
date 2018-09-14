package co.smartreceipts.android.purchases.subscriptions

import co.smartreceipts.android.apis.WebServiceManager
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.purchases.apis.subscriptions.SubscriptionsApiService
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.utils.log.Logger
import javax.inject.Inject

@ApplicationScope
class RemoteSubscriptionManager @Inject constructor(private val purchaseWallet: PurchaseWallet,
                                                    private val webServiceManager: WebServiceManager,
                                                    private val subscriptionApiResponseValidator: SubscriptionApiResponseValidator) {

    fun initialize() {
        webServiceManager.getService(SubscriptionsApiService::class.java)
                .getSubscriptions()
                .map {
                    subscriptionApiResponseValidator.getActiveSubscriptions(it)
                }
                .subscribe ({
                    Logger.info(this, "Successfully fetched {} remote subscriptions from our APIs.", it.size)
                    purchaseWallet.updateRemotePurchases(it)
                },
                {
                    Logger.error(this, "Failed to fetch our remote subscriptions: {}", it.message)
                })
    }


}