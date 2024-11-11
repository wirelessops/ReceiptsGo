package com.wops.receiptsgo.analytics

import androidx.annotation.VisibleForTesting
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Event
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.google.common.base.Preconditions
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AnalyticsManager @VisibleForTesting
internal constructor(analyticsList: List<Analytics>,
                     userPreferenceManager: UserPreferenceManager,
                     executor: Executor) : Analytics {

    private val analyticsList: CopyOnWriteArrayList<Analytics>
    private val userPreferenceManager: UserPreferenceManager
    private val executor: Executor

    constructor(analytics: List<Analytics>, userPreferenceManager: UserPreferenceManager) : this(analytics, userPreferenceManager, Executors.newSingleThreadExecutor())

    init {
        this.analyticsList = CopyOnWriteArrayList(Preconditions.checkNotNull(analyticsList))
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager)
        this.executor = Preconditions.checkNotNull(executor)
    }

    override fun record(event: Event) {
        executor.execute {
            if (userPreferenceManager.get(UserPreference.Privacy.EnableAnalytics)) {
                for (analytics in analyticsList) {
                    analytics.record(event)
                }
            }
        }
    }
}
