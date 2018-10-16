package co.smartreceipts.android.identity.apis.organizations

import co.smartreceipts.android.apis.moshi.SmartReceiptsMoshiBuilder
import co.smartreceipts.android.model.factory.CategoryBuilderFactory
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory
import com.squareup.moshi.Moshi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class OrganizationsResponseTest {

    companion object {
        private const val JSON_EMPTY = "" +
                "{\n" +
                "}"

        private const val JSON = "{\n" +
                "    \"organizations\": [\n" +
                "        {\n" +
                "            \"id\": \"2587917096\",\n" +
                "            \"name\": \"Test organization\",\n" +
                "            \"created_at_iso8601\": \"2018-08-25T12:48:47.628Z\",\n" +
                "            \"app_settings\": {\n" +
                "                \"Configurations\": {\n" +
                "                    \"IsSettingsEnable\": null\n" +
                "                },\n" +
                "                \"Settings\": {\n" +
                "                    \"TripDuration\": null,\n" +
                "                    \"isocurr\": \"AED\",\n" +
                "                    \"dateseparator\": \"-\",\n" +
                "                    \"trackcostcenter\": null,\n" +
                "                    \"PredictCats\": null,\n" +
                "                    \"MatchNameCats\": null,\n" +
                "                    \"MatchCommentCats\": null,\n" +
                "                    \"OnlyIncludeExpensable\": null,\n" +
                "                    \"ExpensableDefault\": null,\n" +
                "                    \"IncludeTaxField\": null,\n" +
                "                    \"TaxPercentage\": null,\n" +
                "                    \"PreTax\": null,\n" +
                "                    \"EnableAutoCompleteSuggestions\": null,\n" +
                "                    \"MinReceiptPrice\": null,\n" +
                "                    \"DefaultToFirstReportDate\": null,\n" +
                "                    \"ShowReceiptID\": null,\n" +
                "                    \"UseFullPage\": null,\n" +
                "                    \"UsePaymentMethods\": null,\n" +
                "                    \"IncludeCSVHeaders\": null,\n" +
                "                    \"PrintByIDPhotoKey\": null,\n" +
                "                    \"PrintCommentByPhoto\": null,\n" +
                "                    \"EmailTo\": \"\",\n" +
                "                    \"EmailCC\": \"\",\n" +
                "                    \"EmailBCC\": \"\",\n" +
                "                    \"EmailSubject\": \"\",\n" +
                "                    \"SaveBW\": null,\n" +
                "                    \"LayoutIncludeReceiptDate\": null,\n" +
                "                    \"LayoutIncludeReceiptCategory\": null,\n" +
                "                    \"LayoutIncludeReceiptPicture\": null,\n" +
                "                    \"MileageTotalInReport\": null,\n" +
                "                    \"MileageRate\": null,\n" +
                "                    \"MileagePrintTable\": null,\n" +
                "                    \"MileageAddToPDF\": null,\n" +
                "                    \"PdfFooterString\": null\n" +
                "                },\n" +
                "                \"Categories\": [\n" +
                "                    {\n" +
                "                        \"uuid\": \"075059fe-38c8-4fd6-b195-923448d0b08a\",\n" +
                "                        \"Name\": \"Cat 1\",\n" +
                "                        \"Code\": \"Cat1Code\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"PaymentMethods\": [\n" +
                "                    {\n" +
                "                        \"uuid\": \"41230f03-3254-4428-a806-946b9b30f935\",\n" +
                "                        \"Code\": \"PayM1\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"CSVColumns\": [\n" +
                "                    {\n" +
                "                        \"uuid\": \"fff24e83-fe5a-47b5-86fa-376d449fe348\",\n" +
                "                        \"Code\": \"CSVcol1\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"PDFColumns\": [\n" +
                "                    {\n" +
                "                        \"uuid\": \"bec00e55-80ad-4147-9c39-9d4a5acb635f\",\n" +
                "                        \"Code\": \"PDFcol1\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            \"error\": {\n" +
                "                \"has_error\": false,\n" +
                "                \"errors\": []\n" +
                "            },\n" +
                "            \"organization_users\": [\n" +
                "                {\n" +
                "                    \"id\": 15,\n" +
                "                    \"user_id\": 50,\n" +
                "                    \"organization_id\": 11,\n" +
                "                    \"role\": 1,\n" +
                "                    \"created_at\": \"2018-08-25T12:48:49.757Z\",\n" +
                "                    \"updated_at\": \"2018-08-25T12:48:49.757Z\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}"
    }

    lateinit var moshi: Moshi

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        moshi = SmartReceiptsMoshiBuilder().create()
    }

    @Test
    fun deserializeEmptyResponse() {
        val response = moshi.adapter(OrganizationsResponse::class.java).fromJson(JSON_EMPTY)
        assertNotNull(response)
        val organizations = response!!.organizations
        assertNotNull(organizations)
        assertTrue(organizations.isEmpty())
    }

    @Test
    fun deserializeResponse() {
        val response = moshi.adapter(OrganizationsResponse::class.java).fromJson(JSON)
        assertNotNull(response)

        val organizations = response!!.organizations
        assertNotNull(organizations)
        assertFalse(organizations.isEmpty())
        assertEquals(1, organizations.size)

        val organization = organizations[0]
        assertEquals("2587917096", organization.id)
        assertEquals("Test organization", organization.name)
        //todo 14.10.18 test date
//        assertEquals(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2018-08-25T12:48:47.628Z"), organization.createdAt)

        val error = organization.error
        assertNotNull(error)
        assertFalse(error.hasError)
        assertNotNull(error.errors)
        assertTrue(error.errors.isEmpty())

        val organizationUsers = organization.organizationUsers
        assertNotNull(organizationUsers)
        assertEquals(1, organizationUsers.size)

        val user = organizationUsers[0]
        assertEquals("15", user.id)
        assertEquals("50", user.userId)
        assertEquals("11", user.organizationId)
        assertEquals(OrganizationUser.UserRole.ADMIN, user.role)
        //todo 14.10.18 test dates

        val appSettings = organization.appSettings
        assertNotNull(appSettings)
        assertNotNull(appSettings.categories)
        assertNotNull(appSettings.paymentMethods)
        assertNotNull(appSettings.settings)
        assertNotNull(appSettings.configurations)

        val categories = appSettings.categories
        assertEquals(1, categories.size)
        val category = CategoryBuilderFactory()
            .setUuid(UUID.fromString("075059fe-38c8-4fd6-b195-923448d0b08a"))
            .setName("Cat 1")
            .setCode("Cat1Code")
            .build()
        assertEquals(category, categories[0])

        val paymentMethods = appSettings.paymentMethods
        assertEquals(1, paymentMethods.size)
        val method = PaymentMethodBuilderFactory()
            .setUuid(UUID.fromString("41230f03-3254-4428-a806-946b9b30f935"))
            .setMethod("PayM1")
            .build()
        assertEquals(method, paymentMethods[0])


        val configurations = appSettings.configurations
        assertNotNull(configurations)
        assertNull(configurations.isSettingsEnabled) //todo 14.10.18 server gets null now

        val settings = appSettings.settings
        assertNotNull(settings)
        //todo 14.10.18 test organization settings

    }
}
