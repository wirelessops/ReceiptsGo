package com.wops.receiptsgo.tooltip.rating

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.core.di.scopes.FragmentScope
import com.wops.receiptsgo.rating.FeedbackDialogFragment
import com.wops.receiptsgo.rating.RatingDialogFragment
import javax.inject.Inject

@FragmentScope
class RateThisAppTooltipRouter @Inject constructor(private val navigationHandler: NavigationHandler<ReceiptsGoActivity>) {

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
