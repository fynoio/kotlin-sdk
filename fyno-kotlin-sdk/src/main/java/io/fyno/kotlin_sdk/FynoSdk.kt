package io.fyno.kotlin_sdk

import android.content.Context
import android.os.Handler
import android.os.Looper
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
        integrationId: String,
        userId: String? = null,
        version: String = "live"
    ) {
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                FynoCore.initialize(context, workspaceId, integrationId, version)
                if (!userId.isNullOrBlank()) {
                    FynoCore.identify(uniqueId = userId, update = true)
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
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            Handler(Looper.getMainLooper()).postDelayed({
                FynoPush().registerPush(
                    xiaomiApplicationId,
                    xiaomiApplicationKey,
                    pushRegion
                )
            }, 5000);
        }
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
