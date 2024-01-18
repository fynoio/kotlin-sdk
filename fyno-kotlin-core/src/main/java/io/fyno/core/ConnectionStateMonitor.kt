package io.fyno.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ConnectionStateMonitor : NetworkCallback() {
    private var isNetworkCallbackRegistered = false
    private lateinit var cbContext: Context
    private val networkRequest: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    fun enable(context: Context) {
        cbContext = context
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if(isNetworkCallbackRegistered) {
            connectivityManager.unregisterNetworkCallback(this)
        } else {
            connectivityManager.registerNetworkCallback(networkRequest, this)
            isNetworkCallbackRegistered = true
        }
    }

    override fun onAvailable(network: Network) {
        runBlocking {
            CoroutineScope(Dispatchers.IO).launch {
                RequestHandler.processDbRequests()
                RequestHandler.processCBRequests(cbContext)
            }
        }
    }
}