package co.smartreceipts.android.utils

import android.os.Build


/**
 * A simple utility for us to determine if our current process is a Robolectric (ie Unit Test) one
 * or not
 */
class RobolectricMonitor {

    companion object {
        private const val ROBOLECTRIC_FINGERPRINT = "robolectric"

        fun areUnitTestsRunning() : Boolean = ROBOLECTRIC_FINGERPRINT == Build.FINGERPRINT
    }

}