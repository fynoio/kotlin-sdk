//package io.fyno.pushlibrary.notificationIntents
//
//import io.fyno.pushlibrary.ActionClickListener
//import io.fyno.pushlibrary.FynoPush
//import io.fyno.pushlibrary.NotificationClickListener
//import io.fyno.pushlibrary.NotificationReceivedListener
//
//class Handlers {
//    val listener = FynoPush.
//    fun handleNotificationClick(extras: String?) {
//        (this.listener as? NotificationClickListener)?.onNotificationClick(extras)
//    }
//
//    fun handleActionClick(extras: String?) {
//        (listener as? ActionClickListener)?.onActionClick(extras)
//    }
//
//    fun handleNotificationReceived(extras: String?) {
//        (listener as? NotificationReceivedListener)?.onNotificationReceived(extras)
//    }
//}