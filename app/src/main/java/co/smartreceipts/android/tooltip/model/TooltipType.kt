package co.smartreceipts.android.tooltip.model

import co.smartreceipts.android.R

/**
 * Tracks different types of static tooltips supported within the app along with their priorities.
 * This should be treated differently from dynamic tooltips in that there are no supported "format
 * arguments" when loading a string from our resources
 */
enum class TooltipType(override val displayStyle: TooltipDisplayStyle,
                       override val priority: Int,
                       override val messageResourceId: Int,
                       override val showWarningIcon: Boolean,
                       override val showCloseIcon: Boolean,
                       override val showCancelButton: Boolean) : TooltipMetadata {

    AutomaticBackupRecoveryHint(TooltipDisplayStyle.Informational, 100, R.string.tooltip_automatic_backups_recovery_hint, false, true, false),
    FirstReportHint(TooltipDisplayStyle.Informational, 75, R.string.tooltip_first_report_hint, false, true, false),
    PrivacyPolicy(TooltipDisplayStyle.Informational, 10, R.string.tooltip_review_privacy, false, true, false),
    RateThisApp(TooltipDisplayStyle.Question, 50, R.string.rating_tooltip_text, false, false, false)

}