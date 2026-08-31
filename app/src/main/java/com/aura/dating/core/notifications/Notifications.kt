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

enum class NotificationType {
    NEW_LIKE,
    NEW_MATCH,
    NEW_MESSAGE,
    SUPER_LIKE,
    SYSTEM
}

@Singleton
class NotificationHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val ALERT_CHANNEL_ID = "aura_notifications"
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

    fun showNotification(
        title: String,
        body: String,
        type: NotificationType,
        extraData: Map<String, String> = emptyMap()
    ) {
        try {
            ensureChannelExists()

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                extraData.forEach { (k, v) -> putExtra(k, v) }
                putExtra("notification_type", type.name)
            }

            val reqCode = (System.currentTimeMillis() % 100000).toInt()
            val pendingIntent = PendingIntent.getActivity(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationBuilder = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 300, 200, 300))
                .setContentIntent(pendingIntent)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(reqCode, notificationBuilder.build())
            Log.d("AuraNotification", "Notification shown successfully: $title - $body")
        } catch (e: Exception) {
            Log.e("AuraNotification", "Failed to show notification: ${e.message}", e)
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

        val type = try {
            NotificationType.valueOf(typeString)
        } catch (e: Exception) {
            NotificationType.SYSTEM
        }

        notificationHandler.showNotification(
            title = title,
            body = body,
            type = type,
            extraData = remoteMessage.data
        )
    }
}
