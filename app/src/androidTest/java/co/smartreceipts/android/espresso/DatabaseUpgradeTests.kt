package co.smartreceipts.android.espresso

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import co.smartreceipts.android.SmartReceiptsApplication
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.espresso.test.runner.BeforeApplicationOnCreate
import co.smartreceipts.android.espresso.test.utils.TestResourceReader
import co.smartreceipts.android.model.Keyed
import co.smartreceipts.android.model.PaymentMethod
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.persistence.DatabaseHelper
import org.apache.commons.io.IOUtils
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class DatabaseUpgradeTests {

    companion object {

        @Suppress("unused")
        @JvmStatic
        @BeforeApplicationOnCreate
        fun setUpBeforeApplicationOnCreate() {
            Log.i("DatabaseUpgradeTests", "Copying our test v15 database onto the local device...")

            // Set up the database info for the source and destination
            val inputStream = TestResourceReader().openStream(TestResourceReader.DATABASE_V15)
            val databaseLocation = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), DatabaseHelper.DATABASE_NAME)

            Log.i("DatabaseUpgradeTests", "Path: ${databaseLocation.absolutePath}")
            Log.i("DatabaseUpgradeTests", "Stream: ${TestResourceReader().openFile(TestResourceReader.DATABASE_V15).absolutePath}")

            // Copy our test from from our resources folder to the device
            val outputStream = FileOutputStream(databaseLocation)
            IOUtils.copy(inputStream, outputStream)
            IOUtils.closeQuietly(inputStream)
            IOUtils.closeQuietly(outputStream)
        }

    }

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(SmartReceiptsActivity::class.java)

    private lateinit var databaseHelper: DatabaseHelper

    private lateinit var context: Context

    @Before
    fun setUp() {
        val application = activityTestRule.activity.application as SmartReceiptsApplication
        databaseHelper = application.databaseHelper
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * This test runs through an old test database, so it simple exists to verify that we were able
     * to properly upgrade and that no data was lost in the process. It should be further noted that
     * while the database contains a number of receipts, most of them are copies of each other. As
     * such, we perform our validation of the receipts using one of three methods below:
     * <ul>
     *  <li>[assertPictureReceipt]</li>
     *  <li>[assertFullPictureReceipt]</li>
     *  <li>[assertPdfSampleReceipt]</li>
     * </ul>
     */
    @Test
    fun upgradeFromV15() {
        // TODO: Instead of sleep, use an idling resource that triggers once #onUpgrade is complete
        Thread.sleep(TimeUnit.SECONDS.toMillis(7)) // Wait a few seconds to ensure the database loads

        // First - confirm that we're on the latest database version
        assertEquals(DatabaseHelper.DATABASE_VERSION, databaseHelper.readableDatabase.version)

        // Next - confirm each of our trips and the data within
        val trips = databaseHelper.tripsTable.get().blockingGet()
        assertNotNull(trips)
        assertEquals(3, trips.size)

        // Data that we'll want to store for final comparisons
        val allReceipts = mutableListOf<Receipt>()
        var receiptIdCounter = 2
        var receiptIndexCounter = 1;

        // Confirm the data within Report 1
        val report1 = trips[0]
        assertEquals(1, report1.id)
        assertEquals("Report 1", report1.name)
        assertEquals("Report 1", report1.directory.name)
        assertEquals("11/17/16", report1.getFormattedStartDate(context, "/"))
        assertEquals("11/20/16", report1.getFormattedEndDate(context, "/"))
        assertEquals("$45.00", report1.price.currencyFormattedPrice.replace(",", "."))
        assertEquals("USD", report1.tripCurrency.currencyCode)
        assertEquals("", report1.comment)
        assertEquals("", report1.costCenter)

        val report1Receipts = databaseHelper.receiptsTable.get(report1, false).blockingGet()
        allReceipts.addAll(report1Receipts)
        report1Receipts.forEach {
            assertEquals(receiptIdCounter++, it.id)
            assertEquals(receiptIndexCounter++, it.index)
            assertPictureReceipt(it, report1) // Note: All receipts in report 1 are of this style
        }


        // Confirm the data within Report 2
        val report2 = trips[1]
        assertEquals(2, report2.id)
        assertEquals("Report 2", report2.name)
        assertEquals("Report 2", report2.directory.name)
        assertEquals("11/17/16", report2.getFormattedStartDate(context, "/"))
        assertEquals("11/20/16", report2.getFormattedEndDate(context, "/"))
        assertEquals("$50.00", report2.price.currencyFormattedPrice.replace(",", "."))
        assertEquals("USD", report2.tripCurrency.currencyCode)
        assertEquals("", report2.comment)
        assertEquals("", report2.costCenter)

        // Confirm the data within Report 3
        val report3 = trips[2]
        assertEquals(3, report3.id)
        assertEquals("Report 3", report3.name)
        assertEquals("Report 3", report3.directory.name)
        assertEquals("11/17/16", report3.getFormattedStartDate(context, "/"))
        assertEquals("11/20/16", report3.getFormattedEndDate(context, "/"))
        assertEquals("$42.00", report3.price.currencyFormattedPrice.replace(",", "."))
        assertEquals("USD", report3.tripCurrency.currencyCode)
        assertEquals("", report3.comment)
        assertEquals("", report3.costCenter)

        // Verify that none of our items have the same uuid
        assertNoUuidsAreEqual(trips)
        assertNoUuidsAreEqual(allReceipts)
    }

    private fun assertPictureReceipt(receipt: Receipt, parent: Trip) {
        assertEquals(parent, receipt.trip)
        assertEquals("$5.00", receipt.price.currencyFormattedPrice.replace(",", "."))
        assertEquals("USD", receipt.price.currencyCode)
        assertEquals("$0.00", receipt.tax.currencyFormattedPrice.replace(",", "."))
        assertEquals("USD", receipt.tax.currencyCode)
        assertEquals("11/17/16", receipt.getFormattedDate(context, "/"))
        // TODO: Category
        assertEquals("", receipt.comment)
        assertEquals(PaymentMethod.NONE, receipt.paymentMethod)
        assertTrue(receipt.isReimbursable)
        assertFalse(receipt.isFullPage)
    }

    private fun assertFullPictureReceipt(receipt: Receipt) {

    }

    private fun assertPdfSampleReceipt(receipt: Receipt) {

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

}