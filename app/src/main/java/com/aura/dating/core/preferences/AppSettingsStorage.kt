package com.aura.dating.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.aura.dating.core.notifications.NotificationType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_settings_prefs")

interface AppSettingsStorage {
    val pushNotificationsEnabledFlow: Flow<Boolean>
    val newMatchesPushFlow: Flow<Boolean>
    val messagesPushFlow: Flow<Boolean>
    val likesPushFlow: Flow<Boolean>
    val showOnlineStatusFlow: Flow<Boolean>
    val showDistanceFlow: Flow<Boolean>

    suspend fun setPushNotificationsEnabled(enabled: Boolean)
    suspend fun setNewMatchesPushEnabled(enabled: Boolean)
    suspend fun setMessagesPushEnabled(enabled: Boolean)
    suspend fun setLikesPushEnabled(enabled: Boolean)
    suspend fun setShowOnlineStatus(show: Boolean)
    suspend fun setShowDistance(show: Boolean)

    suspend fun isNotificationAllowed(type: NotificationType): Boolean
}

@Singleton
class DataStoreAppSettingsStorage @Inject constructor(
    @ApplicationContext private val context: Context
) : AppSettingsStorage {

    companion object {
        private val KEY_PUSH_ENABLED = booleanPreferencesKey("push_enabled")
        private val KEY_NEW_MATCHES_PUSH = booleanPreferencesKey("new_matches_push")
        private val KEY_MESSAGES_PUSH = booleanPreferencesKey("messages_push")
        private val KEY_LIKES_PUSH = booleanPreferencesKey("likes_push")
        private val KEY_SHOW_ONLINE = booleanPreferencesKey("show_online")
        private val KEY_SHOW_DISTANCE = booleanPreferencesKey("show_distance")
    }

    override val pushNotificationsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_PUSH_ENABLED] ?: true
    }

    override val newMatchesPushFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_NEW_MATCHES_PUSH] ?: true
    }

    override val messagesPushFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_MESSAGES_PUSH] ?: true
    }

    override val likesPushFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_LIKES_PUSH] ?: true
    }

    override val showOnlineStatusFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_SHOW_ONLINE] ?: true
    }

    override val showDistanceFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KEY_SHOW_DISTANCE] ?: true
    }

    override suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_PUSH_ENABLED] = enabled }
    }

    override suspend fun setNewMatchesPushEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_NEW_MATCHES_PUSH] = enabled }
    }

    override suspend fun setMessagesPushEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_MESSAGES_PUSH] = enabled }
    }

    override suspend fun setLikesPushEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_LIKES_PUSH] = enabled }
    }

    override suspend fun setShowOnlineStatus(show: Boolean) {
        context.settingsDataStore.edit { it[KEY_SHOW_ONLINE] = show }
    }

    override suspend fun setShowDistance(show: Boolean) {
        context.settingsDataStore.edit { it[KEY_SHOW_DISTANCE] = show }
    }

    override suspend fun isNotificationAllowed(type: NotificationType): Boolean {
        val prefs = context.settingsDataStore.data.first()
        val masterEnabled = prefs[KEY_PUSH_ENABLED] ?: true
        if (!masterEnabled) return false

        return when (type) {
            NotificationType.NEW_MESSAGE -> prefs[KEY_MESSAGES_PUSH] ?: true
            NotificationType.NEW_MATCH -> prefs[KEY_NEW_MATCHES_PUSH] ?: true
            NotificationType.NEW_LIKE, NotificationType.SUPER_LIKE -> prefs[KEY_LIKES_PUSH] ?: true
            NotificationType.SYSTEM -> true
        }
    }
}
