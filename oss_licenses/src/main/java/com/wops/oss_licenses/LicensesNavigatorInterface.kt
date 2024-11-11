package com.wops.oss_licenses

import android.content.Context
import android.content.Intent

interface LicensesNavigatorInterface {

    fun getLicensesActivityIntent(context: Context, ossActivityTitleId: Int): Intent?
}