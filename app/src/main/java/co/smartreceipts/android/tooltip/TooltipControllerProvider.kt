package co.smartreceipts.android.tooltip

import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.tooltip.backup.AutomaticBackupRecoveryHintUserController
import co.smartreceipts.android.tooltip.model.StaticTooltip
import co.smartreceipts.android.tooltip.privacy.PrivacyPolicyTooltipController
import co.smartreceipts.android.tooltip.rating.AppRatingTooltipController
import co.smartreceipts.android.tooltip.report.FirstReportHintTooltipController
import javax.inject.Inject
import javax.inject.Provider

/**
 * Manages the process of mapping a given [StaticTooltip] to a [TooltipController] implementation.
 *
 * Note: This exists as the [FragmentScope], since each of our [TooltipController] instances is
 * also tied to that scope
 */
@FragmentScope
class TooltipControllerProvider @Inject constructor(private val automaticBackupRecoveryHintTooltipProvider: Provider<AutomaticBackupRecoveryHintUserController>,
                                                    private val firstReportHintTooltipProvider: Provider<FirstReportHintTooltipController>,
                                                    private val privacyPolicyTooltipProvider: Provider<PrivacyPolicyTooltipController>,
                                                    private val appRatingTooltipProvider: Provider<AppRatingTooltipController>) {

    /**
     * Fetches the appropriate [TooltipController] for a given [StaticTooltip]
     *
     * @param tooltip the [StaticTooltip] to determine if we should display
     * @return the corresponding [TooltipController]
     */
    fun get(tooltip: StaticTooltip): TooltipController {
        return when (tooltip) {
            StaticTooltip.AutomaticBackupRecoveryHint -> automaticBackupRecoveryHintTooltipProvider.get()
            StaticTooltip.FirstReportHint -> firstReportHintTooltipProvider.get()
            StaticTooltip.PrivacyPolicy -> privacyPolicyTooltipProvider.get()
            StaticTooltip.RateThisApp -> appRatingTooltipProvider.get()
        }
    }
}