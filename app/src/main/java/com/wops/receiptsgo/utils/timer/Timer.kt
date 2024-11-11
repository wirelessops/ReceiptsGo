package com.wops.receiptsgo.utils.timer

import android.os.SystemClock
import com.wops.receiptsgo.BuildConfig
import com.wops.analytics.log.Logger


/**
 * A utility class to assist us with performing timed traces of various applications flows.
 *
 * Note: We only allow this to be used in debug builds
 */
object Timer {

    private var lastMeasuredTime : Long = SystemClock.uptimeMillis()
    private var measurementCount = 0

    @JvmStatic
    fun measure(caller: Any) {
        if (BuildConfig.DEBUG) {
            val callerMethod = Thread.currentThread().stackTrace[3]?.methodName
            val currentTime = SystemClock.uptimeMillis()
            if (measurementCount > 0) {
                Logger.debug(this, "[{}.{}]({}) Measuring a timer difference of {}ms", caller.javaClass.simpleName, callerMethod, measurementCount, currentTime - lastMeasuredTime)
            } else {
                Logger.debug(this, "[{}.{}] Initiating timer measurements from this call...", caller.javaClass.simpleName, callerMethod)
            }
            lastMeasuredTime = currentTime
            measurementCount += 1
        }
    }

}