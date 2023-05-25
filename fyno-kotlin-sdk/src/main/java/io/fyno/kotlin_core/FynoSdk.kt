package io.fyno.kotlin_core

import android.content.Context
import io.fyno.core.FynoCore
import io.fyno.core.utils.LogLevel
import io.fyno.pushlibrary.FynoPush
import io.fyno.pushlibrary.models.PushRegion

class FynoSdk {
    companion object{
        fun initialize(context: Context, workspaceId: String, token: String, userId: String?=null) {
            if(userId.isNullOrBlank())
                FynoCore.initialize(context, workspaceId, token)
            else
                FynoCore.initialize(context, workspaceId, token, userId)
        }

        fun registerPush(xiaomiApplicationId: String? = "", xiaomiApplicationKey: String? = "", pushRegion: PushRegion? = PushRegion.INDIA, fcmIntegration: String? = "", miIntegration: String = ""){
            FynoPush().registerPush(xiaomiApplicationId, xiaomiApplicationKey, pushRegion, fcmIntegration, miIntegration)
        }

        fun identify(uniqueId: String) {
            FynoCore.identify(uniqueId)
        }
        fun resetUser() {
            FynoCore.resetUser()
        }
        fun resetConfig() {
            FynoCore.resetConfig()
        }
        fun saveConfig(wsId: String, apiKey: String,fcmIntegration: String, miIntegration: String) {
            FynoCore.saveConfig(wsId, apiKey, fcmIntegration, miIntegration)
        }
        fun setLogLevel(level: LogLevel) {
            FynoCore.setLogLevel(level)
        }
    }
}