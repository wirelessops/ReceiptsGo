package co.smartreceipts.android.purchases.subscriptions

import co.smartreceipts.android.apis.WebServiceManager
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.identity.IdentityManager
import co.smartreceipts.android.purchases.apis.subscriptions.SubscriptionsApiService
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.utils.log.Logger
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

@ApplicationScope
class RemoteSubscriptionManager constructor(private val purchaseWallet: PurchaseWallet,
                                            private val webServiceManager: WebServiceManager,
                                            private val identityManager: IdentityManager,
                                            private val subscriptionApiResponseValidator: SubscriptionApiResponseValidator,
                                            private val subscribeOnScheduler: Scheduler) {

    @Inject
    constructor(purchaseWallet: PurchaseWallet,
                webServiceManager: WebServiceManager,
                identityManager: IdentityManager,
                subscriptionApiResponseValidator: SubscriptionApiResponseValidator) :
            this(purchaseWallet, webServiceManager, identityManager, subscriptionApiResponseValidator, Schedulers.io())

    /**
     * Fetches all new remote subscriptions that are associated with this account.
     *
     * @return an [Observable], which will emit a [Set] of [InAppPurchase] instances that are new
     * to this account.
     */
    fun getNewRemotePurchases() : Observable<Set<InAppPurchase>> {
        return identityManager.isLoggedInStream
                .subscribeOn(subscribeOnScheduler)
                .flatMap { isSignedIn ->
                    if (isSignedIn) {
                        webServiceManager.getService(SubscriptionsApiService::class.java)
                                .getSubscriptions()
                                .map {
                                    subscriptionApiResponseValidator.getActiveSubscriptions(it)
                                }
                    } else {
                        Observable.empty()
                    }
                }
                .doOnNext {
                    Logger.info(this, "Successfully fetched {} remote subscriptions from our APIs.", it.size)
                }
                .flatMap {
                    // Note: This was super lazily done.
                    // If we ever add support for multiple subscription types, we should make this cleaner
                    val havePlusSubscriptionBefore = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)
                    purchaseWallet.updateRemotePurchases(it)
                    val hasPlusSubscriptionAfter = purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)
                    if (!havePlusSubscriptionBefore && hasPlusSubscriptionAfter) {
                        Observable.just(Collections.singleton(InAppPurchase.SmartReceiptsPlus))
                    } else {
                        Observable.just(Collections.emptySet())
                    }
                }
                .doOnError {
                    Logger.error(this, "Failed to fetch our remote subscriptions: {}", it.message)
                }
                .onErrorReturn { Collections.emptySet() }
    }


}