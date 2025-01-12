package com.wops.receiptsgo.tooltip.rating

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.wops.analytics.Analytics
import com.wops.analytics.events.Events
import com.wops.receiptsgo.R
import com.wops.receiptsgo.rating.AppRatingManager
import com.wops.receiptsgo.tooltip.TooltipView
import com.wops.receiptsgo.tooltip.model.TooltipInteraction
import com.wops.receiptsgo.tooltip.model.TooltipMetadata
import com.wops.receiptsgo.tooltip.model.TooltipType
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RateThisAppTooltipControllerTest {

    companion object {
        private val TOOLTIP_METADATA = TooltipMetadata(TooltipType.RateThisApp, ApplicationProvider.getApplicationContext<Context>().getString(R.string.rating_tooltip_text))
    }
    
    lateinit var controller: RateThisAppTooltipController
    
    @Mock
    lateinit var tooltipView: TooltipView
    
    @Mock
    lateinit var router: RateThisAppTooltipRouter
    
    @Mock
    lateinit var appRatingManager: AppRatingManager
    
    @Mock
    lateinit var analytics: Analytics

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        controller = RateThisAppTooltipController(ApplicationProvider.getApplicationContext(), tooltipView, router, appRatingManager, analytics)
    }

    @Test
    fun displayTooltipWhenWeShouldAskForRating() {
        whenever(appRatingManager.checkIfNeedToAskRating()).thenReturn(Single.just(true))
        controller.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.of(TOOLTIP_METADATA))
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