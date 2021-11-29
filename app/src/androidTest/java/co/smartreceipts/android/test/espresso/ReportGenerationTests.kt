package co.smartreceipts.android.test.espresso

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import co.smartreceipts.android.R
import co.smartreceipts.android.SmartReceiptsApplication
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.android.test.utils.CustomActions.Companion.waitForView
import co.smartreceipts.android.test.utils.CustomActions.Companion.withIndex
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReportGenerationTests {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(SmartReceiptsActivity::class.java)

    private var authority: String = ""
    private lateinit var activity: Activity
    private lateinit var databaseHelper: DatabaseHelper

    @Before
    fun setUp() {
        Intents.init()
        activityScenarioRule.scenario.onActivity { activity ->
            this.activity = activity
            val application = activity.application as SmartReceiptsApplication
            databaseHelper = application.databaseHelper
        }

        authority = String.format(Locale.US, "%s.fileprovider", InstrumentationRegistry.getInstrumentation().targetContext.packageName)

        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        intending(not(isInternal())).respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    private fun createReport(reportName: String) {
        // Click on the "new report" button
        onView(withId(R.id.trip_action_new)).perform(click())

        // Close the keyboard
        Espresso.closeSoftKeyboard()

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.name)).check(matches(isDisplayed()))
        onView(withId(R.id.start_date)).check(matches(isDisplayed()))
        onView(withId(R.id.end_date)).check(matches(isDisplayed()))
        onView(withId(R.id.spinner)).check(matches(isDisplayed()))
        onView(withId(R.id.trip_comment)).check(matches(isDisplayed()))
        onView(withId(R.id.cost_center)).check(matches(not(isDisplayed())))

        // Create a trip with the passed report name
        onView(withId(R.id.name)).perform(replaceText(reportName), closeSoftKeyboard())

        // Wait a second to ensure the keyboard closed
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Save the trip
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        onView(isRoot()).perform(waitForView(R.id.bottom_app_bar, 20000))
        onView(isRoot()).perform(waitForView(R.id.no_data, 20000))

        // Verify that we have an empty report
        onView(withIndex(withId(R.id.no_data_text), 0)).check(matches(withText(R.string.receipt_no_data)))
    }

    private fun createReceiptGoToGenerate() {
        // Open receipt creation dialog
        onView(withId(R.id.fab)).perform(click())

        // Click on "text only" button
        onView(withId(R.id.new_text)).perform(click())

        // Close the keyboard
        Espresso.closeSoftKeyboard()

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_name)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_price)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_date)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_comment)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_expensable)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_fullpage)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_currency)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_category)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_tax1)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_exchange_rate)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_exchanged_result)).check(matches(not(isDisplayed())))
        //todo following view doesn't apply to fire department variant, find a way to test variants
