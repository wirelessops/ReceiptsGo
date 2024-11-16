package com.wops.oss_licenses

import android.content.Context
import android.content.Intent
import com.wops.core.di.scopes.ApplicationScope
//import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import javax.inject.Inject

@ApplicationScope
class LicensesNavigator @Inject constructor() : LicensesNavigatorInterface {

   override fun getLicensesActivityIntent(context: Context, ossActivityTitleId: Int): Intent? {

//        OssLicensesMenuActivity.setActivityTitle(context.getString(ossActivityTitleId))
//        return Intent(context, OssLicensesMenuActivity::class.java)
       return null
   }
}