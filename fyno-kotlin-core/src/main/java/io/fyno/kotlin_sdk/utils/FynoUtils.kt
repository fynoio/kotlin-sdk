package com.fynoio.pushsdk.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import io.fyno.kotlin_sdk.utils.FynoConstants
import org.json.JSONObject
import java.util.*

class FynoUtils {
    fun getEndpoint(event: String, ws: String, env: String? = "test", profile: String? = null, newId: String? = null): String {
        when (event) {
            "create_profile" -> if(env === "test") return "${FynoConstants.DEV_ENDPOINT}/$ws/${FynoConstants.PROFILE}" else return "${FynoConstants.PROD_ENDPOINT}/$ws/${FynoConstants.PROFILE}"
            "get_profile" -> if(env === "test") return "${FynoConstants.DEV_ENDPOINT}/$ws/${FynoConstants.PROFILE}" else return "${FynoConstants.PROD_ENDPOINT}/$ws/${FynoConstants.PROFILE}/$profile"
            "merge_profile" -> if(env === "test") return "${FynoConstants.DEV_ENDPOINT}/$ws/${FynoConstants.PROFILE}/$profile/merge/$newId" else return "${FynoConstants.PROD_ENDPOINT}/$ws/${FynoConstants.PROFILE}/$profile/merge/$newId"
            "upsert_profile" -> if(env === "test") return "${FynoConstants.DEV_ENDPOINT}/$ws/${FynoConstants.PROFILE}/$profile" else return "${FynoConstants.PROD_ENDPOINT}/$ws/${FynoConstants.PROFILE}/$profile"
            "update_channel" -> if(env === "test") return "${FynoConstants.DEV_ENDPOINT}/$ws/${FynoConstants.PROFILE}/$profile/channel" else return "${FynoConstants.PROD_ENDPOINT}/$ws/${FynoConstants.PROFILE}/$profile/channel"
            "delete_channel" -> if(env === "test") return "${FynoConstants.DEV_ENDPOINT}/$ws/${FynoConstants.PROFILE}/$profile/channel/delete" else return "${FynoConstants.PROD_ENDPOINT}/$ws/${FynoConstants.PROFILE}/$profile/channel/delete"
            "event_trigger" -> if(env === "test") return "${FynoConstants.DEV_ENDPOINT}/$ws/${FynoConstants.EVENT_PATH}" else return "${FynoConstants.PROD_ENDPOINT}/$ws/${FynoConstants.EVENT_PATH}"
        }
        return ""
    }
}