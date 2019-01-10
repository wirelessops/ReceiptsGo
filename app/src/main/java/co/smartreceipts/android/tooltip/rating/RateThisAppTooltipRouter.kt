package co.smartreceipts.android.tooltip.rating

import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.rating.FeedbackDialogFragment
import co.smartreceipts.android.rating.RatingDialogFragment
import javax.inject.Inject

@FragmentScope
class RateThisAppTooltipRouter @Inject constructor(private val navigationHandler: NavigationHandler<SmartReceiptsActivity>) {

    /**
     * Routes us to the feedback screen
     */
    fun navigateToFeedbackOptions() {
        navigationHandler.showDialog(FeedbackDialogFragment())
    }

    /**
     * Navigates to the rating options
     */
    fun navigateToRatingOptions() {
        navigationHandler.showDialog(RatingDialogFragment())
    }
}
