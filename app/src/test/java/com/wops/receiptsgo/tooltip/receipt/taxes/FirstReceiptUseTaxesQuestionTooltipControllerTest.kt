package com.wops.receiptsgo.tooltip.receipt.taxes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.wops.analytics.Analytics
import com.wops.analytics.events.Events
import com.wops.receiptsgo.R
import com.wops.receiptsgo.columns.ordering.CsvColumnsOrderer
import com.wops.receiptsgo.columns.ordering.PdfColumnsOrderer
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.receiptsgo.tooltip.TooltipView
import com.wops.receiptsgo.tooltip.model.TooltipInteraction
import com.wops.receiptsgo.tooltip.model.TooltipMetadata
import com.wops.receiptsgo.tooltip.model.TooltipType
import com.wops.receiptsgo.tooltip.receipt.FirstReceiptQuestionsUserInteractionStore
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class FirstReceiptUseTaxesQuestionTooltipControllerTest {
    
    companion object {
        private val TOOLTIP_METADATA = TooltipMetadata(TooltipType.FirstReceiptUseTaxesQuestion, ApplicationProvider.getApplicationContext<Context>().getString(R.string.pref_receipt_include_tax_field_title))
    }

    private lateinit var firstReceiptUseTaxesQuestionTooltipController: FirstReceiptUseTaxesQuestionTooltipController

    @Mock
    private lateinit var tooltipView: TooltipView

    @Mock
    private lateinit var store: FirstReceiptQuestionsUserInteractionStore

    @Mock
    private lateinit var userPreferenceManager: UserPreferenceManager

    @Mock
    private lateinit var pdfColumnsOrderer: PdfColumnsOrderer

    @Mock
    private lateinit var csvColumnsOrderer: CsvColumnsOrderer

    @Mock
    private lateinit var analytics: Analytics

    private val scheduler = Schedulers.trampoline()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(false))
        whenever(pdfColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.complete())
        whenever(csvColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.complete())

        firstReceiptUseTaxesQuestionTooltipController = FirstReceiptUseTaxesQuestionTooltipController(ApplicationProvider.getApplicationContext(), tooltipView, store, userPreferenceManager, pdfColumnsOrderer, csvColumnsOrderer, analytics, scheduler)
    }

    @Test
    fun displayTooltipWithoutUserInteractions() {
        whenever(store.hasUserInteractionWithTaxesQuestionOccurred()).thenReturn(Single.just(false))
        firstReceiptUseTaxesQuestionTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.of(TOOLTIP_METADATA))
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWithUserInteractions() {
        whenever(store.hasUserInteractionWithTaxesQuestionOccurred()).thenReturn(Single.just(true))
        firstReceiptUseTaxesQuestionTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.absent())
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWithTaxIncluded() {
        whenever(store.hasUserInteractionWithTaxesQuestionOccurred()).thenReturn(Single.just(false))
        whenever(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField)).thenReturn(Single.just(true))

        firstReceiptUseTaxesQuestionTooltipController.shouldDisplayTooltip()
            .test()
            .await()
            .assertValue(Optional.absent())
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun handleTooltipClickInteraction() {
        val interaction = TooltipInteraction.TooltipClick
        firstReceiptUseTaxesQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verifyZeroInteractions(store)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }

    @Test
    fun handleTooltipCloseCancelInteraction() {
        val interaction = TooltipInteraction.CloseCancelButtonClick
        firstReceiptUseTaxesQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithTaxesQuestionHasOccurred(true)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }

    @Test
    fun handleTooltipNoInteraction() {
        val interaction = TooltipInteraction.NoButtonClick
        firstReceiptUseTaxesQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithTaxesQuestionHasOccurred(true)
        verify(userPreferenceManager)[UserPreference.Receipts.IncludeTaxField] = false
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verify(analytics).record(Events.Informational.ClickedTaxQuestionTipNo)
    }

    @Test
    fun handleTooltipYesInteraction() {
        val interaction = TooltipInteraction.YesButtonClick
        firstReceiptUseTaxesQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithTaxesQuestionHasOccurred(true)
        verify(userPreferenceManager)[UserPreference.Receipts.IncludeTaxField] = true
        verify(pdfColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.TAX, ReceiptColumnDefinitions.ActualDefinition.PRICE)
        verify(csvColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.TAX, ReceiptColumnDefinitions.ActualDefinition.PRICE)
        verify(analytics).record(Events.Informational.ClickedTaxQuestionTipYes)
    }

    @Test
    fun handleTooltipYesInteractionWithPdfError() {
        whenever(pdfColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.error(Exception("test")))

        val interaction = TooltipInteraction.YesButtonClick
        firstReceiptUseTaxesQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithTaxesQuestionHasOccurred(true)
        verify(userPreferenceManager)[UserPreference.Receipts.IncludeTaxField] = true
        verify(pdfColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.TAX, ReceiptColumnDefinitions.ActualDefinition.PRICE)
        verify(csvColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.TAX, ReceiptColumnDefinitions.ActualDefinition.PRICE)
        verify(analytics).record(Events.Informational.ClickedTaxQuestionTipYes)
    }

    @Test
    fun handleTooltipYesInteractionWithCsvError() {
        whenever(csvColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.error(Exception("test")))

        val interaction = TooltipInteraction.YesButtonClick
        firstReceiptUseTaxesQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithTaxesQuestionHasOccurred(true)
        verify(userPreferenceManager)[UserPreference.Receipts.IncludeTaxField] = true
        verify(pdfColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.TAX, ReceiptColumnDefinitions.ActualDefinition.PRICE)
        verify(csvColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.TAX, ReceiptColumnDefinitions.ActualDefinition.PRICE)
        verify(analytics).record(Events.Informational.ClickedTaxQuestionTipYes)
    }

    @Test
    fun handleTooltipYesInteractionWithBothPdfAndCsvError() {
        whenever(pdfColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.error(Exception("test")))
        whenever(csvColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.error(Exception("test")))

        val interaction = TooltipInteraction.YesButtonClick
        firstReceiptUseTaxesQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithTaxesQuestionHasOccurred(true)
        verify(userPreferenceManager)[UserPreference.Receipts.IncludeTaxField] = true
        verify(pdfColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.TAX, ReceiptColumnDefinitions.ActualDefinition.PRICE)
        verify(csvColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.TAX, ReceiptColumnDefinitions.ActualDefinition.PRICE)
        verify(analytics).record(Events.Informational.ClickedTaxQuestionTipYes)
    }

    @Test
    fun consumeTooltipClickInteraction() {
        firstReceiptUseTaxesQuestionTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.TooltipClick)
        verifyZeroInteractions(tooltipView)

        verifyZeroInteractions(store)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }

    @Test
    fun consumeTooltipCloseCancelInteraction() {
        firstReceiptUseTaxesQuestionTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.CloseCancelButtonClick)
        verify(tooltipView).hideTooltip()

        verifyZeroInteractions(store)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }

    @Test
    fun consumeTooltipYesInteraction() {
        firstReceiptUseTaxesQuestionTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.YesButtonClick)
        verify(tooltipView).hideTooltip()

        verifyZeroInteractions(store)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }

    @Test
    fun consumeTooltipNoInteraction() {
        firstReceiptUseTaxesQuestionTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.YesButtonClick)
        verify(tooltipView).hideTooltip()

        verifyZeroInteractions(store)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }
}