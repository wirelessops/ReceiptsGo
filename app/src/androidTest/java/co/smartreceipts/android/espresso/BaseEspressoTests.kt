package co.smartreceipts.android.espresso

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import co.smartreceipts.android.R
import co.smartreceipts.android.activities.SmartReceiptsActivity
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

        // Verify that all the relevant views our displayed
        onView(withId(R.id.action_save)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_name)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_start)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_end)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_currency)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_comment)).check(matches(isDisplayed()))
        onView(withId(R.id.dialog_tripmenu_cost_center)).check(matches(not<View>(isDisplayed())))
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

        // Create a trip, entitled "Test"
        onView(withId(R.id.dialog_tripmenu_name)).perform(replaceText("Test"), closeSoftKeyboard())
        onView(withId(R.id.action_save)).perform(click())

        // Up Button Navigation
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())

        // Verify that we have a list item with test
        onView(withId(android.R.id.title)).check(matches(withText("Test")))
    }

}
