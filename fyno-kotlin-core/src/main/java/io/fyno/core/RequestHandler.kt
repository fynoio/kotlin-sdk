package io.fyno.core

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.util.Log
import io.fyno.core.FynoCore.Companion.TAG
import io.fyno.core.helpers.SQLDataHelper
import io.fyno.core.utils.FynoContextCreator
import io.fyno.core.utils.Logger
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.pow
import kotlin.properties.Delegates

object RequestHandler {

    private const val TIMEOUT = 6000
    private const val MAX_BACKOFF_DELAY: Long = 60000
    private const val MAX_RETRIES = 1
    private lateinit var sqlDataHelper: SQLDataHelper

    interface NetworkCallback {
        fun onSuccess(response: String)
        fun onError(error: String)
    }

    data class Request(val url: String?, val postData: JSONObject?, val method: String = "POST")

    private suspend fun handleRetries(
        request: Request,
        id: Int? = 0,
        context: Context? = null
    ): Boolean {
        return withContext(Dispatchers.IO){
            var retries = 0
            while (retries < MAX_RETRIES) {
                try {
                    Logger.d("RequestHandler", "handleRetries: Retry attempt $retries for ${request.url}")
                    val success =  async { doRequestAsync(request, id, context) }.await()
                    Logger.d("RequestHandler", "handleRetries: Request started: ${request.url}, retry count = $retries, res -> $success")
                    if (success) return@withContext true
                } catch (e: Exception) {
                    Logger.d("RequestHandler", "handleRetries: Request failed: ${e.message}")
                    val delayMillis = calculateDelay(retries)
                    delay(delayMillis)
                    retries++
                }
            }
            Logger.w("RequestHandler", "handleRetries: Max retries reached for request: ${request.url}")
            return@withContext false
        }
    }

    private fun calculateDelay(retryCount: Int): Long {
        return minOf(4.0.pow(retryCount.toDouble()).toLong() * 1000, MAX_BACKOFF_DELAY)
    }

    private suspend fun doRequestAsync(request: Request, id: Int? = 0, context: Context? = null): Boolean {
        val mergeRegex = Regex(".*/track/(test|live)/profile/.*/merge/(.*)")
        return withContext(Dispatchers.IO) {
            val url = URL(request.url)
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            if (url.path.matches(Regex(".*/track/(test|live)/profile$"))) {
                request.postData?.getString("distinct_id")?.let { distinctId ->
                    val token = async { JWTRequestHandler().fetchAndSetJWTToken(distinctId) }.await()
                    if (token.isNullOrEmpty()) {
                        Logger.w(TAG,"JWT token fetch failed");
                        return@withContext false
                    }
                }
            }
            Logger.i(TAG, "doRequestAsync: request started for $url")
            conn.setRequestProperties()
            conn.readTimeout = TIMEOUT
            conn.connectTimeout = TIMEOUT
            conn.requestMethod = request.method
            conn.doInput = true
            conn.doOutput = true

            request.postData?.let { postData ->
                conn.outputStream.use { os ->
                    OutputStreamWriter(os, "UTF-8").use { writer ->
                        writer.write(postData.toString())
                    }
                }
            }

            val responseCode = conn.responseCode
            Logger.i(TAG, "doRequestAsync: Request finished with code : $responseCode")
            when (responseCode) {
                in 200..299 -> {
                    if (url.path.matches(mergeRegex)) {
                        mergeRegex.find(url.path)?.groups?.get(2)?.value?.let {
                            val token = async { JWTRequestHandler().fetchAndSetJWTToken(it) }.await()
                            if (token.isNullOrEmpty()) {
                                Logger.w(TAG,"JWT token fetch failed");
                                return@withContext false
                            }
                        }
                    }
                    if (isCallBackRequest(request.url)) {
                        deleteCBRequestFromDb(id, context?.applicationContext)
                        conn.disconnect()
                        return@withContext true
                    }
                    FynoContextCreator.sqlDataHelper.deleteRequestByID(id, "requests")
                    conn.disconnect()
                    true
                }
                in 400..499 -> {
                    if(responseCode == 404) {
                        conn.disconnect()
                        return@withContext false
                    }
                    val inputStream = conn.errorStream
                    val response = inputStream.bufferedReader().use(BufferedReader::readText)
                    val jsonResponse = JSONObject(response)
                    val message = jsonResponse.getString("_message")
                    if (responseCode == 401) {
                        if (message == "jwt_expired") {
                            async { JWTRequestHandler().fetchAndSetJWTToken(FynoUser.getIdentity()) }.await()
                            conn.disconnect()
                            async { doRequestAsync(request) }.await()
                        }
                    }
                    Logger.i(TAG, "doRequestAsync: Request failed with response code: $responseCode")
                    if (isCallBackRequest(request.url)) {
                        deleteCBRequestFromDb(id, context)
                        conn.disconnect()
                        return@withContext true
                    }
                    FynoContextCreator.sqlDataHelper.deleteRequestByID(id, "requests")
                    Logger.w(TAG, "doRequestAsync: Request failed: $message")
                    conn.disconnect()
                    true
                }
                else -> {
                    Logger.e(
                        TAG,
                        "doRequestAsync: Request failed with response code: $responseCode",
                        Exception("doRequestAsync: Request failed with response code: $responseCode")
                    )
                    conn.disconnect()
                    false
                }
            }
        }
    }

