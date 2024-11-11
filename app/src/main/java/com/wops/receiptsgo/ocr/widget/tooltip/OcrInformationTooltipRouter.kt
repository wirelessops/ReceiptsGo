package com.wops.receiptsgo.ocr.widget.tooltip

import javax.inject.Inject

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.SmartReceiptsActivity
import com.wops.core.di.scopes.FragmentScope

@FragmentScope
class OcrInformationTooltipRouter @Inject constructor(val navigationHandler: NavigationHandler<SmartReceiptsActivity>) {

    fun navigateToOcrConfigurationScreen() {
        this.navigationHandler.navigateToOcrConfigurationFragment()
    }
}