//        onView(withId(R.id.receipt_input_payment_method)).check(matches(not(isDisplayed())))

        // Create a receipt, entitled "Test" priced at $12.34
        onView(withId(R.id.receipt_name)).perform(replaceText("Test Receipt"))
        onView(withId(R.id.receipt_price)).perform(replaceText("12.34"), closeSoftKeyboard())

        // Wait a second to ensure the keyboard closed
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Save the receipt
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        onView(isRoot()).perform(waitForView(R.id.bottom_app_bar, 20000))
        onView(isRoot()).perform(waitForView(R.id.receipts_container, 20000))

        // Verify that we have a list item with Test Receipt
        onView(allOf(isDescendantOfA(withId(R.id.receipts_container)), withId(R.id.text_name))).check(matches(withText("Test Receipt")))

        // Go to generate screen
        onView(withContentDescription(R.string.report_info_generate)).perform(click())

        // Wait to ensure everything loads
        onView(isRoot()).perform(waitForView(R.id.dialog_email_checkbox_pdf_full, 20000))
    }

    @Test
    fun createTripAddReceiptGeneratePDF() {
        val uri = Uri.parse("content://$authority/public-files-path/PDF Report/PDF Report.pdf".replace(" ", "%20"))

        // Create our trip
        createReport("PDF Report")

        createReceiptGoToGenerate()

        // Check the box for Full PDF Report
        onView(withId(R.id.dialog_email_checkbox_pdf_full)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.fab)).perform(click())

        // Wait until everything loads
        Thread.sleep(TimeUnit.SECONDS.toMillis(5))

        // Verify the intent chooser with a PDF report was displayed
        intended(allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(`is`(Intent.EXTRA_INTENT),
                        allOf(
                                hasAction(Intent.ACTION_SEND_MULTIPLE),
                                hasType("application/pdf"),
                                hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                                hasExtra(Intent.EXTRA_STREAM, arrayListOf(uri))
                        )
                )
        ))
    }

    @Test
    fun createTripAddReceiptGeneratePDFNoTable() {
        val uri = Uri.parse("content://$authority/public-files-path/PDF Report No Table/PDF Report No TableImages.pdf".replace(" ", "%20"))

        // Create our trip
        createReport("PDF Report No Table")

        createReceiptGoToGenerate()

        // Check the box for PDF Report - No Table
        onView(withId(R.id.dialog_email_checkbox_pdf_images)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.fab)).perform(click())

        // Wait until everything loads
        Thread.sleep(TimeUnit.SECONDS.toMillis(5))

        // Verify the intent chooser with a PDF report was displayed
        intended(allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(`is`(Intent.EXTRA_INTENT),
                        allOf(
                                hasAction(Intent.ACTION_SEND_MULTIPLE),
                                hasType("application/pdf"),
                                hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                                hasExtra(Intent.EXTRA_STREAM, arrayListOf(uri))
                        )
                )
        ))
    }

    @Test
    fun createTripAddReceiptGenerateCSV() {
        val uri = Uri.parse("content://$authority/public-files-path/CSV Report/CSV Report.csv".replace(" ", "%20"))

        // Create our trip
        createReport("CSV Report")

        createReceiptGoToGenerate()

        // Check the box for CSV File
        onView(withId(R.id.dialog_email_checkbox_csv)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.fab)).perform(click())

        // Wait until everything loads
        Thread.sleep(TimeUnit.SECONDS.toMillis(5))

        // Verify the intent chooser with a CSV report was displayed
        intended(allOf(
                hasAction(Intent.ACTION_CHOOSER),
                hasExtra(`is`(Intent.EXTRA_INTENT),
                        allOf(
                                hasAction(Intent.ACTION_SEND_MULTIPLE),
                                hasType("text/comma-separated-values"),
                                hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                                hasExtra(Intent.EXTRA_STREAM, arrayListOf(uri))
                        )
                )
        ))
    }

    @Test
    fun createTripAddReceiptGenerateZip() {
        // Create our trip
        createReport("Zip Report")

        createReceiptGoToGenerate()

        // Check the box for Zip - Receipts Files
        onView(withId(R.id.dialog_email_checkbox_zip)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.fab)).perform(click())

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // Verify the toast was displayed
            onView(withText(R.string.report_error_undetermined)).inRoot(withDecorView(not(activity.window.decorView))).check(matches(isDisplayed()))
        } else {
            // Wait until everything loads
            Thread.sleep(TimeUnit.SECONDS.toMillis(5))

            // Verify the intent chooser with a Zip file was displayed
            intended(allOf(
                    hasAction(Intent.ACTION_CHOOSER),
                    hasExtra(`is`(Intent.EXTRA_INTENT),
                            allOf(
                                    hasAction(Intent.ACTION_SEND_MULTIPLE),
                                    hasType("application/zip"),
                                    hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            )
                    ),
                    hasExtra(Intent.EXTRA_TITLE, activity.resources.getString(R.string.send_email))
            ))
        }
    }

    @Test
    fun createTripAddReceiptGenerateZipWithMetadata() {
        // Create our trip
        createReport("Zip Report with Metadata")

        createReceiptGoToGenerate()

        // Check the box for Zip - Receipt Files with Metadata
        onView(withId(R.id.dialog_email_checkbox_zip_with_metadata)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.fab)).perform(click())

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // Verify the toast was displayed
            onView(withText(R.string.report_error_undetermined)).inRoot(withDecorView(not(activity.window.decorView))).check(matches(isDisplayed()))
        } else {
            // Wait until everything loads
            Thread.sleep(TimeUnit.SECONDS.toMillis(5))

            // Verify the intent chooser with a Zip file was displayed
            intended(allOf(
                    hasAction(Intent.ACTION_CHOOSER),
                    hasExtra(`is`(Intent.EXTRA_INTENT),
                            allOf(
                                    hasAction(Intent.ACTION_SEND_MULTIPLE),
                                    hasType("application/zip"),
                                    hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            )
                    ),
                    hasExtra(Intent.EXTRA_TITLE, activity.resources.getString(R.string.send_email))
            ))
        }
    }

    @Test
    fun createTripAddReceiptGenerateMultipleReports() {
        val uri = Uri.parse("content://$authority/public-files-path/All File Type Report/All File Type Report.pdf".replace(" ", "%20"))
        val uri1 = Uri.parse("content://$authority/public-files-path/All File Type Report/All File Type ReportImages.pdf".replace(" ", "%20"))
        val uri2 = Uri.parse("content://$authority/public-files-path/All File Type Report/All File Type Report.csv".replace(" ", "%20"))

        // Create our trip
        createReport("All File Type Report")

        createReceiptGoToGenerate()

        // Check all of the file type boxes
        onView(withId(R.id.dialog_email_checkbox_pdf_full)).perform(click())
        onView(withId(R.id.dialog_email_checkbox_pdf_images)).perform(click())
        onView(withId(R.id.dialog_email_checkbox_csv)).perform(click())
//        onView(withId(R.id.dialog_email_checkbox_zip)).perform(click())
//        onView(withId(R.id.dialog_email_checkbox_zip_with_metadata)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.fab)).perform(click())

        // Verify the intent chooser with all files was displayed
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            intended(allOf(
                    hasAction(Intent.ACTION_CHOOSER),
                    hasExtra(`is`(Intent.EXTRA_INTENT),
                            allOf(
                                    hasAction(Intent.ACTION_SEND_MULTIPLE),
                                    hasType("application/octet-stream"),
                                    hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                                    hasExtra(Intent.EXTRA_STREAM, arrayListOf(uri, uri1, uri2))
                            )
                    )
            ))
        } else {
            // Wait until everything loads
            Thread.sleep(TimeUnit.SECONDS.toMillis(5))

            intended(allOf(
                    hasAction(Intent.ACTION_CHOOSER),
                    hasExtra(`is`(Intent.EXTRA_INTENT),
                            allOf(
                                    hasAction(Intent.ACTION_SEND_MULTIPLE),
                                    hasType("application/octet-stream"),
                                    hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            )
                    ),
                    hasExtra(Intent.EXTRA_TITLE, activity.resources.getString(R.string.send_email))
            ))
        }
    }

    // TODO: 21.10.2021 Turn next two tests on when the issue will be fixed https://github.com/android/android-test/issues/803
