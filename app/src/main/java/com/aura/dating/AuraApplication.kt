package com.aura.dating

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.getSystemService
import com.aura.dating.core.notifications.GlobalNotificationManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AuraApplication : Application() {

    @Inject
    lateinit var globalNotificationManager: GlobalNotificationManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        globalNotificationManager.startListening()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "aura_notifications"
            val channelName = "Aura Notifications"
            val channelDesc = "Notifications for likes, matches, and messages"

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDesc
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService<NotificationManager>()
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
