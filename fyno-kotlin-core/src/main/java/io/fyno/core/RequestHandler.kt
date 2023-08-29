package io.fyno.core

import android.util.Log
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object RequestHandler {
    @Throws(IOException::class)
    fun requestPOST(r_url: String?, postDataParams: JSONObject?, method: String = "POST"): Int {
        val url = URL(r_url)
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer ${FynoUser.getApi()}")
        conn.readTimeout = 3000
        conn.connectTimeout = 3000
        conn.requestMethod = method
        conn.doInput = true
        conn.doOutput = true
        val os: OutputStream = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        if (postDataParams != null)
            writer.write(postDataParams.toString())
        writer.flush()
        writer.close()
        os.close()
        val responseCode: Int = conn.responseCode // To Check for 200
        Log.d("RequestPost", "requestPOST method = $method url = $url: "+conn.responseMessage,)
        if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == HttpsURLConnection.HTTP_CREATED || responseCode == HttpsURLConnection.HTTP_ACCEPTED) {
            Log.i("RequestPost", "requestPOST: "+conn.responseMessage)
        } else {
            Log.e("RequestPost", "requestPOST: "+conn.responseMessage)
        }
        return responseCode
    }
    @Throws(IOException::class)
    fun requestGET(r_url: String?, postDataParams: JSONObject? = null, method: String = "GET"): Int {
        val url = URL(r_url)
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer ${FynoUser.getApi()}")
        conn.readTimeout = 3000
        conn.connectTimeout = 3000
        conn.requestMethod = method
        conn.doInput = true
        conn.doOutput = true
        val os: OutputStream = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        if(postDataParams != null)
            writer.write(postDataParams.toString())
        writer.flush()
        writer.close()
        os.close()
        val responseCode: Int = conn.responseCode // To Check for 200
        Log.d("RequestGet", "requestGET: "+conn.responseMessage)
        return responseCode;
    }
}