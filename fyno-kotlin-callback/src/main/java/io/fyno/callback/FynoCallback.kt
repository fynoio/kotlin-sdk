package io.fyno.callback

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import io.fyno.callback.models.MessageStatus
import io.fyno.core.FynoUser
import io.fyno.core.RequestHandler
import org.json.JSONObject
import java.util.*

class FynoCallback {
    val TAG = "FYNO_CALLBACK"
    lateinit var fynoContext: Context
    var fynoPreferences: SharedPreferences? = null
    fun setApiKey(context: Context, api_key: String){
        fynoContext = context
        fynoPreferences = context.getSharedPreferences(
            context.packageName + "-" + "fynoio",
            ContextWrapper.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor? = fynoPreferences?.edit()
        editor?.putString("SECRET", api_key)
        editor?.commit()
    }
    fun getApiKey(): String? {
        return FynoUser.getApi()
    }
    fun updateStatus(callback_url: String, status: MessageStatus, action: JSONObject? = JSONObject("{}")) {
        if(getApiKey().isNullOrEmpty()){
            Log.e(TAG, "Unable to update delivery status as api key is not set")
            return
        }
        try {
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
            if(action?.length()!! > 0){
                message.put("action", action)
            }
            message.put("deviceDetails", deviceState)
            postDataParams.put("status", status)
            postDataParams.put("eventType", "Delivery")
            postDataParams.put("timestamp", Date())
            postDataParams.put("message", message)
            postDataParams.put("dlr_params", callbackObject[1])
            Thread(Runnable {
                RequestHandler.requestPOST(callback_url, postDataParams)
            }).start()
        } catch (e: Exception) {
            Log.e(TAG, "Unable to update message delivery status", e)
        }
    }
}