package io.fyno.pushlibrary.notificationIntents

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.fyno.callback.FynoCallback
import io.fyno.callback.models.MessageStatus
import io.fyno.core.FynoCore
import io.fyno.core.utils.Logger
import io.fyno.pushlibrary.FynoPush
import io.fyno.pushlibrary.firebase.FcmHandlerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class NotificationDismissedReceiver : BroadcastReceiver() {
    val ACTION_DISMISSED_CLICK = "io.fyno.pushlibrary.NOTIFICATION_ACTION"
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val callback = intent.extras!!.getString("io.fyno.kotlin_sdk.notificationIntents.notificationDismissedReceiver.callback")
            val id = intent.extras!!.getInt("io.fyno.kotlin_sdk.notificationIntents.notificationDismissedReceiver.notificationId")
            Logger.d("NotificationDismissed", "onReceive: $callback")
            if (callback != null) {
                runBlocking(Dispatchers.IO) {
                    FynoCallback().updateStatus(context, callback, MessageStatus.DISMISSED)
                }
            }
            val cintent = Intent()
            cintent.action = ACTION_DISMISSED_CLICK
            cintent.putExtra("io.fyno.pushlibrary.notification.action", "dismissed")
            cintent.component = null
            FcmHandlerService.getInstance()::fynoCallback.let {it1->
                try {
                    FcmHandlerService.getInstance().fynoCallback.onNotificationDismissed(id.toString())
                } catch (e:Exception){
                    Logger.w("FynoSDK", "Push events are not available")
                }
            }
            context.applicationContext.sendBroadcast(cintent)
        }catch (e:Exception){
            Logger.e("${FynoCore.TAG}-PushDismissed", e.message.toString(),e)
        }

    }
}