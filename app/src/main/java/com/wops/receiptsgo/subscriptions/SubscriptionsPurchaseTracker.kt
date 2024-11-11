package com.wops.receiptsgo.subscriptions

import androidx.annotation.CheckResult
import co.smartreceipts.analytics.log.Logger
import com.wops.receiptsgo.purchases.PurchaseEventsListener
import com.wops.receiptsgo.purchases.PurchaseManager
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.model.PurchaseFamily
import com.wops.receiptsgo.purchases.model.Subscription
import com.wops.receiptsgo.purchases.source.PurchaseSource
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet
import com.wops.receiptsgo.sync.BackupProvidersManager
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.core.identity.IdentityManager
import co.smartreceipts.core.sync.provider.SyncProvider
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ApplicationScope
class SubscriptionsPurchaseTracker constructor(
    private val purchaseWallet: PurchaseWallet,
    private val purchaseManager: PurchaseManager,
    private val identityManager: IdentityManager,
    private val backupProvidersManager: Lazy<BackupProvidersManager>,
    private val subscriptionUploader: SubscriptionUploader,
    private val subscribeOnScheduler: Scheduler = Schedulers.io()
) : PurchaseEventsListener {

    @Inject
    constructor(
        purchaseWallet: PurchaseWallet,
        purchaseManager: PurchaseManager,
        identityManager: IdentityManager,
        backupProvidersManager: Lazy<BackupProvidersManager>,
        subscriptionUploader: SubscriptionUploader
    ) : this(
        purchaseWallet,
        purchaseManager,
        identityManager,
        backupProvidersManager,
        subscriptionUploader,
        Schedulers.io()
    )


    @CheckResult
    fun initialize(): Completable {
        Logger.debug(this, "SubscriptionsPurchaseTracker init")
        purchaseManager.addEventListener(this)

        return identityManager.isLoggedInStream
//            .subscribeOn(subscribeOnScheduler)
            .filter { it }
            .firstOrError()
            .flatMap { purchaseManager.queryUnacknowledgedSubscriptions() }
            .flatMapCompletable { purchases ->
                var result = Completable.complete()

                val completables: List<Completable> = purchases.map { purchase ->
                    val managedProduct = Subscription(
                        InAppPurchase.from(purchase.skus.first())!!,
                        purchase.originalJson,
                        purchase.purchaseToken,
                        purchase.signature
                    )
                    subscriptionUploader.isSubscriptionUploaded(purchase)
                        .onErrorResumeNext {
                            if (it is NoSuchElementException) {
                                Logger.debug(this, "Unacknowledged subscription is not uploaded to the server")

                                // trying to upload purchase to the server
                                subscriptionUploader.uploadSubscription(managedProduct)
                            } else {
                                Completable.error(it)
                            }
                        }
                        .andThen { purchaseManager.acknowledgePurchase(purchase) }
                }

                completables.forEach {
                    result = result.mergeWith(it.onErrorComplete())
                }
                result
            }
            .subscribeOn(subscribeOnScheduler)
            .observeOn(subscribeOnScheduler)
    }


    override fun onPurchaseSuccess(inAppPurchase: InAppPurchase, purchaseSource: PurchaseSource) {
        if (!inAppPurchase.purchaseFamilies.contains(PurchaseFamily.SubscriptionPlans)) return

        val managedProduct = purchaseWallet.getLocalInAppManagedProduct(inAppPurchase) ?: return

        identityManager.isLoggedInStream
            .subscribeOn(Schedulers.io())
            .filter { it }
            .firstOrError()
            .doOnSuccess {
                // handle backup provider reset for the case of downgrading Premium Plan -> Standard Plan
                if (managedProduct.inAppPurchase == InAppPurchase.StandardSubscriptionPlan
                    && backupProvidersManager.get().syncProvider == SyncProvider.GoogleDrive
                ) {
                    backupProvidersManager.get()
                        .setAndInitializeSyncProvider(SyncProvider.None, null)
                }
            }
            .subscribe()
    }

    override fun onPurchaseFailed(purchaseSource: PurchaseSource) {
        /* no-op */
    }

    override fun onPurchasePending() {
        /* no-op */
    }

}