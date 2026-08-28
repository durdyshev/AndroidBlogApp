package com.aura.dating.core.notifications

import com.aura.dating.core.common.result.Result
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.domain.notifications.repository.NotificationRepository
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalNotificationManager @Inject constructor(
    private val clientProvider: SupabaseClientProvider,
    private val tokenStorage: TokenStorage,
    private val notificationHandler: NotificationHandler,
    private val notificationRepository: NotificationRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val seenNotificationIds = ConcurrentHashMap.newKeySet<String>()
    private val json = Json { ignoreUnknownKeys = true }
    private var listenerJob: Job? = null

    fun startListening() {
        if (listenerJob?.isActive == true) return

        listenerJob = scope.launch {
            while (isActive) {
                val userId = tokenStorage.getUserId()
                if (!userId.isNullOrBlank()) {
                    // 1. Initial fetch to mark existing notifications as seen so we don't spam old notifications
                    val initialRes = notificationRepository.getNotifications()
                    if (initialRes is Result.Success) {
                        initialRes.data.forEach { seenNotificationIds.add(it.id) }
                    }

                    // 2. Realtime WebSocket + Polling Loop
                    launch { listenRealtimeNotifications(userId) }
                    launch { pollNotificationsLoop(userId) }
                    break
                }
                delay(2000)
            }
        }
    }

    private suspend fun pollNotificationsLoop(userId: String) {
        while (scope.isActive) {
            delay(4000)
            try {
                val res = notificationRepository.getNotifications()
                if (res is Result.Success) {
                    res.data.forEach { notif ->
                        if (!notif.isRead && seenNotificationIds.add(notif.id)) {
                            // Show system notification
                            notificationHandler.showNotification(
                                title = notif.title,
                                body = notif.body,
                                type = notif.type,
                                extraData = mapOf("actor_id" to (notif.actorId ?: ""), "notification_id" to notif.id)
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore transient errors
            }
        }
    }

    private suspend fun listenRealtimeNotifications(userId: String) {
        while (scope.isActive) {
            try {
                val token = tokenStorage.getAccessToken() ?: clientProvider.anonKey
                val wsUrl = clientProvider.baseUrl
                    .replace("https://", "wss://")
                    .replace("http://", "ws://") +
                        "/realtime/v1/websocket?apikey=${clientProvider.anonKey}&vsn=1.0.0"

                clientProvider.httpClient.webSocket(urlString = wsUrl) {
                    val joinNotifications = """{"topic":"realtime:public:notifications:user_id=eq.$userId","event":"phx_join","payload":{"access_token":"$token"},"ref":"1"}"""
                    send(Frame.Text(joinNotifications))

                    while (isActive) {
                        val incoming = incoming.receive()
                        if (incoming is Frame.Text) {
                            val frameText = incoming.readText()
                            handleNotificationFrame(frameText)
                        }
                    }
                }
            } catch (_: Exception) {
                delay(5000)
            }
        }
    }

    private fun handleNotificationFrame(frameText: String) {
        try {
            val jsonObject = json.parseToJsonElement(frameText).jsonObject
            val payload = jsonObject["payload"]?.jsonObject ?: return
            val record = payload["record"]?.jsonObject ?: return

            val id = record["id"]?.jsonPrimitive?.content ?: return
            val title = record["title"]?.jsonPrimitive?.content ?: "Aura"
            val body = record["body"]?.jsonPrimitive?.content ?: ""
            val typeStr = record["type"]?.jsonPrimitive?.content ?: "SYSTEM"
            val actorId = record["actor_id"]?.jsonPrimitive?.content ?: ""

            val type = try { NotificationType.valueOf(typeStr) } catch (_: Exception) { NotificationType.SYSTEM }

            if (seenNotificationIds.add(id)) {
                notificationHandler.showNotification(
                    title = title,
                    body = body,
                    type = type,
                    extraData = mapOf("actor_id" to actorId, "notification_id" to id)
                )
            }
        } catch (_: Exception) {
            // Ignore parsing errors
        }
    }
}
