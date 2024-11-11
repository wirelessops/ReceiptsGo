package com.wops.receiptsgo.tooltip.privacy

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
class PrivacyPolicyTooltipControllerTest {

    companion object {
        private val TOOLTIP_METADATA = TooltipMetadata(TooltipType.PrivacyPolicy, ApplicationProvider.getApplicationContext<Context>().getString(R.string.tooltip_review_privacy))
    }

    private lateinit var privacyPolicyTooltipController: PrivacyPolicyTooltipController

    @Mock
    private lateinit var tooltipView: TooltipView

    @Mock
    private lateinit var router: PrivacyPolicyRouter

    @Mock
    private lateinit var store: PrivacyPolicyUserInteractionStore

    @Mock
    private lateinit var regionChecker: RegionChecker

    @Mock
    private lateinit var tripTableController: TripTableController

    @Mock
    private lateinit var analytics: Analytics

    private val scheduler = Schedulers.trampoline()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        privacyPolicyTooltipController = PrivacyPolicyTooltipController(ApplicationProvider.getApplicationContext(), tooltipView, router, store, regionChecker, tripTableController, analytics, scheduler)
    }

    @Test
    fun displayTooltipWithoutUserInteractionsForEuUser() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))
        whenever(regionChecker.isInTheEuropeanUnion()).thenReturn(true)
        privacyPolicyTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.of(TOOLTIP_METADATA))
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun displayTooltipWithoutUserInteractionsForNonEuUserWithMultipleTrips() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))
        whenever(regionChecker.isInTheEuropeanUnion()).thenReturn(false)
        whenever(tripTableController.get()).thenReturn(Single.just(arrayListOf(TripUtils.newDefaultTrip(), TripUtils.newDefaultTrip())))
        privacyPolicyTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.of(TOOLTIP_METADATA))
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun displayTooltipWithoutUserInteractionsForNonEuUserWithNoTrips() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))
        whenever(regionChecker.isInTheEuropeanUnion()).thenReturn(false)
        whenever(tripTableController.get()).thenReturn(Single.just(arrayListOf()))
        privacyPolicyTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.absent())
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWithUserInteractions() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(true))
        privacyPolicyTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.absent())
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun handleTooltipInteraction() {
        val interaction = TooltipInteraction.TooltipClick
        privacyPolicyTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setUserHasInteractedWithPrivacyPolicy(true)
        verify(analytics).record(Events.Informational.ClickedPrivacyPolicyTip)
    }

    @Test
    fun consumeTooltipClickInteraction() {
        privacyPolicyTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.TooltipClick)
        verify(tooltipView).hideTooltip()
        verify(router).navigateToPrivacyPolicyControls()
    }

    @Test
    fun consumeTooltipCloseInteraction() {
        privacyPolicyTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.CloseCancelButtonClick)
        verify(tooltipView).hideTooltip()
        verifyZeroInteractions(router)
    }

}