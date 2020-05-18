package co.smartreceipts.android.tooltip.receipt.taxes

import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.core.di.scopes.FragmentScope
import javax.inject.Inject

@FragmentScope
class ConfigureSecondTaxHintRouter @Inject constructor(val navigationHandler: NavigationHandler<SmartReceiptsActivity>) {

    fun navigateToTaxSettings() {
        this.navigationHandler.navigateToSettingsScrollToReceiptSettings()
    }
}