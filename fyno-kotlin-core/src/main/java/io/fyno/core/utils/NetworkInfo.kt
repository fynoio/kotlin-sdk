package io.fyno.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager


enum class NetworkType(val networkName: String) {
    WIFI("wifi"),
    G2("2G"),
    G3("3G"),
    G4("4G"),
    G5("5G"),
    CELLULAR("cellular"),
    VPN("vpn"),
    UNKNOWN("-")
}

object NetworkDetails {
    fun getConnectionType(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi; 3: vpn
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
                        result = 3
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = 2
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = 1
                    } else if(type == ConnectivityManager.TYPE_VPN) {
                        result = 3
                    }
                }
            }
        }
        return result
    }
    fun getNetworkType(): NetworkType {
        val context = FynoContextCreator.getContext()
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val currentNetwork = getConnectionType(context)
//        val capabilities = cm.getNetworkCapabilities(currentNetwork)
//        val linkProperties = cm.getLinkProperties(currentNetwork)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return NetworkType.WIFI
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return NetworkType.CELLULAR
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
                        return NetworkType.VPN
                    } else {
                        return NetworkType.UNKNOWN
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        return NetworkType.WIFI
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        return when (subtype) {
                            TelephonyManager.NETWORK_TYPE_GPRS,
                            TelephonyManager.NETWORK_TYPE_EDGE,
                            TelephonyManager.NETWORK_TYPE_CDMA,
                            TelephonyManager.NETWORK_TYPE_1xRTT,
                            TelephonyManager.NETWORK_TYPE_IDEN,
                            TelephonyManager.NETWORK_TYPE_GSM -> NetworkType.G2

                            TelephonyManager.NETWORK_TYPE_UMTS,
                            TelephonyManager.NETWORK_TYPE_EVDO_0,
                            TelephonyManager.NETWORK_TYPE_EVDO_A,
                            TelephonyManager.NETWORK_TYPE_HSDPA,
                            TelephonyManager.NETWORK_TYPE_HSUPA,
                            TelephonyManager.NETWORK_TYPE_HSPA,
                            TelephonyManager.NETWORK_TYPE_EVDO_B,
                            TelephonyManager.NETWORK_TYPE_EHRPD,
                            TelephonyManager.NETWORK_TYPE_HSPAP,
                            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> NetworkType.G3

                            TelephonyManager.NETWORK_TYPE_LTE,
                            TelephonyManager.NETWORK_TYPE_IWLAN,
                            19 -> NetworkType.G4
                            20 -> NetworkType.G5
                            else -> NetworkType.UNKNOWN
                        }
                    } else if(type == ConnectivityManager.TYPE_VPN) {
                        return NetworkType.VPN
                    } else {
                        return NetworkType.UNKNOWN
                    }
                }
            }
        }
        return NetworkType.UNKNOWN
    }

    fun isOnline(context: Context): Boolean {
//        val connectivityMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
//            return connectivityMgr.activeNetworkInfo != null
//
//        } else {
//            for (network in connectivityMgr.allNetworks) {
//                val networkCapabilities: NetworkCapabilities? = connectivityMgr.getNetworkCapabilities(network)
//                return (networkCapabilities!!.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
//                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
//                        (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)))
//            }
//        }
//        return false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @SuppressLint("MissingPermission") val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }
}