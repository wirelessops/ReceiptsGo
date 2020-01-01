package co.smartreceipts.android.tooltip.backup

import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.core.di.scopes.FragmentScope
import javax.inject.Inject


/**
 * A router instance, which is responsible for navigating to our automatic backups page with the
 * intention of allowing the user to access their previous backups
 */
@FragmentScope
class AutomaticBackupRecoveryHintRouter @Inject constructor(val navigationHandler: NavigationHandler<SmartReceiptsActivity>) {

    /**
     * Routes to the automatic backups page with the intention of allowing the user to potentially
     * recover from a past backup
     */
    fun navigateToAutomaticBackupConfiguration() {
        this.navigationHandler.navigateToBackupMenu()
    }
}