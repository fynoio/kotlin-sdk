package io.fyno.core

import android.util.Log
import io.fyno.core.utils.FynoUtils
import io.fyno.core.helpers.Config
import io.fyno.core.utils.FynoConstants
import io.fyno.core.utils.FynoContextCreator
import io.fyno.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

object FynoUser {
    private const val TAG = "FynoUser"
    private fun updatePush(tokenType: String, token: String) {
        val permissions = if (FynoCore.areNotificationPermissionsEnabled()) 1 else 0
        if (getIdentity().isNotEmpty() && getWorkspace().isNotEmpty()) {
            if((permissions.toString() != FynoContextCreator.sqlDataHelper!!.getConfigByKey(
                "fyno_push_permission"
            ).value) || (getIdentity() != FynoContextCreator.sqlDataHelper!!.getConfigByKey(
                    "fyno_push_distinct_id"
                ).value) || (FynoContextCreator.sqlDataHelper!!.getConfigByKey("fyno_push_permission_first_time").value == "false")
            ){
                runBlocking {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            FynoCore.getString("VERSION")
                            Logger.i(TAG, "updatePush: Updating token for user ${getIdentity()}")
                            val endpoint = FynoUtils().getEndpoint(
                                "update_channel",
                                getWorkspace(),
                                env = "live",
                                profile = getIdentity(),
                                newId = null,
                                version = FynoCore.getString("VERSION")
                            )
                            val requestBody = JSONObject().apply {
                                put("channel", JSONObject().apply {
                                    put("push", JSONArray(listOf(JSONObject().apply {
                                        put("token", "$tokenType:$token")
                                        put("integration_id", getFynoIntegration())
                                        put("status", permissions)
                                    })))
                                })
                            }
                            RequestHandler.requestPOST(endpoint, requestBody, "PATCH")
                            FynoContextCreator.sqlDataHelper?.insertConfigByKey(
                                Config(
                                    key = "fyno_${tokenType}_token",
                                    value = token
                                )
                            )
                            FynoContextCreator.sqlDataHelper?.insertConfigByKey(
                                Config(
                                    key = "fyno_push_permission",
                                    value = permissions.toString()
                                )
                            )
                            FynoContextCreator.sqlDataHelper?.insertConfigByKey(
                                Config(
                                    key = "fyno_push_distinct_id",
                                    value = getIdentity()
                                )
                            )
                            FynoContextCreator.sqlDataHelper?.insertConfigByKey(
                                Config(
                                    key = "fyno_push_permission_first_time",
                                    value = "true"
                                )
                            )
                        } catch (e: Exception) {
                            Logger.d(
                                TAG,
                                "Exception in set${tokenType}Token: ${e.message}"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun updateInapp(token: String, integrationId: String){
        if (getIdentity().isNotEmpty() && getWorkspace().isNotEmpty()) {
            runBlocking {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val endpoint = FynoUtils().getEndpoint(
                            "update_channel",
                            getWorkspace(),
                            env = "live",
                            profile = token,
                            newId = null,
                            version = FynoCore.getString("VERSION")
                        )
                        val requestBody = JSONObject().apply {
                            put("channel", JSONObject().apply {
                                put("inapp", JSONArray(listOf(JSONObject().apply {
                                    put("token", token)
                                    put("integration_id", integrationId)
                                    put("status", 1)
                                })))
                            })
                        }
                        RequestHandler.requestPOST(endpoint, requestBody, "PATCH")
                        FynoContextCreator.sqlDataHelper?.insertConfigByKey(
                            Config(
                                key = "fyno_inapp_token",
                                value = token
                            )
                        )
                    } catch (e: Exception) {
                        Logger.d(
                            TAG,
                            "Exception in set Inapp Token: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    private fun updateChannel(channel: String, token: String) {
        if (getIdentity().isNotEmpty() && getWorkspace().isNotEmpty()) {
            runBlocking {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val endpoint = FynoUtils().getEndpoint(
                            "update_channel",
                            getWorkspace(),
                            env = "live",
                            profile = getIdentity(),
                            newId = null,
                            version = FynoCore.getString("VERSION")
                        )
                        val requestBody = JSONObject().apply {
                            put("channel", JSONObject().apply {
                                put(channel, token)
                            })
                        }
                        RequestHandler.requestPOST(endpoint, requestBody, "PATCH")
                        FynoContextCreator.sqlDataHelper?.insertConfigByKey(
                            Config(
                                key = "fyno_${channel}",
                                value = token
                            )
                        )
                    } catch (e: Exception) {
                        Logger.d(TAG, "Exception in set${channel}: ${e.message}")
                    }
                }
            }
        }
    }

    private fun getToken(tokenType: String): String? {
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_${tokenType}_token")?.value
    }

    private fun getIntegrationId(tokenType: String): String {
        return when (tokenType) {
            "fcm_token" -> getFcmIntegration()
            "mi_token" -> getMiIntegration()
            else -> ""
        }
    }

    fun identify(distinctId: String) {
        FynoContextCreator.sqlDataHelper?.insertConfigByKey(Config(key = "fyno_distinct_id", value = distinctId))
    }

    fun getIdentity(): String {
        val distinctId = FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_distinct_id")?.value
            ?: ""
        return distinctId
    }

    fun setFcmToken(token: String) {
        updatePush("fcm_token", token)
    }

    fun getFcmToken(): String? {
        return getToken("fcm_token")
    }

    fun setMiToken(token: String) {
        updatePush("mi_token", token)
    }

    fun getMiToken(): String? {
        return getToken("mi_token")
    }

    fun setApnsToken(token: String) {
        updatePush("apns_token", token)
    }

    fun getApnsToken(): String {
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_apns_token")?.value ?: ""
    }

    fun setEmail(email: String) {
        updateChannel("email", email)
    }

    fun getEmail(): String {
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_email")?.value ?: ""
    }

    fun setMobile(mobile: String) {
        updateChannel("sms", mobile)
    }

    fun getMobile(): String {
        return getToken("mobile") ?: ""
    }

    fun setWhatsapp(mobile: String) {
        updateChannel("whatsapp", mobile)
    }

    fun getWhatsapp(): String {
        return getToken("wa_mobile") ?: ""
    }

    fun setWorkspace(workspaceId: String) {
        FynoContextCreator.sqlDataHelper?.insertConfigByKey(Config(key = "fyno_wsid", value = workspaceId))
    }

    fun getWorkspace(): String {
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_wsid")?.value ?: ""
    }

    fun setFynoIntegration(integrationId: String) {
        FynoContextCreator.sqlDataHelper?.insertConfigByKey(Config(key = "fyno_integration_id", value = integrationId))
    }

    fun getFynoIntegration(): String {
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_integration_id")?.value ?: ""
    }

    fun setFcmIntegration(integrationId: String) {
        FynoContextCreator.sqlDataHelper?.insertConfigByKey(Config(key = "fyno_fcm_integration_id", value = integrationId))
    }

    fun getFcmIntegration(): String {
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_fcm_integration_id")?.value ?: ""
    }

    fun setMiIntegration(integrationId: String) {
        FynoContextCreator.sqlDataHelper?.insertConfigByKey(Config(key = "fyno_mi_integration_id", value = integrationId))
    }

    fun getMiIntegration(): String {
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_mi_integration_id")?.value ?: ""
    }

    fun setApi(secret: String) {
        FynoContextCreator.sqlDataHelper?.insertConfigByKey(Config(key = "fyno_ws_secret", value = secret))
    }

    fun getApi(): String {
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_ws_secret")?.value ?: ""
    }

    fun setUserName(name: String) {
        FynoContextCreator.sqlDataHelper?.insertConfigByKey(Config(key = "fyno_user_name", value = name))
    }

    fun getUserName(): String {
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_user_name")?.value ?: ""
    }

    fun setJWTToken(jwtToken:String){
        FynoContextCreator.sqlDataHelper?.insertConfigByKey(Config(key = "jwt_token", value = jwtToken))
    }

    fun getJWTToken(): String{
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("jwt_token")?.value ?: ""
    }

    fun setInapp(distinct_id: String, integrationId: String){
        updateInapp(distinct_id, integrationId);
    }

    fun getInapp(): String {
        return FynoContextCreator.sqlDataHelper?.getConfigByKey("fyno_inapp_token")?.value ?: ""
    }
}