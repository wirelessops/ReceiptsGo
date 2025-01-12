package com.wops.receiptsgo.tooltip.receipt.taxes

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.core.di.scopes.FragmentScope
import javax.inject.Inject

@FragmentScope
class ConfigureSecondTaxHintRouter @Inject constructor(val navigationHandler: NavigationHandler<ReceiptsGoActivity>) {

    fun navigateToTaxSettings() {
        this.navigationHandler.navigateToSettingsScrollToReceiptSettings()
    }
}