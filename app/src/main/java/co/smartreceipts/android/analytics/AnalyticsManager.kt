package co.smartreceipts.android.analytics

import androidx.annotation.VisibleForTesting

import com.google.common.base.Preconditions
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.Executors

import co.smartreceipts.android.analytics.events.Event
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference

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
