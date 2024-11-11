package com.wops.receiptsgo.test.espresso

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.wops.receiptsgo.R
import com.wops.receiptsgo.ReceiptsGoApplication
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.receiptsgo.persistence.DatabaseHelper
import com.wops.receiptsgo.test.utils.CustomActions.Companion.waitForView
import com.wops.receiptsgo.test.utils.CustomActions.Companion.withIndex
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaseEspressoTests {

    @Rule
    @JvmField
    val activityScenarioRule = ActivityScenarioRule(ReceiptsGoActivity::class.java)

    private lateinit var activity: ReceiptsGoActivity
    private lateinit var application: ReceiptsGoApplication
    private lateinit var databaseHelper: DatabaseHelper

    @Before
    fun setUp() {
        Intents.init()
        activityScenarioRule.scenario.onActivity { activity ->
            this.activity = activity
            application = activity.application as ReceiptsGoApplication
            databaseHelper = application.databaseHelper
        }
    }

    private fun launchTripEditor() {
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
        onView(withId(R.id.trip_comment)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.cost_center)).check(matches(not(isDisplayed())))
    }

    @Test
    fun launchTripEditorAndCreateTestTrip() {
        launchTripEditor()

        // Create a trip, entitled "Test"
        onView(withId(R.id.name)).perform(replaceText("Test"), closeSoftKeyboard())

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

    @Test
    fun launchTripEditorAndCreateTestTripAndNavigateBackToTheTripPage() {
        launchTripEditor()

        // Create a trip, entitled "Test2"
        onView(withId(R.id.name)).perform(replaceText("Test2"), closeSoftKeyboard())

        // Wait a second to ensure the keyboard closed
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Save the trip
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        onView(isRoot()).perform(waitForView(R.id.bottom_app_bar, 20000))
        onView(isRoot()).perform(waitForView(R.id.no_data, 20000))

        // Verify that we have an empty report
        onView(withIndex(withId(R.id.no_data_text), 0)).check(matches(withText(R.string.receipt_no_data)))

        // Up Button Navigation
        Espresso.pressBack()

        // Wait until everything loads
        onView(isRoot()).perform(waitForView(R.id.trip_action_new, 20000))
        onView(isRoot()).perform(waitForView(R.id.text_name, 20000))

        // Verify that we have a list item with Test2
        onView(withId(R.id.text_name)).check(matches(withText("Test2")))
    }

    @Test
    fun launchTripEditorCreateTripAddTextOnlyReceipt() {
        launchTripEditor()

        // Create a trip, entitled "Test3"
        onView(withId(R.id.name)).perform(replaceText("Test3"), closeSoftKeyboard())

        // Wait a second to ensure the keyboard closed
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Save the trip
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
//        onView(isRoot()).perform(waitForView(R.id.bottom_app_bar, 20000))
        onView(isRoot()).perform(waitForView(R.id.no_data_text, 20000))

        // Verify that we have an empty report
        onView(withIndex(withId(R.id.no_data_text), 0)).check(matches(withText(R.string.receipt_no_data)))

        // Tap on + fab button
        onView(withId(R.id.fab)).perform(click())

        // Click on "text only" button on the bottomSheetDialog
        onView(withId(R.id.new_text)).perform(click())

        // Close the keyboard
        Espresso.closeSoftKeyboard()

        // Verify that all the relevant views are displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_name)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_price)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_date)).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_comment)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_expensable)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_fullpage)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_currency)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_category)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.receipt_tax1)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_exchange_rate)).check(matches(not(isDisplayed())))
        onView(withId(R.id.receipt_exchanged_result)).check(matches(not(isDisplayed())))
        //todo following view doesn't apply to fire department variant, find a way to test variants
//        onView(withId(R.id.receipt_input_payment_method)).check(matches(not(isDisplayed())))

        // Create a receipt, entitled "Test" priced at $12.34
        onView(withId(R.id.receipt_name)).perform(scrollTo(), replaceText("Test Receipt"))
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
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        Intents.release()
    }

}
