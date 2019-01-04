package co.smartreceipts.android.versioning

import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.tooltip.backup.AutomaticBackupRecoveryHintVersionUpgradedListener
import java.util.*
import javax.inject.Inject

/**
 * Tracks the complete list of actions that we perform when we upgrade our application version
 */
@ApplicationScope
class AppVersionUpgradesList @Inject constructor(private val automaticBackupRecoveryHintVersionUpgradedListener: AutomaticBackupRecoveryHintVersionUpgradedListener) {

    fun getUpgradeListeners() : List<VersionUpgradedListener> {
        return Collections.singletonList(automaticBackupRecoveryHintVersionUpgradedListener)
    }
}