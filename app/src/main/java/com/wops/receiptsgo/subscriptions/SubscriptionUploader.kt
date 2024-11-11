package com.wops.receiptsgo.subscriptions

import co.smartreceipts.analytics.log.Logger
import com.wops.receiptsgo.apis.SmartReceiptsApiException
import com.wops.receiptsgo.apis.WebServiceManager
import com.wops.receiptsgo.purchases.apis.purchases.MobileAppPurchasesService
import com.wops.receiptsgo.purchases.apis.purchases.PurchaseRequest
import com.wops.receiptsgo.purchases.apis.purchases.PurchaseResponse
import com.wops.receiptsgo.purchases.model.ManagedProduct
import com.wops.receiptsgo.purchases.subscriptions.RemoteSubscription
import com.wops.receiptsgo.purchases.subscriptions.RemoteSubscriptionManager
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.android.billingclient.api.Purchase
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

@ApplicationScope
class SubscriptionUploader @Inject constructor(
    private val webServiceManager: WebServiceManager,
    private val remoteSubscriptionManager: RemoteSubscriptionManager
) {

    fun uploadSubscription(managedProduct: ManagedProduct): Completable {
        Logger.info(this, "Uploading subscription purchase: ${managedProduct.inAppPurchase}")
        return webServiceManager.getService(MobileAppPurchasesService::class.java)
            .addPurchase(PurchaseRequest(managedProduct))
            .flatMapCompletable { purchaseResponse: PurchaseResponse? ->
                Logger.debug(this, "Received purchase response of $purchaseResponse")

                if (purchaseResponse?.status == "valid") {
                    return@flatMapCompletable Completable.complete()
                } else {
                    val errors = purchaseResponse?.errors ?: emptyList()
                    return@flatMapCompletable Completable.error(Exception("Got error while tried to upload purchase ${managedProduct.inAppPurchase.sku}: $errors"))
                }

            }
            .onErrorResumeNext { t: Throwable ->
                Logger.debug(
                    this,
                    "Got error while tried to upload purchase ${managedProduct.inAppPurchase.sku}: ${t.localizedMessage}"
                )

                if (t is SmartReceiptsApiException) {
                    val errors = t.errorResponse?.errors ?: emptyList()
                    if (t.response.code() == 422 && errors.contains("Purchase has already been taken")) {
                        return@onErrorResumeNext Completable.complete()
                    }
                }
                Completable.error(t)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Checks if purchase is uploaded to the server
     * Note: currently on the sever side subscription state automatically refreshes each 3 days
     */
    fun isSubscriptionUploaded(purchase: Purchase): Completable {
        val sku = purchase.skus.first()
        return remoteSubscriptionManager.getRemoteSubscriptions()
            .flatMapCompletable { subs: Set<RemoteSubscription> ->
                subs.forEach { subscription ->
                    if (sku == subscription.inAppPurchase.sku && subscription.expirationDate >= Date()) {
                        Logger.debug(this, "Found uploaded subscription with the same sku")
                        return@flatMapCompletable Completable.complete()
                    }
                }
                Logger.debug(this, "Didn't find uploaded subscription with the same sku")
                return@flatMapCompletable Completable.error(NoSuchElementException())
            }
    }

}