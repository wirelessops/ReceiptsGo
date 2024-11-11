package com.wops.receiptsgo.test.espresso

import android.content.Context
import android.util.Log
import androidx.annotation.CallSuper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.wops.receiptsgo.ReceiptsGoApplication
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.receiptsgo.date.DateFormatter
import com.wops.receiptsgo.model.*
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions
import com.wops.receiptsgo.persistence.DatabaseHelper
import com.wops.receiptsgo.test.utils.TestLocaleToggler
import com.wops.receiptsgo.test.utils.TestResourceReader
import com.wops.core.persistence.DatabaseConstants
import com.google.common.base.Preconditions
import org.apache.commons.io.IOUtils
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * An abstract class that supports pre-assembling all local data and verifying that it is correct
 * for a database upgrade
 */
abstract class UpgradeFromKnownDatabaseValidator {

    companion object {
        
        private const val TAG = "UpgradeFromKnownDatabaseValidator"

        @Suppress("unused")
        @JvmStatic
        fun setUpBeforeApplicationOnCreate(databaseName: String) {
            Log.i(TAG, "Copying our test v15 database onto the local device...")
            val externalFilesDir = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
            val databaseLocation = File(externalFilesDir, DatabaseConstants.DATABASE_NAME)
            copyFile(databaseName, databaseLocation)

            Log.i(TAG, "Creating report folders...")
            val report1Folder = File(externalFilesDir, "Report 1")
            val report2Folder = File(externalFilesDir, "Report 2")
            val report3Folder = File(externalFilesDir, "Report 3")
            Preconditions.checkArgument(report1Folder.exists() || report1Folder.mkdirs())
            Preconditions.checkArgument(report2Folder.exists() || report2Folder.mkdirs())
            Preconditions.checkArgument(report3Folder.exists() || report3Folder.mkdirs())

            Log.i(TAG, "Copying over placeholder images/pdfs...")
            copyFile(TestResourceReader.V15_IMAGE, File(report1Folder, "1_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report1Folder, "2_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report1Folder, "3_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report1Folder, "4_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report1Folder, "5_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report1Folder, "6_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report1Folder, "7_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report1Folder, "8_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report1Folder, "9_Picture.jpg"))

            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "1_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "2_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "3_Full picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "4_Full picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "5_Full picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "6_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "14794171342646_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "7_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "8_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "9_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report2Folder, "10_Picture.jpg"))

