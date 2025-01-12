package com.wops.receiptsgo.ocr.widget.tooltip

import javax.inject.Inject

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.core.di.scopes.FragmentScope

@FragmentScope
class OcrInformationTooltipRouter @Inject constructor(val navigationHandler: NavigationHandler<ReceiptsGoActivity>) {

    fun navigateToOcrConfigurationScreen() {
        this.navigationHandler.navigateToOcrConfigurationFragment()
    }
}
