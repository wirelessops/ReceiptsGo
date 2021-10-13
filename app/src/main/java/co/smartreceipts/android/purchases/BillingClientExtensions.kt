package co.smartreceipts.android.purchases

import co.smartreceipts.analytics.log.Logger
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
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