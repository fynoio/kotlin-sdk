package io.fyno.kotlin_sdk

import android.content.Context
import io.fyno.core.FynoCore
import io.fyno.core.utils.LogLevel
import io.fyno.pushlibrary.FynoPush
import io.fyno.pushlibrary.models.PushRegion
//import io.sentry.Sentry
//import io.sentry.protocol.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


public object FynoSdk {
    fun initialize(context: Context, workspaceId: String, token: String, userId: String? = null, version: String = "live") {
        runBlocking(Dispatchers.IO) {
            FynoCore.initialize(context, workspaceId, token, version)
            userId?.let {
                FynoCore.identify(uniqueId = it, update = true)
//                    Sentry.configureScope {
//                        val user = User()
//                        user.id = userId
//                        it.user = user
//                    }
            }
        }
        private fun logRegToken() {
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(
                            ContentValues.TAG,
                            "Fetching FCM registration token failed",
                            task.exception
                        )
                    }

                    // Get new FCM registration token
                    val token = task.result
                    setString("FCMToken",token)
                    // Log and toast
                    val msg = "FCM Registration token: $token"
                    Log.d(ContentValues.TAG, msg)
                    Toast.makeText(appContext, "New Token $msg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }

        fun updateStatus(callback_url: String, status: MessageStatus) {
            val postDataParams = JSONObject()
            val deviceState = JSONObject()
            val message = JSONObject()
            val callbackObject = callback_url.split("?").toTypedArray()
            deviceState.put("brand", Build.BRAND)
            deviceState.put("deviceName", Build.DEVICE)
            deviceState.put("deviceClass", "")
            deviceState.put("manufacturer", Build.MANUFACTURER)
            deviceState.put("deviceModel", Build.MODEL)
            deviceState.put("OsVersion", Build.VERSION.SDK_INT)
            message.put("deviceDetails", deviceState)
            postDataParams.put("status", status)
            postDataParams.put("eventType", "Delivery")
            postDataParams.put("timestamp", Date())
            postDataParams.put("message", message)
            postDataParams.put("dlr_params", callbackObject[1])
            Thread(Runnable {
                RequestHandler.requestPOST(callback_url, postDataParams)
            }).start()
            //TODO("Add extra data for action click as in which click and what is the action")
        }
    }

    fun registerPush(xiaomiApplicationId: String? = "", xiaomiApplicationKey: String? = "", pushRegion: PushRegion? = PushRegion.INDIA, integrationId: String = "") {
        FynoPush().registerPush(xiaomiApplicationId, xiaomiApplicationKey, pushRegion, integrationId)
    }

    fun identify(uniqueId: String, userName: String? = null) {
        runBlocking(Dispatchers.IO) {
                FynoCore.identify(uniqueId, userName, true)
        }
    }

    fun resetUser() {
        runBlocking(Dispatchers.IO) {
                FynoCore.resetUser()
        }
    }

    fun resetConfig() {
        FynoCore.resetConfig()
    }

    fun saveConfig(wsId: String, apiKey: String, fcmIntegration: String, miIntegration: String) {
        FynoCore.saveConfig(wsId, apiKey, fcmIntegration, miIntegration)
    }

    fun setLogLevel(level: LogLevel) {
        FynoCore.setLogLevel(level)
    }

    fun mergeProfile(oldDistinctId: String, newDistinctId: String) {
        runBlocking(Dispatchers.IO) {
                FynoCore.mergeProfile(oldDistinctId, newDistinctId)
        }
    }
}