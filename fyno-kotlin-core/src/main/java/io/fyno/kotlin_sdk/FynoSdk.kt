package io.fyno.kotlin_sdk

import android.annotation.SuppressLint
import android.content.*
import android.util.Log
import com.fynoio.pushsdk.utils.FynoUtils
import io.fyno.kotlin_sdk.helpers.SQLDataHelper
import io.fyno.kotlin_sdk.utils.FynoContextCreator
import io.fyno.kotlin_sdk.utils.LogLevel
import io.fyno.kotlin_sdk.utils.Logger
import io.fyno.kotlin_sdk.utils.NetworkDetails
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class FynoSdk {
    companion object {
        val TAG = "FynoSDK"
        lateinit var appContext: Context
        var fynoPreferences: SharedPreferences? = null
        fun setString(key: String?, value: String?) {
            val editor: SharedPreferences.Editor? = fynoPreferences?.edit()
            editor?.putString(key, value)
            editor?.apply()
        }
        fun setFlag(key: String?, value: Boolean?) {
            val editor: SharedPreferences.Editor? = fynoPreferences?.edit()
            if (value != null) {
                editor?.putBoolean(key, value)
            }
            editor?.apply()
        }
        fun getString(key: String): String? {
            val pref: SharedPreferences? = fynoPreferences
            if (pref != null) {
                return pref.getString(key, "")
            }
            return ""
        }
        internal fun getFlag(key: String): Boolean? {
            val pref: SharedPreferences? = fynoPreferences
            if (pref != null) {
                return pref.getBoolean(key, false)
            }
            return false
        }

        fun initialize(context: Context, WSId: String, integrationId: String, token: String, userId: String?=null) {
            if (WSId.isEmpty()) {
                throw IllegalArgumentException("Workspace Id is empty")
            }
            appContext = context
            FynoContextCreator.context = context
            NetworkDetails.getNetworkType()
            fynoPreferences = context.getSharedPreferences(
                context.packageName + "-" + "fynoio",
                ContextWrapper.MODE_PRIVATE
            )
            setLogLevel(LogLevel.VERBOSE)
            setString("WS_ID", WSId)
            setString("INTEGRATION", integrationId)
            setString("SECRET", token)
            FynoUser.setWorkspace(WSId)
            FynoUser.setFcmIntegration(integrationId)
            FynoUser.setApi(token)
            if(userId.isNullOrBlank() && FynoUser.getIdentity().isNullOrBlank()){
                val uuid = UUID.randomUUID().toString()
                Thread(Runnable {
                    RequestHandler.requestPOST(FynoUtils().getEndpoint("create_profile", FynoUser.getWorkspace()), JSONObject().put("distinct_id", uuid), "POST")
                    identify(uuid)
                    setFlag("isDirty", true)
                }).start()
            } else if(userId != null && userId.isNotEmpty()){
                Thread(Runnable {
                    RequestHandler.requestPOST(FynoUtils().getEndpoint("upsert_profile", FynoUser.getWorkspace(), profile = userId), getParamsObj(userId), "PUT")
                    setFlag("isDirty", false)
                }).start()
            }
        }

        fun initialize(context: Context, WSId: String, integrationId: String, token: String) {
            if (WSId.isEmpty()) {
                throw IllegalArgumentException("Workspace Id is empty")
            }
            FynoContextCreator.context = context
            appContext = context
            NetworkDetails.getNetworkType()
            fynoPreferences = context.getSharedPreferences(
                context.packageName + "-" + "fynoio",
                ContextWrapper.MODE_PRIVATE
            )
            setLogLevel(LogLevel.VERBOSE)
            setString("WS_ID", WSId)
            setString("INTEGRATION", integrationId)
            setString("SECRET", token)
            FynoUser.setWorkspace(WSId)
            FynoUser.setFcmIntegration(integrationId)
            FynoUser.setApi(token)
            if(FynoUser.getIdentity().isNullOrEmpty()){
                val uuid = UUID.randomUUID().toString()
                Thread(Runnable {
                    RequestHandler.requestPOST(FynoUtils().getEndpoint("create_profile", FynoUser.getWorkspace()), JSONObject().put("distinct_id", uuid), "POST")
                    identify(uuid)
                    setFlag("isDirty", true)
                }).start()
            }
        }

        fun identify(uniqueId: String, update: Boolean? = true) {
            try {
                val oldDistinctId = FynoUser.getIdentity()
                if (oldDistinctId == uniqueId) {
                    return
                }
                FynoUser.identify(uniqueId)
                Log.d(TAG, "identify: $uniqueId, $oldDistinctId")
                if (update == true && getFlag("isDirty") == false) {
                    if(oldDistinctId.isNullOrBlank()){
                        Thread(Runnable {
                            RequestHandler.requestPOST(FynoUtils().getEndpoint("upsert_profile", FynoUser.getWorkspace(), profile = uniqueId), getParamsObj(uniqueId), "PUT")
                            setFlag("isDirty", false)
                        }).start()
                    } else {
                        Thread(Runnable {
                            RequestHandler.requestPOST(FynoUtils().getEndpoint("merge_profile", FynoUser.getWorkspace(), profile = oldDistinctId, newId = uniqueId), null, "PATCH")
                            setFlag("isDirty", true)
                        }).start()
                    }
                }
            } catch (e: Exception) {
                Logger.e(FynoSdk.TAG, "Unable to update user profile", e)
            }
        }
        private fun getParamsObj(uniqueId: String): JSONObject {
            val fcmToken = FynoUser.getFcmToken()
            val jsonObject = JSONObject()
            val channelObj = JSONObject()
            val pushObj = JSONArray()
            jsonObject.put("distinct_id", uniqueId)
            if (!fcmToken.isNullOrBlank()) {
                pushObj.put(JSONObject().put("token", "fcm_token:$fcmToken").put("integration_id",
                    FynoUser.getFcmIntegration()).put("status", 1))
            }
            val xiaomiToken = FynoUser.getMiToken()
            if (!xiaomiToken.isNullOrBlank()) {
                pushObj.put(JSONObject().put("token", "mi_token:$xiaomiToken").put("integration_id",
                    FynoUser.getMiIntegration()).put("status", 1))
            }
            channelObj.put("push", pushObj)
            jsonObject.put("channel", channelObj)
            return jsonObject
        }

        fun resetUser() {
            val uuid = UUID.randomUUID().toString()
            val deleteJson = JSONObject().put("push",JSONArray().put("fcm_token:${FynoUser.getFcmToken()}").put("mi_token:${FynoUser.getMiToken()}"))
            val fcmToken = FynoUser.getFcmToken()
            val jsonObject = JSONObject()
            val channelObj = JSONObject()
            val pushObj = JSONArray()
            jsonObject.put("distinct_id", uuid)
            if (!fcmToken.isNullOrBlank()) {
                pushObj.put(JSONObject().put("token", "fcm_token:$fcmToken").put("integration_id",
                    FynoUser.getFcmIntegration()).put("status", 1))
            }
            val xiaomiToken = FynoUser.getMiToken()
            if (!xiaomiToken.isNullOrBlank()) {
                pushObj.put(JSONObject().put("token", "mi_token:$xiaomiToken").put("integration_id",
                    FynoUser.getMiIntegration()).put("status", 1))
            }
            channelObj.put("push", pushObj)
            jsonObject.put("channel", channelObj)
            Thread(Runnable {
                RequestHandler.requestPOST(FynoUtils().getEndpoint("delete_channel", FynoUser.getWorkspace(), profile = FynoUser.getIdentity()), deleteJson, "POST")
                RequestHandler.requestPOST(FynoUtils().getEndpoint("create_profile", FynoUser.getWorkspace()), jsonObject, "POST")
                identify(uuid, false)
                setFlag("isDirty", false)
                FynoUser.identify(uuid)
            }).start()
        }
        fun resetConfig() {
            FynoUser.setWorkspace("")
            FynoUser.setApi("")
            FynoUser.setMiIntegration("")
            FynoUser.setFcmIntegration("")
        }
        fun saveConfig(wsId: String, apiKey: String,fcmIntegration: String, miIntegration: String) {
            FynoUser.setWorkspace(wsId)
            FynoUser.setApi(apiKey)
            FynoUser.setMiIntegration(miIntegration)
            FynoUser.setFcmIntegration(fcmIntegration)
        }
        fun setLogLevel(level: LogLevel) {
            Logger.Level = level
        }
    }
}