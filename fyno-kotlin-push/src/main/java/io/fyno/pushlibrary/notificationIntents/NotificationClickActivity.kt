package io.fyno.pushlibrary.notificationIntents

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import io.fyno.callback.FynoCallback
import io.fyno.core.FynoCore
import io.fyno.callback.models.MessageStatus

class NotificationClickActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("NotificationClick", "onCreate: ")
        handleNotificationClick()
        super.onCreate(savedInstanceState)
        finish()
    }

    private fun handleNotificationClick() {
        val callback = intent.extras!!.getString("io.fyno.kotlin_sdk.notificationIntents.notificationClickedReceiver.callback")
        var action = intent.extras!!.getString("io.fyno.kotlin_sdk.notificationIntents.notificationClickedReceiver.action")
        Log.d("NotificationClicked", "onReceive: $callback")
        if (callback != null) {
            FynoCallback().updateStatus(callback, MessageStatus.CLICKED)
        }
        if(action!=null) {
            var intent: Intent? = null
            if (action.startsWith("http://") or action.startsWith("https://")) {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(action)).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            } else if(action.startsWith("www.")){
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$action")).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            } else {
                try {
                    intent = Intent(FynoCore.appContext, Class.forName(action))
                } catch (e: ClassNotFoundException) {
                    Log.e("ClassNotFound", "onStart: ${e.message}", )
                    intent = FynoCore.appContext.packageManager.getLaunchIntentForPackage(FynoCore.appContext.packageName)
                }

            }
            startActivity(intent)
        }
        Log.d("NotificationClick", "onStart: ")
    }
}