//    @Test
//    fun createTripNoReceiptError() { // fails
//        // Create our trip
//        createReport("Empty Report No Receipts Error")
//
//        // Go to generate screen
//        onView(withContentDescription(R.string.report_info_generate)).perform(click())
//
//        // Wait to ensure everything loads
//        onView(isRoot()).perform(waitForView(R.id.dialog_email_checkbox_pdf_full, 20000))
//
//        // Check all of the file type boxes
//        onView(withId(R.id.dialog_email_checkbox_pdf_full)).perform(click())
//        onView(withId(R.id.dialog_email_checkbox_pdf_images)).perform(click())
//        onView(withId(R.id.dialog_email_checkbox_csv)).perform(click())
//        onView(withId(R.id.dialog_email_checkbox_zip)).perform(click())
//        onView(withId(R.id.dialog_email_checkbox_zip_with_metadata)).perform(click())
//
//        // Tap on the generate button
//        onView(withId(R.id.fab)).perform(click())
//
//        // Verify the toast was displayed
//        onView(withText(R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS)).inRoot(withDecorView(not(activity.window.decorView))).check(matches(isDisplayed()))
//    }

//    @Test
//    fun createTripNoReportSelectedError() {
//        // Create our trip
//        createReport("Empty Report None Selected Error")
//
//        // Go to generate screen
//        onView(withContentDescription(R.string.report_info_generate)).perform(click())
//
//        // Wait to ensure everything loads
//        onView(isRoot()).perform(waitForView(R.id.dialog_email_checkbox_pdf_full, 20000))
//
//        // Tap on the generate button
//        onView(withId(R.id.fab)).perform(click())
//
//        // Verify the toast was displayed
//        onView(withText(R.string.DIALOG_EMAIL_TOAST_NO_SELECTION)).inRoot(withDecorView(not(activity.window.decorView))).check(matches(isDisplayed()))
//    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        Intents.release()
    }
}
