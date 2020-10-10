package co.smartreceipts.android.test.espresso

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import co.smartreceipts.android.R
import co.smartreceipts.android.activities.SmartReceiptsActivity
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaseEspressoTests {

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(SmartReceiptsActivity::class.java)

    @Test
    fun launchTripEditor() {
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
        onView(withId(R.id.action_save)).perform(click())
    }

    @Test
    fun launchTripEditorAndCreateTestTripAndNavigateBackToTheTripPage() {
        launchTripEditor()

        // Create a trip, entitled "Test2"
        onView(withId(R.id.dialog_tripmenu_name)).perform(replaceText("Test2"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Up Button Navigation
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Verify that we have a list item with Test2
        onView(withId(R.id.title)).check(matches(withText("Test2")))
    }

    @Test
    fun launchTripEditorCreateTripAddTextOnlyReceipt() {
        launchTripEditor()

        // Create a trip, entitled "Test3"
        onView(withId(R.id.dialog_tripmenu_name)).perform(replaceText("Test3"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

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
        onView(withId(R.id.action_save)).perform(click())

        // Wait a second to ensure that everything loaded
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Verify that we have a list item with Test Receipt
        onView(withId(R.id.title)).check(matches(withText("Test Receipt")))
    }

}
