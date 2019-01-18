package co.smartreceipts.android.test.runner

import android.app.Application
import android.os.Bundle
import android.support.test.internal.runner.RunnerArgs
import android.support.test.runner.AndroidJUnitRunner
import android.util.Log


/**
 * Extends the base [AndroidJUnitRunner] implementation with support for a special
 * [BeforeApplicationOnCreate] annotation. This annotation can be supplied to allow us to run code
 * before [Application.onCreate] is called, allowing us to pre-load various portions of the app
 */
@Suppress("unused")
class ApplicationAwareAndroidJUnitRunner : AndroidJUnitRunner() {

    private val TAG = "ApplicationAwareAndroidJUnitRunner"

    private lateinit var runnerArgs: RunnerArgs

    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        Log.d(TAG, "Launching our ApplicationAwareAndroidJUnitRunner")
        runnerArgs = RunnerArgs.Builder().fromManifest(this).fromBundle(arguments).build()
        runnerArgs.tests?.let { tests ->
            tests.forEach {
                val methods = Class.forName(it.testClassName).declaredMethods
                methods.forEach { method ->
                    if (method.isAnnotationPresent(BeforeApplicationOnCreate::class.java)) {
                        Log.d(TAG, "Invoking method before Application#onCreate: $method")
                        try {
                            method.invoke(null)
                        } catch (e: Exception) {
                            // Note: I can't figure out how to make this fail other than this logging :/
                            if (e is IllegalArgumentException) {
                                Log.e(TAG, "Please ensure that the @BeforeApplicationOnCreate is only applied to a static method: $method")
                            } else {
                                Log.e(TAG, "An exception occurred in our local test stream. Abandoning test run", e)
                            }
                        }
                    }
                }
            }
        }
    }
}