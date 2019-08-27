package co.smartreceipts.android.tooltip.receipt.paymentmethods

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.smartreceipts.android.R
import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.columns.ordering.CsvColumnsOrderer
import co.smartreceipts.android.columns.ordering.PdfColumnsOrderer
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.receipt.FirstReceiptQuestionsUserInteractionStore
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
class FirstReceiptUsePaymentMethodsQuestionTooltipControllerTest {

    companion object {
        private val TOOLTIP_METADATA = TooltipMetadata(TooltipType.FirstReceiptUsePaymentMethodsQuestion, ApplicationProvider.getApplicationContext<Context>().getString(R.string.pref_receipt_use_payment_methods_title))
    }

    private lateinit var firstReceiptUsePaymentMethodsQuestionTooltipController: FirstReceiptUsePaymentMethodsQuestionTooltipController

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
        whenever(pdfColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.complete())
        whenever(csvColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.complete())
        firstReceiptUsePaymentMethodsQuestionTooltipController = FirstReceiptUsePaymentMethodsQuestionTooltipController(ApplicationProvider.getApplicationContext(), tooltipView, store, userPreferenceManager, pdfColumnsOrderer, csvColumnsOrderer, analytics, scheduler)
    }

    @Test
    fun displayTooltipWithoutUserInteractions() {
        whenever(store.hasUserInteractionWithPaymentMethodsQuestionOccurred()).thenReturn(Single.just(false))
        firstReceiptUsePaymentMethodsQuestionTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.of(TOOLTIP_METADATA))
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWithUserInteractions() {
        whenever(store.hasUserInteractionWithPaymentMethodsQuestionOccurred()).thenReturn(Single.just(true))
        firstReceiptUsePaymentMethodsQuestionTooltipController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.absent())
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun handleTooltipClickInteraction() {
        val interaction = TooltipInteraction.TooltipClick
        firstReceiptUsePaymentMethodsQuestionTooltipController.handleTooltipInteraction(interaction)
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
        firstReceiptUsePaymentMethodsQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithPaymentMethodsQuestionHasOccurred(true)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }

    @Test
    fun handleTooltipNoInteraction() {
        val interaction = TooltipInteraction.NoButtonClick
        firstReceiptUsePaymentMethodsQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithPaymentMethodsQuestionHasOccurred(true)
        verify(userPreferenceManager)[UserPreference.Receipts.UsePaymentMethods] = false
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verify(analytics).record(Events.Informational.ClickedPaymentMethodsQuestionTipNo)
    }

    @Test
    fun handleTooltipYesInteraction() {
        val interaction = TooltipInteraction.YesButtonClick
        firstReceiptUsePaymentMethodsQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithPaymentMethodsQuestionHasOccurred(true)
        verify(userPreferenceManager)[UserPreference.Receipts.UsePaymentMethods] = true
        verify(pdfColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE)
        verify(csvColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE)
        verify(analytics).record(Events.Informational.ClickedPaymentMethodsQuestionTipYes)
    }

    @Test
    fun handleTooltipYesInteractionWithPdfError() {
        whenever(pdfColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.error(Exception("test")))

        val interaction = TooltipInteraction.YesButtonClick
        firstReceiptUsePaymentMethodsQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithPaymentMethodsQuestionHasOccurred(true)
        verify(userPreferenceManager)[UserPreference.Receipts.UsePaymentMethods] = true
        verify(pdfColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE)
        verify(csvColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE)
        verify(analytics).record(Events.Informational.ClickedPaymentMethodsQuestionTipYes)
    }

    @Test
    fun handleTooltipYesInteractionWithCsvError() {
        whenever(csvColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.error(Exception("test")))

        val interaction = TooltipInteraction.YesButtonClick
        firstReceiptUsePaymentMethodsQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithPaymentMethodsQuestionHasOccurred(true)
        verify(userPreferenceManager)[UserPreference.Receipts.UsePaymentMethods] = true
        verify(pdfColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE)
        verify(csvColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE)
        verify(analytics).record(Events.Informational.ClickedPaymentMethodsQuestionTipYes)
    }

    @Test
    fun handleTooltipYesInteractionWithBothPdfAndCsvError() {
        whenever(pdfColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.error(Exception("test")))
        whenever(csvColumnsOrderer.insertColumnAfter(any(), any())).thenReturn(Completable.error(Exception("test")))

        val interaction = TooltipInteraction.YesButtonClick
        firstReceiptUsePaymentMethodsQuestionTooltipController.handleTooltipInteraction(interaction)
                .test()
                .await()
                .assertComplete()
                .assertNoErrors()

        verify(store).setInteractionWithPaymentMethodsQuestionHasOccurred(true)
        verify(userPreferenceManager)[UserPreference.Receipts.UsePaymentMethods] = true
        verify(pdfColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE)
        verify(csvColumnsOrderer).insertColumnAfter(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD, ReceiptColumnDefinitions.ActualDefinition.CATEGORY_CODE)
        verify(analytics).record(Events.Informational.ClickedPaymentMethodsQuestionTipYes)
    }

    @Test
    fun consumeTooltipClickInteraction() {
        firstReceiptUsePaymentMethodsQuestionTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.TooltipClick)
        verifyZeroInteractions(tooltipView)

        verifyZeroInteractions(store)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }

    @Test
    fun consumeTooltipCloseCancelInteraction() {
        firstReceiptUsePaymentMethodsQuestionTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.CloseCancelButtonClick)
        verify(tooltipView).hideTooltip()

        verifyZeroInteractions(store)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }

    @Test
    fun consumeTooltipYesInteraction() {
        firstReceiptUsePaymentMethodsQuestionTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.YesButtonClick)
        verify(tooltipView).hideTooltip()

        verifyZeroInteractions(store)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }

    @Test
    fun consumeTooltipNoInteraction() {
        firstReceiptUsePaymentMethodsQuestionTooltipController.consumeTooltipInteraction().accept(TooltipInteraction.YesButtonClick)
        verify(tooltipView).hideTooltip()

        verifyZeroInteractions(store)
        verifyZeroInteractions(userPreferenceManager)
        verifyZeroInteractions(pdfColumnsOrderer)
        verifyZeroInteractions(csvColumnsOrderer)
        verifyZeroInteractions(analytics)
    }
}