package co.smartreceipts.android.purchases.subscriptions

import co.smartreceipts.android.purchases.apis.subscriptions.SubscriptionsApiResponse
import co.smartreceipts.android.purchases.model.PurchaseFamily

/**
 * Assists us with converting [SubscriptionsApiResponse], which is returned from our APIs, to a
 * [List] of [RemoteSubscription], each of which is assumed to be active.
 *
 * Note as per the API specification, we are only provided active subscriptions when making our
 * initial request. As a result (so long as we're not reading from the cache), all subscriptions
 * are assumed to be active.
 */
class SubscriptionApiResponseValidator {

    /**
     * Converts a [SubscriptionsApiResponse], which is returned from our APIs, to a [List] of
     * [RemoteSubscription], each of which is assumed to be active.
     *
     * @param subscriptionsApiResponse the [SubscriptionsApiResponse] from our API
     * @return a [List] of [RemoteSubscription]
     */
    fun convert(subscriptionsApiResponse: SubscriptionsApiResponse) : List<RemoteSubscription> {
        val remoteSubscriptions = mutableListOf<RemoteSubscription>()
        subscriptionsApiResponse.subscriptions?.let { subscriptions ->
            subscriptions.forEach {
                val purchaseFamily = getPurchaseFamily(it.product_name)
                if (purchaseFamily != null && it.id != null && it.expires_at != null) {
                    // Note: Our current API spec assumes that only active subscriptions are returned
                    remoteSubscriptions.add(RemoteSubscription(it.id, purchaseFamily, it.expires_at))
                }
            }
        }
        return remoteSubscriptions
    }

    private fun getPurchaseFamily(productName: String?) : PurchaseFamily? {
        return if (SMART_RECEIPTS_PLUS.equals(productName, true)) {
            PurchaseFamily.SmartReceiptsPlus
        } else {
            null
        }
    }

    companion object {
        private const val SMART_RECEIPTS_PLUS = "Smart Receipts Plus"
    }
}