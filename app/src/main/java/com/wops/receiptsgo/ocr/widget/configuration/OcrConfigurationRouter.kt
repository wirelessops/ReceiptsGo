package com.wops.receiptsgo.ocr.widget.configuration

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.SmartReceiptsActivity
import co.smartreceipts.core.di.scopes.FragmentScope
import javax.inject.Inject

@FragmentScope
class OcrConfigurationRouter @Inject constructor(private val navigationHandler: NavigationHandler<SmartReceiptsActivity>) {

    fun navigateBack(): Boolean {
        return this.navigationHandler.navigateBack()
    }

    fun navigateToLoginScreen() {
        navigationHandler.navigateToLoginScreen()
    }

    fun navigateToSubscriptionsScreen() {
        navigationHandler.navigateToSubscriptionsActivity()
    }
}