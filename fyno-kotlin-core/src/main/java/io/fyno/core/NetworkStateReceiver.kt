package io.fyno.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class NetworkStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (isNetworkConnected(context)) {
            // The network is back online, trigger retry mechanism here
            runBlocking {
                CoroutineScope(Dispatchers.IO).launch {
                    RequestHandler.processDbRequests()
                    RequestHandler.processCBRequests(context)
                }
            }
        }
    }

    private fun isNetworkConnected(context: Context?): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val networkCapabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            return networkCapabilities != null && (networkCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo: NetworkInfo? = cm.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}