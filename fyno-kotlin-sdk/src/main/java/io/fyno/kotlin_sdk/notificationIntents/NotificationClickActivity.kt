package com.fynoio.pushsdk.notificationIntents

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.fyno.kotlin_sdk.FynoSdk
import com.fynoio.pushsdk.models.MessageStatus

class NotificationClickActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("NotificationClick", "onCreate: ")
        handleNotificationClick()
        super.onCreate(savedInstanceState)
    }

    private fun handleNotificationClick() {
        val callback = intent.extras!!.getString("com.fynoapp.notification.notificationIntents.notificationClickedReceiver.callback")
        var action = intent.extras!!.getString("com.fynoapp.notification.notificationIntents.notificationClickedReceiver.action")
        Log.d("NotificationClicked", "onReceive: $callback")
        if (callback != null) {
            FynoSdk.updateStatus(callback, MessageStatus.CLICKED)
        }
        if(action!=null) {
            var intent: Intent? = null
            if (action.startsWith("http://") or action.startsWith("https://")) {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(action)).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else if(action.startsWith("www.")){
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$action")).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                try {
                    intent = Intent(FynoSdk.appContext, Class.forName(action))
                } catch (e: ClassNotFoundException) {
                    Log.e("ClassNotFound", "onStart: ${e.message}", )
                    intent = FynoSdk.appContext.packageManager.getLaunchIntentForPackage(FynoSdk.appContext.packageName)
                }

            }
            FynoSdk.appContext.applicationContext.startActivity(intent)
        }
        Log.d("NotificationClick", "onStart: ")
    }
}