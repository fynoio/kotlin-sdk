package io.fyno.pushlibrary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

open class FynoBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent!!.action) {
            "io.fyno.pushlibrary.NOTIFICATION_CLICKED" -> {
                Log.d("LocalTag", "Custom broadcast received FOR CLICK")
            }
            "io.fyno.pushlibrary.NOTIFICATION_ACTION_CLICKED" -> {
                Log.d("LocalTag", "Custom broadcast received ACTION_CLICK")
            }
            "io.fyno.pushlibrary.NOTIFICATION_DISMISSED" -> {
                Log.d("LocalTag", "Custom broadcast received DISMISSED")
            }
        }
    }
}