package io.fyno.kotlin_sdk

import org.json.JSONObject

interface FynoMessageListener{
    fun onReceiveFynoMessage(remoteMessage: JSONObject)
}

class InAppListener{
    var listener: FynoMessageListener? = null

    fun setFynoListener( newlistener: FynoMessageListener){
        listener = newlistener
    }

    fun onMessageReceived(remoteMessage: JSONObject){
        listener?.onReceiveFynoMessage(remoteMessage)
    }
}