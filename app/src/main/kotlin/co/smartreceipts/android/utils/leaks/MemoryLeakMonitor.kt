package co.smartreceipts.android.utils.leaks

import android.app.Application
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.utils.RobolectricMonitor
import co.smartreceipts.android.utils.log.Logger
import com.squareup.leakcanary.LeakCanary
import javax.inject.Inject


/**
 * Provides a simple wrapper around [LeakCanary] to allows us to monitor for memory leaks within our
 * app
 */
@ApplicationScope
class MemoryLeakMonitor @Inject constructor(private val application: Application) {

    fun initialize() {
        when {
            RobolectricMonitor.areUnitTestsRunning() -> Logger.debug(this, "Ignoring LeakCanary as we're running unit tests...")
            LeakCanary.isInAnalyzerProcess(application) -> Logger.debug(this, "Ignoring this process as it's the LeakCanary analyzer one...")
            else -> LeakCanary.install(application)
        }
    }
}