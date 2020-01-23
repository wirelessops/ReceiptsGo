package co.smartreceipts.android.utils.leaks

import android.app.Application
import android.os.Build
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.utils.RobolectricMonitor
import co.smartreceipts.analytics.log.Logger
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
            (Build.VERSION_CODES.O .. Build.VERSION_CODES.P).contains(Build.VERSION.SDK_INT) -> Logger.debug(this, "Ignoring LeakCanary on Android {} due to an Android bug. See https://github.com/square/leakcanary/issues/1081", Build.VERSION.SDK_INT)
            else -> LeakCanary.install(application)
        }
    }
}