package co.smartreceipts.android.purchases.subscriptions

import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.apis.WebServiceManager
import co.smartreceipts.android.purchases.apis.subscriptions.SubscriptionsApiService
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.core.identity.IdentityManager
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
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
     * Fetches all remote subscriptions that are associated with this account.
     *
     * @return an [Observable], which will emit a [Set] of [RemoteSubscription] instances that are new
     * to this account.
     */
    fun getRemoteSubscriptions() : Single<Set<RemoteSubscription>> {
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
            .doOnError {
                Logger.error(this, "Failed to fetch our remote subscriptions: {}", it.message)
            }
            .onErrorReturn { Collections.emptySet() }
            .firstOrError()
    }

    /**
     * Fetches all remote subscriptions that are associated with this account and are not present in the local wallet.
     *
     * @return an [Observable], which will emit a [Set] of [RemoteSubscription] instances that are new
     * to this account.
     */
    fun getNewRemoteSubscriptions() : Observable<Set<RemoteSubscription>> {
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
                    val missedSubscriptions: MutableSet<RemoteSubscription> = mutableSetOf()
                    val resultNewSubscriptions: MutableSet<RemoteSubscription> = mutableSetOf()

                    for (remoteSubscription in it) {
                        if (!purchaseWallet.hasActivePurchase(remoteSubscription.inAppPurchase)) {
                            missedSubscriptions.add(remoteSubscription)
                        }
                    }

                    purchaseWallet.updateRemotePurchases(it)

                    for (missedSubscription in missedSubscriptions) {
                        if (purchaseWallet.hasActivePurchase(missedSubscription.inAppPurchase)) {
                            resultNewSubscriptions.add(missedSubscription)
                        }
                    }

                    Observable.just(resultNewSubscriptions.toSet())
                }
                .doOnError {
                    Logger.error(this, "Failed to fetch our remote subscriptions: {}", it.message)
                }
                .onErrorReturn { Collections.emptySet() }
    }
}