    private fun HttpURLConnection.setRequestProperties() {
        this.setRequestProperty("Content-Type", "application/json")
        if (FynoContextCreator.isInitialized()) {
            this.setRequestProperty("x-fn-app-id", FynoContextCreator.getContext()?.packageName)
            this.setRequestProperty("integration", FynoUser.getFynoIntegration())
            this.setRequestProperty("verify_token",FynoUser.getJWTToken())
        }
    }

    @SuppressLint("Range")
    suspend fun processDbRequests(caller: String? = "") {
        withContext(Dispatchers.IO) {
            async {
                var cursor: Cursor? = null
                var req_id by Delegates.notNull<Int>()
                try {
                    while (true) {
                        if (cursor != null && !cursor.isClosed) cursor.close()
                        cursor = FynoContextCreator.sqlDataHelper.getNextRequest("requests")
                        if (cursor.moveToNext()) {
                            val url =
                                cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_URL))
                            val postDataStr =
                                cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_POST_DATA))
                            val postData =
                                if (postDataStr != null) JSONObject(postDataStr) else null
                            val method =
                                cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_METHOD))
                            val id = cursor.getInt(cursor.getColumnIndex(SQLDataHelper.COLUMN_ID))
                            val lastProcessedTimeMillis =
                                cursor.getLong(cursor.getColumnIndex(SQLDataHelper.COLUMN_LAST_PROCESSED_AT))
                            req_id = id
                            val timeDifference =
                                System.currentTimeMillis() - lastProcessedTimeMillis

                            if (caller != "requestPOST" && timeDifference < 2000) {
                                cursor.close()
                                continue
                            }
                            cursor.close()

                            FynoContextCreator.sqlDataHelper.updateStatusAndLastProcessedTime(
                                id,
                                "requests",
                                "processing"
                            )

                            val request = Request(url, postData, method)
                            val success = async { handleRetries(request, id) }.await();
                            if (success) {
                                FynoContextCreator.sqlDataHelper.deleteRequestByID(id, "requests")
                            } else {
                                FynoContextCreator.sqlDataHelper.updateStatusAndLastProcessedTime(
                                    id,
                                    "requests",
                                    "not_processed"
                                )
                                break;
                            }
                        } else {
                            break
                        }
                    }
                } catch (e: Exception) {
                    Logger.w(TAG, "Unable to process the request")
                    req_id.let {
                        FynoContextCreator.sqlDataHelper.updateStatusAndLastProcessedTime(
                            req_id,
                            "requests",
                            "not_processed"
                        )
                    }
                } finally {
                    cursor?.close()
                }
            }.await()
        }
    }

