package co.smartreceipts.android.purchases.apis.purchases

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder
import co.smartreceipts.android.identity.apis.me.MeResponse
import co.smartreceipts.android.identity.apis.me.MeResponseTest
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
class PurchaseResponseTest {

    companion object {
        private const val JSON_EMPTY = "" +
                "{\n" +
                "}"

        private const val OLD_PURCHASE_RESPONSE_JSON = "{\n" +
                "   \"mobile_app_purchase\":{\n" +
                "      \"id\":\"1234\",\n" +
                "      \"user_id\":\"5678\",\n" +
                "      \"pay_service\":\"Google Play\",\n" +
                "      \"purchase_id\":\"GPA.12345\",\n" +
                "      \"purchase_time\":1538076456,\n" +
                "      \"status\":\"Purchased\",\n" +
                "      \"created_at\":1538076460\n" +
                "   }\n" +
                "}"

        private const val NEW_PURCHASE_RESPONSE_JSON = "{\n" +
                "   \"mobile_app_purchase\":{\n" +
                "      \"id\":\"1234\",\n" +
                "      \"user_id\":\"5678\",\n" +
                "      \"pay_service\":\"Google Play\",\n" +
                "      \"purchase_id\":\"GPA.12345\",\n" +
                "      \"purchase_time_iso8601\":\"2018-09-27T19:54:38.000Z\",\n" +
                "      \"status\":\"Purchased\",\n" +
                "      \"created_at_iso8601\":\"2018-09-27T19:54:42.000Z\"\n" +
                "   }\n" +
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
    fun deserializeEmptyResponse() {
        val response = gson.fromJson<PurchaseResponse>(PurchaseResponseTest.JSON_EMPTY, PurchaseResponse::class.java)
        assertNotNull(response)
        assertNull(response.mobile_app_purchase)
    }

    @Test
    fun deserializeOldResponseFormat() {
        val response = gson.fromJson<PurchaseResponse>(PurchaseResponseTest.OLD_PURCHASE_RESPONSE_JSON, PurchaseResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.mobile_app_purchase)

        val purchase = response.mobile_app_purchase!!
        assertEquals("1234", purchase.id)
    }

    @Test
    fun deserializeNewResponseFormat() {
        val response = gson.fromJson<PurchaseResponse>(PurchaseResponseTest.NEW_PURCHASE_RESPONSE_JSON, PurchaseResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.mobile_app_purchase)

        val purchase = response.mobile_app_purchase!!
        assertEquals("1234", purchase.id)
    }
}