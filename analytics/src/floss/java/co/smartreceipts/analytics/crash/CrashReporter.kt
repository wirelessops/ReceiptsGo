package co.smartreceipts.analytics.crash

import javax.inject.Inject

class CrashReporter @Inject constructor(): CrashReporterInterface {

    override fun initialize(isCrashTrackingEnabled: Boolean) {/*no-op*/}
}