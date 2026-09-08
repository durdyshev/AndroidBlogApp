package com.aura.dating

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.aura.dating.core.localization.LanguageManager
import com.aura.dating.core.notifications.AuraNotificationService
import com.aura.dating.core.preferences.AppSettingsStorage
import com.aura.dating.core.presence.PresenceManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AuraApplication : Application() {

    @Inject
    lateinit var presenceManager: PresenceManager

    @Inject
    lateinit var appSettingsStorage: AppSettingsStorage

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        presenceManager.initialize(this)
        AuraNotificationService.start(this)

        CoroutineScope(Dispatchers.Main).launch {
            val savedLang = appSettingsStorage.getSelectedLanguageCode()
            if (savedLang.isNotBlank() && savedLang != "system") {
                LanguageManager.applyLanguage(this@AuraApplication, savedLang)
            }
        }
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

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