            copyFile(TestResourceReader.V15_IMAGE, File(report3Folder, "11_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report3Folder, "11_Full picture.jpg"))
            copyFile(TestResourceReader.V15_PDF, File(report3Folder, "3_Pdf sample.pdf"))
            copyFile(TestResourceReader.V15_IMAGE, File(report3Folder, "4_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report3Folder, "5_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report3Folder, "6_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report3Folder, "7_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report3Folder, "8_Picture.jpg"))
            copyFile(TestResourceReader.V15_IMAGE, File(report3Folder, "9_Picture.jpg"))
        }

        @JvmStatic
        private fun copyFile(resourceName: String, destination: File) {
            try {
                TestResourceReader().openStream(resourceName).use { inputStream ->
                    FileOutputStream(destination).use { outputStream ->
                        IOUtils.copy(inputStream, outputStream)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Copy failed: $resourceName -> ${destination.absolutePath}", e)
                throw e
            }
        }

    }

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(ReceiptsGoActivity::class.java)

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var context: Context

    private lateinit var dateFormatter: DateFormatter

    @Before
    @CallSuper
    fun setUp() {
        val application = activityTestRule.activity.application as ReceiptsGoApplication
        databaseHelper = application.databaseHelper
        dateFormatter = application.dateFormatter

        // Set the Locale to en-US for consistency purposes
        val nonLocalizedContext = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = nonLocalizedContext.resources.configuration
        configuration.setLocale(Locale.US)
        TestLocaleToggler.setDefaultLocale(Locale.US)
        context = nonLocalizedContext.createConfigurationContext(configuration)
    }

    @After
    @CallSuper
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    /**
     * This test runs through an old test database, so it simple exists to verify that we were able
     * to properly upgrade and that no data was lost in the process. It should be further noted that
     * while the database contains a number of receipts, most of them are copies of each other. As
     * such, we perform our validation of the receipts using one of three methods below:
     * <ul>
     *  <li>[verifyPictureReceipt]</li>
     *  <li>[verifyFullPictureReceipt]</li>
     *  <li>[verifyPdfSampleReceipt]</li>
     * </ul>
     */
    @Test
    fun upgradeDatabase() {
        // TODO: Instead of sleep, use an idling resource that triggers once #onUpgrade is complete
        Thread.sleep(TimeUnit.SECONDS.toMillis(7)) // Wait a few seconds to ensure the database loads

        // First - confirm that we're on the latest database version
        assertEquals(DatabaseHelper.DATABASE_VERSION, databaseHelper.readableDatabase.version)

        // Next - verify each of our categories
        var categoryId = 0
        val categories = databaseHelper.categoriesTable.blocking
        verifyCategory(categories[categoryId++], "<Category>", "NUL", 0)
        verifyCategory(categories[categoryId++], "Airfare", "AIRP", 0)
        verifyCategory(categories[categoryId++], "Books/Periodicals", "ZBKP", 0)
        verifyCategory(categories[categoryId++], "Breakfast", "BRFT", 0)
        verifyCategory(categories[categoryId++], "Car Rental", "RCAR", 0)
        verifyCategory(categories[categoryId++], "Cell Phone", "ZCEL", 0)
        val dinnerCategory = categories[categoryId++]
        verifyCategory(dinnerCategory, "Dinner", "DINN", 0)
        verifyCategory(categories[categoryId++], "Dues/Subscriptions", "ZDUE", 0)
        verifyCategory(categories[categoryId++], "Entertainment", "ENT", 0)
        verifyCategory(categories[categoryId++], "Gasoline", "GAS", 0)
        verifyCategory(categories[categoryId++], "Gift","GIFT", 0)
        verifyCategory(categories[categoryId++], "Hotel", "HTL", 0)
        verifyCategory(categories[categoryId++], "Laundry","LAUN", 0)
        val lunchCategory = categories[categoryId++]
        verifyCategory(lunchCategory, "Lunch", "LNCH", 0)
        verifyCategory(categories[categoryId++], "Meals (Justified)", "ZMEO", 0)
        verifyCategory(categories[categoryId++], "Other", "MISC", 0)
        verifyCategory(categories[categoryId++], "Parking/Tolls", "PARK", 0)
        verifyCategory(categories[categoryId++], "Postage/Shipping", "POST", 0)
        verifyCategory(categories[categoryId++], "Stationery/Stations", "ZSTS", 0)
        verifyCategory(categories[categoryId++], "Taxi/Bus", "TAXI", 0)
        verifyCategory(categories[categoryId++], "Telephone/Fax", "TELE", 0)
        verifyCategory(categories[categoryId++], "Tip", "TIP", 0)
        verifyCategory(categories[categoryId++], "Train", "TRN", 0)
        verifyCategory(categories[categoryId], "Training Fees", "ZTRN", 0)

        // Next - verify each of our payment methods
        val paymentMethods = databaseHelper.paymentMethodsTable.blocking
        verifyPaymentMethod(paymentMethods[0], 1, "Unspecified", 0)
        verifyPaymentMethod(paymentMethods[1], 2, "Corporate Card", 0)
        verifyPaymentMethod(paymentMethods[2], 3, "Personal Card", 0)
        val personalCardPaymentMethod = paymentMethods[2]
        verifyPaymentMethod(paymentMethods[3], 4, "Check", 0)
        verifyPaymentMethod(paymentMethods[4], 5, "Cash", 0)

        // Next - verify each of our CSV columns
        val csvColumns = databaseHelper.csvTable.blocking
        verifyCsvColumns(csvColumns[0], 1, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE, 0)
        verifyCsvColumns(csvColumns[1], 2, ReceiptColumnDefinitions.ActualDefinition.NAME, 0)
        verifyCsvColumns(csvColumns[2], 3, ReceiptColumnDefinitions.ActualDefinition.PRICE, 0)
        verifyCsvColumns(csvColumns[3], 4, ReceiptColumnDefinitions.ActualDefinition.CURRENCY, 0)
        verifyCsvColumns(csvColumns[4], 5, ReceiptColumnDefinitions.ActualDefinition.DATE, 0)

        // Next - verify each of our PDF columns
        val pdfColumns = databaseHelper.pdfTable.blocking
        verifyPdfColumns(pdfColumns[0], 1, ReceiptColumnDefinitions.ActualDefinition.NAME, 0)
        verifyPdfColumns(pdfColumns[1], 2, ReceiptColumnDefinitions.ActualDefinition.PRICE, 0)
        verifyPdfColumns(pdfColumns[2], 3, ReceiptColumnDefinitions.ActualDefinition.DATE, 0)
        verifyPdfColumns(pdfColumns[3], 4, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME, 0)
        verifyPdfColumns(pdfColumns[4], 5, ReceiptColumnDefinitions.ActualDefinition.REIMBURSABLE, 0)
        verifyPdfColumns(pdfColumns[5], 6, ReceiptColumnDefinitions.ActualDefinition.PICTURED, 0)

        // Next - confirm each of our trips and the data within
        val trips = databaseHelper.tripsTable.blocking
        assertNotNull(trips)
        assertEquals(3, trips.size)

        // Data that we'll want to store for final comparisons
        val allReceipts = mutableListOf<Receipt>()
        val allDistances = mutableListOf<Distance>()

        // Receipt counters
        var lastReceiptCustomOrderId = 0L
        var receiptIndexCounter = 1

        // Confirm the data within Report 1
        val report1 = trips[0]
        assertEquals(1, report1.id)
        assertEquals("Report 1", report1.name)
        assertEquals("Report 1", report1.directory.name)
        assertEquals("11/16/16", dateFormatter.getFormattedDate(report1.startDisplayableDate))
        assertEquals("11/20/16", dateFormatter.getFormattedDate(report1.endDisplayableDate))
        assertEquals("$45.00", report1.price.currencyFormattedPrice)
        assertEquals("USD", report1.tripCurrency.code)
        assertEquals("Comment", report1.comment)
        assertEquals("Cost Center", report1.costCenter)

        // And the receipts in report 1
        val report1Receipts = databaseHelper.receiptsTable.getBlocking(report1, false)
        allReceipts.addAll(report1Receipts)
        report1Receipts.forEach {
            assertEquals(receiptIndexCounter++, it.index)
            assertTrue(it.customOrderId > lastReceiptCustomOrderId) // These should be increasing for receipts
            lastReceiptCustomOrderId = it.customOrderId
            verifyPictureReceipt(it, report1, dinnerCategory) // Note: All receipts in report 1 are of this style
        }

        // And the distances
        val report1Distances = databaseHelper.distanceTable.getBlocking(report1, false)
        assertTrue(report1Distances.isEmpty())
        allDistances.addAll(report1Distances)

        // Confirm the data within Report 2
        val report2 = trips[1]
        assertEquals(2, report2.id)
        assertEquals("Report 2", report2.name)
        assertEquals("Report 2", report2.directory.name)
        assertEquals("11/17/16", dateFormatter.getFormattedDate(report2.startDisplayableDate))
        assertEquals("11/20/16", dateFormatter.getFormattedDate(report2.endDisplayableDate))
        assertEquals("$50.00", report2.price.currencyFormattedPrice)
        assertEquals("USD", report2.tripCurrency.code)
        assertEquals("", report2.comment)
        assertEquals("", report2.costCenter)

        // And the receipts in report 2
        receiptIndexCounter = 1
        lastReceiptCustomOrderId = 0
        val report2Receipts = databaseHelper.receiptsTable.getBlocking(report2, false)
        allReceipts.addAll(report2Receipts)
        report2Receipts.forEach {
            assertEquals(receiptIndexCounter++, it.index)
            assertTrue(it.customOrderId > lastReceiptCustomOrderId) // These should be increasing for receipts
            lastReceiptCustomOrderId = it.customOrderId
            when (receiptIndexCounter - 1) {
                in 1..5 -> verifyPictureReceipt(it, report2, dinnerCategory)
                in 6..8 -> verifyFullPictureReceipt(it, report2, lunchCategory, personalCardPaymentMethod)
                else -> verifyPictureReceipt(it, report2, dinnerCategory, "11/19/16")
            }
        }

        // And the distances
        val report2Distances = databaseHelper.distanceTable.getBlocking(report2, false)
        assertTrue(report2Distances.isEmpty())
        allDistances.addAll(report2Distances)

        // Confirm the data within Report 3
        val report3 = trips[2]
        assertEquals(3, report3.id)
        assertEquals("Report 3", report3.name)
        assertEquals("Report 3", report3.directory.name)
        assertEquals("11/17/16", dateFormatter.getFormattedDate(report3.startDisplayableDate))
        assertEquals("11/20/16", dateFormatter.getFormattedDate(report3.endDisplayableDate))
        assertEquals("$68.60", report3.price.currencyFormattedPrice)
        assertEquals("USD", report3.tripCurrency.code)
        assertEquals("", report3.comment)
        assertEquals("", report3.costCenter)

        // And the receipts in report 3
        receiptIndexCounter = 1
        lastReceiptCustomOrderId = 0
        val report3Receipts = databaseHelper.receiptsTable.getBlocking(report3, false)
        allReceipts.addAll(report3Receipts)
        report3Receipts.forEach {
            assertEquals(receiptIndexCounter++, it.index)
            assertTrue(it.customOrderId > lastReceiptCustomOrderId) // These should be increasing for receipts
            lastReceiptCustomOrderId = it.customOrderId
            when (receiptIndexCounter - 1) {
                1 -> verifyPictureReceipt(it, report3, dinnerCategory)
                2 -> verifyFullPictureReceipt(it, report3, lunchCategory, personalCardPaymentMethod)
                3 -> verifyPdfSampleReceipt(it, report3, dinnerCategory)
                in 4..9 -> verifyPictureReceipt(it, report3, dinnerCategory, "11/20/16")
                10 -> verifyTextReceipt(it, report3, lunchCategory)
                else -> verifyPictureReceipt(it, report3, dinnerCategory, "11/20/16")
            }
        }

        // And the distances
        val report3Distances = databaseHelper.distanceTable.getBlocking(report3, false)
        assertTrue(report3Distances.size == 1)
        val distance = report3Distances[0]
        assertEquals(1, distance.id)
        assertEquals(report3, distance.trip)
        assertEquals("1.500", distance.decimalFormattedRate)
        assertEquals("$3.00", distance.price.currencyFormattedPrice)
        assertEquals("USD", distance.price.currencyCode)
        assertEquals("Location", distance.location)
        assertEquals("11/20/16", dateFormatter.getFormattedDate(distance.displayableDate))
        assertEquals("Comment", distance.comment)

        allDistances.addAll(report3Distances)

        // Verify that none of our items have the same uuid
        assertNoUuidsAreEqual(categories)
        assertNoUuidsAreEqual(paymentMethods)
        assertNoUuidsAreEqual(csvColumns)
        assertNoUuidsAreEqual(pdfColumns)
        assertNoUuidsAreEqual(trips)
        assertNoUuidsAreEqual(allReceipts)
        assertNoUuidsAreEqual(allDistances)

        // Verify that our receipts don't point to the same file
        assertNoFilesAreEqual(allReceipts)
    }

    private fun verifyCategory(category: Category, name: String, code: String, customOrderId: Long) {
        assertEquals(name, category.name)
        assertEquals(code, category.code)
        assertEquals(customOrderId, category.customOrderId)
    }

    private fun verifyPaymentMethod(paymentMethod: PaymentMethod, id: Int, name: String, customOrderId: Long) {
        assertEquals(id, paymentMethod.id)
        assertEquals(name, paymentMethod.method)
        assertEquals(customOrderId, paymentMethod.customOrderId)
    }

    private fun verifyCsvColumns(csvColumn: Column<Receipt>, id: Int, type: ReceiptColumnDefinitions.ActualDefinition, customOrderId: Long) {
        assertEquals(id, csvColumn.id)
        assertEquals(type.columnType, csvColumn.type)
        assertEquals(customOrderId, csvColumn.customOrderId)
    }

    private fun verifyPdfColumns(pdfColumn: Column<Receipt>, id: Int, type: ReceiptColumnDefinitions.ActualDefinition, customOrderId: Long) {
        assertEquals(id, pdfColumn.id)
        assertEquals(type.columnType, pdfColumn.type)
        assertEquals(customOrderId, pdfColumn.customOrderId)
    }

    private fun verifyPictureReceipt(receipt: Receipt, parent: Trip, category: Category, date: String = "11/17/16") {
        assertEquals(parent, receipt.trip)
        assertEquals("Picture", receipt.name)
        assertNotNull(receipt.file)
        assertTrue(receipt.hasImage())
        assertFalse(receipt.hasPDF())
        assertTrue(Pattern.compile("\\d+_Picture.jpg").matcher(receipt.fileName).matches())
        assertEquals("$5.00", receipt.price.currencyFormattedPrice)
        assertEquals("USD", receipt.price.currencyCode)
        assertEquals("$0.00", receipt.tax.currencyFormattedPrice)
        assertEquals("USD", receipt.tax.currencyCode)
        assertEquals(date, dateFormatter.getFormattedDate(receipt.displayableDate))
        assertEquals(category, receipt.category)
        assertEquals("", receipt.comment)
        assertEquals(PaymentMethod.NONE, receipt.paymentMethod)
        assertTrue(receipt.isReimbursable)
        assertFalse(receipt.isFullPage)
    }

    private fun verifyFullPictureReceipt(receipt: Receipt, parent: Trip, category: Category, paymentMethod: PaymentMethod, date: String = "11/18/16") {
        assertEquals(parent, receipt.trip)
        assertNotNull(receipt.file)
        assertTrue(receipt.hasImage())
        assertFalse(receipt.hasPDF())
        assertTrue(Pattern.compile("\\d+_Full picture.jpg").matcher(receipt.fileName).matches())
        assertEquals("Full picture", receipt.name)
        assertEquals("$5.00", receipt.price.currencyFormattedPrice)
        assertEquals("USD", receipt.price.currencyCode)
        assertEquals("$1.50", receipt.tax.currencyFormattedPrice)
        assertEquals("USD", receipt.tax.currencyCode)
        assertEquals(date, dateFormatter.getFormattedDate(receipt.displayableDate))
        assertEquals(category, receipt.category)
        assertEquals("", receipt.comment)
        assertEquals(paymentMethod, receipt.paymentMethod)
        assertTrue(receipt.isReimbursable)
        assertTrue(receipt.isFullPage)
    }

    private fun verifyPdfSampleReceipt(receipt: Receipt, parent: Trip, category: Category, date: String = "11/19/16") {
        assertEquals(parent, receipt.trip)
        assertNotNull(receipt.file)
        assertFalse(receipt.hasImage())
        assertTrue(receipt.hasPDF())
        assertEquals("3_Pdf sample.pdf", receipt.fileName)
        assertEquals("Pdf sample", receipt.name)
        assertEquals("$2.00", receipt.price.currencyFormattedPrice)
        assertEquals("USD", receipt.price.currencyCode)
        assertEquals("$0.00", receipt.tax.currencyFormattedPrice)
        assertEquals("USD", receipt.tax.currencyCode)
        assertEquals(date, dateFormatter.getFormattedDate(receipt.displayableDate))
        assertEquals(category, receipt.category)
        assertEquals("", receipt.comment)
        assertEquals(PaymentMethod.NONE, receipt.paymentMethod)
        assertFalse(receipt.isReimbursable)
        assertFalse(receipt.isFullPage)
    }

    private fun verifyTextReceipt(receipt: Receipt, parent: Trip, category: Category) {
        assertEquals(parent, receipt.trip)
        assertNull(receipt.file)
        assertFalse(receipt.hasImage())
        assertFalse(receipt.hasPDF())
        assertEquals("Text", receipt.name)
        assertEquals("€13.30", receipt.price.currencyFormattedPrice)
        assertEquals("EUR", receipt.price.currencyCode)
        assertEquals("€13.30", receipt.price.currencyFormattedPrice)
        assertEquals("€0.00", receipt.tax.currencyFormattedPrice)
        assertEquals("EUR", receipt.price.exchangeRate.baseCurrencyCode)
        assertTrue(receipt.price.exchangeRate.supportsExchangeRateFor("USD"))
        assertEquals("2.000,000", receipt.price.exchangeRate.getDecimalFormattedExchangeRate("USD"))
        assertEquals("EUR", receipt.tax.currencyCode)
        assertEquals("11/20/16", dateFormatter.getFormattedDate(receipt.displayableDate))
        assertEquals(category, receipt.category)
        assertEquals("Comment", receipt.comment)
        assertEquals(PaymentMethod.NONE, receipt.paymentMethod)
        assertTrue(receipt.isReimbursable)
        assertFalse(receipt.isFullPage)
    }

    private fun assertNoUuidsAreEqual(keyedItems: List<Keyed>) {
        keyedItems.forEach { item1 ->
            keyedItems.forEach {item2 ->
                if (item1 != item2) {
                    // Don't compare if it's the same item
                    assertThat(item1.uuid, not(equalTo(item2.uuid)))
                }
            }
        }
    }

    private fun assertNoFilesAreEqual(receipts: List<Receipt>) {
        receipts.forEach { item1 ->
            receipts.forEach {item2 ->
                if (item1 != item2) {
                    // Don't compare if it's the same item
                    assertThat(item1.file, not(equalTo(item2.file)))
                }
            }
        }
    }

}