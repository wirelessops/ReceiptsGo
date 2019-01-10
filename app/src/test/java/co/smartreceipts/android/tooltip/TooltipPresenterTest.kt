package co.smartreceipts.android.tooltip

import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import com.hadisatrio.optional.Optional
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TooltipPresenterTest {

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

    private val tooltipClickStream = PublishSubject.create<Any>()
    private val buttonNoClickStream = PublishSubject.create<Any>()
    private val buttonYesClickStream = PublishSubject.create<Any>()
    private val buttonCancelClickStream = PublishSubject.create<Any>()
    private val closeIconClickStream = PublishSubject.create<Any>()

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
        tooltipPresenter = TooltipPresenter(view, tooltipControllerProvider, analytics)
    }

    @Test
    fun clicksAreIgnoredWhenNoTooltipsAreSupported() {
        whenever(view.getSupportedTooltips()).thenReturn(emptyList())
        tooltipPresenter.subscribe()
        verify(view, never()).display(any())
        tooltipClickStream.onNext(Any())
        buttonNoClickStream.onNext(Any())
        buttonYesClickStream.onNext(Any())
        buttonCancelClickStream.onNext(Any())
        closeIconClickStream.onNext(Any())
        verifyZeroInteractions(privacyPolicyTooltipInteractionConsumer, rateThisAppTooltipInteractionConsumer)
    }

    @Test
    fun clicksAreIgnoredWhenNoTooltipIsDisplayed() {
        tooltipPresenter.subscribe()
        verify(view, never()).display(any())
        tooltipClickStream.onNext(Any())
        buttonNoClickStream.onNext(Any())
        buttonYesClickStream.onNext(Any())
        buttonCancelClickStream.onNext(Any())
        closeIconClickStream.onNext(Any())
        verifyZeroInteractions(privacyPolicyTooltipInteractionConsumer, rateThisAppTooltipInteractionConsumer)
    }

    @Test
    fun clicksAreHandledWhenThePrivacyTooltipIsShown() {
        whenever(privacyPolicyController.shouldDisplayTooltip()).thenReturn(Single.just(Optional.of(TooltipType.PrivacyPolicy)))
        tooltipPresenter.subscribe()
        verify(view).display(TooltipType.PrivacyPolicy)
        tooltipClickStream.onNext(Any())
        buttonNoClickStream.onNext(Any())
        buttonYesClickStream.onNext(Any())
        buttonCancelClickStream.onNext(Any())
        closeIconClickStream.onNext(Any())
        verify(privacyPolicyTooltipInteractionConsumer).accept(TooltipInteraction.TooltipClick)
        verify(privacyPolicyTooltipInteractionConsumer).accept(TooltipInteraction.NoButtonClick)
        verify(privacyPolicyTooltipInteractionConsumer).accept(TooltipInteraction.YesButtonClick)
        verify(privacyPolicyTooltipInteractionConsumer, times(2)).accept(TooltipInteraction.CloseCancelButtonClick)
        verifyZeroInteractions(rateThisAppTooltipInteractionConsumer)
    }

    @Test
    fun clicksAreHandledWhenTheRateTooltipIsShown() {
        whenever(rateThisAppController.shouldDisplayTooltip()).thenReturn(Single.just(Optional.of(TooltipType.RateThisApp)))
        tooltipPresenter.subscribe()
        verify(view).display(TooltipType.RateThisApp)
        tooltipClickStream.onNext(Any())
        buttonNoClickStream.onNext(Any())
        buttonYesClickStream.onNext(Any())
        buttonCancelClickStream.onNext(Any())
        closeIconClickStream.onNext(Any())
        verify(rateThisAppTooltipInteractionConsumer).accept(TooltipInteraction.TooltipClick)
        verify(rateThisAppTooltipInteractionConsumer).accept(TooltipInteraction.NoButtonClick)
        verify(rateThisAppTooltipInteractionConsumer).accept(TooltipInteraction.YesButtonClick)
        verify(rateThisAppTooltipInteractionConsumer, times(2)).accept(TooltipInteraction.CloseCancelButtonClick)
        verifyZeroInteractions(privacyPolicyTooltipInteractionConsumer)
    }

    @Test
    fun verifyTheHigherPriorityTooltipWins() {
        whenever(rateThisAppController.shouldDisplayTooltip()).thenReturn(Single.just(Optional.of(TooltipType.RateThisApp)))
        whenever(privacyPolicyController.shouldDisplayTooltip()).thenReturn(Single.just(Optional.of(TooltipType.PrivacyPolicy)))
        tooltipPresenter.subscribe()
        verify(view).display(TooltipType.RateThisApp)
    }

}