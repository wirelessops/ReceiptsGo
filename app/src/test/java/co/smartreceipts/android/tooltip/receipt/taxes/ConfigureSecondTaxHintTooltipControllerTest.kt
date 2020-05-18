package co.smartreceipts.android.tooltip.receipt.taxes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.android.R
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.receipt.FirstReceiptQuestionsUserInteractionStore
import co.smartreceipts.core.identity.apis.me.User
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.verify
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
class ConfigureSecondTaxHintTooltipControllerTest {

    companion object {
        private val TOOLTIP_METADATA = TooltipMetadata(
            TooltipType.ConfigureSecondTaxHint, ApplicationProvider.getApplicationContext<Context>().getString(
                R.string.tooltip_include_second_tax_hint))
    }

    private lateinit var configureSecondTaxHintTooltipController: ConfigureSecondTaxHintTooltipController

    @Mock
    private lateinit var tooltipView: TooltipView

    @Mock
    private lateinit var router: ConfigureSecondTaxHintRouter

    @Mock
    private lateinit var store: FirstReceiptQuestionsUserInteractionStore

    @Mock
    private lateinit var userPreferences: UserPreferenceManager

    @Mock
    private lateinit var analytics: Analytics

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        configureSecondTaxHintTooltipController = ConfigureSecondTaxHintTooltipController(
            ApplicationProvider.getApplicationContext(), tooltipView, router, store, userPreferences, analytics, Schedulers.trampoline()
        )

        whenever(userPreferences.getSingle(UserPreference.Receipts.IncludeTax2Field)).thenReturn(Single.just(false))
    }

    @Test
    fun displayTooltipWithNoInteractionsAndTaxIncluded() {
        whenever(store.hasUserInteractionWithSecondTaxHintOccurred()).thenReturn(Single.just(false))
        whenever(userPreferences.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))

        configureSecondTaxHintTooltipController.shouldDisplayTooltip().test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(Optional.of(TOOLTIP_METADATA))
    }

    @Test
    fun displayTooltipWithNoInteractionsAndTaxIncludedAndTax2Included() {
        whenever(store.hasUserInteractionWithSecondTaxHintOccurred()).thenReturn(Single.just(false))
        whenever(userPreferences.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))
        whenever(userPreferences.getSingle(UserPreference.Receipts.IncludeTax2Field)).thenReturn(Single.just(true))

        configureSecondTaxHintTooltipController.shouldDisplayTooltip().test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(Optional.absent())
    }

    @Test
    fun doNotDisplayTooltipWithNoInteractionsAndTaxNotIncluded() {
        whenever(store.hasUserInteractionWithSecondTaxHintOccurred()).thenReturn(Single.just(false))
        whenever(userPreferences.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(false))

        configureSecondTaxHintTooltipController.shouldDisplayTooltip().test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(Optional.absent())
    }

    @Test
    fun doNotDisplayTooltipWithInteractionsAndTaxIncluded() {
        whenever(store.hasUserInteractionWithSecondTaxHintOccurred()).thenReturn(Single.just(true))
        whenever(userPreferences.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))

        configureSecondTaxHintTooltipController.shouldDisplayTooltip().test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(Optional.absent())
    }

    @Test
    fun doNotDisplayTooltipWithInteractionsAndTaxNotIncluded() {
        whenever(store.hasUserInteractionWithSecondTaxHintOccurred()).thenReturn(Single.just(true))
        whenever(userPreferences.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(false))

        configureSecondTaxHintTooltipController.shouldDisplayTooltip().test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(Optional.absent())
    }

    @Test
    fun handleTooltipInteractionTest() {
        val interaction = TooltipInteraction.TooltipClick
        configureSecondTaxHintTooltipController.handleTooltipInteraction(interaction)
            .test()
            .await()
            .assertComplete()
            .assertNoErrors()

        verify(store).setInteractionWithSecondTaxHintOccured(true)
        verify(analytics).record(Events.Informational.ClickedConfigureSecondTaxTip)
    }

    @Test
    fun consumeTooltipInteractionTest() {
        configureSecondTaxHintTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.TooltipClick)

        verify(tooltipView).hideTooltip()
        verify(router).navigateToTaxSettings()
    }
}