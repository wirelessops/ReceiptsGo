package com.wops.receiptsgo.tooltip.privacy

import javax.inject.Inject

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.SmartReceiptsActivity
import co.smartreceipts.core.di.scopes.FragmentScope

@FragmentScope
class PrivacyPolicyRouter @Inject constructor(val navigationHandler: NavigationHandler<SmartReceiptsActivity>) {

    fun navigateToPrivacyPolicyControls() {
        this.navigationHandler.navigateToSettingsScrollToPrivacySection()
    }
}
