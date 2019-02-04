package co.smartreceipts.android.utils

import android.os.Build
import android.os.StrictMode
import co.smartreceipts.android.utils.log.Logger
import java.util.concurrent.Executors

/**
 * A simple wrapper around our [StrictMode] configuration to allow us to enable this for testing
 * purposes
 */
object StrictModeConfiguration {

    private val MAX_STACK_DEPTH_TO_CHECK = 15

    /**
     * Enables strict mode
     */
    @JvmStatic
    fun enable() {
        Logger.debug(this, "Enabling strict mode")
        val threadPolicyBuilder = StrictMode.ThreadPolicy.Builder()
        threadPolicyBuilder.detectNetwork()
        // threadPolicyBuilder.detectCustomSlowCalls() Note: Excluding as AdMob can fail this
        threadPolicyBuilder.detectDiskReads()
        threadPolicyBuilder.detectDiskWrites()
        // threadPolicyBuilder.detectUnbufferedIo() Note: Excluding as our 3p libraries can fail this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            threadPolicyBuilder.detectResourceMismatches()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // We use a custom policy to white-list specific failures
            threadPolicyBuilder.penaltyListener(Executors.newSingleThreadExecutor(), StrictMode.OnThreadViolationListener {
                // This we use this to check for a strict mode violation that occurs due to instant run
                // https://stackoverflow.com/questions/51021362/strictmode-disk-read-violation-on-empty-activitys-setcontentview
                val stackTrace = it.stackTrace.asList()
                stackTrace.reversed()
                stackTrace.subList(0, Math.min(stackTrace.size, MAX_STACK_DEPTH_TO_CHECK))
                var hasInflationTraceElement = false
                var hasDexTraceElement = false
                var hasPreferenceManagerInflation = false
                stackTrace.forEach { stackTraceElement ->
                    if (stackTraceElement.toString().contains("LayoutInflater.createView")) {
                        hasInflationTraceElement = true
                    }
                    if (stackTraceElement.toString().contains("BaseDexClassLoader.findClass")) {
                        hasDexTraceElement = true
                    }
                    if (stackTraceElement.toString().contains("PreferenceManager.inflateFromResource")) {
                        hasPreferenceManagerInflation = true
                    }
                }
                val isWhiteListed = (hasInflationTraceElement and hasDexTraceElement) or hasPreferenceManagerInflation
                if (!isWhiteListed) {
                    throw it
                } else {
                    Logger.warn(this, "Ignoring StrictMode Failure for WhiteListed violation", it.message)
                }
            })
        } else {
            threadPolicyBuilder.penaltyLog()
        }

        threadPolicyBuilder.build()
        StrictMode.setThreadPolicy(threadPolicyBuilder.build())


        val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
        vmPolicyBuilder.detectActivityLeaks()
        vmPolicyBuilder.detectFileUriExposure()
        vmPolicyBuilder.detectLeakedClosableObjects()
        vmPolicyBuilder.detectLeakedRegistrationObjects()
        vmPolicyBuilder.detectLeakedSqlLiteObjects()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            vmPolicyBuilder.detectCleartextNetwork()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vmPolicyBuilder.detectContentUriWithoutPermission()
        }
        // vmPolicyBuilder.detectUntaggedSockets() Note: We exclude this one as many of our 3p libraries fail it
        vmPolicyBuilder.penaltyLog()
        StrictMode.setVmPolicy(vmPolicyBuilder.build())
    }
}