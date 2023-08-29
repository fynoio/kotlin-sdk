package io.fyno.kotlin_sdk.utils

import android.util.Log

enum class LogLevel(val num: Int) {
    VERBOSE(1),
    DEBUG(2),
    INFO(3),
    ERROR(4),
    OFF(Int.MAX_VALUE)
}

object Logger {
    var Level = LogLevel.VERBOSE
    fun i(tag: String, message: String) {
        if (logAllowed(LogLevel.INFO.num))
            Log.i(tag, message)
    }
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (logAllowed(LogLevel.ERROR.num))
            Log.e(tag, message, throwable)
    }
    private fun logAllowed(level: Int): Boolean {
        return Level.num <= level
    }
}
