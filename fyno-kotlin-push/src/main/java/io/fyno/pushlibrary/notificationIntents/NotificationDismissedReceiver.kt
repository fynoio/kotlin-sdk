package io.fyno.pushlibrary.notificationIntents

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.fyno.callback.FynoCallback
import io.fyno.callback.models.MessageStatus

class NotificationDismissedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val callback = intent.extras!!.getString("io.fyno.kotlin_sdk.notificationIntents.notificationDismissedReceiver.callback")
        Log.d("NotificationDismissed", "onReceive: $callback")
        if (callback != null) {
            FynoCallback().updateStatus(callback, MessageStatus.DISMISSED)
        }
    }
}