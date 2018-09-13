package co.smartreceipts.android.purchases.apis.subscriptions

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder
import co.smartreceipts.android.date.Iso8601DateFormat
import co.smartreceipts.android.model.ColumnDefinitions
import co.smartreceipts.android.model.Receipt
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SubscriptionsApiResponseTest {

    companion object {
        private const val JSON_WITH_SUBSCRIPTIONS = "" +
                "{\n" +
                "  \"subscriptions\": [\n" +
                "    {\n" +
                "      \"id\": 30,\n" +
                "      \"subscription_provider\": \"Stripe\",\n" +
                "      \"product_name\": \"Smart Receipts Plus\",\n" +
                "      \"purchased_at\": \"2018-08-22T22:13:08.000Z\",\n" +
                "      \"expires_at\": \"2018-09-22T22:13:08.000Z\"\n" +
                "    }\n" +
                "  ]\n" +
                "}"

        private const val JSON_WITHOUT_SUBSCRIPTIONS = "" +
                "{\n" +
                "  \"subscriptions\": [\n" +
                "  ]\n" +
                "}"

        private const val JSON_EMPTY = "" +
                "{\n" +
                "}"
    }

    @Mock
    lateinit var columnDefinitions: ColumnDefinitions<Receipt>

    lateinit var gson: Gson

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        gson = SmartReceiptsGsonBuilder(columnDefinitions).create()
    }

    @Test
    fun validateDeSerializationWithSubscriptions() {
        val response = gson.fromJson<SubscriptionsApiResponse>(JSON_WITH_SUBSCRIPTIONS, SubscriptionsApiResponse::class.java)
        assertNotNull(response)

        assertNotNull(response.subscriptions)
        val subscriptions = response.subscriptions!!
        assertTrue(subscriptions.size == 1)

        val subscription = subscriptions[0]
        assertNotNull(subscription)
        assertEquals(30, subscription.id)
        assertEquals("Stripe", subscription.subscription_provider)
        assertEquals("Smart Receipts Plus", subscription.product_name)

        val iso8601DateFormat = Iso8601DateFormat()
        assertEquals("2018-08-22T22:13:08.000Z", iso8601DateFormat.format(subscription.purchased_at))
        assertEquals("2018-09-22T22:13:08.000Z", iso8601DateFormat.format(subscription.expires_at))
    }

    @Test
    fun validateDeSerializationWithoutSubscriptions() {
        val response = gson.fromJson<SubscriptionsApiResponse>(JSON_WITHOUT_SUBSCRIPTIONS, SubscriptionsApiResponse::class.java)
        assertNotNull(response)

        assertNotNull(response.subscriptions)
        val subscriptions = response.subscriptions!!
        assertTrue(subscriptions.isEmpty())
    }

    @Test
    fun validateDeSerializationForEmptyJson() {
        val response = gson.fromJson<SubscriptionsApiResponse>(JSON_EMPTY, SubscriptionsApiResponse::class.java)
        assertNotNull(response)
        assertNull(response.subscriptions)
    }
}