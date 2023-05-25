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
import io.fyno.kotlin_sdk.FynoSdk
import io.fyno.kotlin_sdk.FynoUser
import io.fyno.kotlin_sdk.utils.Logger
import io.fyno.pushlibrary.firebase.FcmHandlerService
import io.fyno.pushlibrary.mipush.MiPushHelper
import io.fyno.pushlibrary.models.PushRegion

class FynoPush {
    fun showPermissionDialog(){
        val intent = Intent(FynoSdk.appContext, GetPermissions::class.java)
        if(Build.VERSION.SDK_INT <= 24)
            return
        val mNotificationManager = FynoSdk.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(!mNotificationManager.areNotificationsEnabled())
            FynoSdk.appContext.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or  Intent.FLAG_ACTIVITY_SINGLE_TOP ))
        else
            Logger.e(FcmHandlerService.TAG, "Notification Permissions are allowed")
    }


    private fun registerFCM(FCM_Integration_Id: String){
        try {
            val app = FirebaseApp.initializeApp(FynoSdk.appContext)
            saveFcmToken()
        } catch (e:Exception) {
            Logger.e(FcmHandlerService.TAG,"Unable to register FCM", e)
        }
    }
    private fun saveFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(
                        ContentValues.TAG,
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                }
                val token = task.result
                Logger.i(FynoSdk.TAG, "Fetching FCM registration token: $token")
                FynoUser.setFcmToken(token)
            }
        } catch (e: Exception) {
            e.message?.let { Log.e(FynoSdk.TAG, it) }
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
                    Logger.e(FynoSdk.TAG +"MiHelper", "setMiRegion: Region not found, Mi supports India,Europe,Russia and Global regions")
                }
            }
            MiPushClient.setRegion(miRegion)
        } catch (e: Exception) {
            Logger.e(FynoSdk.TAG +"MiHelper", "setMiRegion: Failed to set region",e)
        }
    }
    fun registerMiPush(App_Id: String, App_Key:String, Integration_Id: String){
        try {
            MiPushClient.registerPush(FynoSdk.appContext.applicationContext,App_Id,App_Key)
            val miToken = MiPushClient.getRegId(FynoSdk.appContext)
            FynoUser.setMiIntegration(Integration_Id)
            FynoUser.setMiToken(miToken)
            com.xiaomi.mipush.sdk.Logger.setLogger(FynoSdk.appContext, object : LoggerInterface {
                override fun setTag(tag: String?) {
                    Logger.i(MiPushHelper.TAG, "XMPushTag : $tag")
                }

                override fun log(message: String?) {
                    Logger.i(MiPushHelper.TAG, "$message")
                }

                override fun log(message: String?, throwable: Throwable?) {
                    Logger.e(MiPushHelper.TAG, "$message", throwable)
                }
            })
            Logger.i("MiToken", "Mi Push token registered: "+ MiPushClient.getRegId(FynoSdk.appContext))
        } catch (e: Exception) {
            Logger.e("MiToken", "Mi Push token registered: "+ MiPushClient.getRegId(FynoSdk.appContext))
        }
    }

    private fun identifyOem(model: String): Boolean {
        val brands = listOf("xiaomi", "oppo", "vivo", "huawei", "honor", "meizu", "oneplus", "realme", "tecno", "infinix")
        return brands.contains(model)
    }

    fun registerPush(App_Id: String? = "", App_Key: String? = "", pushRegion: PushRegion? = PushRegion.INDIA, FCM_Integration_Id: String? = "", Mi_Integration_Id: String = ""){
        showPermissionDialog()
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if(identifyOem(Build.MANUFACTURER.toLowerCase())){
                    if (!App_Id.isNullOrEmpty() && !App_Key.isNullOrEmpty() && !Mi_Integration_Id.isNullOrEmpty()) {
                        if (pushRegion != null) {
                            setMiRegion(pushRegion)
                        } else {
                            setMiRegion(PushRegion.INDIA)
                        }
                        registerMiPush(App_Id, App_Key, Mi_Integration_Id)
                    } else {
                        if (!FCM_Integration_Id.isNullOrEmpty()) {
                            registerFCM(FCM_Integration_Id)
                        } else {
                            Log.e("FynoSDK", "registerPush: FCM Integration ID is required, received null", )
                        }
                    };
                } else {
                    if (!FCM_Integration_Id.isNullOrEmpty()) {
                        registerFCM(FCM_Integration_Id)
                    } else {
                        Log.e("FynoSDK", "registerPush: FCM Integration ID is required, received null", )
                    }
                }
            },
            5000 // value in milliseconds
        )
    }
}