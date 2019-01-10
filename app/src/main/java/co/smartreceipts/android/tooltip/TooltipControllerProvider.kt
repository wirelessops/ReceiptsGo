package co.smartreceipts.android.tooltip

import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.tooltip.backup.AutomaticBackupRecoveryHintUserController
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.privacy.PrivacyPolicyTooltipController
import co.smartreceipts.android.tooltip.rating.RateThisAppTooltipController
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
class TooltipControllerProvider @Inject constructor(private val automaticBackupRecoveryHintTooltipProvider: Provider<AutomaticBackupRecoveryHintUserController>,
                                                    private val firstReportHintTooltipProvider: Provider<FirstReportHintTooltipController>,
                                                    private val privacyPolicyTooltipProvider: Provider<PrivacyPolicyTooltipController>,
                                                    private val rateThisAppTooltipProvider: Provider<RateThisAppTooltipController>) {

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
        }
    }
}