package com.fynoio.pushsdk.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.graphics.toColorInt
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.fyno.kotlin_sdk.FynoSdk
import com.fynoio.pushsdk.notificationIntents.NotificationDismissedReceiver
import com.fynoio.pushsdk.notificationIntents.NotificationActionClickActivity
import com.fynoio.pushsdk.notificationIntents.NotificationClickActivity
import com.google.firebase.messaging.RemoteMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import io.fyno.kotlin_sdk.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.properties.Delegates


class TemplateBuilder{
    private fun createOnDismissedIntent(
        context: Context,
        notificationId: Int,
        callbackUrl: String
    ): PendingIntent? {
        val intent = Intent(context, NotificationDismissedReceiver::class.java)
        intent.putExtra("com.fynoapp.notification.notificationIntents.notificationDismissedReceiver.notificationId", notificationId)
        intent.putExtra("com.fynoapp.notification.notificationIntents.notificationDismissedReceiver.callback", callbackUrl)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            notificationId, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private fun createOnClickIntent(
        context: Context,
        notificationId: Int,
        callbackUrl: String,
        action: String
    ): PendingIntent? {
        val intent = Intent(context, NotificationClickActivity::class.java)
        intent.putExtra("com.fynoapp.notification.notificationIntents.notificationClickedReceiver.notificationId", notificationId)
        intent.putExtra("com.fynoapp.notification.notificationIntents.notificationClickedReceiver.callback", callbackUrl)
        intent.putExtra("com.fynoapp.notification.notificationIntents.notificationClickedReceiver.action", action)
        return PendingIntent.getActivity(
            context.applicationContext,
            notificationId, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createActionClickIntent(
        context: Context,
        notificationId: Int,
        callbackUrl: String?,
        action: String?,
    ): PendingIntent? {
        val intent = Intent(context, NotificationActionClickActivity::class.java)
        intent.extras?.getStringArrayList("com.fynoapp.notification.notificationIntents.notificationActionClickClickedReceiver.action")
        intent.putExtra("com.fynoapp.notification.notificationIntents.notificationActionClickClickedReceiver.notificationId", notificationId)
        intent.putExtra("com.fynoapp.notification.notificationIntents.notificationActionClickClickedReceiver.callback", callbackUrl)
//            intent.putExtra("com.fynoapp.notification.notificationIntents.notificationActionClickClickedReceiver.action", )
        intent.action = action
        return PendingIntent.getActivity(
            context.applicationContext,
            notificationId, intent, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    internal fun build(remoteMessage: RemoteMessage  ): Triple<Notification,Int, NotificationManager>  {
        Log.d("FCMReceived", "sendNotification: "+remoteMessage.data.toString())
        val data: Map<String,String> = remoteMessage.data
        val id = if (data["tag"] != null)
            data["tag"].hashCode()
        else
            Random(System.currentTimeMillis()).nextInt(1000)
        val notificationManager: NotificationManager = FynoSdk.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                FynoSdk.appContext.getString(R.string.CHANNEL_ID),
                FynoSdk.appContext.getString(R.string.CHANNEL_NAME),
                NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.description = FynoSdk.appContext.getString(R.string.CHANNEL_DESCRIPTION)
            notificationChannel.setShowBadge(true)
            val notificationManager: NotificationManager = FynoSdk.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(FynoSdk.appContext,"Fyno")
            .setAutoCancel(true)
            .setSmallIcon(FynoSdk.appContext.resources.getIdentifier("ic_launcher_foreground", "drawable", FynoSdk.appContext.packageName))
            .setWhen(System.currentTimeMillis())
            .setContentText(data["body"])
            .setContentTitle(data["title"])
            .setSubText(data["subtitle"])
            .setContentIntent(data["callback"]?.let {
                if(data["action"] != null){
                    createOnClickIntent(
                        FynoSdk.appContext, id,
                        it, data["action"].toString()
                    )
                } else {
                    createOnClickIntent(
                        FynoSdk.appContext, id,
                        it, "null"
                    )
                }
            })
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setDeleteIntent(data["callback"]?.let {
                createOnDismissedIntent(
                    FynoSdk.appContext, id,
                    it
                )
            })

        if (data["color"] != null) {
            notificationBuilder.color = data["color"]!!.toColorInt()
        }
        if (data["bigText"] != null)
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle().bigText(data["bigText"])
            )
        if (data["bigPicture"] != null) {
            val bigPictureBitmap =
                Glide.with(FynoSdk.appContext).asBitmap().apply(RequestOptions().encodeFormat(Bitmap.CompressFormat.JPEG).encodeQuality(50)).load(data["bigPicture"]).submit().get()
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(bigPictureBitmap)
                    .bigLargeIcon(null)
            )
        }
        if (data["icon"] != null) {
            val smallPictureBitmap =
                Glide.with(FynoSdk.appContext).asBitmap().apply(RequestOptions().encodeFormat(Bitmap.CompressFormat.JPEG).encodeQuality(50)).load(data["icon"]).submit().get()
            notificationBuilder.setLargeIcon(smallPictureBitmap)
        } else {
            notificationBuilder.setLargeIcon(
                BitmapFactory.decodeResource(
                    FynoSdk.appContext.resources,
                    FynoSdk.appContext.resources.getIdentifier("ic_launcher_foreground", "drawable", FynoSdk.appContext.packageName)
                )
            )
        }

        if (data["buttons"] != null) {
            val buttonsArray = JSONArray(data["buttons"])
            for (i in 0 until buttonsArray.length()) {
                val buttonObj = (buttonsArray[i] as JSONObject)
                notificationBuilder.addAction(
                    0,
                    buttonObj.get("label") as String, createActionClickIntent(FynoSdk.appContext, id, data["callback"].toString(),buttonObj.get("action").toString())
                )
            }
        }
        /*if(template == "HTML")
        {
            val styledTitle =
                HtmlCompat.fromHtml(data["title"].toString(), HtmlCompat.FROM_HTML_MODE_COMPACT)
            val styledText = HtmlCompat.fromHtml(data["body"].toString(), HtmlCompat.FROM_HTML_MODE_COMPACT)
            val styledBigText =  HtmlCompat.fromHtml(data["bigText"].toString(), HtmlCompat.FROM_HTML_MODE_COMPACT)
            notificationBuilder.setContentTitle(styledTitle)
                .setContentText(styledText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(styledBigText))
        } else if(template == "Custom"){
            val bigPictureBitmap =
                Glide.with(this).asBitmap().apply(RequestOptions().encodeFormat(Bitmap.CompressFormat.JPEG).encodeQuality(50)).load(data["bigPicture"]).submit().get();
            val remoteViews = RemoteViews(packageName, R.layout.notification_expanded)
            remoteViews.setImageViewBitmap(R.id.image, bigPictureBitmap)
            notificationBuilder.setCustomBigContentView(remoteViews).setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }*/
        val builder = notificationBuilder.build()
        if(data["sticky"] == "true")
            builder.flags = (Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)
        return Triple(builder, id, notificationManager)
    }

    internal fun build(pushData: MiPushMessage ): Triple<Notification, Int, NotificationManager> {
        Log.d("MiPushReceived", "sendNotification: $pushData")
        lateinit var builder: Notification
        var id by Delegates.notNull<Int>()
        lateinit var notificationManager: NotificationManager
        if(pushData.passThrough.toString() == "1"){
            val data = JSONObject(pushData.content)
            id = if (data.has("key"))
                data["tag"].hashCode()
            else
                Random(System.currentTimeMillis()).nextInt(1000)
            notificationManager = FynoSdk.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //Features of channel
                val notificationChannel = NotificationChannel(
                    FynoSdk.appContext.getString(R.string.CHANNEL_ID),
                    FynoSdk.appContext.getString(R.string.CHANNEL_NAME),
                    NotificationManager.IMPORTANCE_HIGH) //Priority

                notificationChannel.description = FynoSdk.appContext.getString(R.string.CHANNEL_DESCRIPTION)

                //Notification is visible on the App's icon
                notificationChannel.setShowBadge(true)

                //We need the notification Manager to register the notification channel
                val notificationManager = FynoSdk.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                //Creates the notification
                notificationManager.createNotificationChannel(notificationChannel)
            }
            val notificationBuilder = NotificationCompat.Builder(FynoSdk.appContext,"Fyno")
                .setAutoCancel(true)
            if(data.has("smallIcon"))
                notificationBuilder.setSmallIcon(FynoSdk.appContext.resources.getIdentifier(data["smallIcon"] as String, "drawable", FynoSdk.appContext.packageName))
            else
                notificationBuilder.setSmallIcon(FynoSdk.appContext.resources.getIdentifier("ic_launcher_foreground", "drawable", FynoSdk.appContext.packageName))
            notificationBuilder.setWhen(System.currentTimeMillis())
            if(data.has("body"))notificationBuilder.setContentText(data["body"].toString())
            if(data.has("title"))notificationBuilder.setContentTitle(data["title"].toString())
            if(data.has("subtitle"))notificationBuilder.setSubText(data["subtitle"].toString())
            if(data.has("callback")) {
                notificationBuilder.setContentIntent(data["callback"].toString()?.let {
                    if (data.has("action")) {
                        createOnClickIntent(
                            FynoSdk.appContext, id,
                            it, data["action"].toString()
                        )
                    } else {
                        createOnClickIntent(
                            FynoSdk.appContext, id,
                            it, "null"
                        )
                    }
                })
                .setDeleteIntent(data["callback"].toString()?.let {
                    createOnDismissedIntent(
                        FynoSdk.appContext, id,
                        it
                    )
                })
            }
            if(data.has("sound"))notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

            if (data.has("color")) {
                notificationBuilder.color = data["color"].toString().toColorInt()
            }
            if (data.has("bigText"))
                notificationBuilder.setStyle(
                    NotificationCompat.BigTextStyle().bigText(data["bigText"].toString())
                )
            if (data.has("bigPicture")) {
                val bigPictureBitmap =
                    Glide.with(FynoSdk.appContext).asBitmap().apply(RequestOptions().encodeFormat(Bitmap.CompressFormat.JPEG).encodeQuality(50)).load(data["bigPicture"].toString()).submit().get()
                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(bigPictureBitmap)
                        .bigLargeIcon(null)
                )
            }
            if (data.has("icon")) {
                val smallPictureBitmap =
                    Glide.with(FynoSdk.appContext).asBitmap().apply(RequestOptions().encodeFormat(Bitmap.CompressFormat.JPEG).encodeQuality(50)).load(data["icon"].toString()).submit().get()
                notificationBuilder.setLargeIcon(smallPictureBitmap)
            }
//        else {
//            notificationBuilder.setLargeIcon(
//                BitmapFactory.decodeResource(
//                    FynoSdk.appContext.resources,
//                    FynoSdk.appContext.resources.getIdentifier("ic_launcher_foreground", "drawable", FynoSdk.appContext.packageName)
//                )
//            )
//        }

            if (data.has("buttons")) {
                val buttonsArray = data["buttons"] as JSONArray
                for (i in 0 until buttonsArray.length()) {
                    val buttonObj = (buttonsArray[i] as JSONObject)
                    notificationBuilder.addAction(
                        0,
                        buttonObj.get("label") as String, createActionClickIntent(FynoSdk.appContext, id,
                            (if(data.has("callback"))data["callback"] else "null") as String,buttonObj.get("action").toString())
                    )
                }
            }
            builder = notificationBuilder.build()
        }
        return Triple(builder, id, notificationManager)
    }

    internal fun build(data: JSONObject ): Triple<Notification,Int, NotificationManager>  {
        Log.d("InAppMessage", "sendNotification: $data")
        val notificationManager: NotificationManager = FynoSdk.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Features of channel
            val notificationChannel = NotificationChannel(
                FynoSdk.appContext.getString(R.string.CHANNEL_ID),
                FynoSdk.appContext.getString(R.string.CHANNEL_NAME),
                NotificationManager.IMPORTANCE_HIGH) //Priority

            notificationChannel.description = FynoSdk.appContext.getString(R.string.CHANNEL_DESCRIPTION)

            //Notification is visible on the App's icon
            notificationChannel.setShowBadge(true)

            //We need the notification Manager to register the notification channel
            val notificationManager = FynoSdk.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            //Creates the notification
            notificationManager.createNotificationChannel(notificationChannel)
        }
        var id = 0
        if (data.has("tag"))
            id= data["tag"].hashCode()
        else
            id = Random(System.currentTimeMillis()).nextInt(1000)
        val notificationBuilder = NotificationCompat.Builder(FynoSdk.appContext,"Fyno");
        notificationBuilder.setAutoCancel(true)
            .setSmallIcon(FynoSdk.appContext.resources.getIdentifier("ic_launcher_foreground", "drawable", FynoSdk.appContext.packageName))
            .setWhen(System.currentTimeMillis())
            .setContentText(data.getString("body"))
            .setContentTitle(data.getString("title"))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        if(data.has("callback")){
            notificationBuilder.setContentIntent(data["callback"]?.let {
                createOnClickIntent(
                    FynoSdk.appContext, id,
                    it as String, data["action"].toString()
                )
            }).setDeleteIntent(data["callback"]?.let {
                createOnDismissedIntent(
                    FynoSdk.appContext, id,
                    it as String
                )
            })
        }

        val builder = notificationBuilder.build();
        return Triple(builder, id, notificationManager)
    }
}