package io.fyno.core_java.utils;

import android.util.Log;

public class Logger {
    private static LogLevel Level = LogLevel.VERBOSE;
    public static void setLevel(LogLevel newLevel) {

    }
    public static void i(String tag, String message) {
        if (Level.getNum() <= LogLevel.INFO.getNum()) {
            Log.i(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (Level.getNum() <= LogLevel.ERROR.getNum()) {
            Log.d(tag, message);
        }
    }

    public static void w(String tag, String message, Throwable throwable) {
        if (Level.getNum() <= LogLevel.ERROR.getNum()) {
            Log.d(tag, message, throwable);
        }
    }

    public static void e(String tag, String message) {
        if (Level.getNum() <= LogLevel.ERROR.getNum()) {
            Log.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (Level.getNum() <= LogLevel.ERROR.getNum()) {
            Log.e(tag, message, throwable);
        }
    }

    public static void d(String tag, String message) {
        if (Level.getNum() <= LogLevel.DEBUG.getNum()) {
            Log.d(tag, message);
        }
    }
}
