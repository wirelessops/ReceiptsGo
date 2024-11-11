package com.wops.receiptsgo.tooltip.rating

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.SmartReceiptsActivity
import com.wops.receiptsgo.rating.FeedbackDialogFragment
import com.wops.receiptsgo.rating.RatingDialogFragment
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RateThisAppTooltipRouterTest {

    lateinit var router: RateThisAppTooltipRouter

    @Mock
    lateinit var navigationHandler: NavigationHandler<SmartReceiptsActivity>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        router = RateThisAppTooltipRouter(navigationHandler)
    }

    @Test
    fun navigateToFeedbackOptions() {
        router.navigateToFeedbackOptions()
        verify(navigationHandler).showDialog(any<FeedbackDialogFragment>())
    }

    @Test
    fun navigateToRatingOptions() {
        router.navigateToRatingOptions()
        verify(navigationHandler).showDialog(any<RatingDialogFragment>())
    }

}