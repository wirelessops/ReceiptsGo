package co.smartreceipts.android.tooltip

import co.smartreceipts.core.di.scopes.FragmentScope
import co.smartreceipts.android.ocr.widget.tooltip.OcrInformationTooltipController
import co.smartreceipts.android.tooltip.backup.AutomaticBackupRecoveryHintUserController
import co.smartreceipts.android.tooltip.image.ImageCroppingTooltipController
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.privacy.PrivacyPolicyTooltipController
import co.smartreceipts.android.tooltip.rating.RateThisAppTooltipController
import co.smartreceipts.android.tooltip.receipt.paymentmethods.FirstReceiptUsePaymentMethodsQuestionTooltipController
import co.smartreceipts.android.tooltip.receipt.taxes.FirstReceiptUseTaxesQuestionTooltipController
import co.smartreceipts.android.tooltip.receipt.taxes.ConfigureSecondTaxHintTooltipController
import co.smartreceipts.android.tooltip.report.FirstReportHintTooltipController
import javax.inject.Inject
import javax.inject.Provider

/**
 * Manages the process of mapping a given [TooltipType] to a [TooltipController] implementation.
 *
 * Note: This exists as the [FragmentScope], since each of our [TooltipController] instances is
 * also tied to that scope
 */
@FragmentScope
class TooltipControllerProvider @Inject constructor(
    private val automaticBackupRecoveryHintTooltipProvider: Provider<AutomaticBackupRecoveryHintUserController>,
    private val firstReportHintTooltipProvider: Provider<FirstReportHintTooltipController>,
    private val privacyPolicyTooltipProvider: Provider<PrivacyPolicyTooltipController>,
    private val rateThisAppTooltipProvider: Provider<RateThisAppTooltipController>,
    private val ocrInformationTooltipProvider: Provider<OcrInformationTooltipController>,
    private val firstReceiptUseTaxesQuestionTooltipProvider: Provider<FirstReceiptUseTaxesQuestionTooltipController>,
    private val firstReceiptUsePaymentMethodsQuestionTooltipProvider: Provider<FirstReceiptUsePaymentMethodsQuestionTooltipController>,
    private val croppingTooltipProvider: Provider<ImageCroppingTooltipController>,
    private val configureSecondTaxHintTooltipProvider: Provider<ConfigureSecondTaxHintTooltipController>
) {

    /**
     * Fetches the appropriate [TooltipController] for a given [TooltipType]
     *
     * @param tooltip the [TooltipType] to determine if we should display
     * @return the corresponding [TooltipController]
     */
    fun get(tooltip: TooltipType): TooltipController {
        return when (tooltip) {
            TooltipType.AutomaticBackupRecoveryHint -> automaticBackupRecoveryHintTooltipProvider.get()
            TooltipType.FirstReportHint -> firstReportHintTooltipProvider.get()
            TooltipType.PrivacyPolicy -> privacyPolicyTooltipProvider.get()
            TooltipType.RateThisApp -> rateThisAppTooltipProvider.get()
            TooltipType.OcrInformation -> ocrInformationTooltipProvider.get()
            TooltipType.FirstReceiptUseTaxesQuestion -> firstReceiptUseTaxesQuestionTooltipProvider.get()
            TooltipType.FirstReceiptUsePaymentMethodsQuestion -> firstReceiptUsePaymentMethodsQuestionTooltipProvider.get()
            TooltipType.ImageCropping -> croppingTooltipProvider.get()
            TooltipType.ConfigureSecondTaxHint -> configureSecondTaxHintTooltipProvider.get()
        }
    }
}