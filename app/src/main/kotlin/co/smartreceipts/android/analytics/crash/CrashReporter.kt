package co.smartreceipts.android.analytics.crash

import android.content.Context
import co.smartreceipts.android.BuildConfig

import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore

import javax.inject.Inject

import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import io.fabric.sdk.android.Fabric

@ApplicationScope
class CrashReporter @Inject constructor(private val context: Context, private val userPreferenceManager: UserPreferenceManager) {

    /**
     * Initializes our crash reporter to determine if we should track crashes or not
     */
    fun initialize() {
        // Set up Crashlytics, disabling it when the user has elected to disable the functionality
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG || userPreferenceManager.get(UserPreference.Privacy.EnableCrashTracking)).build())
                .build()

        // Initialize Fabric with the custom crashlytics instance
        Fabric.with(context, crashlyticsKit)

    }
}
