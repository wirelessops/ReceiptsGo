package co.smartreceipts.android.tooltip.rating

import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.rating.FeedbackDialogFragment
import co.smartreceipts.android.rating.RatingDialogFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import com.nhaarman.mockito_kotlin.*
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
        verify(navigationHandler).showDialog(any(FeedbackDialogFragment::class.java))
    }

    @Test
    fun navigateToRatingOptions() {
        router.navigateToRatingOptions()
        verify(navigationHandler).showDialog(any(RatingDialogFragment::class.java))
    }

}