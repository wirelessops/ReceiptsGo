package com.wops.receiptsgo.purchases.subscriptions

import com.wops.receiptsgo.purchases.apis.subscriptions.Subscription
import com.wops.receiptsgo.purchases.apis.subscriptions.SubscriptionsApiResponse
import com.wops.receiptsgo.purchases.model.InAppPurchase
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class SubscriptionApiResponseValidatorTest {

    private val remoteSubscriptionConverter = SubscriptionApiResponseValidator()

    @Test
    fun convertResponseWithNullList() {
        val remoteSubscriptions = remoteSubscriptionConverter.getActiveSubscriptions(SubscriptionsApiResponse(null))
        assertNotNull(remoteSubscriptions)
        assertTrue(remoteSubscriptions.isEmpty())
    }

    @Test
    fun convertResponseWithSubscriptionsList() {
        val validSubscription = Subscription(ID, "Google", "and_autorec_1month", PURCHASE_DATE, EXPIRATION_DATE_LATEST)
        val validSubscriptionOlder = Subscription(ID, "Google", "and_autorec_pro_1month", PURCHASE_DATE, EXPIRATION_DATE_NORMAL)
        val invalidSubscription1 = Subscription(null, null, null, null, null)
        val invalidSubscription2 = Subscription(1, "Google", null, PURCHASE_DATE, EXPIRATION_DATE_NORMAL)
        val invalidSubscription3 = Subscription(1, "Google", "Some random purchase", PURCHASE_DATE, EXPIRATION_DATE_NORMAL)
        val invalidSubscription4 = Subscription(ID, "Google", "and_autorec_1month ", PURCHASE_DATE, EXPIRATION_DATE_OLD)

        val subscriptions = listOf(validSubscription, invalidSubscription1, invalidSubscription2,
            invalidSubscription3, invalidSubscription4, validSubscriptionOlder)
        val remoteSubscriptions = remoteSubscriptionConverter.getActiveSubscriptions(SubscriptionsApiResponse(subscriptions))

        assertNotNull(remoteSubscriptions)
        assertTrue(remoteSubscriptions.size == 1)

        val remoteSubscription = remoteSubscriptions.distinct()[0]
        assertNotNull(remoteSubscription)
        assertEquals(ID, remoteSubscription.id)
        assertEquals(InAppPurchase.StandardSubscriptionPlan, remoteSubscription.inAppPurchase)
        assertEquals(EXPIRATION_DATE_LATEST, remoteSubscription.expirationDate)
    }

    companion object {
        private const val ID: Long = 20
        private val PURCHASE_DATE = Date(1500000000000L)
        private val EXPIRATION_DATE_NORMAL = Date(System.currentTimeMillis()  + 7200000L) // current time + 2 hours
        private val EXPIRATION_DATE_LATEST = Date(System.currentTimeMillis()  + 14400000L) // current time + 4 hours
        private val EXPIRATION_DATE_OLD = Date(System.currentTimeMillis()  - 7200000L) // current time - 2 hours
    }
}