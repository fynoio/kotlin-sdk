package io.fyno.pushlibrary.notificationIntents

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.fyno.callback.FynoCallback
import io.fyno.core.FynoCore
import io.fyno.callback.models.MessageStatus
import io.fyno.core.utils.Logger
import io.fyno.pushlibrary.FynoPush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.json.JSONObject



class NotificationActionClickActivity : AppCompatActivity() {
    val ACTION_NOTIFICATIONACTION_CLICK = "io.fyno.pushlibrary.NOTIFICATION_ACTION"

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Logger.d("${FynoCore.TAG}-NotificationActionClick", "onCreate: ")
            val launchintent = handleActionClick()
            intent.getStringExtra("io.fyno.kotlin_sdk.notificationIntents.extras")
                ?.let { launchintent?.putExtra("io.fyno.pushlibrary.notification.payload", it) }
            this.applicationContext.startActivity(intent)
            val cintent = Intent()
            cintent.action = ACTION_NOTIFICATIONACTION_CLICK
            cintent.putExtra("io.fyno.pushlibrary.notification.action", "action_clicked")
            cintent.putExtra("io.fyno.pushlibrary.notification.intent", intent.toString())
            cintent.component = null
            sendBroadcast(cintent)
            super.onCreate(savedInstanceState)
            finish()
        }catch(e:Exception) {
            Logger.e("${FynoCore.TAG}-NotificationActionClick", e.message.toString(),e)
        }
    }

    private fun handleActionClick(): Intent? {
        var newintent: Intent? = null

        val callback =
            intent.extras!!.getString("io.fyno.kotlin_sdk.notificationIntents.notificationActionClickClickedReceiver.callback")
        val notificationId =
            intent.extras!!.getInt("io.fyno.kotlin_sdk.notificationIntents.notificationActionClickClickedReceiver.notificationId")
        var action = intent.action
        var label =
            intent.extras!!.getString("io.fyno.kotlin_id.notificationIntents.notificationActionClickClickedReceiver.label")
        Logger.d("${FynoCore.TAG}-NotificationActionClick", "onReceive: $callback")
        if (callback != null) {
            runBlocking(Dispatchers.IO) {
                FynoCallback().updateStatus(
                    applicationContext,
                    callback,
                    MessageStatus.CLICKED,
                    JSONObject().put("label", label).put("action", action)
                )
            }
        }
        if (action != null) {
            if (action.startsWith("http://") or action.startsWith("https://")) {
                newintent = Intent(Intent.ACTION_VIEW, Uri.parse(action)).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            } else if (action.startsWith("www.")) {
                newintent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$action")).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            } else {
                try {
                    newintent = Intent(this, Class.forName(action)).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
                    }
                } catch (e: ClassNotFoundException) {
                    Logger.w("${FynoCore.TAG}-ClassNotFound", "handleActionClick: ${e.message}")
                    newintent = this.packageManager.getLaunchIntentForPackage(this.packageName)
                        ?.apply {
                            flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
                        }
                }
            }
        }
//        putExtra("io.fyno.kotlin_sdk.notificationIntents.extras")
//        FynoPush.PushObject.handleActionClick(intent.extras!!.getString("io.fyno.kotlin_sdk.notificationIntents.extras"))
        val mNotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(notificationId)

        return newintent
    }
}