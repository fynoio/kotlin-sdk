package io.fyno.kotlin_sdk.notificationIntents

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.fyno.kotlin_sdk.FynoSdk
import io.fyno.kotlin_sdk.models.MessageStatus

class NotificationDismissedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val callback = intent.extras!!.getString("com.fynoapp.notification.notificationIntents.notificationDismissedReceiver.callback")
        Log.d("NotificationDismissed", "onReceive: $callback")
        if (callback != null) {
            FynoSdk.updateStatus(callback, MessageStatus.DISMISSED)
        }
    }
}