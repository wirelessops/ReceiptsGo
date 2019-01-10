package co.smartreceipts.android.tooltip.report

import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.model.StaticTooltip
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.utils.TripUtils
import com.hadisatrio.optional.Optional
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FirstReportHintTooltipControllerTest  {

    private lateinit var firstReportHintTooltipController: FirstReportHintTooltipController

    @Mock
    private lateinit var tooltipView: TooltipView

    @Mock
    private lateinit var store: FirstReportHintUserInteractionStore

    @Mock
    private lateinit var tripTableController: TripTableController

    @Mock
    private lateinit var analytics: Analytics

    private val scheduler = Schedulers.trampoline()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        firstReportHintTooltipController = FirstReportHintTooltipController(tooltipView, store, tripTableController, analytics, scheduler)
    }

    @Test
    fun doNoDisplayTooltipWithoutUserInteractionsForUserWithMultipleTrips() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))
        whenever(tripTableController.get()).thenReturn(Single.just(arrayListOf(TripUtils.newDefaultTrip(), TripUtils.newDefaultTrip())))
        firstReportHintTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.absent())
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun displayTooltipWithoutUserInteractionsForUserWithNoTrips() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))
        whenever(tripTableController.get()).thenReturn(Single.just(arrayListOf()))
        firstReportHintTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.of(StaticTooltip.FirstReportHint))
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWithUserInteractions() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(true))
        firstReportHintTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.absent())
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun handleTooltipInteraction() {
        val interaction = TooltipInteraction.TooltipClick
        firstReportHintTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionHasOccurred(true)
        verify(analytics).record(Events.Informational.ClickedFirstReportHintTip)
    }

    @Test
    fun consumeTooltipClickInteraction() {
        firstReportHintTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.TooltipClick)
        verifyZeroInteractions(tooltipView)
    }

    @Test
    fun consumeTooltipCloseInteraction() {
        firstReportHintTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.CloseCancelButtonClick)
        verify(tooltipView).hideTooltip()
    }

}