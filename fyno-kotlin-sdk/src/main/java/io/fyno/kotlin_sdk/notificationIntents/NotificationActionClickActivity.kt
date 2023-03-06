package com.fynoio.pushsdk.notificationIntents

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import io.fyno.kotlin_sdk.FynoSdk
import com.fynoio.pushsdk.models.MessageStatus


class NotificationActionClickActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("NotificationActionClick", "onCreate: ")
        handleActionClick()
        super.onCreate(savedInstanceState)
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleActionClick() {
        val callback = intent.extras!!.getString("com.fynoapp.notification.notificationIntents.notificationActionClickClickedReceiver.callback")
        val notificationId = intent.extras!!.getInt("com.fynoapp.notification.notificationIntents.notificationActionClickClickedReceiver.notificationId")
        var action = intent.action
        Log.d("NotificationActionClick", "onReceive: $callback")
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
                    intent = Intent(FynoSdk.appContext, Class.forName(action)).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                } catch (e: ClassNotFoundException) {
                    Log.e("ClassNotFound", "onStart: ${e.message}")
                    intent = FynoSdk.appContext.packageManager.getLaunchIntentForPackage(FynoSdk.appContext.packageName)
                        ?.apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                }

            }
            FynoSdk.appContext.applicationContext.startActivity(intent)
        }
        Log.d("NotificationActionClick", "onStart: ")
        val mNotificationManager = FynoSdk.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(notificationId)
    }
}