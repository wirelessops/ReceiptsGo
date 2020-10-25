package co.smartreceipts.android.test.espresso

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import co.smartreceipts.android.R
import co.smartreceipts.android.SmartReceiptsApplication
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.persistence.DatabaseHelper
import org.awaitility.Awaitility
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReportGenerationTests {

    @get:Rule
    val mIntentsRule = IntentsTestRule(SmartReceiptsActivity::class.java)

    private lateinit var databaseHelper: DatabaseHelper
    private var authority: String = ""

    @Before
    fun setUp() {
        Awaitility.setDefaultPollDelay(Duration.ofSeconds(10))
        Awaitility.setDefaultTimeout(Duration.ofSeconds(60))

        val application = mIntentsRule.activity.application as SmartReceiptsApplication
        databaseHelper = application.databaseHelper

        authority = String.format(Locale.US, "%s.fileprovider", InstrumentationRegistry.getInstrumentation().targetContext.packageName)

        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        intending(not(isInternal())).respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    private fun createReport(reportName: String) {
        // Click on the "new report" button
        onView(withId(R.id.trip_action_new)).perform(click())

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_name)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_start)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_end)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_currency)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_comment)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_cost_center)).check(matches(not(isDisplayed())))

        // Create a trip with the passed report name
        onView(withId(R.id.dialog_tripmenu_name)).perform(replaceText(reportName), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        await.untilCallTo {
            databaseHelper.tripsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

        // Verify that we have an empty report
        onView(withIndex(withId(R.id.no_data), 0)).check(matches(withText(R.string.receipt_no_data)))
    }

    @Test
    fun createTripAddReceiptGeneratePDF() {
        val uri = Uri.parse("content://$authority/public-files-path/PDF Report/PDF Report.pdf".replace(" ", "%20"))

        // Create our trip
        createReport("PDF Report")

        // Open the fab menu
        onView(allOf(withParent(withId(R.id.fab_menu)), withClassName(endsWith("ImageView")), isDisplayed())).perform(click())

        // Click on "text only" button
        onView(withId(R.id.receipt_action_text)).perform(click())

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_DATE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_COMMENT)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_EXPENSABLE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_FULLPAGE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CURRENCY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CATEGORY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_TAX1)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchange_rate)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchanged_result)).check(matches(not(isDisplayed())))
        //todo following view doesn't apply to fire department variant, find a way to test variants
//        onView(withId(R.id.receipt_input_payment_method)).check(matches(not(isDisplayed())))

        // Create a receipt, entitled "Test" priced at $12.34
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).perform(replaceText("Test Receipt"))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).perform(replaceText("12.34"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        await.untilCallTo {
            databaseHelper.receiptsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

        // Verify that we have a list item with Test Receipt
        onView(withId(R.id.title)).check(matches(withText("Test Receipt")))
//        onView(withIndex(withId(R.id.title), 0)).check(matches(withText("Test Receipt")))

        // Go to generate screen
        onView(withText(R.string.report_info_generate)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Check the box for Full PDF Report
        onView(withId(R.id.dialog_email_checkbox_pdf_full)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.receipt_action_send)).perform(click())

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

        // Open the fab menu
        onView(allOf(withParent(withId(R.id.fab_menu)), withClassName(endsWith("ImageView")), isDisplayed())).perform(click())

        // Click on "text only" button
        onView(withId(R.id.receipt_action_text)).perform(click())

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_DATE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_COMMENT)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_EXPENSABLE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_FULLPAGE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CURRENCY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CATEGORY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_TAX1)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchange_rate)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchanged_result)).check(matches(not(isDisplayed())))
        //todo following view doesn't apply to fire department variant, find a way to test variants
//        onView(withId(R.id.receipt_input_payment_method)).check(matches(not(isDisplayed())))

        // Create a receipt, entitled "Test" priced at $12.34
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).perform(replaceText("Test Receipt"))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).perform(replaceText("12.34"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        await.untilCallTo {
            databaseHelper.receiptsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

        // Verify that we have a list item with Test Receipt
        onView(withId(R.id.title)).check(matches(withText("Test Receipt")))
//        onView(withIndex(withId(R.id.title), 0)).check(matches(withText("Test Receipt")))

        // Go to generate screen
        onView(withText(R.string.report_info_generate)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Check the box for PDF Report - No Table
        onView(withId(R.id.dialog_email_checkbox_pdf_images)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.receipt_action_send)).perform(click())

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

        // Open the fab menu
        onView(allOf(withParent(withId(R.id.fab_menu)), withClassName(endsWith("ImageView")), isDisplayed())).perform(click())

        // Click on "text only" button
        onView(withId(R.id.receipt_action_text)).perform(click())

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_DATE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_COMMENT)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_EXPENSABLE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_FULLPAGE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CURRENCY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CATEGORY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_TAX1)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchange_rate)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchanged_result)).check(matches(not(isDisplayed())))
        //todo following view doesn't apply to fire department variant, find a way to test variants
