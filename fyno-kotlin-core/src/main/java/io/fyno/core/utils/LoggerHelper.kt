package io.fyno.core.utils
//
//import io.sentry.Breadcrumb
//import io.sentry.Hint
//import io.sentry.Sentry
//import io.sentry.SentryEvent
//import io.sentry.SentryLevel
//import io.sentry.protocol.Message
import java.util.Date


class LoggerHelper {
    fun logDebug(TAG:String, message: String) {
//        val breadcrumb = Breadcrumb()
//        breadcrumb.type = TAG
//        breadcrumb.message = message
//        breadcrumb.level = SentryLevel.DEBUG
//        Sentry.addBreadcrumb(breadcrumb)
    }

    fun logInfo(TAG: String, message: String) {
//        val breadcrumb = Breadcrumb()
//        breadcrumb.type = TAG
//        breadcrumb.message = message
//        breadcrumb.level = SentryLevel.INFO
//        Sentry.addBreadcrumb(breadcrumb)
    }

    fun logWarning(TAG: String, message: String) {
//        val breadcrumb = Breadcrumb()
//        breadcrumb.type = TAG
//        breadcrumb.message = message
//        breadcrumb.level = SentryLevel.WARNING
//        Sentry.addBreadcrumb(breadcrumb)
//        Sentry.captureMessage(message, SentryLevel.WARNING)
    }

    fun logError(TAG: String, message: String, throwable: Throwable) {
//        val hint = Hint()
//        hint.set(TAG, message)
//        Sentry.captureException(throwable,hint)
    }
}