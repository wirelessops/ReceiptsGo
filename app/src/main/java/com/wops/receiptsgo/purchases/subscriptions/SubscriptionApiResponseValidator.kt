package com.wops.receiptsgo.purchases.subscriptions

import com.wops.receiptsgo.purchases.apis.subscriptions.SubscriptionsApiResponse
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.model.PurchaseFamily
import com.wops.core.di.scopes.ApplicationScope
import java.util.*
import javax.inject.Inject

/**
 * Assists us with converting [SubscriptionsApiResponse], which is returned from our APIs, to a
 * [List] of [RemoteSubscription], each of which is assumed to be active.
 *
 * Note as per the API specification, we are only provided active subscriptions when making our
 * initial request. As a result (so long as we're not reading from the cache), all subscriptions
 * are assumed to be active.
 */
@ApplicationScope
class SubscriptionApiResponseValidator @Inject constructor() {

    /**
     * Converts a [SubscriptionsApiResponse], which is returned from our APIs, to a [List] of
     * [RemoteSubscription], each of which is assumed to be active.
     *
     * @param subscriptionsApiResponse the [SubscriptionsApiResponse] from our API
     * @return a [List] of [RemoteSubscription]
     */
    fun getActiveSubscriptions(subscriptionsApiResponse: SubscriptionsApiResponse): Set<RemoteSubscription> {
        val remoteSubscriptions = mutableSetOf<RemoteSubscription>()

        subscriptionsApiResponse.subscriptions?.let { subscriptions ->
            val currentDate = Date()
            // to ensure that we have single active plan subscription
            var latestPlanSubs: RemoteSubscription? = null

            // Note: Our current API returns subscriptions that are active or was active last 3 days
            subscriptions.forEach {
                val purchase = InAppPurchase.from(it.product_name)

                if (purchase != null && it.id != null && it.expires_at_iso8601 != null && it.expires_at_iso8601 > currentDate) {
                    if (!purchase.purchaseFamilies.contains(PurchaseFamily.SubscriptionPlans)) {
                        remoteSubscriptions.add(
                            RemoteSubscription(it.id, purchase, it.expires_at_iso8601)
                        )
                    } else {
                        if (latestPlanSubs == null || it.expires_at_iso8601 > latestPlanSubs!!.expirationDate) {
                            latestPlanSubs = RemoteSubscription(it.id, purchase, it.expires_at_iso8601)
                        }
                    }
                }
            }

            latestPlanSubs?.let { remoteSubscriptions.add(it) }
        }
        return remoteSubscriptions
    }
}