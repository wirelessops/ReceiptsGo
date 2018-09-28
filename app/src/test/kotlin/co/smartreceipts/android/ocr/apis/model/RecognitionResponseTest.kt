package co.smartreceipts.android.ocr.apis.model

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder
import co.smartreceipts.android.date.Iso8601DateFormat
import co.smartreceipts.android.identity.apis.me.MeResponse
import co.smartreceipts.android.identity.apis.me.MeResponseTest
import co.smartreceipts.android.model.ColumnDefinitions
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.utils.TestUtils
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecognitionResponseTest {

    companion object {

        private const val JSON_EMPTY = "" +
                "{\n" +
                "}"

        private const val OLD_RECOGNITION_RESPONSE_WITHOUT_DATA_JSON = "{\n" +
                "   \"recognition\":{\n" +
                "      \"id\":\"123456\",\n" +
                "      \"status\":\"Pending\",\n" +
                "      \"s3_path\":\"ocr/id.jpg\",\n" +
                "      \"data\":null,\n" +
                "      \"created_at\":1538152873265\n" +
                "   }\n" +
                "}"

        private const val OLD_RECOGNITION_RESPONSE_WITH_DATA_JSON = "{\n" +
                "   \"recognition\":{\n" +
                "      \"id\":\"123456\",\n" +
                "      \"status\":\"Complete\",\n" +
                "      \"s3_path\":\"ocr/id.jpg\",\n" +
                "      \"data\":{\n" +
                "         \"recognition_data\":{\n" +
                "            \"totalAmount\":{\n" +
                "               \"data\":19.5,\n" +
                "               \"confidenceLevel\":0.9\n" +
                "            },\n" +
                "            \"taxAmount\":{\n" +
                "               \"data\":1.15,\n" +
                "               \"confidenceLevel\":0.3\n" +
                "            },\n" +
                "            \"confidenceLevel\":0.465,\n" +
                "            \"date\":{\n" +
                "               \"data\":\"2018-08-28T00:00:00.000Z\",\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantName\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantAddress\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantCity\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantState\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantCountryCode\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantTypes\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            }\n" +
                "         },\n" +
                "         \"amount\":19.5,\n" +
                "         \"tax\":1.15\n" +
                "      },\n" +
                "      \"created_at\":1538152873265\n" +
                "   }\n" +
                "}"

        private const val NEW_RECOGNITION_RESPONSE_WITHOUT_DATA_JSON = "{\n" +
                "   \"recognition\":{\n" +
                "      \"id\":\"123456\",\n" +
                "      \"status\":\"Pending\",\n" +
                "      \"s3_path\":\"ocr/id.jpg\",\n" +
                "      \"data\":null,\n" +
                "      \"created_at\":\"2018-09-28T16:41:13.265Z\"\n" +
                "   }\n" +
                "}"

        private const val NEW_RECOGNITION_RESPONSE_WITH_DATA_JSON = "{\n" +
                "   \"recognition\":{\n" +
                "      \"id\":\"123456\",\n" +
                "      \"status\":\"Complete\",\n" +
                "      \"s3_path\":\"ocr/id.jpg\",\n" +
                "      \"data\":{\n" +
                "         \"recognition_data\":{\n" +
                "            \"totalAmount\":{\n" +
                "               \"data\":19.5,\n" +
                "               \"confidenceLevel\":0.9\n" +
                "            },\n" +
                "            \"taxAmount\":{\n" +
                "               \"data\":1.15,\n" +
                "               \"confidenceLevel\":0.3\n" +
                "            },\n" +
                "            \"confidenceLevel\":0.465,\n" +
                "            \"date\":{\n" +
                "               \"data\":\"2018-08-28T00:00:00.000Z\",\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantName\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantAddress\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantCity\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantState\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantCountryCode\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            },\n" +
                "            \"merchantTypes\":{\n" +
                "               \"confidenceLevel\":0\n" +
                "            }\n" +
                "         },\n" +
                "         \"amount\":19.5,\n" +
                "         \"tax\":1.15\n" +
                "      },\n" +
                "      \"created_at\":\"2018-09-28T16:41:13.265Z\"\n" +
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
        val response = gson.fromJson<RecognitionResponse>(RecognitionResponseTest.JSON_EMPTY, RecognitionResponse::class.java)
        assertNotNull(response)
        assertNull(response.recognition)
    }

    @Test
    fun deserializeOldRecognitionResponseWithoutData() {
        val response = gson.fromJson<RecognitionResponse>(RecognitionResponseTest.OLD_RECOGNITION_RESPONSE_WITHOUT_DATA_JSON, RecognitionResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.recognition)

        val recognition = response.recognition!!
        assertEquals("123456", recognition.id)
        assertEquals("Pending", recognition.status)
        assertEquals("ocr/id.jpg", recognition.s3Path)
        assertEquals(Iso8601DateFormat().parse("2018-09-28T16:41:13.265Z"), recognition.createdAt)
        assertNull(recognition.data)
    }

    @Test
    fun deserializeNewRecognitionResponseWithoutData() {
        val response = gson.fromJson<RecognitionResponse>(RecognitionResponseTest.NEW_RECOGNITION_RESPONSE_WITHOUT_DATA_JSON, RecognitionResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.recognition)

        val recognition = response.recognition!!
        assertEquals("123456", recognition.id)
        assertEquals("Pending", recognition.status)
        assertEquals("ocr/id.jpg", recognition.s3Path)
        assertEquals(Iso8601DateFormat().parse("2018-09-28T16:41:13.265Z"), recognition.createdAt)
        assertNull(recognition.data)
    }

    @Test
    fun deserializeOldRecognitionResponseWithData() {
        val response = gson.fromJson<RecognitionResponse>(RecognitionResponseTest.OLD_RECOGNITION_RESPONSE_WITH_DATA_JSON, RecognitionResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.recognition)

        val recognition = response.recognition!!
        assertEquals("123456", recognition.id)
        assertEquals("Complete", recognition.status)
        assertEquals("ocr/id.jpg", recognition.s3Path)
        assertEquals(Iso8601DateFormat().parse("2018-09-28T16:41:13.265Z"), recognition.createdAt)
        assertNotNull(recognition.data?.recognitionData)

        val data = recognition.data!!.recognitionData!!
        assertEquals(19.5, data.totalAmount!!.data!!, TestUtils.DOUBLE_EPSILON)
        assertEquals(1.15, data.taxAmount!!.data!!, TestUtils.DOUBLE_EPSILON)
        assertEquals("2018-08-28T00:00:00.000Z", data.date!!.data!!)
    }

    @Test
    fun deserializeNewRecognitionResponseWithData() {
        val response = gson.fromJson<RecognitionResponse>(RecognitionResponseTest.NEW_RECOGNITION_RESPONSE_WITH_DATA_JSON, RecognitionResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.recognition)

        val recognition = response.recognition!!
        assertEquals("123456", recognition.id)
        assertEquals("Complete", recognition.status)
        assertEquals("ocr/id.jpg", recognition.s3Path)
        assertEquals(Iso8601DateFormat().parse("2018-09-28T16:41:13.265Z"), recognition.createdAt)
        assertNotNull(recognition.data?.recognitionData)

        val data = recognition.data!!.recognitionData!!
        assertEquals(19.5, data.totalAmount!!.data!!, TestUtils.DOUBLE_EPSILON)
        assertEquals(1.15, data.taxAmount!!.data!!, TestUtils.DOUBLE_EPSILON)
        assertEquals("2018-08-28T00:00:00.000Z", data.date!!.data!!)
    }

}