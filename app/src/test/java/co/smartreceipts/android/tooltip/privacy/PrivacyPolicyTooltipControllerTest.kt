package co.smartreceipts.android.tooltip.privacy

import co.smartreceipts.android.R
import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import co.smartreceipts.android.utils.TripUtils
import com.hadisatrio.optional.Optional
import io.reactivex.Single

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import com.nhaarman.mockito_kotlin.*
import io.reactivex.schedulers.Schedulers
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PrivacyPolicyTooltipControllerTest {

    companion object {
        private val TOOLTIP_METADATA = TooltipMetadata(TooltipType.PrivacyPolicy, RuntimeEnvironment.application.getString(R.string.tooltip_review_privacy))
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
        privacyPolicyTooltipController = PrivacyPolicyTooltipController(RuntimeEnvironment.application, tooltipView, router, store, regionChecker, tripTableController, analytics, scheduler)
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