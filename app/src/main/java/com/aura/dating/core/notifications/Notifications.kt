package com.aura.dating.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aura.dating.MainActivity
import com.aura.dating.R
import com.aura.dating.core.security.TokenStorage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import android.content.BroadcastReceiver
import com.aura.dating.core.preferences.AppSettingsStorage
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

enum class NotificationType {
    NEW_LIKE,
    NEW_MATCH,
    NEW_MESSAGE,
    SUPER_LIKE,
    SYSTEM
}

class NotificationDismissReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_DISMISS_CONVERSATION = "com.aura.dating.ACTION_DISMISS_CONVERSATION"
        const val EXTRA_CONVERSATION_KEY = "extra_conversation_key"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_DISMISS_CONVERSATION) {
            val key = intent.getStringExtra(EXTRA_CONVERSATION_KEY)
            if (!key.isNullOrBlank()) {
                NotificationHandler.clearBufferForKey(key)
            }
        }
    }
}

@Singleton
class NotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appSettingsStorage: AppSettingsStorage
) {
    companion object {
        const val ALERT_CHANNEL_ID = "aura_notifications"
        private val unreadMessageBuffer = ConcurrentHashMap<String, MutableList<String>>()
        private val recentNotificationTimestamps = ConcurrentHashMap<String, Long>()

        fun clearBufferForKey(key: String) {
            unreadMessageBuffer.remove(key)
        }
    }

    init {
        ensureChannelExists()
    }

    private fun ensureChannelExists() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val channel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Aura Messages & Matches",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time alerts for incoming messages and matches"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(soundUri, null)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun clearNotificationsForConversation(conversationId: String) {
        try {
            clearBufferForKey(conversationId)
            val notifId = Math.abs(conversationId.hashCode())
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(notifId)
        } catch (e: Exception) {
            Log.e("AuraNotification", "Failed to clear conversation notifications", e)
        }
    }

    suspend fun showNotification(
        title: String,
        body: String,
        type: NotificationType,
        extraData: Map<String, String> = emptyMap()
    ) {
        try {
            val isAllowed = appSettingsStorage.isNotificationAllowed(type)
            if (!isAllowed) {
                Log.d("AuraNotification", "Notification for type $type is disabled in user Settings. Skipping.")
                return
            }

            val conversationId = extraData["conversation_id"]?.takeIf { it.isNotBlank() }
            val senderId = extraData["sender_id"]?.takeIf { it.isNotBlank() }
            val groupKey = conversationId ?: senderId ?: "general_chat"

            // Deduplication check to prevent dual triggers (Realtime + Background Sync / FCM)
            val dedupKey = extraData["message_id"]?.takeIf { it.isNotBlank() } ?: "$type:$groupKey:$body"
            val now = System.currentTimeMillis()
            val lastSent = recentNotificationTimestamps[dedupKey] ?: 0L
            if (now - lastSent < 2000L) {
                Log.d("AuraNotification", "Duplicate notification ignored: $dedupKey")
                return
            }
            recentNotificationTimestamps[dedupKey] = now
            if (recentNotificationTimestamps.size > 200) {
                val cutoff = now - 60000L
                recentNotificationTimestamps.entries.removeIf { it.value < cutoff }
            }

            ensureChannelExists()

            val notifId = if (type == NotificationType.NEW_MESSAGE) {
                Math.abs(groupKey.hashCode())
            } else {
                val actorId = extraData["actor_id"]?.takeIf { it.isNotBlank() } ?: extraData["notification_id"]
                if (!actorId.isNullOrBlank()) Math.abs(actorId.hashCode()) else (System.currentTimeMillis() % 100000).toInt()
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                action = "ACTION_OPEN_NOTIF_${notifId}"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                extraData.forEach { (k, v) -> putExtra(k, v) }
                putExtra("notification_type", type.name)
                putExtra("title", title)
                if (conversationId != null) putExtra("conversation_id", conversationId)
                if (senderId != null) putExtra("sender_id", senderId)
                if (!hasExtra("sender_name")) putExtra("sender_name", title)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Delete Intent to clear buffer if user dismisses/swipes away the notification
            val deleteIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
                action = NotificationDismissReceiver.ACTION_DISMISS_CONVERSATION
                putExtra(NotificationDismissReceiver.EXTRA_CONVERSATION_KEY, groupKey)
            }
            val deletePendingIntent = PendingIntent.getBroadcast(
                context,
                notifId,
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationBuilder = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 300, 200, 300))
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)

            if (type == NotificationType.NEW_MESSAGE) {
                // Track and stack unread messages from this sender
                val messageList = unreadMessageBuffer.compute(groupKey) { _, existingList ->
                    (existingList ?: Collections.synchronizedList(mutableListOf())).apply { add(body) }
                } ?: mutableListOf(body)

                val count = messageList.size
                if (count > 1) {
                    val displayTitle = "$title ($count yeni mesaj)"
                    notificationBuilder.setContentTitle(displayTitle)
                    notificationBuilder.setContentText(body)
                    notificationBuilder.setNumber(count)

                    val inboxStyle = NotificationCompat.InboxStyle()
                        .setBigContentTitle(displayTitle)
                        .setSummaryText("$count mesaj")

                    // Show up to the last 6 messages in the stacked card
                    val recentMessages = messageList.takeLast(6)
                    for (msg in recentMessages) {
                        inboxStyle.addLine(msg)
                    }
                    notificationBuilder.setStyle(inboxStyle)
                } else {
                    notificationBuilder.setContentTitle(title)
                    notificationBuilder.setContentText(body)
                    notificationBuilder.setNumber(1)
                    notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
                }
            } else {
                notificationBuilder.setContentTitle(title)
                notificationBuilder.setContentText(body)
                notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(notifId, notificationBuilder.build())
            Log.d("AuraNotification", "Notification shown (id: $notifId, type: $type): $title - $body")
        } catch (e: Exception) {
            Log.e("AuraNotification", "Failed to show notification", e)
        }
    }
}

@AndroidEntryPoint
class AuraFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var tokenStorage: TokenStorage

    @Inject
    lateinit var notificationHandler: NotificationHandler

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            val userId = tokenStorage.getUserId()
            if (!userId.isNullOrBlank()) {
                // Sync token
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Aura"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val typeString = remoteMessage.data["type"] ?: "SYSTEM"
        val type = NotificationType.entries.firstOrNull { it.name == typeString } ?: NotificationType.SYSTEM

        CoroutineScope(Dispatchers.IO).launch {
            notificationHandler.showNotification(
                title = title,
                body = body,
                type = type,
                extraData = remoteMessage.data
            )
        }
    }
}
