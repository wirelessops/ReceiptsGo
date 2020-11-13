package co.smartreceipts.android.test.runner

import android.app.Instrumentation
import android.os.Bundle
import android.util.Log
import androidx.test.internal.runner.listener.InstrumentationResultPrinter
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.runner.Description
import org.junit.runner.notification.RunListener

internal class CrashingRunListener : RunListener() {

    @Volatile
    private lateinit var bundle: Bundle

    @Volatile
    private var isTestRunning = false

    override fun testRunStarted(description: Description) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()!!
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (isTestRunning) {
                isTestRunning = false
                reportTestFailure(
                        "Instrumentation test failed due to uncaught exception in thread [${thread.name}]:\n" +
                                Log.getStackTraceString(throwable)
                )
            }
            defaultHandler.uncaughtException(thread, throwable)
        }
    }

    override fun testStarted(description: Description) {
        val testClass = description.className
        val testName = description.methodName
        bundle = Bundle()
        bundle.putString(Instrumentation.REPORT_KEY_IDENTIFIER, CrashingRunListener::class.java.name)
        bundle.putString(InstrumentationResultPrinter.REPORT_KEY_NAME_CLASS, testClass)
        bundle.putString(InstrumentationResultPrinter.REPORT_KEY_NAME_TEST, testName)
        isTestRunning = true
    }

    override fun testFinished(description: Description?) {
        isTestRunning = false
    }

    /**
     * Reports that the test has failed, with the provided [message].
     */
    private fun reportTestFailure(message: String) {
        bundle.putString(InstrumentationResultPrinter.REPORT_KEY_STACK, message)
        InstrumentationRegistry.getInstrumentation()
                .sendStatus(InstrumentationResultPrinter.REPORT_VALUE_RESULT_FAILURE, bundle)
    }
}