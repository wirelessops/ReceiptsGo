package com.wops.analytics

import com.wops.analytics.impl.AnalyticsLogger

class AnalyticsProvider {

    fun getAnalytics(): List<Analytics> {
        return listOf(AnalyticsLogger())
    }
}