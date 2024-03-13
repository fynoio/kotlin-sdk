package io.fyno.core

import io.fyno.core.utils.FynoConstants
import io.fyno.core.utils.Logger
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date

class JWTRequestHandler {
    internal fun fetchAndSetJWTToken(distinctID: String) {
        val url = URL(FynoConstants.PROD_ENDPOINT + "/" + FynoUser.getWorkspace() + "/" + distinctID + "/token")
        URL(url.protocol, url.host, 3000, url.file)
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"

        val responseCode: Int = conn.responseCode

        Logger.d(
            "fetchAndSetJWTToken",
            "fetchAndSetJWTToken method = ${conn.requestMethod} url = $url: ${conn.responseMessage} || time: ${Date().time}"
        )

        when (responseCode) {
            in 200..299 -> {
                Logger.i("RequestPost", "requestPOST: ${conn.responseMessage}")
                val inputStream = conn.inputStream
                val response = inputStream.bufferedReader().use(BufferedReader::readText)
                val jsonResponse = JSONObject(response)
                val token = jsonResponse.getString("token")
                FynoUser.setJWTToken(token)
            }

            in 400..499 -> {
                Logger.i(FynoCore.TAG, "Request failed with response code: $responseCode")
            }

            else -> {
                Logger.i(FynoCore.TAG, "Request failed with response code: $responseCode")
            }
        }
        conn.disconnect()
    }
}