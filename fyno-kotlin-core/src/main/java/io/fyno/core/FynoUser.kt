package io.fyno.core

import com.fynoio.pushsdk.utils.FynoUtils
import io.fyno.core.helpers.Config
import io.fyno.core.utils.FynoContextCreator
import org.json.JSONObject

object FynoUser {
    fun identify(distinctId: String) {
        FynoContextCreator.sqlDataHelper.insert_configByKey(Config(key = "fyno_distinct_id", value = distinctId))
    }
    fun getIdentity(): String {
        return FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_distinct_id").value ?: ""
    }
    fun setFcmToken(token: String) {
        FynoContextCreator.sqlDataHelper.insert_configByKey(Config(key = "fyno_fcm_token", value = token))
        Thread(Runnable {
            RequestHandler.requestPOST(FynoUtils().getEndpoint("update_channel", getWorkspace(), profile = getIdentity()),
                JSONObject("{channel: {push: [{token: 'fcm_token:$token',integration_id: ${getFcmIntegration().toString()}, status: 0}]}}")
                , "PATCH")
        }).start()

    }
    fun getFcmToken(): String? {
        return if(FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_fcm_token").value != null)
            FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_fcm_token").value.toString()
        else
            null
    }
    fun setMiToken(token: String) {
        FynoContextCreator.sqlDataHelper.insert_configByKey(Config(key = "fyno_xiomi_token", value = token))
        if(!getIdentity().isNullOrEmpty() && !getWorkspace().isNullOrEmpty()){
            Thread(Runnable {
                RequestHandler.requestPOST(FynoUtils().getEndpoint("update_channel", getWorkspace(), profile = getIdentity()),
                    JSONObject("{channel: {push: [{token: 'mi_token:$token',integration_id: ${getMiIntegration().toString()}, status: 0}]}}")
                    , "PATCH")
            }).start()
        }
    }
    fun getMiToken(): String? {
        return if(FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_xiomi_token").value != null)
            FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_xiomi_token").value.toString()
        else
            null
    }
    fun setApnsToken(token: String) {
        FynoContextCreator.sqlDataHelper.insert_configByKey(Config(key = "fyno_apns_token", value = token))
        if(!getIdentity().isNullOrEmpty() && !getWorkspace().isNullOrEmpty())
            Thread(Runnable { RequestHandler.requestPOST(FynoUtils().getEndpoint("update_channel", getWorkspace(), profile = getIdentity()),
                JSONObject("{channel: {push: [{token: 'apns_token:$token', status: 0}]}}")
                , "PATCH") }).start()
    }
    fun getApnsToken(): String {
        return FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_apns_token").value.toString()
    }
    fun setEmail(token: String) {
        FynoContextCreator.sqlDataHelper.insert_configByKey(Config(key = "fyno_apns_token", value = token))
        if(!getIdentity().isNullOrEmpty() && !getWorkspace().isNullOrEmpty())
            Thread(Runnable {
                RequestHandler.requestPOST(FynoUtils().getEndpoint("update_channel", getWorkspace(), profile = getIdentity()),
                    JSONObject("{channel: {email: $token}}")
                    , "PATCH")
            }).start()
    }
    fun getEmail(): String {
        return (FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_apns_token").value.toString())
    }
    fun setMobile(token: String) {
        FynoContextCreator.sqlDataHelper.insert_configByKey(Config(key = "fyno_apns_token", value = token))
        if(!getIdentity().isNullOrEmpty() && !getWorkspace().isNullOrEmpty())
            Thread(Runnable {
                RequestHandler.requestPOST(FynoUtils().getEndpoint("update_channel", getWorkspace(), profile = getIdentity()),
                    JSONObject("{channel: {sms: $token}}")
                    , "PATCH")
            })
    }
    fun getMobile(): String {
        return (FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_apns_token").value.toString())
    }
    fun setWorkspace(workspaceId: String) {
        FynoContextCreator.sqlDataHelper.insert_configByKey(Config(key = "fyno_wsid", value = workspaceId))
    }
    fun getWorkspace(): String {
        return (FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_wsid").value.toString())
    }
    fun setFcmIntegration(integrationId: String) {
        FynoContextCreator.sqlDataHelper.insert_configByKey(Config(key = "fyno_fcm_integration_id", value = integrationId))
    }
    fun getFcmIntegration(): String {
        return (FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_fcm_integration_id").value.toString())
    }
    fun setMiIntegration(integrationId: String) {
        FynoContextCreator.sqlDataHelper.insert_configByKey(Config(key = "fyno_mi_integration_id", value = integrationId))
    }
    fun getMiIntegration(): String {
        return (FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_mi_integration_id").value.toString())
    }
    fun setApi(secret: String) {
        FynoContextCreator.sqlDataHelper.insert_configByKey(Config(key = "fyno_ws_secret", value = secret))
    }
    fun getApi(): String {
        return (FynoContextCreator.sqlDataHelper.getconfigByKey("fyno_ws_secret").value.toString())
    }
}