package com.wops.receiptsgo.tooltip.privacy

import javax.inject.Inject

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.core.di.scopes.FragmentScope

@FragmentScope
class PrivacyPolicyRouter @Inject constructor(val navigationHandler: NavigationHandler<ReceiptsGoActivity>) {

    fun navigateToPrivacyPolicyControls() {
        this.navigationHandler.navigateToSettingsScrollToPrivacySection()
    }
}
