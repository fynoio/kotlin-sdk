package io.fyno.core

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.util.Log
import io.fyno.core.FynoCore.Companion.TAG
import io.fyno.core.utils.FynoContextCreator
import io.fyno.core.utils.Logger
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date
import kotlin.math.pow
import io.fyno.core.helpers.SQLDataHelper

object RequestHandler {
    private const val TIMEOUT = 6000
    private const val MAX_BACKOFF_DELAY: Long = 60000
    private const val MAX_RETRIES = 3
    private lateinit var sqlDataHelper: SQLDataHelper

    data class Request(val url: String?, val postData: JSONObject?, val method: String = "POST")

    private fun isCallBackRequest(url:String?):Boolean{
        return !url.isNullOrEmpty() && url.contains("callback.fyno.io")
    }

    @Throws(Exception::class)
    suspend fun requestPOST(
        r_url: String?,
        postDataParams: JSONObject?,
        method: String = "POST",
        context: Context? = null
    ) {
        try {
            val request = Request(r_url, postDataParams, method)
            if (FynoContextCreator.isInitialized()) {
                if(isCallBackRequest(r_url)){
                    saveCBRequestToDb(request, context)
                    processCBRequests(context,"requestPOST")
                } else{
                    saveRequestToDb(request)
                    processDbRequests("requestPOST")
                }
            } else {
                if(isCallBackRequest(r_url)) {
                    saveCBRequestToDb(request, context)
                    processCBRequests(context,"requestPOST")
                }
            }
        } catch (e: Exception) {
            Logger.w(TAG, "requestPOST: Failed to send request - ${e.message}")
        }
    }


    // Function to save requests to SQLite database
    private fun saveRequestToDb(request: Request, id:Int? = 0) {
        FynoContextCreator.sqlDataHelper.insertRequest(request, "requests", id)
    }

    // Function to save CB requests to SQLite database
    private fun saveCBRequestToDb(request: Request, context: Context?) {
        sqlDataHelper = SQLDataHelper(context)
        sqlDataHelper.insertRequest(request, "callbacks")
        sqlDataHelper.close()
    }

    private fun deleteCBRequestFromDb(id: Int?, context: Context?) {
        sqlDataHelper = SQLDataHelper(context)
        sqlDataHelper.deleteRequestByID(id, "callbacks")
        sqlDataHelper.close()
    }

    // Function to handle the retry mechanism
    private suspend fun handleRetries(
        request: Request,
        id: Int? = 0,
        context: Context? = null
    ): Boolean {
        var retries = 0
        while (retries < MAX_RETRIES) {
            try {
                // Attempt the request
                doRequest(request, id, context)
                return true // Request successful, exit the retry loop
            } catch (e: Exception) {
                Logger.d(TAG, "Request failed: ${e.message}")
                // Implement a backoff strategy here (e.g., exponential backoff)
                val delayMillis = calculateDelay(retries)
                delay(delayMillis)
                retries++
            }
        }
        Logger.w(TAG, "Max retries reached for request: ${request.url}")
        if (isCallBackRequest(request.url)) {
            sqlDataHelper= SQLDataHelper(context)
            sqlDataHelper.updateStatusAndLastProcessedTime(id,"callbacks","not_processed")
            sqlDataHelper.close()
        } else {
            FynoContextCreator.sqlDataHelper.updateStatusAndLastProcessedTime(id, "requests", "not_processed")
        }
        return false
    }

    private fun calculateDelay(retryCount: Int): Long {
        return minOf(4.0.pow(retryCount.toDouble()).toLong() * 1000, MAX_BACKOFF_DELAY)
    }

