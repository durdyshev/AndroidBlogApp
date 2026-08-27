package com.aura.dating.data.chat.realtime

import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.domain.chat.model.Message
import com.aura.dating.domain.chat.model.MessageStatus
import com.aura.dating.domain.chat.model.MessageType
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRealtimeManager @Inject constructor(
    private val clientProvider: SupabaseClientProvider,
    private val tokenStorage: TokenStorage
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val json = Json { ignoreUnknownKeys = true }

    private val messageFlows = ConcurrentHashMap<String, MutableSharedFlow<Message>>()
    private val typingFlows = ConcurrentHashMap<String, MutableSharedFlow<Boolean>>()
    private val activeSessions = ConcurrentHashMap<String, WebSocketSession>()
    private val activeJobs = ConcurrentHashMap<String, Job>()

    fun observeMessages(conversationId: String): Flow<Message> {
        val flow = messageFlows.getOrPut(conversationId) {
            MutableSharedFlow(replay = 0, extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        }
        connectRealtimeChannel(conversationId)
        return flow.asSharedFlow()
    }

    fun observeTyping(conversationId: String): Flow<Boolean> {
        val flow = typingFlows.getOrPut(conversationId) {
            MutableSharedFlow(replay = 1, extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        }
        connectRealtimeChannel(conversationId)
        return flow.asSharedFlow()
    }

    suspend fun emitTyping(conversationId: String, isTyping: Boolean) {
        val myId = tokenStorage.getUserId() ?: return
        val session = activeSessions[conversationId] ?: return
        val broadcastMsg = """{"topic":"realtime:typing:$conversationId","event":"broadcast","payload":{"type":"broadcast","event":"typing","payload":{"user_id":"$myId","is_typing":$isTyping}},"ref":"${System.currentTimeMillis()}"}"""
        try {
            session.send(Frame.Text(broadcastMsg))
        } catch (_: Exception) {
            // Ignore socket send errors
        }
    }

    private fun connectRealtimeChannel(conversationId: String) {
        if (activeJobs[conversationId]?.isActive == true) return

        val job = scope.launch {
            try {
                val wsUrl = clientProvider.baseUrl
                    .replace("https://", "wss://")
                    .replace("http://", "ws://") +
                        "/realtime/v1/websocket?apikey=${clientProvider.anonKey}&vsn=1.0.0"

                clientProvider.httpClient.webSocket(urlString = wsUrl) {
                    activeSessions[conversationId] = this

                    // 1. Subscribe to postgres_changes for messages in this conversation
                    val joinMessages = """{"topic":"realtime:public:messages:conversation_id=eq.$conversationId","event":"phx_join","payload":{},"ref":"1"}"""
                    send(Frame.Text(joinMessages))

                    // 2. Subscribe to typing broadcast topic
                    val joinTyping = """{"topic":"realtime:typing:$conversationId","event":"phx_join","payload":{"config":{"broadcast":{"self":false}}},"ref":"2"}"""
                    send(Frame.Text(joinTyping))

                    while (isActive) {
                        val incoming = incoming.receive()
                        if (incoming is Frame.Text) {
                            val text = incoming.readText()
                            handleRealtimeEvent(conversationId, text)
                        }
                    }
                }
            } catch (_: Exception) {
                // Realtime will reconnect on next attempt
            } finally {
                activeSessions.remove(conversationId)
            }
        }
        activeJobs[conversationId] = job
    }

    private fun handleRealtimeEvent(conversationId: String, frameText: String) {
        try {
            val jsonObject = json.parseToJsonElement(frameText).jsonObject
            val event = jsonObject["event"]?.jsonPrimitive?.content
            val payload = jsonObject["payload"]?.jsonObject ?: return

            // Handle typing broadcast event
            if (event == "broadcast") {
                val broadcastEvent = payload["event"]?.jsonPrimitive?.content
                if (broadcastEvent == "typing") {
                    val innerPayload = payload["payload"]?.jsonObject ?: return
                    val senderId = innerPayload["user_id"]?.jsonPrimitive?.content
                    val isTyping = innerPayload["is_typing"]?.jsonPrimitive?.booleanOrNull ?: false

                    scope.launch {
                        val myId = tokenStorage.getUserId()
                        // Only propagate typing status if it originated from partner (not self)
                        if (senderId != null && senderId != myId) {
                            typingFlows[conversationId]?.emit(isTyping)
                        }
                    }
                }
                return
            }

            // Handle new postgres record message
            val record = payload["record"]?.jsonObject ?: return
            val id = record["id"]?.jsonPrimitive?.content ?: return
            val convId = record["conversation_id"]?.jsonPrimitive?.content ?: conversationId
            val senderId = record["sender_id"]?.jsonPrimitive?.content ?: return
            val content = record["content"]?.jsonPrimitive?.content ?: ""
            val messageTypeStr = record["message_type"]?.jsonPrimitive?.content ?: "TEXT"
            val mediaUrl = record["media_url"]?.jsonPrimitive?.content
            val createdAt = record["created_at"]?.jsonPrimitive?.content

            val msg = Message(
                id = id,
                conversationId = convId,
                senderId = senderId,
                content = content,
                messageType = try { MessageType.valueOf(messageTypeStr) } catch (_: Exception) { MessageType.TEXT },
                mediaUrl = mediaUrl,
                status = MessageStatus.DELIVERED,
                createdAtMillis = createdAt?.let { DateTimeUtils.parseIsoDate(it) } ?: System.currentTimeMillis()
            )

            scope.launch {
                messageFlows[conversationId]?.emit(msg)
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }
    }
}
