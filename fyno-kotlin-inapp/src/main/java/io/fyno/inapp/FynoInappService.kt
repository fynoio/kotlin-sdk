package io.fyno.inapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.fyno.core.utils.Logger
import io.fyno.pushlibrary.helper.NotificationHelper
import org.json.JSONObject
import io.socket.client.IO
import io.socket.client.Socket

open class FynoInappService : Service() {
    private val TAG = "SocketInapp"
    var isNotified = false;
    lateinit var listener: InAppListener
    lateinit var mSocket: Socket
    var page = 1
    override fun onBind(intent: Intent?): IBinder? {
        // TODO("Not yet implemented")
        return TODO("Provide the return value")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("SocService", "onTaskRemoved: ${mSocket.isActive.toString()}")
    }

    internal fun initSocket(user_id: String, wsid: String, signature: String, l: InAppListener){
        val auth = mutableMapOf("user_id" to user_id, "WS_ID" to wsid)
        val headerValue = mutableListOf(signature)
        val headers = mutableMapOf("x-fyno-signature" to headerValue)
        listener = l
        var options: IO.Options? = IO.Options.builder().setAuth(auth).setExtraHeaders(headers).build()
        mSocket = IO.socket("https://inapp.dev.fyno.io",options)
        if(!mSocket.isActive){
            mSocket.connect().on("message"){
                Log.d("IncomingMessage", it[0].toString())
                val notificationContent: JSONObject =
                    (it[0] as JSONObject).get("notification_content") as JSONObject;
                Log.d(TAG, "initSocket: Message Received ${notificationContent.toString()}")
                handleFynoMessageReceived(notificationContent)
            }.on("connectionSuccess"){
                mSocket.emit("get:messages",JSONObject("{\"filter\":\"all\",\"page\":$page}"))
                page = page + 1
            }.on("messages:state"){
                Log.d(TAG, "Incoming Messages: ${it[0]}")
            }.on("statusUpdated"){
                Log.d(TAG, "Updated Messages: ${it[0]}")
            }.on("disconnect"){
                Log.d(TAG, "disconnected: $it")
            }
        }
        listener = l
    }

    open fun notify(remoteMessage: JSONObject) {
        if(mSocket.isActive) {
            Logger.d("In-FynoSDK", "handleFynoMessageReceived: ${remoteMessage.toString()}")
            NotificationHelper.renderInappMessage(this, remoteMessage.toString())
        }
    }

    open fun handleFynoMessageReceived(remoteMessage: JSONObject){
        if(mSocket.isActive) {
            listener.onMessageReceived(remoteMessage)
        }
    }

    open fun loadMore(){
        if(mSocket.isActive) {
            mSocket.emit("get:messages", JSONObject("{\"filter\":\"all\",\"page\":$page}"))
            page = page + 1
        }
    }

    open fun markAll(){
        if(mSocket.isActive) {
            mSocket.emit("markAll:read")
            page = page + 1
        }
    }

    open fun deleteAll(){
        if(mSocket.isActive) {
            mSocket.emit("markAll:delete")
            page = page + 1
        }
    }

    open fun markMessage(msg: JSONObject) {
        if(mSocket.isActive) {
            mSocket.emit("message:read", msg)
        }
    }

    open fun deleteMessage(msg: JSONObject) {
        if(mSocket.isActive) {
            mSocket.emit("message:deleted", msg)
        }
    }
}