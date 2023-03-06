package com.fynoio.pushsdk.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable

class FynoUtils {
    fun getResourceString(context: Context, key: String?, defaultStr: String): String? {
        val resources: Resources = context.resources
        val resId: Int = resources.getIdentifier(key, "string", context.packageName)
        return if (resId != 0) resources.getString(resId) else defaultStr
    }

    fun getResource(context: Context, key: String?): Drawable {
        val resources: Resources = context.resources
        val resId: Int = resources.getIdentifier(key, "drawable", context.packageName)
        return resources.getDrawable(resId)
    }
}