package co.smartreceipts.android.tooltip.rating

import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.rating.AppRatingManager
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import com.hadisatrio.optional.Optional
import io.reactivex.Single
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import com.nhaarman.mockito_kotlin.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppRatingTooltipControllerTest {
    
    lateinit var controller: AppRatingTooltipController
    
    @Mock
    lateinit var tooltipView: TooltipView
    
    @Mock
    lateinit var router: AppRatingTooltipRouter
    
    @Mock
    lateinit var appRatingManager: AppRatingManager
    
    @Mock
    lateinit var analytics: Analytics

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        controller = AppRatingTooltipController(tooltipView, router, appRatingManager, analytics)
    }

    @Test
    fun displayTooltipWhenWeShouldAskForRating() {
        whenever(appRatingManager.checkIfNeedToAskRating()).thenReturn(Single.just(true))
        controller.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.of(TooltipType.RateThisApp))
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWhenWeShouldNotAskForRating() {
        whenever(appRatingManager.checkIfNeedToAskRating()).thenReturn(Single.just(false))
        controller.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.absent())
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun handleTooltipInteractionForTooltipClick() {
        val interaction = TooltipInteraction.TooltipClick
        controller.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verifyZeroInteractions(analytics, appRatingManager)
    }

    @Test
    fun handleTooltipInteractionForCloseCancelClick() {
        val interaction = TooltipInteraction.CloseCancelButtonClick
        controller.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verifyZeroInteractions(analytics, appRatingManager)
    }

    @Test
    fun handleTooltipInteractionForYesClick() {
        val interaction = TooltipInteraction.YesButtonClick
        controller.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(appRatingManager).dontShowRatingPromptAgain()
        verify(analytics).record(Events.Ratings.UserAcceptedRatingPrompt)
    }

    @Test
    fun handleTooltipInteractionForNoClick() {
        val interaction = TooltipInteraction.NoButtonClick
        controller.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(appRatingManager).dontShowRatingPromptAgain()
        verify(analytics).record(Events.Ratings.UserDeclinedRatingPrompt)
    }

    @Test
    fun consumeYesClickInteraction() {
        controller.consumeTooltipInteraction().accept(TooltipInteraction.YesButtonClick)
        Mockito.verify(tooltipView).hideTooltip()
        Mockito.verify(router).navigateToRatingOptions()
    }

    @Test
    fun consumeNoInteraction() {
        controller.consumeTooltipInteraction().accept(TooltipInteraction.NoButtonClick)
        Mockito.verify(tooltipView).hideTooltip()
        Mockito.verify(router).navigateToFeedbackOptions()
    }

    @Test
    fun consumeTooltipClickInteraction() {
        controller.consumeTooltipInteraction().accept(TooltipInteraction.TooltipClick)
        verifyZeroInteractions(tooltipView, router)
    }

    @Test
    fun consumeCloseClickInteraction() {
        controller.consumeTooltipInteraction().accept(TooltipInteraction.CloseCancelButtonClick)
        verifyZeroInteractions(tooltipView, router)
    }

}