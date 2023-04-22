package co.smartreceipts.android.purchases.apis.purchases

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder
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

        private const val RESPONSE_JSON ="{\n" +
                "  \"purchase\": {\n" +
                "    \"id\": \"1234\",\n" +
                "    \"user_id\": \"6780527139\",\n" +
                "    \"pay_service\": \"Google Play\",\n" +
                "    \"purchase_id\": \"12999763169054705758.1371079406387615&rojeslcdyyiapnqcynkjyyjh\",\n" +
                "    \"product_id\": \"ocr_purchase_10\",\n" +
                "    \"package_name\": \"wb.receiptspro\",\n" +
                "    \"purchase_time\": \"2012-08-22T23:41:40.000Z\",\n" +
                "    \"created_at\": \"2022-02-05T08:11:39.114Z\",\n" +
                "    \"updated_at\": \"2022-02-05T08:11:39.114Z\"\n" +
                "  },\n" +
                "  \"status\": \"valid\"\n" +
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
        val response = gson.fromJson<PurchaseResponse>(
            JSON_EMPTY,
            PurchaseResponse::class.java
        )
        assertNotNull(response)
        assertNull(response.purchase)
    }

    @Test
    fun deserializeResponseFormat() {
        val response = gson.fromJson<PurchaseResponse>(
            RESPONSE_JSON,
            PurchaseResponse::class.java
        )
        assertNotNull(response)
        assertNotNull(response.purchase)

        val purchase = response.purchase!!
        assertEquals("1234", purchase.id)
        assertEquals("valid", response.status)
    }
}