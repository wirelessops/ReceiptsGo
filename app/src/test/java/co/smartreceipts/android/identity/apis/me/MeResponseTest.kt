package co.smartreceipts.android.identity.apis.me

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
import java.util.*

@RunWith(RobolectricTestRunner::class)
class MeResponseTest {

    companion object {

        private const val JSON_EMPTY = "" +
                "{\n" +
                "}"

        private const val OLD_ME_RESPONSE_JSON = "{\n" +
                "   \"user\":{\n" +
                "      \"id\":\"1234\",\n" +
                "      \"email\":\"email@email.com\",\n" +
                "      \"created_at\":1410400000,\n" +
                "      \"name\":\"Name\",\n" +
                "      \"display_name\":\"Display Name\",\n" +
                "      \"provider\":\"Provider\",\n" +
                "      \"registration_ids\":[\n" +
                "         \"REGISTRATION_ID\"\n" +
                "      ],\n" +
                "      \"confirmed_at\":\"2014-09-22T11:06:33.556Z\",\n" +
                "      \"confirmation_sent_at\":null,\n" +
                "      \"cognito_token\":\"COGNITO_TOKEN\",\n" +
                "      \"cognito_token_expires_at\":1538076327,\n" +
                "      \"identity_id\":\"IDENTITY_ID\",\n" +
                "      \"recognitions_available\":359\n" +
                "   }\n" +
                "}"

        private const val NEW_ME_RESPONSE_JSON = "{\n" +
                "   \"user\":{\n" +
                "      \"id\":\"1234\",\n" +
                "      \"email\":\"email@email.com\",\n" +
                "      \"created_at\":\"2014-09-11T03:00:12.368Z\",\n" +
                "      \"name\":\"Name\",\n" +
                "      \"display_name\":\"Display Name\",\n" +
                "      \"provider\":\"Provider\",\n" +
                "      \"registration_ids\":[\n" +
                "         \"REGISTRATION_ID\"\n" +
                "      ],\n" +
                "      \"confirmed_at\":\"2014-09-22T11:06:33.556Z\",\n" +
                "      \"confirmation_sent_at\":null,\n" +
                "      \"cognito_token\":\"COGNITO_TOKEN\",\n" +
                "      \"cognito_token_expires_at\":\"2015-09-22T11:06:33.556Z\",\n" +
                "      \"identity_id\":\"IDENTITY_ID\",\n" +
                "      \"recognitions_available\":359\n" +
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
        val response = gson.fromJson<MeResponse>(MeResponseTest.JSON_EMPTY, MeResponse::class.java)
        assertNotNull(response)
        assertNull(response.user)
    }

    @Test
    fun deserializeOldResponseFormat() {
        val response = gson.fromJson<MeResponse>(MeResponseTest.OLD_ME_RESPONSE_JSON, MeResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.user)

        val user = response.user!!
        assertEquals("1234", user.id)
        assertEquals("email@email.com", user.email)
        assertEquals("Name", user.name)
        assertEquals("Display Name", user.displayName)
        assertEquals("REGISTRATION_ID", user.registrationIds?.get(0))
        assertEquals("COGNITO_TOKEN", user.cognitoToken)
        assertEquals(Date(1538076327L), user.cognitoTokenExpiresAt)
        assertEquals("IDENTITY_ID", user.identityId)
        assertEquals(359, user.recognitionsAvailable)
    }

    @Test
    fun deserializeNewResponseFormat() {
        val response = gson.fromJson<MeResponse>(MeResponseTest.NEW_ME_RESPONSE_JSON, MeResponse::class.java)
        assertNotNull(response)
        assertNotNull(response.user)

        val user = response.user!!
        assertEquals("1234", user.id)
        assertEquals("email@email.com", user.email)
        assertEquals("Name", user.name)
        assertEquals("Display Name", user.displayName)
        assertEquals("REGISTRATION_ID", user.registrationIds?.get(0))
        assertEquals("COGNITO_TOKEN", user.cognitoToken)
        assertEquals(Iso8601DateFormat().parse("2015-09-22T11:06:33.556Z"), user.cognitoTokenExpiresAt)
        assertEquals("IDENTITY_ID", user.identityId)
        assertEquals(359, user.recognitionsAvailable)
    }
}