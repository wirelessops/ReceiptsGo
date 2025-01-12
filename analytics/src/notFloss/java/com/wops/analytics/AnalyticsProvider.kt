package com.wops.analytics

import android.content.Context
import com.wops.analytics.impl.AnalyticsLogger

class AnalyticsProvider constructor(val context: Context) {

    fun getAnalytics(): List<Analytics> = listOf(AnalyticsLogger(), FirebaseAnalytics(context))
}