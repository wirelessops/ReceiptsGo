package com.wops.analytics.crash

import android.content.Context
import com.wops.analytics.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

class CrashReporter @Inject constructor(private val context: Context) : CrashReporterInterface {

    /**
     * Initializes our crash reporter to determine if we should track crashes or not
     */
    override fun initialize(isCrashTrackingEnabled: Boolean) {
        // must be initialized before using Firebase objects
        FirebaseApp.initializeApp(context)

        // Set up Crashlytics, disabling it when the user has elected to disable the functionality
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(isCrashTrackingEnabled)
        } else {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
    }
}