//        onView(withId(R.id.receipt_input_payment_method)).check(matches(not(isDisplayed())))

        // Create a receipt, entitled "Test" priced at $12.34
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).perform(replaceText("Test Receipt"))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).perform(replaceText("12.34"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        await.untilCallTo {
            databaseHelper.receiptsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

        // Verify that we have a list item with Test Receipt
        onView(withId(R.id.title)).check(matches(withText("Test Receipt")))
//        onView(withIndex(withId(R.id.title), 0)).check(matches(withText("Test Receipt")))

        // Go to generate screen
        onView(withText(R.string.report_info_generate)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Check the box for CSV File
        onView(withId(R.id.dialog_email_checkbox_csv)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.receipt_action_send)).perform(click())

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

        // Open the fab menu
        onView(allOf(withParent(withId(R.id.fab_menu)), withClassName(endsWith("ImageView")), isDisplayed())).perform(click())

        // Click on "text only" button
        onView(withId(R.id.receipt_action_text)).perform(click())

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_DATE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_COMMENT)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_EXPENSABLE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_FULLPAGE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CURRENCY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CATEGORY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_TAX1)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchange_rate)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchanged_result)).check(matches(not(isDisplayed())))
        //todo following view doesn't apply to fire department variant, find a way to test variants
//        onView(withId(R.id.receipt_input_payment_method)).check(matches(not(isDisplayed())))

        // Create a receipt, entitled "Test" priced at $12.34
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).perform(replaceText("Test Receipt"))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).perform(replaceText("12.34"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        await.untilCallTo {
            databaseHelper.receiptsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

        // Verify that we have a list item with Test Receipt
        onView(withId(R.id.title)).check(matches(withText("Test Receipt")))
//        onView(withIndex(withId(R.id.title), 0)).check(matches(withText("Test Receipt")))

        // Go to generate screen
        onView(withText(R.string.report_info_generate)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Check the box for Zip - Receipts Files
        onView(withId(R.id.dialog_email_checkbox_zip)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.receipt_action_send)).perform(click())

        // Verify the intent chooser with a Zip file was displayed
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            intended(allOf(
                    hasAction(Intent.ACTION_CHOOSER),
                    hasExtra(`is`(Intent.EXTRA_INTENT),
                            allOf(
                                    hasAction(Intent.ACTION_SEND_MULTIPLE),
                                    hasType("application/octet-stream"),
                                    hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                                    hasExtra(Intent.EXTRA_STREAM, ArrayList<Uri>())
                            )
                    )
            ))
        } else {
            intended(allOf(
                    hasAction(Intent.ACTION_CHOOSER),
                    hasExtra(`is`(Intent.EXTRA_INTENT),
                            allOf(
                                    hasAction(Intent.ACTION_SEND_MULTIPLE),
                                    hasType("application/zip"),
                                    hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            )
                    ),
                    hasExtra(Intent.EXTRA_TITLE, mIntentsRule.activity.resources.getString(R.string.send_email))
            ))
        }
    }

    @Test
    fun createTripAddReceiptGenerateZipWithMetadata() {
        // Create our trip
        createReport("Zip Report with Metadata")

        // Open the fab menu
        onView(allOf(withParent(withId(R.id.fab_menu)), withClassName(endsWith("ImageView")), isDisplayed())).perform(click())

        // Click on "text only" button
        onView(withId(R.id.receipt_action_text)).perform(click())

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_DATE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_COMMENT)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_EXPENSABLE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_FULLPAGE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CURRENCY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CATEGORY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_TAX1)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchange_rate)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchanged_result)).check(matches(not(isDisplayed())))
        //todo following view doesn't apply to fire department variant, find a way to test variants
//        onView(withId(R.id.receipt_input_payment_method)).check(matches(not(isDisplayed())))

        // Create a receipt, entitled "Test" priced at $12.34
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).perform(replaceText("Test Receipt"))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).perform(replaceText("12.34"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        await.untilCallTo {
            databaseHelper.receiptsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

        // Verify that we have a list item with Test Receipt
        onView(withId(R.id.title)).check(matches(withText("Test Receipt")))
//        onView(withIndex(withId(R.id.title), 0)).check(matches(withText("Test Receipt")))

        // Go to generate screen
        onView(withText(R.string.report_info_generate)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Check the box for Zip - Receipt Files with Metadata
        onView(withId(R.id.dialog_email_checkbox_zip_with_metadata)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.receipt_action_send)).perform(click())

        // give app time to generate files and display intent chooser


        // Verify the intent chooser with a Zip file was displayed
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            intended(allOf(
                    hasAction(Intent.ACTION_CHOOSER),
                    hasExtra(`is`(Intent.EXTRA_INTENT),
                            allOf(
                                    hasAction(Intent.ACTION_SEND_MULTIPLE),
                                    hasType("application/octet-stream"),
                                    hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                                    hasExtra(Intent.EXTRA_STREAM, ArrayList<Uri>())
                            )
                    )
            ))
        } else {
            intended(allOf(
                    hasAction(Intent.ACTION_CHOOSER),
                    hasExtra(`is`(Intent.EXTRA_INTENT),
                            allOf(
                                    hasAction(Intent.ACTION_SEND_MULTIPLE),
                                    hasType("application/zip"),
                                    hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            )
                    ),
                    hasExtra(Intent.EXTRA_TITLE, mIntentsRule.activity.resources.getString(R.string.send_email))
            ))
        }
    }

    @Test
    fun createTripAddReceiptGenerateAllReports() {
        val uri = Uri.parse("content://$authority/public-files-path/All File Type Report/All File Type Report.pdf".replace(" ", "%20"))
        val uri1 = Uri.parse("content://$authority/public-files-path/All File Type Report/All File Type ReportImages.pdf".replace(" ", "%20"))
        val uri2 = Uri.parse("content://$authority/public-files-path/All File Type Report/All File Type Report.csv".replace(" ", "%20"))

        // Create our trip
        createReport("All File Type Report")

        // Open the fab menu
        onView(allOf(withParent(withId(R.id.fab_menu)), withClassName(endsWith("ImageView")), isDisplayed())).perform(click())

        // Click on "text only" button
        onView(withId(R.id.receipt_action_text)).perform(click())

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_DATE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_COMMENT)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_EXPENSABLE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_FULLPAGE)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CURRENCY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_CATEGORY)).check(matches(isDisplayed()))
        onView(withId(R.id.DIALOG_RECEIPTMENU_TAX1)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchange_rate)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_input_exchanged_result)).check(matches(not(isDisplayed())))
        //todo following view doesn't apply to fire department variant, find a way to test variants
