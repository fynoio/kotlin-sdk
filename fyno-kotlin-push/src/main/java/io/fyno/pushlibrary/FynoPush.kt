package io.fyno.pushlibrary

import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.xiaomi.channel.commonutils.android.Region
import com.xiaomi.channel.commonutils.logger.LoggerInterface
import com.xiaomi.mipush.sdk.MiPushClient
import io.fyno.core.FynoCore
import io.fyno.core.FynoUser
import io.fyno.core.utils.FynoContextCreator
import io.fyno.core.utils.Logger
import io.fyno.pushlibrary.firebase.FcmHandlerService
import io.fyno.pushlibrary.mipush.MiPushHelper
import io.fyno.pushlibrary.models.PushRegion

class FynoPush {
    fun showPermissionDialog(){
        Log.i(FynoCore.TAG, "showPermissionDialog: Im triggered")
        if(Build.VERSION.SDK_INT <= 24)
            return

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(FynoCore.appContext, GetPermissions::class.java)
            val mNotificationManager = FynoCore.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if(!mNotificationManager.areNotificationsEnabled())
                FynoCore.appContext.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or  Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK ))
            else
                Logger.i(FcmHandlerService.TAG, "Notification Permissions are allowed")
        },5000);
    }


    private fun registerFCM(FCM_Integration_Id: String){
        try {
            FynoUser.setFcmIntegration(FCM_Integration_Id)
            FirebaseApp.initializeApp(FynoContextCreator.context.applicationContext)
            saveFcmToken()
        } catch (e:Exception) {
            Logger.w(FcmHandlerService.TAG,"Unable to register FCM", e)
        }
    }
    private fun saveFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result.toString()
                    Logger.i(FynoCore.TAG, "Fetching FCM registration token: $token")
                    FynoUser.setFcmToken(token)
                    return@addOnCompleteListener
                } else {
                    Logger.d(
                        ContentValues.TAG,
                        "Fetching FCM registration token failed - ${task.exception}",
                    )
                    return@addOnCompleteListener
                }
            }
        } catch (e: Exception) {
            e.message?.let { Logger.d(FynoCore.TAG, it) }
        }
    }
    fun setMiRegion(region: PushRegion){
        try {
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
                    Logger.w(FynoCore.TAG +"MiHelper", "setMiRegion: Region not found, Mi supports India,Europe,Russia and Global regions")
                }
            }
            MiPushClient.setRegion(miRegion)
        } catch (e: Exception) {
            Logger.w(FynoCore.TAG +"MiHelper", "setMiRegion: Failed to set region",e)
        }
    }
    fun registerMiPush(App_Id: String, App_Key:String, Integration_Id: String){
        try {
            MiPushClient.registerPush(FynoCore.appContext,App_Id,App_Key)
            FynoUser.setMiIntegration(Integration_Id)
            com.xiaomi.mipush.sdk.Logger.setLogger(FynoCore.appContext, object : LoggerInterface {
                override fun setTag(tag: String?) {
                    Logger.i(MiPushHelper.TAG, "XMPushTag : $tag")
                }

                override fun log(message: String?) {
                    Logger.i(MiPushHelper.TAG, "$message")
                }

                override fun log(message: String?, throwable: Throwable?) {
                    Logger.w(MiPushHelper.TAG, "$message", throwable)
                }
            })
            val miToken = MiPushClient.getRegId(FynoCore.appContext)
            FynoUser.setMiToken(miToken)
            Logger.i("MiToken", "Mi Push token registered: $miToken")
        } catch (e: Exception) {
            Logger.w("MiToken", "Mi Push token registered: "+ MiPushClient.getRegId(FynoCore.appContext))
        }
    }

    private fun identifyOem(model: String): Boolean {
        val brands = listOf("xiaomi", "oppo", "vivo", "huawei", "honor", "meizu", "oneplus", "realme", "tecno", "infinix")
        return brands.contains(model)
    }

    fun registerPush(App_Id: String? = "", App_Key: String? = "", pushRegion: PushRegion? = PushRegion.INDIA, Fyno_Integration_Id: String = "") {
        if (identifyOem(Build.MANUFACTURER.lowercase())) {
            if (!App_Id.isNullOrEmpty() && !App_Key.isNullOrEmpty() && !Fyno_Integration_Id.isNullOrEmpty()) {
                if (pushRegion != null) {
                    setMiRegion(pushRegion)
                } else {
                    setMiRegion(PushRegion.INDIA)
                }
                registerMiPush(App_Id, App_Key, Fyno_Integration_Id)
            } else {
                if (!Fyno_Integration_Id.isNullOrEmpty()) {
                    registerFCM(Fyno_Integration_Id)
                } else {
                    Logger.w(
                        "FynoSDK",
                        "registerPush: FCM Integration ID is required, received null",
                    )
                }
            };
        } else {
            if (!Fyno_Integration_Id.isNullOrEmpty()) {
                registerFCM(Fyno_Integration_Id)
            } else {
                Logger.i("FynoSDK", "registerPush: FCM Integration ID is required, received null",)
            }
        }
    }
}