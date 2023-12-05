package io.fyno.callback

import android.content.Context
import android.os.Build
import io.fyno.callback.models.MessageStatus
import io.fyno.core.RequestHandler
import io.fyno.core.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.time.*
import java.time.format.*
import java.util.*

class FynoCallback {
    private val TAG = "FYNO_CALLBACK"

    fun updateStatus(context: Context, callback_url: String, status: MessageStatus, action: JSONObject? = JSONObject("{}")) {
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

            val formattedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
            } else {
                Date()
            }

            val postDataParams = JSONObject().apply {
                put("status", status)
                put("eventType", "Delivery")
                put("timestamp", "$status at $formattedDate")
                put("message", message)
                put("dlr_params", callbackObject[1])
            }

            runBlocking(Dispatchers.IO) {
                RequestHandler.requestPOST(callback_url, postDataParams, context = context)
            }
        } catch (e: Exception) {
            Logger.w(TAG, "Unable to update message delivery status for url: $callback_url", e)
        }
    }
}
