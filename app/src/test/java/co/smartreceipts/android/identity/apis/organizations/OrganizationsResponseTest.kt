package co.smartreceipts.android.identity.apis.organizations

import co.smartreceipts.android.apis.moshi.SmartReceiptsMoshiBuilder
import co.smartreceipts.android.model.factory.CategoryBuilderFactory
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryCodeColumn
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryNameColumn
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.core.sync.model.impl.DefaultSyncState
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.intellij.lang.annotations.Language
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class OrganizationsResponseTest {

    companion object {
        private const val JSON_EMPTY = "" +
                "{\n" +
                "}"


        @Language("JSON")
        private const val JSON = """{
    "organizations": [
        {
            "id": "2587917096",
            "name": "Test organization",
            "created_at_iso8601": "2018-08-25T12:48:47.628Z",
            "app_settings": {
                "Configurations": {
                    "IsSettingsEnable": null
                },
                "Settings": {
                    "TripDuration": 8,
                    "isocurr": "AED",
                    "dateseparator": "-",
                    "trackcostcenter": true,
                    "PredictCats": false,
                    "MatchNameCats": null,
                    "MatchCommentCats": null,
                    "OnlyIncludeExpensable": null,
                    "ExpensableDefault": null,
                    "IncludeTaxField": null,
                    "TaxPercentage": null,
                    "PreTax": null,
                    "EnableAutoCompleteSuggestions": null,
                    "MinReceiptPrice": 10.5,
                    "DefaultToFirstReportDate": null,
                    "ShowReceiptID": null,
                    "UseFullPage": null,
                    "UsePaymentMethods": null,
                    "IncludeCSVHeaders": null,
                    "PrintByIDPhotoKey": null,
                    "PrintCommentByPhoto": null,
                    "EmailTo": "",
                    "EmailCC": "",
                    "EmailBCC": "",
                    "EmailSubject": "",
                    "SaveBW": null,
                    "LayoutIncludeReceiptDate": null,
                    "LayoutIncludeReceiptCategory": null,
                    "LayoutIncludeReceiptPicture": null,
                    "MileageTotalInReport": null,
                    "MileageRate": null,
                    "MileagePrintTable": null,
                    "MileageAddToPDF": null,
                    "PdfFooterString": null
                },
                "Categories": [
                    {
                        "uuid": "075059fe-38c8-4fd6-b195-923448d0b08a",
                        "Name": "Cat 1",
                        "Code": "Cat1Code"
                    }
                ],
                "PaymentMethods": [
                    {
                        "uuid": "41230f03-3254-4428-a806-946b9b30f935",
                        "Code": "PayM1"
                    }
                ],
                "CSVColumns": [
                    {
                        "uuid": "fff24e83-fe5a-47b5-86fa-376d449fe348",
                        "column_type": 2
                    }
                ],
                "PDFColumns": [
                    {
                        "uuid": "bec00e55-80ad-4147-9c39-9d4a5acb635f",
                        "column_type": 1
                    }
                ]
            },
            "error": {
                "has_error": false,
                "errors": []
            },
            "organization_users": [
                {
                    "id": 15,
                    "user_id": 50,
                    "organization_id": 11,
                    "role": 1,
                    "created_at_iso8601": "2018-08-25T12:48:49.757Z",
                    "updated_at_iso8601": "2018-08-25T12:48:49.757Z"
                }
            ]
        }
    ]
}"""
    }

    private val receiptColumnDefinitions = mock<ReceiptColumnDefinitions>()

    private val jsonAdapter = SmartReceiptsMoshiBuilder(receiptColumnDefinitions).create().adapter(OrganizationsResponse::class.java)

    @Before
    fun setUp() {
        whenever(receiptColumnDefinitions.getColumn(any(), eq(2), any(), any(), eq(UUID.fromString("fff24e83-fe5a-47b5-86fa-376d449fe348"))))
            .thenReturn(ReceiptCategoryNameColumn(-1, DefaultSyncState(), 0, UUID.fromString("fff24e83-fe5a-47b5-86fa-376d449fe348")))

        whenever(receiptColumnDefinitions.getColumn(any(), eq(1), any(), any(), eq(UUID.fromString("bec00e55-80ad-4147-9c39-9d4a5acb635f"))))
            .thenReturn(ReceiptCategoryCodeColumn(-1, DefaultSyncState(), 0, UUID.fromString("bec00e55-80ad-4147-9c39-9d4a5acb635f")))

    }

    @Test
    fun deserializeEmptyResponse() {
        val response = jsonAdapter.fromJson(JSON_EMPTY)
        assertNotNull(response)

        val organizations = response!!.organizations
        assertNotNull(organizations)
        assertTrue(organizations.isEmpty())
    }

    @Test
    fun deserializeRealResponse() {
        val response = jsonAdapter.fromJson(JSON)
        assertNotNull(response)

        val organizations = response!!.organizations
        assertNotNull(organizations)
        assertFalse(organizations.isEmpty())
        assertEquals(1, organizations.size)

        val organization = organizations[0]
        assertEquals("2587917096", organization.id)
        assertEquals("Test organization", organization.name)

        // "created_at_iso8601": "2018-08-25T12:48:47.628Z"
        val date: Date =
            Calendar.getInstance(TimeZone.getTimeZone("GMT")).apply {
                set(2018, 7, 25, 12, 48, 47)
                set(Calendar.MILLISECOND, 628)
            }
                .time
        assertEquals(date, organization.createdAt)


        // Testing Errors section
        val error = organization.error
        assertNotNull(error)
        assertFalse(error.hasError)
        assertNotNull(error.errors)
        assertTrue(error.errors.isEmpty())


        // Testing OrganizationUsers section
        val organizationUsers = organization.organizationUsers
        assertNotNull(organizationUsers)
        assertEquals(1, organizationUsers.size)

        val user = organizationUsers[0]
        assertEquals("15", user.id)
        assertEquals("50", user.userId)
        assertEquals("11", user.organizationId)
        assertEquals(OrganizationUser.UserRole.ADMIN, user.role)
        // created_at": "2018-08-25T12:48:49.757Z", "updated_at": "2018-08-25T12:48:49.757Z"
        val userCreatedDate: Date =
            Calendar.getInstance(TimeZone.getTimeZone("GMT")).apply {
                set(2018, 7, 25, 12, 48, 49)
                set(Calendar.MILLISECOND, 757)
            }
                .time
        assertEquals(userCreatedDate, user.createdAt)
        assertEquals(userCreatedDate, user.updatedAt)


        // Testing AppSettings section
        val appSettings = organization.appSettings
        assertNotNull(appSettings)
        assertNotNull(appSettings.categories)
        assertNotNull(appSettings.paymentMethods)
        assertNotNull(appSettings.preferences)
        assertNotNull(appSettings.configurations)


        // Testing Configurations section
        val configurations = appSettings.configurations
        assertNotNull(configurations)
        assertNull(configurations.isSettingsEnabled)


        // Testing Settings section
        val settings = appSettings.preferences
        assertNotNull(settings)

        assertTrue(settings.containsKey("TripDuration") && settings["TripDuration"] == 8)
        assertTrue(settings.containsKey("isocurr") && settings["isocurr"] != null && settings["isocurr"] == "AED")
        assertTrue(settings.containsKey("trackcostcenter") && settings["trackcostcenter"] == true)
        assertTrue(settings.containsKey("MinReceiptPrice") && settings["MinReceiptPrice"] == 10.5f)
        assertTrue(settings.containsKey("TaxPercentage") && settings["TaxPercentage"] == null)


        // Testing Categories section
        val categories = appSettings.categories
        assertEquals(1, categories.size)
        val category = CategoryBuilderFactory()
            .setUuid(UUID.fromString("075059fe-38c8-4fd6-b195-923448d0b08a"))
            .setName("Cat 1")
            .setCode("Cat1Code")
            .build()
        assertEquals(category, categories[0])


        // Testing PaymentMethods section
        val paymentMethods = appSettings.paymentMethods
        assertEquals(1, paymentMethods.size)
        val method = PaymentMethodBuilderFactory()
            .setUuid(UUID.fromString("41230f03-3254-4428-a806-946b9b30f935"))
            .setMethod("PayM1")
            .build()
        assertEquals(method, paymentMethods[0])


        // Testing CsvColumns section
        val csvColumns = appSettings.csvColumns
        assertEquals(1, csvColumns.size)
        val csvColumn = csvColumns[0]
        assertEquals("fff24e83-fe5a-47b5-86fa-376d449fe348", csvColumn.uuid.toString())
        assertEquals(2, csvColumn.type)

        // Testing PdfColumns section
        val pdfColumns = appSettings.pdfColumns
        assertEquals(1, pdfColumns.size)
        val pdfColumn = pdfColumns[0]
        assertEquals("bec00e55-80ad-4147-9c39-9d4a5acb635f", pdfColumn.uuid.toString())
        assertEquals(1, pdfColumn.type)
    }


    @Test
    fun serializeTest() {
        val response = jsonAdapter.fromJson(JSON)
        val organization = response!!.organizations[0]

        val toJsonResult = jsonAdapter.toJson(response)
        val serializedOrganization = jsonAdapter.fromJson(toJsonResult)!!.organizations[0]

        assertEquals(organization.id, serializedOrganization.id)
        assertEquals(organization.name, serializedOrganization.name)
        assertEquals(organization.createdAt, serializedOrganization.createdAt)
        assertEquals(organization.error, serializedOrganization.error)

        assertEquals(organization.appSettings.csvColumns, serializedOrganization.appSettings.csvColumns)
        assertEquals(organization.appSettings.pdfColumns, serializedOrganization.appSettings.pdfColumns)
        assertEquals(organization.appSettings.categories, serializedOrganization.appSettings.categories)
        assertEquals(organization.appSettings.paymentMethods, serializedOrganization.appSettings.paymentMethods)

        assertEquals(organization.appSettings.preferences["TripDuration"], serializedOrganization.appSettings.preferences["TripDuration"]) //integer
        assertEquals(organization.appSettings.preferences["isocurr"], serializedOrganization.appSettings.preferences["isocurr"]) //string
        assertEquals(organization.appSettings.preferences["trackcostcenter"], serializedOrganization.appSettings.preferences["trackcostcenter"]) //boolean
        assertEquals(organization.appSettings.preferences["MinReceiptPrice"], serializedOrganization.appSettings.preferences["MinReceiptPrice"]) //float
    }

}
