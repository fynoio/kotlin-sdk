package io.fyno.pushlibrary.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.fyno.callback.FynoCallback
import io.fyno.callback.models.MessageStatus
import io.fyno.core.FynoUser
import io.fyno.core.utils.FynoContextCreator
import io.fyno.core.utils.Logger
import io.fyno.pushlibrary.FynoCallbacks
import io.fyno.pushlibrary.FynoPush
import io.fyno.pushlibrary.helper.NotificationHelper.isFynoMessage
import io.fyno.pushlibrary.helper.NotificationHelper.rawMessage
import io.fyno.pushlibrary.helper.NotificationHelper.renderFCMMessage
import java.lang.Exception

open class FcmHandlerService : FirebaseMessagingService() {
    internal lateinit var callback: FynoCallbacks;

    companion object {
        private var instance: FcmHandlerService? = null
        const val TAG = "FYNO_FCM"

        fun getInstance(): FcmHandlerService {
            if (instance == null) {
                instance = FcmHandlerService()
            }
            return instance!!
        }
    }

    fun setCallback(callback: FynoCallbacks) {
        this.callback = callback
    }

    override fun onNewToken(token: String) {
        Logger.d(TAG, "onNewToken: $token")
        if(FynoContextCreator.isInitialized())
            FynoUser.setFcmToken(token)
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            Logger.d(TAG, "onMessageReceived: ${message.rawData}")
            when {
                message.isFynoMessage() -> {
                    val context =
                        if (FynoContextCreator.isInitialized()) FynoContextCreator.getContext() else this
                    if (context != null) {
                        renderFCMMessage(context, message.rawMessage())
                    }
                }

                else -> {
                    val callback = message.data["callback"]
                    if (!callback.isNullOrEmpty()) {
                        FynoCallback().updateStatus(
                            this.applicationContext,
                            callback,
                            MessageStatus.RECEIVED
                        )
                    }
                    super.onMessageReceived(message)
                }
            }
            if (getInstance()::callback.isInitialized)
                getInstance().callback.onNotificationReceived(message)
        } catch (e:Exception) {
            Logger.e(TAG, e.message.toString(),e)
        }
    }
}
