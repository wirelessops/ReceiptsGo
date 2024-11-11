package com.wops.oss_licenses

import android.content.Context
import android.content.Intent
import com.wops.core.di.scopes.ApplicationScope
import javax.inject.Inject

@ApplicationScope
class LicensesNavigator @Inject constructor(): LicensesNavigatorInterface {

    override fun getLicensesActivityIntent(context: Context, ossActivityTitleId: Int): Intent? = null
}