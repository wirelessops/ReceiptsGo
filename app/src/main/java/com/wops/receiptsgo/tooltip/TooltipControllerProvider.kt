package com.wops.receiptsgo.tooltip

import com.wops.core.di.scopes.FragmentScope
import com.wops.receiptsgo.ocr.widget.tooltip.OcrInformationTooltipController
import com.wops.receiptsgo.tooltip.backup.AutomaticBackupRecoveryHintUserController
import com.wops.receiptsgo.tooltip.image.ImageCroppingTooltipController
import com.wops.receiptsgo.tooltip.model.TooltipType
import com.wops.receiptsgo.tooltip.privacy.PrivacyPolicyTooltipController
import com.wops.receiptsgo.tooltip.rating.RateThisAppTooltipController
import com.wops.receiptsgo.tooltip.receipt.paymentmethods.FirstReceiptUsePaymentMethodsQuestionTooltipController
import com.wops.receiptsgo.tooltip.receipt.taxes.FirstReceiptUseTaxesQuestionTooltipController
import com.wops.receiptsgo.tooltip.receipt.taxes.ConfigureSecondTaxHintTooltipController
import com.wops.receiptsgo.tooltip.report.FirstReportHintTooltipController
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