package co.smartreceipts.android.purchases.subscriptions

import co.smartreceipts.android.purchases.apis.subscriptions.Subscription
import org.junit.Assert.*

import co.smartreceipts.android.purchases.apis.subscriptions.SubscriptionsApiResponse
import co.smartreceipts.android.purchases.model.PurchaseFamily
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class SubscriptionApiResponseValidatorTest {

    private val remoteSubscriptionConverter = SubscriptionApiResponseValidator()

    @Test
    fun convertResponseWithNullList() {
        val remoteSubscriptions = remoteSubscriptionConverter.convert(SubscriptionsApiResponse(null))
        assertNotNull(remoteSubscriptions)
        assertTrue(remoteSubscriptions.isEmpty())
    }

    @Test
    fun convertResponseWithSubscriptionsList() {
        val validSubscription = Subscription(ID, "Google", "Smart Receipts Plus", PURCHASE_DATE, EXPIRATION_DATE)
        val invalidSubscription1 = Subscription(null, null, null, null, null)
        val invalidSubscription2 = Subscription(1, "Google", null, PURCHASE_DATE, EXPIRATION_DATE)
        val invalidSubscription3 = Subscription(1, "Google", "Some random purchase", PURCHASE_DATE, EXPIRATION_DATE)
        val subscriptions = listOf(validSubscription, invalidSubscription1, invalidSubscription2, invalidSubscription3)
        val remoteSubscriptions = remoteSubscriptionConverter.convert(SubscriptionsApiResponse(subscriptions))
        assertNotNull(remoteSubscriptions)
        assertTrue(remoteSubscriptions.size == 1)

        val remoteSubscription = remoteSubscriptions[0]
        assertNotNull(remoteSubscription)
        assertEquals(ID, remoteSubscription.id)
        assertEquals(PurchaseFamily.SmartReceiptsPlus, remoteSubscription.purchaseFamily)
        assertEquals(EXPIRATION_DATE, remoteSubscription.expirationDate)
    }

    companion object {
        private const val ID = 20
        private val PURCHASE_DATE = Date(1500000000000L)
        private val EXPIRATION_DATE = Date(1534975988000L)
    }
}