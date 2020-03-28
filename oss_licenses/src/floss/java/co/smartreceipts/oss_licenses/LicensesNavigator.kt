package co.smartreceipts.oss_licenses

import android.content.Context
import android.content.Intent
import co.smartreceipts.core.di.scopes.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class LicensesNavigator @Inject constructor(): LicensesNavigatorInterface {

    override fun getLicensesActivityIntent(context: Context, ossActivityTitleId: Int): Intent? = null
}