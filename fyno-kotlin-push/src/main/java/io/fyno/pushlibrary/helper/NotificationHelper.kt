package io.fyno.pushlibrary.helper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.messaging.RemoteMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import io.fyno.callback.FynoCallback
import io.fyno.pushlibrary.firebase.FcmHandlerService
import io.fyno.pushlibrary.mipush.MiPushHelper
import io.fyno.callback.models.MessageStatus
import io.fyno.pushlibrary.notification.*
import io.fyno.pushlibrary.notificationIntents.NotificationActionClickActivity
import io.fyno.pushlibrary.notificationIntents.NotificationClickActivity
import io.fyno.pushlibrary.notificationIntents.NotificationDismissedReceiver
import io.fyno.core.utils.FynoConstants
import io.fyno.core.utils.FynoContextCreator
import io.fyno.core.utils.Logger
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


object NotificationHelper {
    private fun createOnDismissedIntent(
        context: Context,
        notificationId: Int,
        callbackUrl: String
    ): PendingIntent? {
        val intent = Intent(context, NotificationDismissedReceiver::class.java)
        intent.putExtra("io.fyno.kotlin_sdk.notificationIntents.notificationDismissedReceiver.notificationId", notificationId)
        intent.putExtra("io.fyno.kotlin_sdk.notificationIntents.notificationDismissedReceiver.callback", callbackUrl)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getBroadcast(
            context,
            notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    private fun createOnClickIntent(
        context: Context,
        notificationId: Int,
        callbackUrl: String,
        action: String
    ): PendingIntent? {
        val intent = Intent(context, NotificationClickActivity::class.java)
        intent.putExtra("io.fyno.kotlin_sdk.notificationIntents.notificationClickedReceiver.notificationId", notificationId)
        intent.putExtra("io.fyno.kotlin_sdk.notificationIntents.notificationClickedReceiver.callback", callbackUrl)
        intent.putExtra("io.fyno.kotlin_sdk.notificationIntents.notificationClickedReceiver.action", action)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            context.applicationContext,
            notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createActionClickIntent(
        context: Context,
        notificationId: Int,
        callbackUrl: String?,
        action: String?,
        label: String?
    ): PendingIntent? {
        val intent = Intent(context, NotificationActionClickActivity::class.java)
        intent.extras?.getStringArrayList("io.fyno.kotlin_sdk.notificationIntents.notificationActionClickClickedReceiver.action")
        intent.putExtra("io.fyno.kotlin_sdk.notificationIntents.notificationActionClickClickedReceiver.notificationId", notificationId)
        intent.putExtra("io.fyno.kotlin_sdk.notificationIntents.notificationActionClickClickedReceiver.callback", callbackUrl)
        intent.putExtra("io.fyno.kotlin_sdk.notificationIntents.notificationActionClickClickedReceiver.label", label)
        intent.action = action
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            context.applicationContext,
            notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    private fun String?.createNotification(): RawMessage {
        this ?: return RawMessage("1", "1")

        val notificationPayloadJO = toNotificationObject()


        val id = notificationPayloadJO.safeString("id") ?: ""
        return RawMessage(
            id = id,
            channelId = notificationPayloadJO.safeString("channel"),
            channelName = notificationPayloadJO.safeString("channelName"),
            channelDescription = notificationPayloadJO.safeString("channelDescription"),
            showBadge = notificationPayloadJO.safeBoolean("badge"),
            cSound = notificationPayloadJO.safeString("cSound"),
            smallIconDrawable = notificationPayloadJO.safeString("icon"),
            color = notificationPayloadJO.safeString("color"),
            notificationTitle = notificationPayloadJO.safeString("title"),
            subTitle = notificationPayloadJO.safeString("subTitle"),
            shortDescription = notificationPayloadJO.safeString("content"),
            longDescription = notificationPayloadJO.safeString("longDescription"),
            iconUrl = notificationPayloadJO.safeString("bigIcon"),
            imageUrl = notificationPayloadJO.safeString("bigPicture"),
            action = notificationPayloadJO.safeString("action"),
            sound = notificationPayloadJO.safeString("sound"),
            callback = notificationPayloadJO.safeString("callback"),
            category = notificationPayloadJO.safeString("category"),
            group = notificationPayloadJO.safeString("group"),
            groupSubText = notificationPayloadJO.safeString("groupSubText"),
            groupShowWhenTimeStamp = notificationPayloadJO.safeBoolean("groupShowWhenTimeStamp"),
            groupWhenTimeStamp = notificationPayloadJO.safeLong("groupWhenTimeStamp"),
            sortKey = notificationPayloadJO.safeString("sortKey"),
            onGoing = notificationPayloadJO.safeBoolean("sticky"),
            autoCancel = notificationPayloadJO.safeBoolean("autoCancel"),
            timeoutAfter = notificationPayloadJO.safeLong("timeoutAfter"),
            showWhenTimeStamp = notificationPayloadJO.safeBoolean("showWhenTimeStamp"),
            whenTimeStamp = notificationPayloadJO.safeLong("whenTimeStamp"),
            actions = getActions(notificationPayloadJO),
            template = notificationPayloadJO.safeString("template")
        )
    }
    private fun getActions(notificationPayloadJO: JSONObject): List<Actions>? {
        val safeActions = notificationPayloadJO.safeJsonArray("actions")
        safeActions ?: return null
        val actionsList = arrayListOf<Actions>()
        for (i in 0 until safeActions.length()) {
            val actionObj = safeActions.getJSONObject(i)
            actionsList.add(
                Actions(
                    id = actionObj.safeString("id"),
                    title = actionObj.safeString("title"),
                    link = actionObj.safeString("link"),
                    iconDrawableName = actionObj.safeString("iconIdentifierName"),
                    notificationId = actionObj.safeString("notificationId"),
                    notificationActionType = when(actionObj.safeString("notificationActionType")){
                        "button" -> NotificationActionType.BUTTON
                        "body" -> NotificationActionType.BODY
                        else -> NotificationActionType.BODY
                    }
                )
            )
        }
        return actionsList
    }
    fun renderInappMessage(context: Context, notification: String?) {
        try {
            if(notification.isNullOrBlank()) return
            showNotification(context.applicationContext, notification.createNotification())
        } catch (e: Exception) {
            Logger.e(FcmHandlerService.TAG, "Push exception", e)
        }
    }

    fun renderFCMMessage(context: Context, remoteMessage: RawMessage) {
        try {
            showNotification(context.applicationContext, remoteMessage, FynoConstants.PUSH_VENDOR_FCM)
        } catch (e: Exception) {
            Logger.e(FcmHandlerService.TAG, "Push exception", e)
        }
    }

    fun renderXiaomiMessage( context: Context, miPushMessage: RawMessage) {
        try {
            showNotification(context.applicationContext, miPushMessage, FynoConstants.PUSH_VENDOR_XIAOMI)
        } catch (e: Exception) {
            Logger.e(MiPushHelper.TAG, "Push exception", e)
        }
    }

    private fun showNotification(context: Context, notification: RawMessage, pushVendor: String? = null) {
        var id = 0
        id = if (notification.id?.isNotBlank() == true)
            notification.id.hashCode()
        else
            Random(System.currentTimeMillis()).nextInt(1000)
        try {
            val notificationModel = notification.getNotificationModel()
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            if(!notificationManagerCompat.areNotificationsEnabled()){
                Logger.e(TAG,"Notifications permission denied")
                return
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = notificationModel.notificationChannel
                notificationManager.getNotificationChannel(notificationChannel.id)?.run {
                    name = notificationChannel.name
                    description = notificationChannel.description
                    notificationChannel.cSound.createRawSoundUri(context)?.let { soundUri ->
                        this.setSound(soundUri,
                            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build())
                    }
                    notificationManager.createNotificationChannel(this)
                }
                val importance = when (notificationChannel.channelImportance) {
                    NotificationChannelImportance.DEFAULT -> NotificationManager.IMPORTANCE_DEFAULT
                    NotificationChannelImportance.MIN -> NotificationManager.IMPORTANCE_MIN
                    NotificationChannelImportance.MAX -> NotificationManager.IMPORTANCE_MAX
                    NotificationChannelImportance.LOW -> NotificationManager.IMPORTANCE_LOW
                    NotificationChannelImportance.HIGH -> NotificationManager.IMPORTANCE_HIGH
                }
                val channel = android.app.NotificationChannel(notificationChannel.id,
                    notificationChannel.name,
                    importance).apply {
                    description = notificationChannel.description
                    setShowBadge(notificationChannel.showBadge)
                    notificationChannel.cSound.createRawSoundUri(context)?.let { soundUri ->
                        setSound(soundUri,
                            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                .build())
                    }
                    lockscreenVisibility = when (notificationChannel.channelLockScreenVisibility) {
                        NotificationChannelVisibility.PRIVATE -> Notification.VISIBILITY_PRIVATE
                        NotificationChannelVisibility.PUBLIC -> Notification.VISIBILITY_PUBLIC
                        NotificationChannelVisibility.SECRET -> Notification.VISIBILITY_SECRET
                    }
                }
                notificationManager.createNotificationChannel(channel)
            }
                val builder = Builder(context, notificationModel.notificationChannel.id)
                val basicNotification = notificationModel.BasicNotification
                if(basicNotification.smallIconDrawable.isNullOrBlank()){
                    val smallIcon = context.resources.getIdentifier("cloudy", "drawable", context.packageName)
                    builder.setSmallIcon(smallIcon)
                } else {
                    val smallIcon = context.resources.getIdentifier(basicNotification.smallIconDrawable, "drawable", context.packageName)
                    builder.setSmallIcon(smallIcon)
                }


                builder.setChannelId(notificationModel.notificationChannel.id)
                basicNotification.priority.let { priority ->
                    builder.priority = when(priority){
                        NotificationPriority.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
                        NotificationPriority.MIN -> NotificationCompat.PRIORITY_MIN
                        NotificationPriority.MAX -> NotificationCompat.PRIORITY_MAX
                        NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
                        NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
                    }
                }
                basicNotification.contentTitle.let {
                    builder.setContentTitle(it)
                }
                basicNotification.contentText.let {
                    builder.setContentText(it)
                }
                basicNotification.subTitle?.let {
                    builder.setSubText(it)
                }
               notificationModel.bigPicture?.let {
                   basicNotification.largeIconUrl?.let {
                       if(it.isNotBlank()){
                           builder.setLargeIcon(
                               Glide.with(FynoContextCreator.context).asBitmap().apply(RequestOptions().encodeFormat(
                                   Bitmap.CompressFormat.JPEG).encodeQuality(50)).load(it).submit().get()
                           )
                       }
                   }
                }
                basicNotification.category?.let {
                    builder.setCategory(it)
                }
                basicNotification.group?.let {
                    builder.setGroup(it)
                }
                basicNotification.color?.let {
                    if(it.isNotBlank()){
                        builder.color = Color.parseColor(it)
                    }
                }
                basicNotification.sound.let { sound ->
                    sound.createRawSoundUri(context)?.let {uri->
                        builder.setSound(uri)
                    }
                }
                basicNotification.timeoutAfter?.let {
                    builder.setTimeoutAfter(it)
                }
                basicNotification.autoCancel?.let {
                    builder.setAutoCancel(it)
                }
                basicNotification.onGoing?.let {
                    builder.setOngoing(it)
                }
                try {
                    builder.setContentIntent(
                        notification.callback?.let {
                            notificationModel.getNotificationBodyAction().link?.let { it1 ->
                                createOnClickIntent(
                                    context, id,
                                    it, it1
                                )
                            }
                        }
                    ).setDeleteIntent(notification.callback?.let {
                        createOnDismissedIntent(
                            context,
                            id,
                            it
                        )
                    })
                } catch (e: Exception) {
                    Logger.e("notification", "setBasicVo", e)
                }
            notificationModel.template?.let { setStyle(it,builder, notificationModel) }
            try {
                notificationModel.actions?.forEachIndexed { _, actionObj ->
                    var actionIcon = 0
                    actionObj.iconDrawableName?.let {
                        actionIcon = context.resources.getIdentifier(actionObj.iconDrawableName, "drawable", context.packageName)
                    }
                    val actionIntent = createActionClickIntent(context, id, notification.callback,actionObj.link,actionObj.title)
                    builder.addAction(
                        actionIcon,
                        actionObj.title,
                        actionIntent
                    )
                }
            } catch (e: Exception) {
                Logger.e("notification", "setNotificationAction", e)
            }
            notificationManager.notify(id, builder.build())
            notification.callback?.let { FynoCallback().updateStatus(it, MessageStatus.RECEIVED) }
        } catch (e: Exception) {
            Logger.e(TAG, "showNotification", e)
        }
    }
    private fun setStyle(style: String, builder: Builder, notification: NotificationModel) {
        when(style){
            "style_bigtext" -> setBigTextStyle(notification, builder)
            "style_bigpicture" -> setBigPictureStyle(notification, builder)
            "style_richtext" -> setRichTextStyle(notification, builder)
        }
    }
    private fun setBigTextStyle(notification: NotificationModel, builder: Builder){
        val bigText = notification.bigText ?: return
        val textStyle = NotificationCompat.BigTextStyle()

        bigText.bigContentTitle?.let {
            textStyle.setBigContentTitle(it)
        }
        bigText.bigText?.let {
            textStyle.bigText(it)
        }
        bigText.summaryText?.let {
            textStyle.setSummaryText(it)
        }

        builder.setStyle(textStyle)
    }
    @SuppressLint("SuspiciousIndentation")
    private fun setBigPictureStyle(notification: NotificationModel, builder: Builder){
        val bigPicture = notification.bigPicture ?: return
        val pictureStyle = NotificationCompat.BigPictureStyle()

        bigPicture.bigContentTitle?.let {
            pictureStyle.setBigContentTitle(it)
        }
        bigPicture.bigPictureUrl?.let {
            pictureStyle.bigPicture(Glide.with(FynoContextCreator.context).asBitmap().apply(RequestOptions().encodeFormat(Bitmap.CompressFormat.JPEG).encodeQuality(50)).load(it).submit().get())
        }
        bigPicture.largeIconUrl?.let {
            pictureStyle.bigLargeIcon(Glide.with(FynoContextCreator.context).asBitmap().apply(RequestOptions().encodeFormat(Bitmap.CompressFormat.JPEG).encodeQuality(50)).load(it).submit().get())
        }
        bigPicture.summaryText?.let {
            pictureStyle.setSummaryText(it)
        }
        if(Build.VERSION.SDK_INT >= 31)
        pictureStyle.showBigPictureWhenCollapsed(true)
        builder.setStyle(pictureStyle)
    }

    private fun setRichTextStyle(notification: NotificationModel, builder: Builder){
        val bigText = notification.bigText ?: return
        val textStyle = NotificationCompat.BigTextStyle()

        bigText.bigContentTitle?.let {
            textStyle.setBigContentTitle(HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT))
        }
        bigText.bigText?.let {
            textStyle.bigText(HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT))
        }
        bigText.summaryText?.let {
            textStyle.setSummaryText(HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_COMPACT))
        }

        builder.setStyle(textStyle)
    }
    fun RemoteMessage.rawMessage(): RawMessage {
        val payload = data["fyno_push"] ?: ""
        return payload.createNotification()
    }

    fun MiPushMessage.rawMessage(): RawMessage {
        val payload = JSONObject(content).getString("fyno_push")
        return payload.createNotification()
    }

    
    private fun String?.toNotificationObject(): JSONObject {
        return try {
            if (isNullOrBlank())
                return JSONObject()
            return JSONObject(this.replace("\\n","").replace("\\",""))
        } catch (e: Exception) {
            JSONObject()
        }
    }

    private fun JSONObject.safeString(key: String): String? {
        return if (!isNull(key))
            getString(key)
        else null
    }

    private fun JSONObject.safeBoolean(key: String): Boolean? {
        return if (!isNull(key))
            getBoolean(key)
        else null
    }

    private fun JSONObject.safeLong(key: String): Long? {
        return if (!isNull(key))
            getLong(key)
        else null
    }

    internal fun JSONObject.safeDouble(key: String): Double? {
        return if (!isNull(key))
            getDouble(key)
        else null
    }
    private fun JSONObject.safeJsonArray(key: String): JSONArray? {
        return if (!isNull(key))
            getJSONArray(key)
        else null
    }

    fun RemoteMessage?.isFynoMessage(): Boolean {
        return this?.data?.containsKey("fyno_push") ?: false
    }

    fun MiPushMessage?.isFynoMessage(): Boolean {
        return this?.content?.toNotificationObject()?.has("fyno_push") ?: false
    }

    private fun String?.createRawSoundUri(context: Context): Uri? {
        var soundFile = this ?: return null
        if (soundFile.isBlank()) {
            return null
        }

        soundFile = soundFile.substringBeforeLast(".")
        context.resources.getIdentifier(soundFile, "raw", context.packageName)

        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context
                .packageName + "/raw/" + soundFile
        )
    }
    private const val TAG = "Fyno_Notification"
}