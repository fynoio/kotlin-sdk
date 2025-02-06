package io.fyno.core.utils

import android.util.Log
//import io.sentry.Sentry

enum class LogLevel(val num: Int) {
    VERBOSE(1),
    DEBUG(2),
    INFO(3),
    ERROR(4),
    OFF(Int.MAX_VALUE)
}

public object Logger {
    var Level = LogLevel.INFO

    fun i(tag: String, message: String) {
        if (Level <= LogLevel.INFO) {
            Log.i(tag, message)
            LoggerHelper().logInfo(tag, message)
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (Level <= LogLevel.ERROR) {
            Log.d(tag, message+throwable)
            LoggerHelper().logWarning(tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        if (Level <= LogLevel.ERROR) {
            Log.e(tag, message, throwable)
            LoggerHelper().logError(tag, message,throwable)
//            Sentry.captureException(Exception("$tag: $message"))
        }
    }

    fun d(tag: String, message: String, throwable: Throwable? = null) {
        if (Level <= LogLevel.DEBUG) {
            Log.d(tag, message)
            LoggerHelper().logDebug(tag, message)
        }
    }
}
