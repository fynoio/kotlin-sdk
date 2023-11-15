package io.fyno.core.utils

import org.json.JSONObject

data class HttpRequest(
    val url: String,
    val postData: JSONObject?,
    val method: String,
    val maxRetries: Int,
    val retryCount: Int = 0
)