package co.smartreceipts.android.utils.log


import androidx.annotation.AnyThread
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

/**
 * A utility class that allows us to perform logging more easily. All static methods rely on a dedicated
 * executor to queue up logging requests to ensure that these never occur on the UiThread
 */
object Logger {

    private val loggingExecutor = Executors.newSingleThreadExecutor()
    private val loggerCache = HashMap<Class<*>, Logger>()

    @JvmStatic
    @AnyThread
    fun debug(caller: Any, msg: String) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).debug(msg)
        }
    }

    @JvmStatic
    @AnyThread
    fun debug(caller: Any, msg: String, t: Throwable) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).debug(msg, t)
        }
    }

    @JvmStatic
    @AnyThread
    fun debug(caller: Any, format: String, vararg arg: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).debug(format, *arg)
        }
    }

    @JvmStatic
    @AnyThread
    fun debug(caller: Any, format: String, arg: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).debug(format, arg)
        }
    }

    @JvmStatic
    @AnyThread
    fun debug(caller: Any, format: String, arg1: Any?, arg2: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).debug(format, arg1, arg2)
        }
    }

    @JvmStatic
    @AnyThread
    fun info(caller: Any, msg: String) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).info(msg)
        }
    }

    @JvmStatic
    @AnyThread
    fun info(caller: Any, msg: String, t: Throwable) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).info(msg, t)
        }
    }

    @JvmStatic
    @AnyThread
    fun info(caller: Any, format: String, vararg arg: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).info(format, *arg)
        }
    }

    @JvmStatic
    @AnyThread
    fun info(caller: Any, format: String, arg: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).info(format, arg)
        }
    }

    @JvmStatic
    @AnyThread
    fun info(caller: Any, format: String, arg1: Any?, arg2: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).info(format, arg1, arg2)
        }
    }

    @JvmStatic
    @AnyThread
    fun warn(caller: Any, msg: String) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).warn(msg)
        }
    }

    @JvmStatic
    @AnyThread
    fun warn(caller: Any, msg: String, t: Throwable) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).warn(msg, t)
        }
    }

    @JvmStatic
    @AnyThread
    fun warn(caller: Any, t: Throwable) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).warn(null, t)
        }
    }

    @JvmStatic
    @AnyThread
    fun warn(caller: Any, format: String, vararg arg: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).warn(format, *arg)
        }
    }

    @JvmStatic
    @AnyThread
    fun warn(caller: Any, format: String, arg: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).warn(format, arg)
        }
    }

    @JvmStatic
    @AnyThread
    fun warn(caller: Any, format: String, arg1: Any?, arg2: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).warn(format, arg1, arg2)
        }
    }

    @JvmStatic
    @AnyThread
    fun error(caller: Any, msg: String) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).error(msg)
        }
    }

    @JvmStatic
    @AnyThread
    fun error(caller: Any, t: Throwable?) {
        loggingExecutor.execute {
            if (t != null && t.stackTrace != null) {
                getLoggerForCaller(caller).error("", t)
            } else {
                getLoggerForCaller(caller).error("Insufficient logging details available for error")
            }
        }
    }

    @JvmStatic
    @AnyThread
    fun error(caller: Any, msg: String, t: Throwable) {
        loggingExecutor.execute {
            if (t.stackTrace != null) {
                getLoggerForCaller(caller).error(msg, t)
            } else {
                getLoggerForCaller(caller).error(msg)
            }
        }
    }

    @JvmStatic
    @AnyThread
    fun error(caller: Any, format: String, vararg arg: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).error(format, *arg)
        }
    }

    @JvmStatic
    @AnyThread
    fun error(caller: Any, format: String, arg: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).error(format, arg)
        }
    }

    @JvmStatic
    @AnyThread
    fun error(caller: Any, format: String, arg1: Any?, arg2: Any?) {
        loggingExecutor.execute {
            getLoggerForCaller(caller).error(format, arg1, arg2)
        }
    }

    /**
     * Note: This class was intentionally designed to not be thread safe as there's relatively low
     * overhead to creating logging instances, so I don't mind if there are a few duplicates due to
     * the lack of thread safety
     */
    private fun getLoggerForCaller(caller: Any): Logger {
        val clazz = caller as? Class<*> ?: caller.javaClass
        var logger = loggerCache[clazz]
        if (logger == null) {
            logger = LoggerFactory.getLogger(clazz)
            loggerCache[clazz] = logger
        }
        return logger!!
    }
}
