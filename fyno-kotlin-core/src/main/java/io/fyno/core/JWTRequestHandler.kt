package io.fyno.core

import io.fyno.core.utils.FynoConstants
import io.fyno.core.utils.Logger
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class JWTRequestHandler {
    internal fun fetchAndSetJWTToken(distinctID: String): String? {
        try {
            val urlString =
                "${FynoConstants.PROD_ENDPOINT}/${FynoUser.getWorkspace()}/$distinctID/token"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "GET"
            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val response =
                    conn.inputStream.bufferedReader().use(BufferedReader::readText)
                val jsonResponse = JSONObject(response)
                conn.disconnect()
                return jsonResponse.getString("token").also {
                    FynoUser.setJWTToken(it)
                }
            } else {
                Logger.d(
                    "JWTRequestHandler",
                    "Request failed with response code: $responseCode for user $distinctID"
                )
                conn.disconnect()
                return null
            }
        } catch (e: Exception) {
            Logger.w("JWTRequestHandler", "Error fetching JWT token", e)
            return null
        }
    }
}
