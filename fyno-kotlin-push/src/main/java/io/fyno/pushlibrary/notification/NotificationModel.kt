package io.fyno.pushlibrary.notification

import java.io.Serializable

data class RawMessage(
    val id: String? = null,
    val channelId: String? = null,
    val channelName: String? = null,
    val channelDescription: String? = null,
    val showBadge: Boolean? = null,
    val channelLockScreenVisibility: NotificationChannelVisibility? = null,
    val cSound: String? = null,
    val channelImportance: NotificationChannelImportance? = null,
    val priority: NotificationPriority? = null,
    val smallIconDrawable: String? = null,
    val color: String? = null,
    val notificationTitle: String? = null,
    val subTitle: String? = null,
    val shortDescription: String? = null,
    val longDescription: String? = null,
    val ticker: String? = null,
    val iconUrl: String? = null,
    val imageUrl: String? = null,
    val action: String? = null,
    val sound: String? = null,
    val category: String? = null,
    val group: String? = null,
    val groupSubText: String? = null,
    val groupShowWhenTimeStamp: Boolean? = null,
    val groupWhenTimeStamp: Long? = null,
    val sortKey: String? = null,
    val onGoing: Boolean? = null,
    val autoCancel: Boolean? = null,
    val timeoutAfter: Long? = null,
    val showWhenTimeStamp: Boolean? = null,
    val whenTimeStamp: Long? = null,
    // Actions
    val actions: List<Actions>? = null,
    val callback: String?= null,
    val template: String?= null

): Serializable {
    fun getNotificationModel(): NotificationModel {
        var NotificationModel = NotificationModel(
            id = id ?: "",
            notificationChannel = NotificationChannel(
                id = channelId ?: "main",
                name = channelName ?: "Main",
                description = channelDescription ?: "",
                showBadge = showBadge ?: true,
                channelLockScreenVisibility = channelLockScreenVisibility ?: NotificationChannelVisibility.PUBLIC,
                channelImportance = channelImportance ?: NotificationChannelImportance.HIGH,
                cSound = cSound
            ),
            BasicNotification = BasicNotification(
                priority = priority ?: NotificationPriority.DEFAULT,
                contentTitle = notificationTitle ?: "",
                contentText = shortDescription ?: "",
                ticker = ticker ?: "",
                largeIconUrl = iconUrl,
                color = color,
                subTitle = subTitle,
                showWhenTimeStamp = showWhenTimeStamp,
                whenTimeStamp = whenTimeStamp,
                onGoing = onGoing,
                autoCancel = autoCancel,
                smallIconDrawable = smallIconDrawable,
                category = category,
                group = group,
                groupSubText = groupSubText,
                groupShowWhenTimeStamp = groupShowWhenTimeStamp,
                groupWhenTimeStamp = groupWhenTimeStamp,
                sortKey = sortKey,
                timeoutAfter = timeoutAfter,
                action = action,
                sound = sound
            ),
            actions = actions?.mapIndexed {
                    index,
                    Actions -> Actions.copy(id = (index + 1).toString(), notificationActionType = NotificationActionType.BUTTON )
            },
            callback = callback,
            template = template
        )

        NotificationModel = if (!imageUrl.isNullOrBlank()) {
            NotificationModel.copy(
                bigPicture = BigPicture(
                    bigContentTitle = notificationTitle ?: "",
                    summaryText = shortDescription ?: "",
                    bigPictureUrl = imageUrl,
                    largeIconUrl = iconUrl
                )
            )
        } else {
            NotificationModel.copy(
                bigText = BigText(
                    title = notificationTitle ?: "",
                    contentText = shortDescription ?: "",
                    bigContentTitle = notificationTitle ?: "",
                    bigText = longDescription ?: ""
                )
            )
        }

        return NotificationModel
    }


}

data class NotificationModel(
    val id: String,
    val notificationChannel: NotificationChannel,
    val BasicNotification: BasicNotification,
    val bigText: BigText? = null,
    val bigPicture: BigPicture? = null,
    val actions: List<Actions>? = null,
    val callback: String?= null,
    val template: String?= null
) {
    fun getNotificationBodyAction(): Actions {
        val action = BasicNotification.action
        return Actions(id = id,link = action, notificationActionType = NotificationActionType.BODY)
    }
}

data class Actions(
    val id: String?,
    val title: String? = null,
    val link: String? = null,
    val iconDrawableName: String? = null,
    val notificationId: String? = null,
    val notificationActionType: NotificationActionType? = null
) : Serializable

enum class NotificationActionType {
    BODY, BUTTON
}

data class NotificationChannel(
    val id: String,
    val name: String,
    val description: String,
    val showBadge: Boolean,
    val channelLockScreenVisibility: NotificationChannelVisibility = NotificationChannelVisibility.PUBLIC,
    val channelImportance: NotificationChannelImportance = NotificationChannelImportance.HIGH,
    val cSound:String?
)

enum class NotificationChannelVisibility {
    PUBLIC, PRIVATE, SECRET
}

enum class NotificationChannelImportance {
    HIGH, LOW, MAX, MIN, DEFAULT
}

enum class NotificationPriority {
    HIGH, LOW, MAX, MIN, DEFAULT
}

data class BasicNotification(
    val priority: NotificationPriority,
    val smallIconDrawable: String? = null,
    val color: String? = null,
    val contentTitle: String,
    val subTitle: String? = null,
    val contentText: String,
    val ticker: String,
    val largeIconUrl: String? = null,
    val action: String? = null,
    val sound: String? = null,
    val category: String? = null,
    val group: String? = null,
    val groupSubText: String? = null,
    val groupShowWhenTimeStamp: Boolean? = null,
    val groupWhenTimeStamp: Long? = null,
    val sortKey: String? = null,
    val onGoing: Boolean? = null,
    val autoCancel: Boolean? = null,
    val timeoutAfter: Long? = null,
    val showWhenTimeStamp: Boolean? = null,
    val whenTimeStamp: Long? = null,
)

data class BigText(
    val title: String? = null,
    val contentText: String? = null,
    val summaryText: String? = null,
    val bigContentTitle: String? = null,
    val bigText: String? = null
)

data class BigPicture(
    val bigContentTitle: String? = null,
    val summaryText: String? = null,
    val bigPictureUrl: String? = null,
    val largeIconUrl: String? = null
)
