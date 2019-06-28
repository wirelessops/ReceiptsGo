package co.smartreceipts.android.test.espresso

import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import co.smartreceipts.android.SmartReceiptsApplication
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.persistence.DatabaseHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class FirstLaunchDatabaseTest {

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(SmartReceiptsActivity::class.java)

    private lateinit var databaseHelper: DatabaseHelper



    @Before
    fun setUp() {
        val application = activityTestRule.activity.application as SmartReceiptsApplication
        databaseHelper = application.databaseHelper
    }


    @Test
    fun upgradeFromV15() {
        // TODO: Instead of sleep, use an idling resource that triggers once #onUpgrade is complete
        Thread.sleep(TimeUnit.SECONDS.toMillis(7)) // Wait a few seconds to ensure the database loads

        // First - confirm that we're on the latest database version
        assertEquals(DatabaseHelper.DATABASE_VERSION, databaseHelper.readableDatabase.version)

        // Verify each data set as empty vs not as appropriate
        val categories = databaseHelper.categoriesTable.get().blockingGet()
        val paymentMethods = databaseHelper.paymentMethodsTable.get().blockingGet()
        val csvColumns = databaseHelper.csvTable.get().blockingGet()
        val pdfColumns = databaseHelper.pdfTable.get().blockingGet()
        val trips = databaseHelper.tripsTable.get().blockingGet()
        assertTrue(categories.isNotEmpty())
        assertTrue(paymentMethods.isNotEmpty())
        assertTrue(csvColumns.isNotEmpty())
        assertTrue(pdfColumns.isNotEmpty())
        assertTrue(trips.isEmpty())
    }

}