    @Throws(Exception::class)
    private fun doRequest(request: Request, id: Int? = 0, context: Context? = null) {
        val url = URL(request.url)
        URL(url.protocol, url.host, 3000, url.file)
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.setRequestProperties()
        conn.readTimeout = TIMEOUT
        conn.connectTimeout = TIMEOUT
        conn.requestMethod = request.method
        conn.doInput = true
        conn.doOutput = true

        if (request.postData != null) {
            val os: OutputStream = conn.outputStream
            val writer = OutputStreamWriter(os, "UTF-8")
            writer.write(request.postData.toString())
            writer.flush()
            writer.close()
            os.close()
        }

        val responseCode: Int = conn.responseCode

        Logger.d(
            "RequestPost",
            "requestPOST method = ${request.method} params=${request.postData.toString()} url = ${request.url}: ${conn.responseMessage} || time: ${Date().time}"
        )

        when (responseCode) {
            in 200..299 -> {
                Logger.i("RequestPost", "requestPOST: ${conn.responseMessage}")
                if(isCallBackRequest(request.url)) {
                    deleteCBRequestFromDb(id,context)
                    return
                }
                FynoContextCreator.sqlDataHelper.deleteRequestByID(id, "requests")
            }

            in 400..499 -> {
                Logger.i(TAG, "Request failed with response code: $responseCode")
                if (isCallBackRequest(request.url)) {
                    deleteCBRequestFromDb(id, context)
                    return
                }
                FynoContextCreator.sqlDataHelper.deleteRequestByID(id, "requests")
            }

            else -> {
                Logger.i(TAG, "Request failed with response code: $responseCode")
                throw Exception("Request failed with response code: $responseCode")
            }
        }
        conn.disconnect()
    }

    private fun HttpURLConnection.setRequestProperties() {
        this.setRequestProperty("Content-Type", "application/json")
        if (FynoContextCreator.isInitialized()) {
            this.setRequestProperty("x-fn-app-id", FynoContextCreator.context.packageName)
            this.setRequestProperty("integration", FynoUser.getFynoIntegration())
            this.setRequestProperty("verify_token", FynoUser.getApi())

        }
    }

    // Function to process requests from SQLite database
    @SuppressLint("Range")
    suspend fun processDbRequests(caller: String? = "") {
        var cursor: Cursor? = null
        try {
            while (true) {
                // Retrieve one request from SQLite database
                cursor = FynoContextCreator.sqlDataHelper.getNextRequest()

                if (cursor.moveToNext()) {
                    val url = cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_URL))
                    val postDataStr =
                        cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_POST_DATA))
                    val postData = if (postDataStr != null) JSONObject(postDataStr) else null
                    val method =
                        cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_METHOD))
                    val id = cursor.getInt(cursor.getColumnIndex(SQLDataHelper.COLUMN_ID))
                    val lastProcessedTimeMillis =
                        cursor.getLong(cursor.getColumnIndex(SQLDataHelper.COLUMN_LAST_PROCESSED_AT))
                    val timeDifference = System.currentTimeMillis() - lastProcessedTimeMillis

                    if (caller != "requestPOST" && timeDifference < 2000) {
                        // Skip this record as the last update time is less than 2 seconds ago
                        continue
                    }

                    FynoContextCreator.sqlDataHelper.updateStatusAndLastProcessedTime(id,"requests","processing")

                    val request = Request(url, postData, method)
                    if (!handleRetries(request, id)) {
                        break
                    }
                } else {
                    break
                }
            }
        } finally {
            cursor?.close()
        }
    }

    // Function to process requests from SQLite database
    @SuppressLint("Range")
    suspend fun processCBRequests(context: Context?, caller:String? = "") {
        // Retrieve requests from SQLite database
        var cursor: Cursor? = null
        try {
            sqlDataHelper = SQLDataHelper(context)
            while (true) {
                // Retrieve one request from SQLite database
                cursor = sqlDataHelper.getNextCBRequest()

                if (cursor.moveToNext()) {
                    val url = cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_URL))
                    val postDataStr =
                        cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_POST_DATA))
                    val postData = if (postDataStr != null) JSONObject(postDataStr) else null
                    val method =
                        cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_METHOD))
                    val id = cursor.getInt(cursor.getColumnIndex(SQLDataHelper.COLUMN_ID))
                    val lastProcessedTimeMillis = cursor.getLong(cursor.getColumnIndex(SQLDataHelper.COLUMN_LAST_PROCESSED_AT))
                    val timeDifference = System.currentTimeMillis() - lastProcessedTimeMillis

                    if (caller != "requestPOST" && timeDifference < 2000) {
                        // Skip this record as the last update time is less than 2 seconds ago
                        continue
                    }

                    sqlDataHelper.updateStatusAndLastProcessedTime(id,"callbacks","processing")

                    val request = Request(url, postData, method)
                    runBlocking {
                        CoroutineScope(Dispatchers.IO).launch {
                            handleRetries(request, id, context)
                        }
                    }
                } else {
                    break
                }
            }
        } finally {
            cursor?.close()
        }
    }
}