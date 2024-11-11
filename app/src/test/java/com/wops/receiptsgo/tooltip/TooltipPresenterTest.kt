package com.wops.receiptsgo.tooltip

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.smartreceipts.analytics.Analytics
import com.wops.receiptsgo.R
import com.wops.receiptsgo.tooltip.model.TooltipInteraction
import com.wops.receiptsgo.tooltip.model.TooltipMetadata
import com.wops.receiptsgo.tooltip.model.TooltipType
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TooltipPresenterTest {

    companion object {
        private val RATE_THIS_APP_TOOLTIP_METADATA = TooltipMetadata(TooltipType.RateThisApp, ApplicationProvider.getApplicationContext<Context>().getString(R.string.rating_tooltip_text))
        private val PRIVACY_POLICY_TOOLTIP_METADATA = TooltipMetadata(TooltipType.PrivacyPolicy, ApplicationProvider.getApplicationContext<Context>().getString(R.string.tooltip_review_privacy))
    }

    lateinit var tooltipPresenter: TooltipPresenter

    @Mock
    lateinit var view: TooltipView

    @Mock
    lateinit var tooltipControllerProvider: TooltipControllerProvider

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var rateThisAppController: TooltipController

    @Mock
    lateinit var privacyPolicyController: TooltipController

    @Mock
    lateinit var rateThisAppTooltipInteractionConsumer: Consumer<TooltipInteraction>

    @Mock
    lateinit var privacyPolicyTooltipInteractionConsumer: Consumer<TooltipInteraction>

    private val tooltipClickStream = PublishSubject.create<Unit>()
    private val buttonNoClickStream = PublishSubject.create<Unit>()
    private val buttonYesClickStream = PublishSubject.create<Unit>()
    private val buttonCancelClickStream = PublishSubject.create<Unit>()
    private val closeIconClickStream = PublishSubject.create<Unit>()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(tooltipControllerProvider.get(TooltipType.RateThisApp)).thenReturn(rateThisAppController)
        whenever(tooltipControllerProvider.get(TooltipType.PrivacyPolicy)).thenReturn(privacyPolicyController)
        whenever(rateThisAppController.shouldDisplayTooltip()).thenReturn(Single.just(Optional.absent()))
        whenever(privacyPolicyController.shouldDisplayTooltip()).thenReturn(Single.just(Optional.absent()))
        whenever(rateThisAppController.handleTooltipInteraction(any())).thenReturn(Completable.complete())
        whenever(privacyPolicyController.handleTooltipInteraction(any())).thenReturn(Completable.complete())
        whenever(rateThisAppController.consumeTooltipInteraction()).thenReturn(rateThisAppTooltipInteractionConsumer)
        whenever(privacyPolicyController.consumeTooltipInteraction()).thenReturn(privacyPolicyTooltipInteractionConsumer)

        whenever(view.getSupportedTooltips()).thenReturn(arrayListOf(TooltipType.RateThisApp, TooltipType.PrivacyPolicy))
        whenever(view.getTooltipClickStream()).thenReturn(tooltipClickStream)
        whenever(view.getButtonNoClickStream()).thenReturn(buttonNoClickStream)
        whenever(view.getButtonYesClickStream()).thenReturn(buttonYesClickStream)
        whenever(view.getButtonCancelClickStream()).thenReturn(buttonCancelClickStream)
        whenever(view.getCloseIconClickStream()).thenReturn(closeIconClickStream)
        tooltipPresenter = TooltipPresenter(view, tooltipControllerProvider, analytics, Schedulers.trampoline())
    }

    @Test
    fun clicksAreIgnoredWhenNoTooltipsAreSupported() {
        whenever(view.getSupportedTooltips()).thenReturn(emptyList())
        tooltipPresenter.subscribe()
        verify(view, never()).display(any())
        tooltipClickStream.onNext(Unit)
        buttonNoClickStream.onNext(Unit)
        buttonYesClickStream.onNext(Unit)
        buttonCancelClickStream.onNext(Unit)
        closeIconClickStream.onNext(Unit)
        verifyZeroInteractions(privacyPolicyTooltipInteractionConsumer, rateThisAppTooltipInteractionConsumer)
    }

    @Test
    fun clicksAreIgnoredWhenNoTooltipIsDisplayed() {
        tooltipPresenter.subscribe()
        verify(view, never()).display(any())
        tooltipClickStream.onNext(Unit)
        buttonNoClickStream.onNext(Unit)
        buttonYesClickStream.onNext(Unit)
        buttonCancelClickStream.onNext(Unit)
        closeIconClickStream.onNext(Unit)
        verifyZeroInteractions(privacyPolicyTooltipInteractionConsumer, rateThisAppTooltipInteractionConsumer)
    }

    @Test
    fun clicksAreHandledWhenThePrivacyTooltipIsShown() {
        whenever(privacyPolicyController.shouldDisplayTooltip()).thenReturn(Single.just(Optional.of(PRIVACY_POLICY_TOOLTIP_METADATA)))
        tooltipPresenter.subscribe()
        verify(view).display(PRIVACY_POLICY_TOOLTIP_METADATA)
        tooltipClickStream.onNext(Unit)
        buttonNoClickStream.onNext(Unit)
        buttonYesClickStream.onNext(Unit)
        buttonCancelClickStream.onNext(Unit)
        closeIconClickStream.onNext(Unit)
        verify(privacyPolicyTooltipInteractionConsumer).accept(TooltipInteraction.TooltipClick)
        verify(privacyPolicyTooltipInteractionConsumer).accept(TooltipInteraction.NoButtonClick)
        verify(privacyPolicyTooltipInteractionConsumer).accept(TooltipInteraction.YesButtonClick)
        verify(privacyPolicyTooltipInteractionConsumer, times(2)).accept(TooltipInteraction.CloseCancelButtonClick)
        verifyZeroInteractions(rateThisAppTooltipInteractionConsumer)
    }

    @Test
    fun clicksAreHandledWhenTheRateTooltipIsShown() {
        whenever(rateThisAppController.shouldDisplayTooltip()).thenReturn(Single.just(Optional.of<TooltipMetadata>(RATE_THIS_APP_TOOLTIP_METADATA)))
        tooltipPresenter.subscribe()
        verify(view).display(RATE_THIS_APP_TOOLTIP_METADATA)
        tooltipClickStream.onNext(Unit)
        buttonNoClickStream.onNext(Unit)
        buttonYesClickStream.onNext(Unit)
        buttonCancelClickStream.onNext(Unit)
        closeIconClickStream.onNext(Unit)
        verify(rateThisAppTooltipInteractionConsumer).accept(TooltipInteraction.TooltipClick)
        verify(rateThisAppTooltipInteractionConsumer).accept(TooltipInteraction.NoButtonClick)
        verify(rateThisAppTooltipInteractionConsumer).accept(TooltipInteraction.YesButtonClick)
        verify(rateThisAppTooltipInteractionConsumer, times(2)).accept(TooltipInteraction.CloseCancelButtonClick)
        verifyZeroInteractions(privacyPolicyTooltipInteractionConsumer)
    }

    @Test
    fun verifyTheHigherPriorityTooltipWins() {
        whenever(rateThisAppController.shouldDisplayTooltip()).thenReturn(Single.just(Optional.of<TooltipMetadata>(RATE_THIS_APP_TOOLTIP_METADATA)))
        whenever(privacyPolicyController.shouldDisplayTooltip()).thenReturn(Single.just(Optional.of<TooltipMetadata>(PRIVACY_POLICY_TOOLTIP_METADATA)))
        tooltipPresenter.subscribe()
        verify(view).display(RATE_THIS_APP_TOOLTIP_METADATA)
    }

}