package io.fyno.core.utils

import android.util.Log
import io.fyno.core.FynoCore

class FynoUtils {
    fun getEndpoint(
        event: String,
        ws: String,
        env: String? = "live",
        profile: String? = null,
        newId: String? = null,
        version: String? = "live"
    ): String {
        val baseEndpoint =
            if (env == "test") FynoConstants.DEV_ENDPOINT else FynoConstants.PROD_ENDPOINT
        val commonPath = "$ws/track/$version/${FynoConstants.PROFILE}"
        Logger.i(
            "GET_ENDPOINT",
            "getEndpoint Params: $event, $ws, $env, $profile, $newId, $version"
        )

        var url = when (event) {
            "create_profile" -> "$baseEndpoint/$commonPath"
            "merge_profile" -> "$baseEndpoint/$commonPath/$profile/merge/$newId?appVersion=${
                FynoCore.getString(
                    "appVersionName"
                )
            }&package=${FynoCore.getString("appPackageName")}"

            "upsert_profile" -> "$baseEndpoint/$commonPath/$profile?appVersion=${
                FynoCore.getString(
                    "appVersionName"
                )
            }&package=${FynoCore.getString("appPackageName")}\""

            "update_channel" -> "$baseEndpoint/$commonPath/$profile/channel?appVersion=${
                FynoCore.getString(
                    "appVersionName"
                )
            }&package=${FynoCore.getString("appPackageName")}\""

            "delete_channel" -> "$baseEndpoint/$commonPath/$profile/channel/delete?appVersion=${
                FynoCore.getString(
                    "appVersionName"
                )
            }&package=${FynoCore.getString("appPackageName")}\""

            else -> ""
        }

        return url
    }
}