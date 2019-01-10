package co.smartreceipts.android.ocr.widget.tooltip

import javax.inject.Inject

import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.di.scopes.FragmentScope

@FragmentScope
class OcrInformationTooltipRouter @Inject constructor(val navigationHandler: NavigationHandler<SmartReceiptsActivity>) {

    fun navigateToOcrConfigurationScreen() {
        this.navigationHandler.navigateToOcrConfigurationFragment()
    }
}
