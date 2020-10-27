package co.smartreceipts.android.test.espresso

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
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
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class BaseEspressoTests {

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(SmartReceiptsActivity::class.java)

    private lateinit var databaseHelper: DatabaseHelper

    @Before
    fun setUp() {
        Awaitility.setDefaultPollDelay(Duration.ofSeconds(10))
        Awaitility.setDefaultTimeout(Duration.ofSeconds(60))

        val application = activityTestRule.activity.application as SmartReceiptsApplication
        databaseHelper = application.databaseHelper
    }

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

        // Wait a second to ensure the keyboard closed
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Save the trip
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
    fun launchTripEditorAndCreateTestTripAndNavigateBackToTheTripPage() {
        launchTripEditor()

        // Create a trip, entitled "Test2"
        onView(withId(R.id.dialog_tripmenu_name)).perform(replaceText("Test2"), closeSoftKeyboard())

        // Wait a second to ensure the keyboard closed
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))

        // Save the trip
        onView(withId(R.id.action_save)).perform(click())

        // Wait until everything loads
        await.untilCallTo {
            databaseHelper.tripsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

        // Verify that we have an empty report
        onView(withIndex(withId(R.id.no_data), 0)).check(matches(withText(R.string.receipt_no_data)))

        // Up Button Navigation
        Espresso.pressBack()

        // Wait until everything loads
        await.untilCallTo {
            databaseHelper.tripsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

        // Verify that we have a list item with Test2
        onView(withId(R.id.title)).check(matches(withText("Test2")))
//        onView(withIndex(withId(R.id.title), 0)).check(matches(withText("Test2")))
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
        await.untilCallTo {
            databaseHelper.tripsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

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
        await.untilCallTo {
            databaseHelper.receiptsTable.blocking
        } matches {
            mutableList -> mutableList!!.size == 1
        }

        // Verify that we have a list item with Test Receipt
        onView(withId(R.id.title)).check(matches(withText("Test Receipt")))
//        onView(withIndex(withId(R.id.title), 0)).check(matches(withText("Test Receipt")))
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
