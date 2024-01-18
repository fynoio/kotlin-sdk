package io.fyno.pushlibrary.notificationIntents

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.fyno.callback.FynoCallback
import io.fyno.core.FynoCore
import io.fyno.callback.models.MessageStatus
import io.fyno.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject



class NotificationActionClickActivity : AppCompatActivity() {
    val ACTION_NOTIFICATIONACTION_CLICK = "io.fyno.pushlibrary.NOTIFICATION_ACTION"

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Logger.d("${FynoCore.TAG}-NotificationActionClick", "onCreate: ")
            super.onCreate(savedInstanceState)
            val launchintent = handleActionClick()
            intent.getStringExtra("io.fyno.kotlin_sdk.notificationIntents.extras")
                ?.let { launchintent?.putExtra("io.fyno.pushlibrary.notification.payload", it) }
            startActivity(launchintent)
            val cintent = Intent()
            cintent.action = ACTION_NOTIFICATIONACTION_CLICK
            cintent.putExtra("io.fyno.pushlibrary.notification.action", "action_clicked")
            cintent.putExtra("io.fyno.pushlibrary.notification.intent", intent.toString())
            cintent.component = null
            sendBroadcast(cintent)
            finish()
        }catch(e:Exception) {
            Logger.e("${FynoCore.TAG}-NotificationActionClick", e.message.toString(),e)
        }
    }

    private fun handleActionClick(): Intent? {
        val callback = intent.getStringExtra("io.fyno.kotlin_sdk.notificationIntents.notificationActionClickClickedReceiver.callback")
        val notificationId = intent.getIntExtra("io.fyno.kotlin_sdk.notificationIntents.notificationActionClickClickedReceiver.notificationId", -1)
        val action = intent.action
        val label = intent.getStringExtra("io.fyno.kotlin_id.notificationIntents.notificationActionClickClickedReceiver.label")

        Logger.d("${FynoCore.TAG}-NotificationActionClick", "onReceive: $callback")

        callback?.let {
            runBlocking(Dispatchers.IO) {
                FynoCallback().updateStatus(
                    applicationContext,
                    it,
                    MessageStatus.CLICKED,
                    JSONObject().put("label", label).put("action", action)
                )
            }
        }

        var newIntent: Intent? = null

        action?.let {
            newIntent = when {
                it.startsWith("http://") or it.startsWith("https://") -> Intent(Intent.ACTION_VIEW, Uri.parse(it))
                it.startsWith("www.") -> Intent(Intent.ACTION_VIEW, Uri.parse("https://$it"))
                else -> {
                    try {
                        Intent(this, Class.forName(it))
                    } catch (e: ClassNotFoundException) {
                        Logger.w("${FynoCore.TAG}-ClassNotFound", "handleActionClick: ${e.message}")
                        packageManager.getLaunchIntentForPackage(packageName)
                    }
                }
            }?.apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
            }
        }

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(notificationId)

        return newIntent
    }

}