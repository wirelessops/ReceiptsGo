package co.smartreceipts.android.tooltip.backup

import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.versioning.VersionUpgradedListener
import javax.inject.Inject


/**
 * A simple implementation of the [VersionUpgradedListener], which checks if the old version of the
 * app was not set (i.e. equals -1), indicating that this is a fresh install. If this is an existing
 * installation, we set inform the [AutomaticBackupRecoveryHintUserInteractionStore] that the user
 * has already interacted with this tooltip (preventing it from appearing to existing users).
 */
@ApplicationScope
class AutomaticBackupRecoveryHintVersionUpgradedListener @Inject constructor(private val store: AutomaticBackupRecoveryHintUserInteractionStore)
    : VersionUpgradedListener {

    override fun onVersionUpgrade(oldVersion: Int, newVersion: Int) {
        if (oldVersion > 0) {
            Logger.info(this, "Not a fresh install. Marking the automatic backup hint as already shown")
            store.setUserHasInteractedWithAutomaticBackupRecoveryHint(true)
        }
    }
}