package com.wops.receiptsgo.utils

import android.os.Handler
import android.os.Looper


/**
 * A utility method that allows us to run a particular operation on the UI Thread
 */
object UiThread {

    val uiThreadHandler = Handler(Looper.getMainLooper())

    inline fun run(crossinline function: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            function()
        } else {
            UiThread.uiThreadHandler.post {
                function()
            }
        }
    }
}