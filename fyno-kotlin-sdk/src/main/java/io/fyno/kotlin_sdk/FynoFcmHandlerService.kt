package io.fyno.kotlin_sdk

import android.util.Log
import com.fynoio.pushsdk.models.MessageStatus
import com.fynoio.pushsdk.notification.TemplateBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

open class FynoFcmHandlerService: FirebaseMessagingService() {
    var TAG = "FynoSdk"
    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken: $token")
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "onMessageReceived:"+message.data)
        if(message.data.isNotEmpty()){
            if(message.data.containsKey("provider")){
                val provider = message.data["provider"]
                if(provider?.toLowerCase() == "fyno"){
                    val (builder,id, manager) = TemplateBuilder().build(message)
                    manager.notify(id,builder)
                }
            }
        }
        message.data["callback"]?.let { FynoSdk.updateStatus(it, MessageStatus.RECEIVED) }
        super.onMessageReceived(message)
    }
}