package com.wops.receiptsgo.purchases

import co.smartreceipts.analytics.log.Logger
import com.android.billingclient.api.*
import com.android.billingclient.api.QueryProductDetailsParams.Product
import io.reactivex.Completable
import io.reactivex.Single

fun BillingClient.queryPurchasesAsSingle(
    @BillingClient.ProductType
    skuType: String
): Single<List<Purchase>> {

    return Single.create { emitter ->
        queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(skuType).build()
        ) { billingResult, purchases ->
            when (val responseCode = billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> emitter.onSuccess(purchases)
                else -> emitter.onError(
                    BillingClientException(responseCode, billingResult.debugMessage)
                )
            }
        }
    }
}

fun BillingClient.querySkuDetailsAsSingle(
    @BillingClient.ProductType
    skuType: String,
    skus: List<String>
): Single<Set<ProductDetails>> {

    return Single.create { emitter ->
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(skus.map { skuId ->
                Product.newBuilder().setProductType(skuType).setProductId(skuId).build()
            })
            .build()

        queryProductDetailsAsync(params) { billingResult, skuDetailsList ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage

            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Logger.info(
                        this,
                        "onSkuDetailsResponse: $responseCode $debugMessage"
                    )

                    val set = skuDetailsList.toSet()
                    emitter.onSuccess(set)
                }

                else -> {
                    Logger.error(this, "onSkuDetailsResponse: $responseCode $debugMessage")
                    emitter.onError(
                        BillingClientException(responseCode, billingResult.debugMessage)
                    )
                }
            }
        }
    }
}

fun BillingClient.consumePurchase(purchaseToken: String): Completable {
    val consumeParams = ConsumeParams
        .newBuilder()
        .setPurchaseToken(purchaseToken)
        .build()

    return Completable.create { emitter ->
        consumeAsync(consumeParams) { billingResult, token ->
            when (val responseCode = billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> emitter.onComplete()
                else -> emitter.onError(BillingClientException(responseCode, billingResult.debugMessage))
            }
        }
    }
}

val ProductDetails.subscriptionFormattedPrice: String?
    get() {
        return this.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
    }