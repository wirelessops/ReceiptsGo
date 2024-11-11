package com.wops.receiptsgo.test.espresso

import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wops.receiptsgo.ReceiptsGoApplication
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.receiptsgo.persistence.DatabaseHelper
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
    val activityTestRule = ActivityTestRule(ReceiptsGoActivity::class.java)

    private lateinit var databaseHelper: DatabaseHelper



    @Before
    fun setUp() {
        val application = activityTestRule.activity.application as ReceiptsGoApplication
        databaseHelper = application.databaseHelper
    }


    @Test
    fun upgradeFromV15() {
        // TODO: Instead of sleep, use an idling resource that triggers once #onUpgrade is complete
        Thread.sleep(TimeUnit.SECONDS.toMillis(7)) // Wait a few seconds to ensure the database loads

        // First - confirm that we're on the latest database version
        assertEquals(DatabaseHelper.DATABASE_VERSION, databaseHelper.readableDatabase.version)

        // Verify each data set as empty vs not as appropriate
        val categories = databaseHelper.categoriesTable.blocking
        val paymentMethods = databaseHelper.paymentMethodsTable.blocking
        val csvColumns = databaseHelper.csvTable.blocking
        val pdfColumns = databaseHelper.pdfTable.blocking
        val trips = databaseHelper.tripsTable.blocking
        assertTrue(categories.isNotEmpty())
        assertTrue(paymentMethods.isNotEmpty())
        assertTrue(csvColumns.isNotEmpty())
        assertTrue(pdfColumns.isNotEmpty())
        assertTrue(trips.isEmpty())
    }

}