package io.fyno.pushlibrary

import com.google.firebase.messaging.RemoteMessage

interface FynoCallbacks {
    fun onNotificationReceived(remoteMessage: RemoteMessage)
    fun onNotificationClicked(payload: String)
    fun onNotificationDismissed(payload: String)
}