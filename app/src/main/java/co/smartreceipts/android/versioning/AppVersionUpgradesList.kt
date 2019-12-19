package co.smartreceipts.android.versioning

import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.tooltip.backup.AutomaticBackupRecoveryHintVersionUpgradedListener
import co.smartreceipts.android.tooltip.receipt.FirstReceiptQuestionsVersionUpgradedListener
import javax.inject.Inject

/**
 * Tracks the complete list of actions that we perform when we upgrade our application version
 */
@ApplicationScope
class AppVersionUpgradesList @Inject constructor(private val automaticBackupRecoveryHintVersionUpgradedListener: AutomaticBackupRecoveryHintVersionUpgradedListener,
                                                 private val firstReceiptUseTaxesQuestionsVersionUpgradedListener: FirstReceiptQuestionsVersionUpgradedListener) {

    fun getUpgradeListeners() : List<VersionUpgradedListener> {
        return listOf(
                automaticBackupRecoveryHintVersionUpgradedListener,
                firstReceiptUseTaxesQuestionsVersionUpgradedListener)
    }
}