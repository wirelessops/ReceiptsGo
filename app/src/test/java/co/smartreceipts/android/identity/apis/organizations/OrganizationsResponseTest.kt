package co.smartreceipts.android.identity.apis.organizations

import co.smartreceipts.android.apis.moshi.SmartReceiptsMoshiBuilder
import co.smartreceipts.android.model.factory.CategoryBuilderFactory
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptCategoryCodeColumn
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.sync.model.impl.DefaultSyncState
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
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
                    "TripDuration": null,
                    "isocurr": "AED",
                    "dateseparator": "-",
                    "trackcostcenter": null,
                    "PredictCats": null,
                    "MatchNameCats": null,
                    "MatchCommentCats": null,
                    "OnlyIncludeExpensable": null,
                    "ExpensableDefault": null,
                    "IncludeTaxField": null,
                    "TaxPercentage": null,
                    "PreTax": null,
                    "EnableAutoCompleteSuggestions": null,
                    "MinReceiptPrice": null,
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
                        "column_type": 1
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
                    "created_at": "2018-08-25T12:48:49.757Z",
                    "updated_at": "2018-08-25T12:48:49.757Z"
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
        whenever(receiptColumnDefinitions.getColumn(any(), eq(1), any(), any(), eq(UUID.fromString("fff24e83-fe5a-47b5-86fa-376d449fe348"))))
            .thenReturn(ReceiptCategoryCodeColumn(-1, DefaultSyncState(), 0, UUID.fromString("fff24e83-fe5a-47b5-86fa-376d449fe348")))

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
    fun deserializeResponse() {
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
        assertNotNull(appSettings.settings)
        assertNotNull(appSettings.configurations)


        // Testing Configurations section
        val configurations = appSettings.configurations
        assertNotNull(configurations)
        assertNull(configurations.isSettingsEnabled)


        // Testing Settings section
        val settings = appSettings.settings
        assertNotNull(settings)
        val jsonObject = settings.jsonObject

        assertTrue(jsonObject.has("TripDuration") && jsonObject.isNull("TripDuration"))
        assertTrue(jsonObject.has("isocurr") && !jsonObject.isNull("isocurr") && jsonObject.getString("isocurr") == "AED")
        assertTrue(jsonObject.has("dateseparator") && !jsonObject.isNull("dateseparator") && jsonObject.getString("dateseparator") == "-")
        assertTrue(jsonObject.has("trackcostcenter") && jsonObject.isNull("trackcostcenter"))
        assertTrue(jsonObject.has("PredictCats") && jsonObject.isNull("PredictCats"))
        assertTrue(jsonObject.has("MatchNameCats") && jsonObject.isNull("MatchNameCats"))
        assertTrue(jsonObject.has("MatchCommentCats") && jsonObject.isNull("MatchCommentCats"))
        assertTrue(jsonObject.has("OnlyIncludeExpensable") && jsonObject.isNull("OnlyIncludeExpensable"))
        assertTrue(jsonObject.has("ExpensableDefault") && jsonObject.isNull("ExpensableDefault"))
        assertTrue(jsonObject.has("IncludeTaxField") && jsonObject.isNull("IncludeTaxField"))
        assertTrue(jsonObject.has("TaxPercentage") && jsonObject.isNull("TaxPercentage"))
        assertTrue(jsonObject.has("PreTax") && jsonObject.isNull("PreTax"))
        assertTrue(jsonObject.has("EnableAutoCompleteSuggestions") && jsonObject.isNull("EnableAutoCompleteSuggestions"))
        assertTrue(jsonObject.has("MinReceiptPrice") && jsonObject.isNull("MinReceiptPrice"))
        assertTrue(jsonObject.has("DefaultToFirstReportDate") && jsonObject.isNull("DefaultToFirstReportDate"))
        assertTrue(jsonObject.has("ShowReceiptID") && jsonObject.isNull("ShowReceiptID"))
        assertTrue(jsonObject.has("UseFullPage") && jsonObject.isNull("UseFullPage"))
        assertTrue(jsonObject.has("UsePaymentMethods") && jsonObject.isNull("UsePaymentMethods"))
        assertTrue(jsonObject.has("IncludeCSVHeaders") && jsonObject.isNull("IncludeCSVHeaders"))
        assertTrue(jsonObject.has("PrintByIDPhotoKey") && jsonObject.isNull("PrintByIDPhotoKey"))
        assertTrue(jsonObject.has("PrintCommentByPhoto") && jsonObject.isNull("PrintCommentByPhoto"))
        assertTrue(jsonObject.has("EmailTo") && !jsonObject.isNull("EmailTo") && jsonObject.getString("EmailTo").isEmpty())
        assertTrue(jsonObject.has("EmailCC") && !jsonObject.isNull("EmailCC") && jsonObject.getString("EmailCC").isEmpty())
        assertTrue(jsonObject.has("EmailBCC") && !jsonObject.isNull("EmailBCC") && jsonObject.getString("EmailBCC").isEmpty())
        assertTrue(jsonObject.has("EmailSubject") && !jsonObject.isNull("EmailSubject") && jsonObject.getString("EmailSubject").isEmpty())
        assertTrue(jsonObject.has("SaveBW") && jsonObject.isNull("SaveBW"))
        assertTrue(jsonObject.has("LayoutIncludeReceiptDate") && jsonObject.isNull("LayoutIncludeReceiptDate"))
        assertTrue(jsonObject.has("LayoutIncludeReceiptCategory") && jsonObject.isNull("LayoutIncludeReceiptCategory"))
        assertTrue(jsonObject.has("LayoutIncludeReceiptPicture") && jsonObject.isNull("LayoutIncludeReceiptPicture"))
        assertTrue(jsonObject.has("MileageTotalInReport") && jsonObject.isNull("MileageTotalInReport"))
        assertTrue(jsonObject.has("MileageRate") && jsonObject.isNull("MileageRate"))
        assertTrue(jsonObject.has("MileagePrintTable") && jsonObject.isNull("MileagePrintTable"))
        assertTrue(jsonObject.has("MileageAddToPDF") && jsonObject.isNull("MileageAddToPDF"))
        assertTrue(jsonObject.has("PdfFooterString") && jsonObject.isNull("PdfFooterString"))


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
        assertEquals(1, csvColumn.type)

        // Testing PdfColumns section
        val pdfColumns = appSettings.pdfColumns
        assertEquals(1, pdfColumns.size)
        val pdfColumn = pdfColumns[0]
        assertEquals("bec00e55-80ad-4147-9c39-9d4a5acb635f", pdfColumn.uuid.toString())
        assertEquals(1, pdfColumn.type)
    }

}
