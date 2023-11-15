package io.fyno.pushlibrary

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi

class FynoCallbackService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val data = intent?.getStringExtra("action")
        if (data != null) {
            val resultIntent = Intent(data)
            resultIntent.putExtra("processedData", data)
            sendBroadcast(resultIntent)
        }
        stopSelf()
        return START_STICKY
    }

}