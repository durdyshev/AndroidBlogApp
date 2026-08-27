package com.aura.dating.core.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aura.dating.MainActivity
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
    fun showNotification(
        title: String,
        body: String,
        type: NotificationType,
        extraData: Map<String, String> = emptyMap()
    ) {
        val channelId = "aura_notifications"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            extraData.forEach { (k, v) -> putExtra(k, v) }
            putExtra("notification_type", type.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
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
        // Store or sync new FCM device token
        CoroutineScope(Dispatchers.IO).launch {
            val userId = tokenStorage.getUserId()
            if (!userId.isNullOrBlank()) {
                // Token will be synced to Supabase device_tokens table
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
