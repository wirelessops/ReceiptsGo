package co.smartreceipts.android.purchases

import co.smartreceipts.analytics.log.Logger
import com.android.billingclient.api.*
import io.reactivex.Completable
import io.reactivex.Single

fun BillingClient.queryPurchasesAsSingle(
    @BillingClient.SkuType
    skuType: String
): Single<List<Purchase>> {

    return Single.create { emitter ->
        queryPurchasesAsync(skuType) { billingResult, purchases ->
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
    @BillingClient.SkuType
    skuType: String,
    skus: List<String>
): Single<Set<SkuDetails>> {

    return Single.create<Set<SkuDetails>> { emitter ->
        val params = SkuDetailsParams.newBuilder()
            .setType(skuType)
            .setSkusList(skus)
            .build()

        querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage

            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    Logger.info(
                        this,
                        "onSkuDetailsResponse: $responseCode $debugMessage"
                    )

                    val set = skuDetailsList?.toSet().orEmpty()
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