package co.smartreceipts.android.tooltip.model

/**
 * Tracks different types of static tooltips supported within the app along with their priorities.
 * This should be treated differently from dynamic tooltips in that there are no supported "format
 * arguments" when loading a string from our resources.
 *
 * For the purposes of easy readability, has a lot of overlap with [TooltipMetadata], but we can
 * remove these enum fields on an as-needed basis to
 */
enum class TooltipType(val displayStyle: TooltipDisplayStyle,
                       val priority: Int,
                       val showWarningIcon: Boolean,
                       val showCloseIcon: Boolean,
                       val showCancelButton: Boolean) {

    AutomaticBackupRecoveryHint(TooltipDisplayStyle.Informational, 100, false, true, false),
    FirstReportHint(TooltipDisplayStyle.Informational, 75, false, true, false),
    RateThisApp(TooltipDisplayStyle.Question, 50, false, false, false),
    PrivacyPolicy(TooltipDisplayStyle.Informational, 10, false, true, false)

}