package com.wops.receiptsgo.versioning

import com.wops.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.tooltip.backup.AutomaticBackupRecoveryHintVersionUpgradedListener
import com.wops.receiptsgo.tooltip.receipt.FirstReceiptQuestionsVersionUpgradedListener
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