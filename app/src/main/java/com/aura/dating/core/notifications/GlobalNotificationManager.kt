package com.aura.dating.core.notifications

import android.util.Log
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.data.chat.local.ChatLocalDataSource
import com.aura.dating.domain.chat.model.Message
import com.aura.dating.domain.chat.model.MessageStatus
import com.aura.dating.domain.chat.model.MessageType
import com.aura.dating.domain.chat.repository.ChatRepository
import com.aura.dating.domain.matching.repository.MatchingRepository
import com.aura.dating.domain.notifications.repository.NotificationRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Serializable
data class SyncMessageSupabaseDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    val content: String,
    @SerialName("message_type") val messageType: String = "TEXT",
    @SerialName("media_url") val mediaUrl: String? = null,
    val status: String = "SENT",
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class SenderProfileShortDto(
    val id: String,
    @SerialName("display_name") val displayName: String
)

@Singleton
class GlobalNotificationManager @Inject constructor(
    private val clientProvider: SupabaseClientProvider,
    private val tokenStorage: TokenStorage,
    private val notificationHandler: NotificationHandler,
    private val notificationRepository: NotificationRepository,
    private val chatLocalDataSource: ChatLocalDataSource,
    private val chatRepositoryProvider: Provider<ChatRepository>,
    private val matchingRepositoryProvider: Provider<MatchingRepository>
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val seenMessageIds = ConcurrentHashMap.newKeySet<String>()
    private val seenNotificationIds = ConcurrentHashMap.newKeySet<String>()
    private val senderNameCache = ConcurrentHashMap<String, String>()
    private val json = Json { ignoreUnknownKeys = true }
    private var listenerJob: Job? = null

    fun startListening() {
        if (listenerJob?.isActive == true) return

        listenerJob = scope.launch {
            tokenStorage.userIdFlow.collectLatest { userId ->
                if (!userId.isNullOrBlank()) {
                    Log.d("AuraNotif", "GlobalNotificationManager listening for userId: $userId")
                    // 1. Initial sync to prefill existing messages and notifications
                    syncLatestHistory(userId, isInitialSync = true)

                    // 2. Realtime WebSocket listener with auto-reconnect & keep-alive
                    listenRealtimeGlobal(userId)
                }
            }
        }
    }

    private suspend fun syncLatestHistory(userId: String, isInitialSync: Boolean = false) {
        try {
            val notifs = notificationRepository.getNotifications()
            if (notifs is Result.Success) {
                notifs.data.forEach { notif ->
                    if (isInitialSync) {
                        seenNotificationIds.add(notif.id)
                    } else if (notif.type != NotificationType.NEW_MESSAGE && !notif.isRead && seenNotificationIds.add(notif.id)) {
                        if (notif.type == NotificationType.NEW_MATCH) {
                            matchingRepositoryProvider.get().getMatches(forceRefresh = true)
                            chatRepositoryProvider.get().getConversations(forceRefresh = true)
                        }
                        notificationHandler.showNotification(
                            title = notif.title,
                            body = notif.body,
                            type = notif.type,
                            extraData = mapOf(
                                "actor_id" to (notif.actorId ?: ""),
                                "notification_id" to notif.id
                            )
                        )
                    }
                }
            }

            val messagesRes = fetchLatestMessagesRemote()
            if (messagesRes is Result.Success) {
                if (isInitialSync) {
                    messagesRes.data.forEach { msg -> seenMessageIds.add(msg.id) }
                } else {
                    messagesRes.data.forEach { msg ->
                        if (msg.senderId != userId && seenMessageIds.add(msg.id)) {
                            val senderName = getSenderName(msg.senderId)
                            notificationHandler.showNotification(
                                title = senderName,
                                body = if (msg.messageType == MessageType.IMAGE) "📷 Sent a photo" else msg.content,
                                type = NotificationType.NEW_MESSAGE,
                                extraData = mapOf(
                                    "message_id" to msg.id,
                                    "conversation_id" to msg.conversationId,
                                    "sender_id" to msg.senderId,
                                    "sender_name" to senderName
                                )
                            )
                        }
                    }
                }
                chatLocalDataSource.saveMessages(messagesRes.data)
            }

            chatRepositoryProvider.get().getConversations(forceRefresh = true)
            matchingRepositoryProvider.get().getMatches(forceRefresh = true)
        } catch (e: Exception) {
            Log.e("AuraNotif", "Sync history error", e)
        }
    }

    private suspend fun fetchLatestMessagesRemote(): Result<List<Message>> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/messages?deleted_at=is.null&order=created_at.desc&limit=30")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<SyncMessageSupabaseDto>>()
                list.map { dto ->
                    Message(
                        id = dto.id,
                        conversationId = dto.conversationId,
                        senderId = dto.senderId,
                        content = dto.content,
                        messageType = MessageType.entries.firstOrNull { it.name == dto.messageType } ?: MessageType.TEXT,
                        mediaUrl = dto.mediaUrl,
                        status = MessageStatus.entries.firstOrNull { it.name == dto.status } ?: MessageStatus.SENT,
                        createdAtMillis = DateTimeUtils.parseIsoDate(dto.createdAt)
                    )
                }
            }
        )
    }

    private suspend fun getSenderName(senderId: String): String {
        senderNameCache[senderId]?.let { return it }

        val res = clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/profiles?id=eq.$senderId&select=id,display_name&limit=1")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<SenderProfileShortDto>>()
                list.firstOrNull()?.displayName
            }
        )

        val name = if (res is Result.Success && !res.data.isNullOrBlank()) res.data else "New Message"
        senderNameCache[senderId] = name
        return name
    }

    private suspend fun listenRealtimeGlobal(userId: String) {
        var isFirstConnection = true
        while (scope.isActive) {
            try {
                // If reconnecting after a drop, catch up on any missed messages/notifications
                if (!isFirstConnection) {
                    syncLatestHistory(userId, isInitialSync = false)
                }
                isFirstConnection = false

                val token = tokenStorage.getAccessToken() ?: clientProvider.anonKey
                val wsUrl = clientProvider.baseUrl
                    .replace("https://", "wss://")
                    .replace("http://", "ws://") +
                        "/realtime/v1/websocket?apikey=${clientProvider.anonKey}&vsn=1.0.0"

                clientProvider.httpClient.webSocket(urlString = wsUrl) {
                    // Listen for real-time notifications
                    val joinNotifications = """{"topic":"realtime:public:notifications:user_id=eq.$userId","event":"phx_join","payload":{"access_token":"$token"},"ref":"1"}"""
                    send(Frame.Text(joinNotifications))

                    // Listen for real-time messages
                    val joinMessages = """{"topic":"realtime:public:messages","event":"phx_join","payload":{"access_token":"$token"},"ref":"2"}"""
                    send(Frame.Text(joinMessages))

                    // Phoenix channel 30-second heartbeat to keep websocket healthy
                    val heartbeatJob = launch {
                        while (isActive) {
                            delay(30000)
                            send(Frame.Text("""{"topic":"phoenix","event":"heartbeat","payload":{},"ref":""}"""))
                        }
                    }

                    try {
                        while (isActive) {
                            val incoming = incoming.receive()
                            if (incoming is Frame.Text) {
                                val frameText = incoming.readText()
                                handleRealtimeIncoming(userId, frameText)
                            }
                        }
                    } finally {
                        heartbeatJob.cancel()
                    }
                }
            } catch (e: Exception) {
                Log.w("AuraNotif", "WebSocket connection lost, reconnecting in 5s", e)
                delay(5000)
            }
        }
    }

    private fun handleRealtimeIncoming(currentUserId: String, frameText: String) {
        try {
            val jsonObject = json.parseToJsonElement(frameText).jsonObject
            val payload = jsonObject["payload"]?.jsonObject ?: return
            val record = payload["record"]?.jsonObject ?: return

            val id = record["id"]?.jsonPrimitive?.content ?: return

            // If incoming is a message
            val conversationId = record["conversation_id"]?.jsonPrimitive?.content
            val senderId = record["sender_id"]?.jsonPrimitive?.content
            if (conversationId != null && senderId != null) {
                if (senderId != currentUserId && seenMessageIds.add(id)) {
                    val content = record["content"]?.jsonPrimitive?.content ?: ""
                    val msgTypeStr = record["message_type"]?.jsonPrimitive?.content ?: "TEXT"
                    val mediaUrl = record["media_url"]?.jsonPrimitive?.content
                    val createdAtStr = record["created_at"]?.jsonPrimitive?.content ?: ""

                    val msg = Message(
                        id = id,
                        conversationId = conversationId,
                        senderId = senderId,
                        content = content,
                        messageType = MessageType.entries.firstOrNull { it.name == msgTypeStr } ?: MessageType.TEXT,
                        mediaUrl = mediaUrl,
                        status = MessageStatus.DELIVERED,
                        createdAtMillis = DateTimeUtils.parseIsoDate(createdAtStr)
                    )

                    scope.launch {
                        chatLocalDataSource.saveMessage(msg)
                        chatLocalDataSource.incrementUnreadCount(
                            conversationId = conversationId,
                            text = if (msg.messageType == MessageType.IMAGE) "📷 Sent a photo" else content,
                            time = msg.createdAtMillis,
                            senderId = senderId
                        )
                        chatRepositoryProvider.get().getConversations(forceRefresh = true)
                        matchingRepositoryProvider.get().getMatches(forceRefresh = true)

                        val senderName = getSenderName(senderId)
                        notificationHandler.showNotification(
                            title = senderName,
                            body = if (msg.messageType == MessageType.IMAGE) "📷 Sent a photo" else content,
                            type = NotificationType.NEW_MESSAGE,
                            extraData = mapOf(
                                "message_id" to id,
                                "conversation_id" to conversationId,
                                "sender_id" to senderId,
                                "sender_name" to senderName
                            )
                        )
                    }
                }
                return
            }

            // If incoming is a general notification
            val title = record["title"]?.jsonPrimitive?.content ?: "Aura"
            val body = record["body"]?.jsonPrimitive?.content ?: ""
            val typeStr = record["type"]?.jsonPrimitive?.content ?: "SYSTEM"
            val actorId = record["actor_id"]?.jsonPrimitive?.content ?: ""
            val type = NotificationType.entries.firstOrNull { it.name == typeStr } ?: NotificationType.SYSTEM

            if (type != NotificationType.NEW_MESSAGE && seenNotificationIds.add(id)) {
                scope.launch {
                    if (type == NotificationType.NEW_MATCH) {
                        matchingRepositoryProvider.get().getMatches(forceRefresh = true)
                        chatRepositoryProvider.get().getConversations(forceRefresh = true)
                    }
                    notificationHandler.showNotification(
                        title = title,
                        body = body,
                        type = type,
                        extraData = mapOf("actor_id" to actorId, "notification_id" to id)
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("AuraNotif", "Failed to parse realtime payload", e)
        }
    }
}
