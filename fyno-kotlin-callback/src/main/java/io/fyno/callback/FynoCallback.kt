package io.fyno.callback

import android.os.Build
import android.util.Log
import io.fyno.callback.models.MessageStatus
import io.fyno.core.RequestHandler
import io.fyno.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.*

class FynoCallback {
    private val TAG = "FYNO_CALLBACK"

    fun updateStatus(callback_url: String, status: MessageStatus, action: JSONObject? = JSONObject("{}")) {
        try {
            val deviceState = JSONObject()
            val message = JSONObject()
            val callbackObject = callback_url.split("?").toTypedArray()
            deviceState.apply {
                put("brand", Build.BRAND)
                put("deviceName", Build.DEVICE)
                put("deviceClass", "")
                put("manufacturer", Build.MANUFACTURER)
                put("deviceModel", Build.MODEL)
                put("OsVersion", Build.VERSION.SDK_INT)
            }

            if (action?.length()!! > 0) {
                message.put("action", action)
            }
            message.put("deviceDetails", deviceState)

            val postDataParams = JSONObject().apply {
                put("status", status)
                put("eventType", "Delivery")
                put("timestamp", Date())
                put("message", message)
                put("dlr_params", callbackObject[1])
            }

            runBlocking(Dispatchers.IO) {
                RequestHandler.requestPOST(callback_url, postDataParams)
            }
        } catch (e: Exception) {
            Logger.w(TAG, "Unable to update message delivery status for url: $callback_url", e)
        }
    }
}
