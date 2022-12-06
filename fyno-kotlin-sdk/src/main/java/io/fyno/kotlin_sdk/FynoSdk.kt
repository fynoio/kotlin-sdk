package io.fyno.kotlin_sdk

import android.app.Application
import android.content.ContextWrapper
import android.os.Build
import android.content.SharedPreferences
import org.json.JSONObject
import java.util.Date
import io.fyno.models.MessageStatus

class FynoSdk {
    companion object {
        private var fynoPreferences: SharedPreferences? = null
        fun setString(key: String?, value: String?) {
            val editor: SharedPreferences.Editor? = fynoPreferences?.edit()
            editor?.putString(key, value)
            editor?.apply()
        }
        fun initialize(context: Application, WSId: String) {
            if (WSId.isEmpty()) {
                throw IllegalArgumentException("Workspace Id is empty")
            }
            fynoPreferences = context.getSharedPreferences(
                context.packageName + "-" + "fynoio",
                ContextWrapper.MODE_PRIVATE
            )
            setString("WS_ID", WSId)
        }

        fun updateStatus(callback_url: String, status: MessageStatus) {
            val postDataParams = JSONObject()
            val deviceState = JSONObject()
            val message: JSONObject = JSONObject()
            var callback_object = callback_url.split("?").toTypedArray()
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
            postDataParams.put("dlr_params", callback_object[1])
            Thread(Runnable {
                RequestHandler.requestPOST(callback , postDataParams)
            }).start()
        }
    }
}
