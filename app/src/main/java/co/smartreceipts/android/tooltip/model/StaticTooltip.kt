package co.smartreceipts.android.tooltip.model

import android.support.annotation.StringRes
import co.smartreceipts.android.R

/**
 * Tracks different types of static tooltips supported within the app along with their priorities.
 * This should be treated differently from dynamic tooltips in that there are no supported "format
 * arguments" when loading a string from our resources
 *
 * @param type defines the [TooltipType] to display
 * @param priority defines the priority. Higher priorities outweigh lower ones
 * @param messageResourceId defines the [StringRes] for the tooltip message
 * @param showWarningIcon determines if we should show a warning icon (ie '!') before the message
 * @param showCloseIcon determines if the 'X' icon should appear to close
 * @param showCancelButton determines if an explicit 'Cancel' button should appear.
 *                        This may be more meaningful in certain contexts that the close icon.
 *                        Tooltips can only have either this or the close icon but not both
 */
enum class StaticTooltip(val type: TooltipType,
                         val priority: Int,
                         @StringRes val messageResourceId: Int,
                         val showWarningIcon: Boolean,
                         val showCloseIcon: Boolean,
                         val showCancelButton: Boolean) {

    AutomaticBackupRecoveryHint(TooltipType.Informational, 100, R.string.tooltip_automatic_backups_recovery_hint, false, true, false),
    PrivacyPolicy(TooltipType.Informational, 10, R.string.tooltip_review_privacy, false, true, false),
    RateThisApp(TooltipType.Question, 50, R.string.rating_tooltip_text, false, false, false)

}