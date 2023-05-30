package io.fyno.pushlibrary.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.fyno.callback.FynoCallback
import io.fyno.callback.models.MessageStatus
import io.fyno.kotlin_sdk.FynoUser
import io.fyno.kotlin_sdk.utils.FynoContextCreator
import io.fyno.pushlibrary.helper.NotificationHelper.renderFCMMessage
import io.fyno.pushlibrary.helper.NotificationHelper.isFynoMessage
import io.fyno.pushlibrary.helper.NotificationHelper.rawMessage


open class FcmHandlerService: FirebaseMessagingService() {

    open fun onNotificationReceived(notification: RemoteMessage){
        Log.d(TAG, "onNotificationReceived: ")
    }
    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken: $token")
        super.onNewToken(token)
    }

//    override fun onMessageReceived(message: RemoteMessage) {
//        Log.d(TAG, "onMessageReceived:"+message.data)
//        if(message.data.isNotEmpty()){
//            if(message.data.containsKey("provider")){
//                val provider = message.data["provider"]
//                if(provider?.lowercase() == "fyno"){
//                    val (builder,id, manager) = TemplateBuilder().build(message)
//                    manager.notify(id,builder)
//                }
//            }
//        }
//        message.data["callback"]?.let { FynoSdk.updateStatus(it, MessageStatus.RECEIVED) }
//    }
    override fun onMessageReceived(message: RemoteMessage) {
    Log.d(TAG, "onMessageReceived: ${message.rawData}")
        if(message.isFynoMessage()){
            renderFCMMessage(FynoContextCreator.context, message.rawMessage())
        } else {
            FynoCallback().updateStatus(message.data.get("callback").toString(), MessageStatus.RECEIVED)
            super.onMessageReceived(message)
        }
        onNotificationReceived(message)
    }

    companion object {
        const val TAG = "FYNO_FCM"
    }
}