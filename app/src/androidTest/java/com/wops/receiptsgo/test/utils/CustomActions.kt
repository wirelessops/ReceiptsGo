package com.wops.receiptsgo.test.utils

import android.view.View
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import java.util.concurrent.TimeoutException

class CustomActions {

    companion object {

        fun withIndex(matcher: Matcher<View?>, index: Int): Matcher<View?>? {
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

        /**
         * This ViewAction tells espresso to wait till a certain view is found in the view hierarchy.
         * @param viewId The id of the view to wait for.
         * @param timeout The maximum time which espresso will wait for the view to show up (in milliseconds)
         */
        fun waitForView(viewId: Int, timeout: Long): ViewAction {
            return object : ViewAction {
                override fun getConstraints(): Matcher<View> {
                    return isRoot()
                }

                override fun getDescription(): String {
                    return "wait for a specific view with id $viewId; during $timeout millis."
                }

                override fun perform(uiController: UiController, rootView: View) {
                    uiController.loopMainThreadUntilIdle()
                    val startTime = System.currentTimeMillis()
                    val endTime = startTime + timeout
                    val viewMatcher = withId(viewId)

                    do {
                        // Iterate through all views on the screen and see if the view we are looking for is there already
                        for (child in TreeIterables.breadthFirstViewTraversal(rootView)) {
                            // found view with required ID
                            if (viewMatcher.matches(child)) {
                                return
                            }
                        }
                        // Loops the main thread for a specified period of time.
                        // Control may not return immediately, instead it'll return after the provided delay has passed and the queue is in an idle state again.
                        uiController.loopMainThreadForAtLeast(100)
                    } while (System.currentTimeMillis() < endTime) // in case of a timeout we throw an exception -&gt; test fails
                    throw PerformException.Builder()
                            .withCause(TimeoutException())
                            .withActionDescription(this.description)
                            .withViewDescription(HumanReadables.describe(rootView))
                            .build()
                }
            }
        }
    }

}