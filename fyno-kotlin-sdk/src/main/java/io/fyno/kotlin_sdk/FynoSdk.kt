package io.fyno.kotlin_sdk

import android.content.Context
import io.fyno.callback.FynoCallback
import io.fyno.callback.models.MessageStatus
import io.fyno.core.FynoCore
import io.fyno.core.utils.LogLevel
import io.fyno.pushlibrary.FynoPush
import io.fyno.pushlibrary.models.PushRegion
//import io.sentry.Sentry
//import io.sentry.protocol.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


public object FynoSdk {
    fun initialize(
        context: Context,
        workspaceId: String,
        token: String,
        userId: String? = null,
        version: String = "live"
    ) {
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                FynoCore.initialize(context, workspaceId, token, version)
                userId?.let {
                    FynoCore.identify(uniqueId = it, update = true)
                    // Sentry.configureScope {
                    //     val user = User()
                    //     user.id = userId
                    //     it.user = user
                    // }
                }
            }
        }
    }

    fun updateStatus(context: Context, callback_url: String, status: MessageStatus) {
        FynoCallback().updateStatus(context,callback_url,status)
    }

    fun registerPush(
        xiaomiApplicationId: String? = "",
        xiaomiApplicationKey: String? = "",
        pushRegion: PushRegion? = PushRegion.INDIA,
        integrationId: String = ""
    ) {
        FynoPush().registerPush(
            xiaomiApplicationId,
            xiaomiApplicationKey,
            pushRegion,
            integrationId
        )
    }

    fun identify(uniqueId: String, userName: String? = null) {
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                FynoCore.identify(uniqueId, userName, true)
            }
        }
    }

    fun resetUser() {
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                FynoCore.resetUser()
            }
        }
    }

    fun resetConfig() {
        FynoCore.resetConfig()
    }

    fun saveConfig(wsId: String, apiKey: String, fcmIntegration: String, miIntegration: String) {
        FynoCore.saveConfig(wsId, apiKey, fcmIntegration, miIntegration)
    }

    fun setLogLevel(level: LogLevel) {
        FynoCore.setLogLevel(level)
    }

    fun mergeProfile(oldDistinctId: String, newDistinctId: String) {
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                FynoCore.mergeProfile(oldDistinctId, newDistinctId)
            }
        }
    }
}
