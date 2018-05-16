package co.smartreceipts.android.tooltip.privacy

import javax.inject.Inject

import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.di.scopes.FragmentScope

@FragmentScope
class PrivacyPolicyRouter @Inject constructor(val navigationHandler: NavigationHandler<SmartReceiptsActivity>) {

    fun navigateToPrivacyPolicyControls() {
        this.navigationHandler.navigateToSettingsScrollToPrivacySection()
    }
}
