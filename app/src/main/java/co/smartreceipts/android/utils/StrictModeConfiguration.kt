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

    /**
     * Enables strict mode
     */
    @JvmStatic
    fun enable() {
        Logger.debug(this, "Enabling strict mode")
        val threadPolicyBuilder = StrictMode.ThreadPolicy.Builder()
        threadPolicyBuilder.detectNetwork()
        threadPolicyBuilder.detectCustomSlowCalls()
        threadPolicyBuilder.detectDiskReads()
        threadPolicyBuilder.detectDiskWrites()
        // threadPolicyBuilder.detectUnbufferedIo() Note: Excluding as our 3p libraries can fail this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            threadPolicyBuilder.detectResourceMismatches()
        }

        // Note: We're temporarily dropping to penalty log to allow for SDK 28
        // StrictMode got better with target28, so it now notices more crashes.
        // Will fix each of these subsequently
        threadPolicyBuilder.penaltyLog()
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