package io.fyno.pushlibrary.notificationIntents

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import io.fyno.callback.FynoCallback
import io.fyno.callback.models.MessageStatus
import io.fyno.core.FynoCore
import io.fyno.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
interface NotificationClickHandler {
    fun onNotificationClicked(extras: Bundle)
}
open class NotificationClickActivity : Activity() {
    private val ACTION_NOTIFICATION_CLICK: String = "io.fyno.pushlibrary.NOTIFICATION_ACTION"
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Logger.d("NotificationClick", "onCreate: ")
            super.onCreate(savedInstanceState)
            val launchintent = handleNotificationClick()
//            launchintent.action = ACTION_NOTIFICATION_CLICK
            launchintent.putExtra("io.fyno.pushlibrary.notification.action", "clicked")
            launchintent.putExtra("io.fyno.pushlibrary.notification.intent", intent.toString())
            intent.getStringExtra("io.fyno.kotlin_sdk.notificationIntents.extras")
                ?.let { launchintent.putExtra("io.fyno.pushlibrary.notification.payload", it) }
            startActivity(launchintent).runCatching {
            }
            val cintent = Intent()
            cintent.action = ACTION_NOTIFICATION_CLICK
            cintent.putExtra("io.fyno.pushlibrary.notification.action", "clicked")
            cintent.putExtra("io.fyno.pushlibrary.notification.intent", intent.toString())
            cintent.component = null
            cintent.getStringExtra("io.fyno.kotlin_sdk.notificationIntents.extras")
                ?.let { launchintent.putExtra("io.fyno.pushlibrary.notification.payload", it) }
            sendBroadcast(cintent)
//            FynoPush.PushObject.handleNotificationClick(cintent.getStringExtra("io.fyno.kotlin_sdk.notificationIntents.extras"))
            finish()
        } catch (e:Exception) {
            Logger.e("${FynoCore.TAG}-NotificationClick",e.message.toString(),e)
        }
    }

    private fun handleNotificationClick(): Intent {
        val callback = intent.extras!!.getString("io.fyno.kotlin_sdk.notificationIntents.notificationClickedReceiver.callback")
        val action = intent.extras!!.getString("io.fyno.kotlin_sdk.notificationIntents.notificationClickedReceiver.action")
        lateinit var intent: Intent;
        Logger.d("${FynoCore.TAG}-NotificationClicked", "onReceive: $callback")
        Logger.d("${FynoCore.TAG}-NotificationClick", "onStart: Click Activity Started")
        if (callback != null) {
            runBlocking(Dispatchers.IO) {
                    FynoCallback().updateStatus(applicationContext, callback, MessageStatus.CLICKED)
            }
        }
        if(action!=null) {
            if (action.startsWith("http://") or action.startsWith("https://")) {
                intent = Intent(Intent.ACTION_VIEW, Uri.parse(action)).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
                }
            } else if(action.startsWith("www.")){
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$action")).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
                }
            } else {
                intent = try {
                    Logger.d("${FynoCore.TAG}-NotificationClick", "handleNotificationClick: class name: ${Class.forName(action)}")
                    Intent(this.applicationContext, Class.forName(action))
                } catch (e: ClassNotFoundException) {
                    Logger.d("${FynoCore.TAG}-ClassNotFound", "onStart: ${e.message}")
                    this.packageManager.getLaunchIntentForPackage(this.packageName)!!
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            intent = this.packageManager.getLaunchIntentForPackage(this.packageName)!!
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        onAction()
        Logger.d("${FynoCore.TAG}-NotificationClick", "onStart: Click Activity Ended after launching $intent intent")
        return intent
    }

    open fun onAction() {}
}