//    @SuppressLint("Range")
//    suspend fun processCBRequests(context: Context?, caller: String? = "") {
//        var cursor: Cursor? = null
//        var req_id by Delegates.notNull<Int>()
//        try {
//            while (true) {
//                if(cursor != null && !cursor.isClosed) cursor.close()
//                cursor = SQLDataHelper(context).getNextRequest("callbacks")
//                if (cursor.moveToNext()) {
//                    val url = cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_URL))
//                    val postDataStr =
//                        cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_POST_DATA))
//                    val postData = if (postDataStr != null) JSONObject(postDataStr) else null
//                    val method =
//                        cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_METHOD))
//                    val id = cursor.getInt(cursor.getColumnIndex(SQLDataHelper.COLUMN_ID))
//                    req_id = id
//                    val lastProcessedTimeMillis =
//                        cursor.getLong(cursor.getColumnIndex(SQLDataHelper.COLUMN_LAST_PROCESSED_AT))
//                    val timeDifference = System.currentTimeMillis() - lastProcessedTimeMillis
//
//                    if (caller != "requestPOST" && timeDifference < 2000) {
//                        cursor.close()
//                        continue
//                    }
//                    cursor.close()
//
//                    SQLDataHelper(context).updateStatusAndLastProcessedTime(
//                        id,
//                        "callbacks",
//                        "processing"
//                    )
//
//                    val request = Request(url, postData, method)
//                    handleRetries(request, id, context)
//                } else {
//                    cursor.close()
//                    break
//                }
//            }
//        } catch (e: Exception) {
//            Logger.w(TAG, "Unable to process the request")
//            req_id.let {
//                SQLDataHelper(context).updateStatusAndLastProcessedTime(
//                    req_id,
//                    "callbacks",
//                    "not_processed"
//                )
//            }
//        } finally {
//            cursor?.close()
//        }
//    }

    @SuppressLint("Range")
    suspend fun processCBRequests(context: Context?, caller: String? = "") {
        withContext(Dispatchers.IO) {
            async {
                var cursor: Cursor? = null
                var req_id by Delegates.notNull<Int>()
                val db = SQLDataHelper(context)
                try {
                    while (true) {
                        if (cursor != null && !cursor.isClosed) cursor.close()
                        cursor = db.getNextRequest("callbacks")
                        if (cursor.moveToNext()) {
                            val url =
                                cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_URL))
                            val postDataStr =
                                cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_POST_DATA))
                            val postData =
                                if (postDataStr != null) JSONObject(postDataStr) else null
                            val method =
                                cursor.getString(cursor.getColumnIndex(SQLDataHelper.COLUMN_METHOD))
                            val id = cursor.getInt(cursor.getColumnIndex(SQLDataHelper.COLUMN_ID))
                            val lastProcessedTimeMillis =
                                cursor.getLong(cursor.getColumnIndex(SQLDataHelper.COLUMN_LAST_PROCESSED_AT))
                            req_id = id
                            val timeDifference =
                                System.currentTimeMillis() - lastProcessedTimeMillis

                            if (caller != "requestPOST" && timeDifference < 2000) {
                                cursor.close()
                                continue
                            }
                            cursor.close()

                            db.updateStatusAndLastProcessedTime(
                                id,
                                "callbacks",
                                "processing"
                            )

                            val request = Request(url, postData, method)
                            val success = async { handleRetries(request, id, context) }.await();
                            if (success) {
                                db.deleteRequestByID(id, "callbacks")
                            } else {
                                db.updateStatusAndLastProcessedTime(
                                    id,
                                    "callbacks",
                                    "not_processed"
                                )
                                db.close()
                                break;
                            }
                        } else {
                            db.close()
                            break
                        }
                    }
                } catch (e: Exception) {
                    Logger.w(TAG, "Unable to process the request")
                    req_id.let {
                        db.updateStatusAndLastProcessedTime(
                            req_id,
                            "callbacks",
                            "not_processed"
                        )
                    }
                } finally {
                    cursor?.close()
                    db.close()
                }
            }.await()
        }
    }

    suspend fun requestPOST(
        r_url: String?,
        postDataParams: JSONObject?,
        method: String = "POST",
        context: Context? = null
    ) {
        try {
            Logger.i(TAG, "requestPOST: Sending POST request")
            val request = Request(r_url, postDataParams, method)
            if (FynoContextCreator.isInitialized()) {
                if (isCallBackRequest(r_url)) {
                    saveCBRequestToDb(request, context)
                    processCBRequests(context, "requestPost")
                } else {
                    saveRequestToDb(request)
                    processDbRequests("requestPost")
                }
            } else {
                if (isCallBackRequest(r_url)) {
                    saveCBRequestToDb(request, context)
                    processCBRequests(context, "requestPost")
                }
            }
        } catch (e: Exception) {
            Logger.w(TAG, "requestPost: Failed to send request - ${e.stackTrace}")
            Logger.e(TAG, "requestPOST: ${e.message}",e )
        }
    }

    suspend fun requestPOSTWithCallbacks(
        r_url: String?,
        postDataParams: JSONObject?,
        method: String = "POST",
        context: Context? = null
    ) {
        try {
            Logger.i(TAG, "requestPOSTWithCallbacks: Sending POST request with callbacks")
            val request = Request(r_url, postDataParams, method)
            if (FynoContextCreator.isInitialized()) {
                if (isCallBackRequest(r_url)) {
                    saveCBRequestToDb(request, context)
                    processCBRequests(context, "requestPOST")
                } else {
                    saveRequestToDb(request)
                    processDbRequests("requestPOST")
                }
            } else {
                if (isCallBackRequest(r_url)) {
                    saveCBRequestToDb(request, context)
                    processCBRequests(context, "requestPOST")
                }
            }
        } catch (e: Exception) {
            Logger.w(TAG, "requestPOSTWithCallbacks: Failed to send request - ${e.message}")
        }
    }

    private fun isCallBackRequest(url: String?): Boolean {
        return !url.isNullOrEmpty() && (url.contains("callback.fyno.io") || url.contains("callback.dev.fyno.io"))
    }

    // Function to save requests to SQLite database
    private fun saveRequestToDb(request: Request, id:Int? = 0) {
        Logger.i(TAG, "saveRequestToDb: Saving request to db with url: ${request.url}")
        FynoContextCreator.sqlDataHelper.insertRequest(request, "requests", id)
    }

    // Function to save CB requests to SQLite database
    private fun saveCBRequestToDb(request: Request, context: Context?) {
        sqlDataHelper = SQLDataHelper(context)
        sqlDataHelper.insertRequest(request, "callbacks")
        sqlDataHelper.close()
    }

    private fun deleteCBRequestFromDb(id: Int?, context: Context?) {
        sqlDataHelper = SQLDataHelper(context?.applicationContext)
        sqlDataHelper.deleteRequestByID(id, "callbacks")
        sqlDataHelper.close()
    }
}
