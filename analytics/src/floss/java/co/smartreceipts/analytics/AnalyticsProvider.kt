package co.smartreceipts.analytics

import co.smartreceipts.analytics.impl.AnalyticsLogger

class AnalyticsProvider {

    fun getAnalytics(): List<Analytics> {
        return listOf(AnalyticsLogger())
    }
}