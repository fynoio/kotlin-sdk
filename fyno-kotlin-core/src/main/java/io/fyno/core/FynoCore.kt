package io.fyno.core

import android.content.*
import androidx.core.app.NotificationManagerCompat
import io.fyno.core.helpers.SQLDataHelper
import io.fyno.core.utils.FynoContextCreator
import io.fyno.core.utils.FynoUtils
import io.fyno.core.utils.LogLevel
import io.fyno.core.utils.Logger
import io.fyno.core.utils.NetworkDetails
//import io.sentry.Sentry
//import io.sentry.protocol.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class FynoCore {
    companion object {
        const val TAG = "FynoSDK"
        lateinit var appContext: Context
        private lateinit var fynoPreferences: SharedPreferences

        fun initialize(context: Context, wsid: String, integration: String, version: String = "live") {
            require(wsid.isNotEmpty()) { "Workspace Id is empty" }

            val sqlDataHelper = SQLDataHelper(context)
            sqlDataHelper.updateAllRequestsToNotProcessed()
            appContext = context
            FynoContextCreator.context = appContext
            NetworkDetails.getNetworkType()
            ConnectionStateMonitor().enable(context)

            fynoPreferences = context.getSharedPreferences(
                "${context.packageName}-fynoio",
                ContextWrapper.MODE_PRIVATE
            )

            setString("WS_ID", wsid)
            setString("VERSION", version)
            FynoUser.setFynoIntegration(integration)
            FynoUser.setWorkspace(wsid)

            if (FynoUser.getIdentity().isEmpty()) {
                val uuid = UUID.randomUUID().toString()
                try {
                    runBlocking {
                        CoroutineScope(Dispatchers.IO).launch {
                            JWTRequestHandler().fetchAndSetJWTToken(uuid)
                            val endpoint = FynoUtils().getEndpoint(
                                "create_profile",
                                FynoUser.getWorkspace(),
                                profile = uuid,
                                version = getString("VERSION")
                            )
                            RequestHandler.requestPOST(
                                endpoint,
                                JSONObject().put("distinct_id", uuid),
                                "POST"
                            )
                            identify(uuid)
                            setFlag("isDirty", true)
                        }
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "In Exception initialize: ${e.message}", e)
                }
            }
        }

        fun identify(uniqueId: String, name: String? = "", update: Boolean = true) {
            if (!FynoContextCreator.isInitialized()) {
                Logger.d(TAG, "Fyno context not initialized - identify rejected/failed")
                return
            }
            val oldDistinctId = FynoUser.getIdentity()
            if (oldDistinctId == uniqueId) {
                updateName(name)
                return
            }
            if (update) {
                runBlocking {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (oldDistinctId.isNotBlank()) {
                            val mergeEndpoint = FynoUtils().getEndpoint(
                                "merge_profile",
                                FynoUser.getWorkspace(),
                                profile = oldDistinctId,
                                newId = uniqueId,
                                version = getString("VERSION")
                            )
                            RequestHandler.requestPOST(mergeEndpoint, null, "PATCH")
                            JWTRequestHandler().fetchAndSetJWTToken(uniqueId)
                        } else {
                            val upsertEndpoint = FynoUtils().getEndpoint(
                                "upsert_profile",
                                FynoUser.getWorkspace(),
                                profile = uniqueId,
                                version = getString("VERSION")
                            )
                            RequestHandler.requestPOST(
                                upsertEndpoint,
                                getParamsObj(uniqueId, name),
                                "PUT"
                            )
                        }
                    }
                }
            }
            FynoUser.identify(uniqueId)
        }
        private fun updateName(name: String?) {
            if(name.isNullOrEmpty()){
                return
            }
            val upsertEndpoint = FynoUtils().getEndpoint(
                "upsert_profile",
                FynoUser.getWorkspace(),
                profile = FynoUser.getIdentity(),
                version = getString("VERSION")
            )
            runBlocking(Dispatchers.IO) {
                RequestHandler.requestPOST(upsertEndpoint, getParamsObj(FynoUser.getIdentity(), name), "PUT")
            }
        }

        private fun getParamsObj(uniqueId: String, name: String? = null): JSONObject {
            val jsonObject = JSONObject()
            val channelObj = JSONObject()
            val pushObj = JSONArray()

            jsonObject.put("distinct_id", uniqueId)
            if (!name.isNullOrEmpty()) {
                jsonObject.put("name", name)
            }

            val fcmToken = FynoUser.getFcmToken()
            val xiaomiToken = FynoUser.getMiToken()

            val notificationStatus = if (areNotificationPermissionsEnabled()) 1 else 0

            if (!fcmToken.isNullOrBlank()) {
                pushObj.put(JSONObject().put("token", "fcm_token:$fcmToken").put("integration_id", FynoUser.getFynoIntegration()).put("status", notificationStatus))
            }

            if (!xiaomiToken.isNullOrBlank()) {
                pushObj.put(JSONObject().put("token", "mi_token:$xiaomiToken").put("integration_id", FynoUser.getFynoIntegration()).put("status", notificationStatus))
            }

            if (pushObj.length() > 0) {
                channelObj.put("push", pushObj)
                jsonObject.put("channel", channelObj)
            }
            return jsonObject
        }

        fun areNotificationPermissionsEnabled(): Boolean {
            val notificationManager = NotificationManagerCompat.from(appContext)
            return notificationManager.areNotificationsEnabled()
        }

        fun resetUser() {
            if (!FynoContextCreator.isInitialized()) {
                Logger.d(TAG, "Fyno context not initialized - reset rejected/failed")
                return
            }
            val uuid = UUID.randomUUID().toString()
            val deleteJson = JSONObject()
            val deleteArr = JSONArray()
            val fcmToken = FynoUser.getFcmToken()
            val xiaomiToken = FynoUser.getMiToken()

            if (!fcmToken.isNullOrEmpty()) {
                deleteArr.put("fcm_token:$fcmToken")
            }

            if (!xiaomiToken.isNullOrEmpty()) {
                deleteArr.put("mi_token:$xiaomiToken")
            }

            if (deleteArr.length() > 0) {
                deleteJson.put("push", deleteArr)
            }

            val jsonObject = getParamsObj(uuid)

            runBlocking {
                CoroutineScope(Dispatchers.IO).launch {
                    val deleteEndpoint = FynoUtils().getEndpoint(
                        "delete_channel",
                        FynoUser.getWorkspace(),
                        profile = FynoUser.getIdentity(),
                        version = getString("VERSION")
                    )
                    RequestHandler.requestPOST(deleteEndpoint, deleteJson, "POST")
                    JWTRequestHandler().fetchAndSetJWTToken(uuid)
                    val createProfileEndpoint = FynoUtils().getEndpoint(
                        "create_profile",
                        FynoUser.getWorkspace(),
                        version = getString("VERSION")
                    )
                    RequestHandler.requestPOST(createProfileEndpoint, jsonObject, "POST")

                    identify(uuid, "", false)
                }
            }
        }

        fun resetConfig() {
            if (!FynoContextCreator.isInitialized()) {
                Logger.d(TAG, "Fyno context not initialized - resetConfig rejected/failed")
                return
            }
            FynoUser.setWorkspace("")
            FynoUser.setApi("")
            FynoUser.setFynoIntegration("")
        }
        fun setLogLevel(level: LogLevel) {
            Logger.Level = level
        }

        fun mergeProfile(oldDistinctId: String, newDistinctId: String) {
            if (!FynoContextCreator.isInitialized()) {
                Logger.i(TAG, "Fyno context not initialized - mergeProfile rejected/failed")
                return
            }
            if (oldDistinctId.isBlank() or newDistinctId.isBlank()) {
                Logger.i(
                    TAG,
                    "mergeProfile: Failed as Both old id and new id are required to merge profile"
                )
                return
            }

            if (oldDistinctId == newDistinctId) {
                Logger.i(TAG, "mergeProfile: No need to merge as both old id and new id are same")
                return
            }

            runBlocking {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val mergeProfileEndpoint = FynoUtils().getEndpoint(
                            "merge_profile",
                            FynoUser.getWorkspace(),
                            profile = oldDistinctId,
                            newId = newDistinctId,
                            version = getString("VERSION")
                        )
                        RequestHandler.requestPOST(mergeProfileEndpoint, null, "PATCH")
                        identify(newDistinctId, "", false)
                    } catch (e: Exception) {
                        Logger.d(TAG, "mergeProfile: Failed with exception ${e.message}")
                    }
                }
            }
        }

        private fun setString(key: String, value: String) {
            fynoPreferences.edit().putString(key, value).apply()
        }

        private fun setFlag(key: String, value: Boolean) {
            fynoPreferences.edit().putBoolean(key, value).apply()
        }

        fun getString(key: String) = fynoPreferences.getString(key, "") ?: ""

        private fun getFlag(key: String) = fynoPreferences.getBoolean(key, false)
    }
}
