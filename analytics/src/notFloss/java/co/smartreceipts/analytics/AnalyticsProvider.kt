package co.smartreceipts.analytics

import android.content.Context
import co.smartreceipts.analytics.impl.AnalyticsLogger

class AnalyticsProvider constructor(val context: Context) {

    fun getAnalytics(): List<Analytics> = listOf(AnalyticsLogger(), FirebaseAnalytics(context))
}