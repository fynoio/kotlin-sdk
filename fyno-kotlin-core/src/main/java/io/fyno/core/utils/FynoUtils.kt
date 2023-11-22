package io.fyno.core.utils

class FynoUtils {
    fun getEndpoint(event: String, ws: String, env: String? = "live", profile: String? = null, newId: String? = null, version: String? = "live"): String {
        val baseEndpoint = if (env == "test") FynoConstants.DEV_ENDPOINT else FynoConstants.PROD_ENDPOINT
        val commonPath = "$ws/$version/${FynoConstants.PROFILE}"

        return when (event) {
            "create_profile" -> "$baseEndpoint/$commonPath"
            "get_profile" -> "$baseEndpoint/$commonPath/$profile"
            "merge_profile" -> "$baseEndpoint/$commonPath/$profile/merge/$newId"
            "upsert_profile" -> "$baseEndpoint/$commonPath/$profile"
            "update_channel" -> "$baseEndpoint/$commonPath/$profile/channel"
            "delete_channel" -> "$baseEndpoint/$commonPath/$profile/channel/delete"
            "event_trigger" -> "$baseEndpoint/$ws/$version/${FynoConstants.EVENT_PATH}"
            else -> ""
        }
    }
}
