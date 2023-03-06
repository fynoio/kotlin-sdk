package io.fyno.kotlin_sdk

import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.fynoio.pushsdk.models.MessageStatus
import com.fynoio.pushsdk.models.PushRegion
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.xiaomi.channel.commonutils.android.Region
import com.xiaomi.mipush.sdk.MiPushClient
import org.json.JSONObject
import java.util.*

class FynoSdk {
    companion object {
        val TAG = "FynoSDK"
        @RequiresApi(Build.VERSION_CODES.N)
        fun showPermissionDialog(){
            val mNotificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if(!mNotificationManager.areNotificationsEnabled())
                appContext.startActivity(Intent(appContext, GetPermissions::class.java))
        }
        lateinit var appContext: Context
        private var fynoPreferences: SharedPreferences? = null
        private fun setString(key: String?, value: String?) {
            val editor: SharedPreferences.Editor? = fynoPreferences?.edit()
            editor?.putString(key, value)
            editor?.apply()
        }
        fun enableInapp(user_id: String, wsid: String, signature: String, flag: Boolean, listener: InAppListener){
            if(!flag)
                return
            else{
                Intent(appContext, FynoInappService::class.java).also { intent ->
                    appContext.startService(intent)
                    FynoInappService().initSocket(user_id, wsid, signature, listener)
                }
            }
        }
        fun setMiRegion(region: PushRegion){
            var miRegion = Region.Global
            when (region) {
                PushRegion.INDIA -> {
                    miRegion = Region.India
                }
                PushRegion.EUROPE -> {
                    miRegion = Region.Europe
                }
                PushRegion.RUSSIA -> {
                    miRegion = Region.Russia
                }
                PushRegion.GLOBAL -> {
                    miRegion = Region.Global
                }
                else -> {
                    Log.e(TAG +"MiHelper", "setMiRegion: Region not found, Mi supports India,Europe,Russia and Global regions")
                }
            }
            MiPushClient.setRegion(miRegion)
        }
        fun registerMiPush(APP_ID: String, APP_KEY:String){
            MiPushClient.registerPush(appContext,APP_ID,APP_KEY)
            setString("MiToken", MiPushClient.getRegId(appContext))
            Log.d("MiToken", "Mi Push token registered: "+MiPushClient.getRegId(appContext))
        }
        fun initialize(context: Context, WSId: String) {
            if (WSId.isEmpty()) {
                throw IllegalArgumentException("Workspace Id is empty")
            }
            if(context == null) {
                throw IllegalArgumentException("Must initialize with the app context")
            }
            appContext = context
            fynoPreferences = context.getSharedPreferences(
                context.packageName + "-" + "fynoio",
                ContextWrapper.MODE_PRIVATE
            )
            setString("WS_ID", WSId)
            registerFCM()
        }

        private fun registerFCM(){
            FirebaseApp.initializeApp(appContext)
            try {
                logRegToken()
            } catch (e:Exception) {
                throw IllegalStateException(e.message)
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
}