package co.smartreceipts.analytics.crash

import android.content.Context
import co.smartreceipts.analytics.BuildConfig
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import javax.inject.Inject


class CrashReporter @Inject constructor(private val context: Context) : CrashReporterInterface {

    /**
     * Initializes our crash reporter to determine if we should track crashes or not
     */
    override fun initialize(isCrashTrackingEnabled: Boolean) {
        // Set up Crashlytics, disabling it when the user has elected to disable the functionality
        val crashlyticsKit = Crashlytics.Builder()
            .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG || isCrashTrackingEnabled).build())
            .build()

        // Initialize Fabric with the custom crashlytics instance
        Fabric.with(context, crashlyticsKit)
    }
}