//        onView(withId(R.id.receipt_input_payment_method)).check(matches(not(isDisplayed())))

        // Create a receipt, entitled "Test" priced at $12.34
        onView(withId(R.id.DIALOG_RECEIPTMENU_NAME)).perform(replaceText("Test Receipt"))
        onView(withId(R.id.DIALOG_RECEIPTMENU_PRICE)).perform(replaceText("12.34"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        await.untilCallTo {
            databaseHelper.receiptsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

        // Verify that we have a list item with Test Receipt
        onView(withId(R.id.title)).check(matches(withText("Test Receipt")))
//        onView(withIndex(withId(R.id.title), 0)).check(matches(withText("Test Receipt")))

        // Go to generate screen
        onView(withText(R.string.report_info_generate)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Check all of the file type boxes
        onView(withId(R.id.dialog_email_checkbox_pdf_full)).perform(click())
        onView(withId(R.id.dialog_email_checkbox_pdf_images)).perform(click())
        onView(withId(R.id.dialog_email_checkbox_csv)).perform(click())
        onView(withId(R.id.dialog_email_checkbox_zip)).perform(click())
        onView(withId(R.id.dialog_email_checkbox_zip_with_metadata)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.receipt_action_send)).perform(click())

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
            intended(allOf(
                    hasAction(Intent.ACTION_CHOOSER),
                    hasExtra(`is`(Intent.EXTRA_INTENT),
                            allOf(
                                    hasAction(Intent.ACTION_SEND_MULTIPLE),
                                    hasType("application/octet-stream"),
                                    hasFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            )
                    ),
                    hasExtra(Intent.EXTRA_TITLE, mIntentsRule.activity.resources.getString(R.string.send_email))
            ))
        }
    }

    @Test
    fun createTripNoReceiptError() {
        // Create our trip
        createReport("Empty Report No Receipts Error")

        // Go to generate screen
        onView(withText(R.string.report_info_generate)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Check all of the file type boxes
        onView(withId(R.id.dialog_email_checkbox_pdf_full)).perform(click())
        onView(withId(R.id.dialog_email_checkbox_pdf_images)).perform(click())
        onView(withId(R.id.dialog_email_checkbox_csv)).perform(click())
        onView(withId(R.id.dialog_email_checkbox_zip)).perform(click())
        onView(withId(R.id.dialog_email_checkbox_zip_with_metadata)).perform(click())

        // Tap on the generate button
        onView(withId(R.id.receipt_action_send)).perform(click())

        // Verify the toast was displayed
        onView(withText(R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS)).inRoot(withDecorView(not(mIntentsRule.activity.window.decorView))).check(matches(isDisplayed()))
    }

    @Test
    fun createTripNoReportSelectedError() {
        // Create our trip
        createReport("Empty Report None Selected Error")

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Go to generate screen
        onView(withText(R.string.report_info_generate)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Tap on the generate button
        onView(withId(R.id.receipt_action_send)).perform(click())

        // Verify the toast was displayed
        onView(withText(R.string.DIALOG_EMAIL_TOAST_NO_SELECTION)).inRoot(withDecorView(not(mIntentsRule.activity.window.decorView))).check(matches(isDisplayed()))
    }

    private fun withIndex(matcher: Matcher<View?>, index: Int): Matcher<View?>? {
        return object : TypeSafeMatcher<View?>() {
            var currentIndex = 0
            override fun describeTo(description: Description) {
                description.appendText("with index: ")
                description.appendValue(index)
                matcher.describeTo(description)
            }

            override fun matchesSafely(view: View?): Boolean {
                return matcher.matches(view) && currentIndex++ == index
            }
        }
    }
}
