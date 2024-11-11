package com.wops.receiptsgo.tooltip.report

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import com.wops.receiptsgo.R
import com.wops.receiptsgo.persistence.database.controllers.impl.TripTableController
import com.wops.receiptsgo.tooltip.TooltipView
import com.wops.receiptsgo.tooltip.model.TooltipInteraction
import com.wops.receiptsgo.tooltip.model.TooltipMetadata
import com.wops.receiptsgo.tooltip.model.TooltipType
import com.wops.receiptsgo.utils.TripUtils
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
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

    companion object {
        private val TOOLTIP_METADATA = TooltipMetadata(TooltipType.FirstReportHint, ApplicationProvider.getApplicationContext<Context>().getString(R.string.tooltip_first_report_hint))
    }

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
        firstReportHintTooltipController = FirstReportHintTooltipController(ApplicationProvider.getApplicationContext(), tooltipView, store, tripTableController, analytics, scheduler)
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
                .assertValue(Optional.of(TOOLTIP_METADATA))
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