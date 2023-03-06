package io.fyno.kotlin_sdk

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
    private val POST : String = "POST"
    @Throws(IOException::class)
    fun requestPOST(r_url: String?, postDataParams: JSONObject) {
        val url = URL(r_url)
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.setRequestProperty("Content-Type", "application/json");
        conn.readTimeout = 3000
        conn.connectTimeout = 3000
        conn.requestMethod = POST
        conn.doInput = true
        conn.doOutput = true
        val os: OutputStream = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(postDataParams.toString())
        writer.flush()
        writer.close()
        os.close()
        val responseCode: Int = conn.responseCode // To Check for 200
        Log.d("RequestPost", "requestPOST: "+conn.responseMessage)
        if (responseCode != HttpsURLConnection.HTTP_OK) {
            throw IOException(conn.responseMessage)
        }
    }
}