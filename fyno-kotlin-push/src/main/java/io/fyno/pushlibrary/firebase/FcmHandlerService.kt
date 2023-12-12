package io.fyno.pushlibrary.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.fyno.callback.FynoCallback
import io.fyno.callback.models.MessageStatus
import io.fyno.core.FynoUser
import io.fyno.core.utils.FynoContextCreator
import io.fyno.core.utils.Logger
import io.fyno.pushlibrary.FynoPush
import io.fyno.pushlibrary.helper.NotificationHelper.isFynoMessage
import io.fyno.pushlibrary.helper.NotificationHelper.rawMessage
import io.fyno.pushlibrary.helper.NotificationHelper.renderFCMMessage
import java.lang.Exception

open class FcmHandlerService : FirebaseMessagingService() {

    open fun onNotificationReceived(notification: RemoteMessage) {
        // Override Method
    }

    override fun onNewToken(token: String) {
        Logger.d(TAG, "onNewToken: $token")
        FynoUser.setFcmToken(token)
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            Logger.d(TAG, "onMessageReceived: ${message.rawData}")
            when {
                message.isFynoMessage() -> {
                    val context = if(FynoContextCreator.isInitialized())FynoContextCreator.context else this
                    renderFCMMessage(context, message.rawMessage())
                }
                else -> {
                    val callback = message.data["callback"]
                    if (!callback.isNullOrEmpty()) {
                        FynoCallback().updateStatus(this.applicationContext, callback, MessageStatus.RECEIVED)
                    }
                    super.onMessageReceived(message)
                }
            }
            onNotificationReceived(message)
        } catch (e:Exception) {
            Logger.e(TAG, e.message.toString(),e)
        }
    }

    companion object {
        const val TAG = "FYNO_FCM"
    }
}
