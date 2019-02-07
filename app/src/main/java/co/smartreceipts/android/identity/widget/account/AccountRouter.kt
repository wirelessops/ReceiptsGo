package co.smartreceipts.android.identity.widget.account

import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.identity.IdentityManager
import co.smartreceipts.android.identity.widget.NeedsLoginRouter
import javax.inject.Inject

@FragmentScope
class AccountRouter @Inject constructor(navigationHandler: NavigationHandler<SmartReceiptsActivity>, identityManager: IdentityManager) :
    NeedsLoginRouter(navigationHandler, identityManager) {

    fun navigateToOcrFragment() {
        navigationHandler.navigateToOcrConfigurationFragment()
    }
}