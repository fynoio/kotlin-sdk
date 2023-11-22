package io.fyno.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Telephony.Mms.Sent
import android.util.Log
import androidx.annotation.RequiresApi
import io.fyno.core.FynoCore.Companion.TAG
import io.fyno.core.utils.FynoContextCreator
import io.fyno.core.utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.json.JSONObject
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.LinkedList
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.pow

object RequestHandler {
    private const val TIMEOUT = 30000
    private const val MAX_BACKOFF_DELAY: Long = 60000
    private const val MAX_RETRIES = 3

    // Channel for queuing requests
    private val requestChannel = Channel<Request>()

    // Queue for offline requests
    internal val offlineRequestsQueue: ConcurrentLinkedQueue<Request> = ConcurrentLinkedQueue()

    // Network callback to listen for connectivity changes
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    data class Request(val url: String?, val postData: JSONObject?, val method: String = "POST")

    @Throws(Exception::class)
    suspend fun requestPOST(
        r_url: String?,
        postDataParams: JSONObject?,
        method: String = "POST",
    ) {
        try {
            val request = Request(r_url, postDataParams, method)
            if(FynoContextCreator.isInitialized()){
                if (isNetworkConnected(FynoContextCreator.context)) {
                    processOfflineRequests()
                    // If there is network, send the request immediately
                    sendRequest(request)
                } else {
                    // If no network, add the request to the offline queue
                    offlineRequestsQueue.add(request)
                }
            } else {
                sendRequest(request)
            }
            // Check if there's network connectivity

        } catch (e: Exception) {
            Logger.w(TAG, "requestPOST: Failed to send request - ${e.message}")
        }
    }

    // Function to send the request to the channel
    private suspend fun sendRequest(request: Request) {
        try {
            Logger.i(TAG, "requestPOST: Started processing ${request.url}")
            // Send the request to the channel
            requestChannel.send(request)
        } catch (e: Exception) {
            Logger.w(TAG, "sendRequest: Failed to send request to channel - ${e.message}")
        }
    }

    // Function to check network connectivity
    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
        } else {
            // Fallback for older Android versions
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo

            if (networkInfo != null) {
                return networkInfo.isConnected
            }

            // If networkInfo is null, there's no active network (no network connectivity)
            return false
        }
    }

    // Function to handle the retry mechanism
    private suspend fun handleRetries(request: Request) {
        var retries = 0
        while (retries < MAX_RETRIES) {
            try {
                // Attempt the request
                doRequest(request)
                return  // Request successful, exit the retry loop
            } catch (e: Exception) {
                Logger.d(TAG, "Request failed: ${e.message}")
                // Implement a backoff strategy here (e.g., exponential backoff)
                val delayMillis = calculateDelay(retries)
                delay(delayMillis)
                retries++
            }
        }
        Logger.w(TAG, "Max retries reached for request: ${request.url}")
        offlineRequestsQueue.add(request)
    }

    // Coroutine to process requests from the channel
    private val requestProcessor = CoroutineScope(Dispatchers.IO).launch {
        for (request in requestChannel) {
            handleRetries(request)
        }
    }

    private fun calculateDelay(retryCount: Int): Long {
        return minOf(2.0.pow(retryCount.toDouble()).toLong(), MAX_BACKOFF_DELAY)
    }

    @Throws(Exception::class)
    private fun doRequest(request: Request) {
        val url = URL(request.url)
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
            "requestPOST method = ${request.method} params=${request.postData.toString()} url = ${request.url}: ${conn.responseMessage}"
        )

        when (responseCode) {
            in 200..299 -> {
                Logger.i("RequestPost", "requestPOST: ${conn.responseMessage}")
            }
            in 400..499 -> {
                Logger.i(TAG,"Request failed with response code: $responseCode")
            }
            else -> {
                Logger.i(TAG,"Request failed with response code: $responseCode")
                throw Exception("Request failed with response code: $responseCode")
            }
        }
    }

    private fun HttpURLConnection.setRequestProperties() {
        this.setRequestProperty("Content-Type", "application/json")
        if (FynoContextCreator.isInitialized()) {
            this.setRequestProperty("Authorization", "Bearer ${FynoUser.getApi()}")
        }
    }

    // Function to process offline requests
    suspend fun processOfflineRequests() {
        try {
            for (request in offlineRequestsQueue) {
                sendRequest(request)
            }
            offlineRequestsQueue.clear()
        } catch (e: Exception) {
            Logger.w(TAG, e.message.toString())
        }
    }
}