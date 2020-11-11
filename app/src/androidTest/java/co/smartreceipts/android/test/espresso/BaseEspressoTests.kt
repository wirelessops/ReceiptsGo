package co.smartreceipts.android.test.espresso

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaseEspressoTests {

    @Rule
    @JvmField
    val activityScenarioRule = ActivityScenarioRule(SmartReceiptsActivity::class.java)

    private lateinit var activity: SmartReceiptsActivity
    private lateinit var application: SmartReceiptsApplication
    private lateinit var databaseHelper: DatabaseHelper

    @Before
    fun setUp() {
        Intents.init()
        activityScenarioRule.scenario.onActivity { activity ->
            this.activity = activity
            application = activity.application as SmartReceiptsApplication
            databaseHelper = application.databaseHelper
        }
    }

    private fun launchTripEditor() {
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
    }

    @Test
    fun launchTripEditorAndCreateTestTrip() {
        launchTripEditor()

        // Create a trip, entitled "Test"
        onView(withId(R.id.dialog_tripmenu_name)).perform(replaceText("Test"), closeSoftKeyboard())

        // Wait a second to ensure the keyboard closed
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Save the trip
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        onView(isRoot()).perform(waitForView(R.id.fab_menu, 10000))

        // Verify that we have an empty report
        onView(withIndex(withId(R.id.no_data), 0)).check(matches(withText(R.string.receipt_no_data)))
    }

    @Test
    fun launchTripEditorAndCreateTestTripAndNavigateBackToTheTripPage() {
        launchTripEditor()

        // Create a trip, entitled "Test2"
        onView(withId(R.id.dialog_tripmenu_name)).perform(replaceText("Test2"), closeSoftKeyboard())

        // Wait a second to ensure the keyboard closed
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Save the trip
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        onView(isRoot()).perform(waitForView(R.id.fab_menu, 10000))

        // Verify that we have an empty report
        onView(withIndex(withId(R.id.no_data), 0)).check(matches(withText(R.string.receipt_no_data)))

        // Up Button Navigation
        Espresso.pressBack()

        // Wait until everything loads
        onView(isRoot()).perform(waitForView(R.id.trip_action_new, 10000))
        Thread.sleep(TimeUnit.SECONDS.toMillis(5))

        // Verify that we have a list item with Test2
        onView(withId(R.id.title)).check(matches(withText("Test2")))
    }

    @Test
    fun launchTripEditorCreateTripAddTextOnlyReceipt() {
        launchTripEditor()

        // Create a trip, entitled "Test3"
        onView(withId(R.id.dialog_tripmenu_name)).perform(replaceText("Test3"), closeSoftKeyboard())

        // Wait a second to ensure the keyboard closed
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Save the trip
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        onView(isRoot()).perform(waitForView(R.id.fab_menu, 10000))

        // Verify that we have an empty report
        onView(withIndex(withId(R.id.no_data), 0)).check(matches(withText(R.string.receipt_no_data)))

        // Open the fab menu (specific to our clans fab library)
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

        // Wait a second to ensure the keyboard closed
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Save the receipt
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        onView(isRoot()).perform(waitForView(R.id.title, 10000))

        // Verify that we have a list item with Test Receipt
        onView(withId(R.id.title)).check(matches(withText("Test Receipt")))
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        Intents.release()
    }

}
