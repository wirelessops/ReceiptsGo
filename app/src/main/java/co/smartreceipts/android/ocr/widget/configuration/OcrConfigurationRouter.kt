package co.smartreceipts.android.ocr.widget.configuration

import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
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