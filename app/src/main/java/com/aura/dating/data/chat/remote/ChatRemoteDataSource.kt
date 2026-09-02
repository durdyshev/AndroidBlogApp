package com.aura.dating.data.chat.remote

import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.domain.chat.model.Conversation
import com.aura.dating.domain.chat.model.Message
import com.aura.dating.domain.chat.model.MessageStatus
import com.aura.dating.domain.chat.model.MessageType
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ConversationSupabaseDto(
    val id: String,
    @SerialName("match_id") val matchId: String? = null,
    @SerialName("last_message_text") val lastMessageText: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("last_message_sender_id") val lastMessageSenderId: String? = null,
    val participants: List<ParticipantProfileJoinDto> = emptyList()
)

@Serializable
data class ParticipantProfileJoinDto(
    @SerialName("user_id") val userId: String,
    @SerialName("last_read_at") val lastReadAt: String? = null,
    val profile: ProfileShortDto? = null
)

@Serializable
data class ProfileShortDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("last_seen_at") val lastSeenAt: String? = null,
    val photos: List<PhotoShortDto> = emptyList()
)

@Serializable
data class PhotoShortDto(
    @SerialName("photo_url") val photoUrl: String,
    @SerialName("is_primary") val isPrimary: Boolean = false
)

@Serializable
data class ConversationIdLookupDto(
    val id: String
)

@Serializable
data class ConversationParticipantLookupDto(
    @SerialName("conversation_id") val conversationId: String
)

@Serializable
data class MessageSupabaseDto(
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
data class UpdateLastReadRequest(
    @SerialName("last_read_at") val lastReadAt: String = "now()"
)

@Serializable
data class SoftDeleteMessageRequest(
    @SerialName("deleted_at") val deletedAt: String = "now()"
)

interface ChatRemoteDataSource {
    suspend fun resolveConversationId(rawId: String): Result<String>
    suspend fun getConversations(currentUserId: String): Result<List<Conversation>>
    suspend fun getMessages(
        conversationId: String,
        limit: Int = 30,
        beforeTimestampIso: String? = null
    ): Result<List<Message>>
    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT,
        mediaUrl: String? = null
    ): Result<Message>
    suspend fun uploadChatImage(conversationId: String, imageBytes: ByteArray): Result<String>
    suspend fun markAsRead(conversationId: String, userId: String): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
}

@Singleton
class SupabaseChatRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : ChatRemoteDataSource {

    override suspend fun resolveConversationId(rawId: String): Result<String> {
        if (rawId.isBlank()) return Result.Success(rawId)

        // 1. Direct query: check if rawId is already a valid conversation ID
        val directLookup = clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/conversations?id=eq.$rawId&select=id&limit=1")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<ConversationIdLookupDto>>()
                list.firstOrNull()?.id
            }
        )

        if (directLookup is Result.Success && !directLookup.data.isNullOrBlank()) {
            return Result.Success(directLookup.data)
        }

        // 2. Direct query: check if rawId is a match ID
        val matchLookup = clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/conversations?match_id=eq.$rawId&select=id&limit=1")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<ConversationIdLookupDto>>()
                list.firstOrNull()?.id
            }
        )

        if (matchLookup is Result.Success && !matchLookup.data.isNullOrBlank()) {
            return Result.Success(matchLookup.data)
        }

        // 3. RPC call: rawId is a target partner user ID, get or create conversation
        val rpcLookup = clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/rpc/get_or_create_conversation")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(mapOf("p_target_user_id" to rawId))
                }
            },
            parser = { response ->
                response.bodyAsText().trim().removeSurrounding("\"")
            }
        )

        if (rpcLookup is Result.Success && rpcLookup.data.isNotBlank() && !rpcLookup.data.contains("error", ignoreCase = true)) {
            return Result.Success(rpcLookup.data)
        }

        return Result.Success(rawId)
    }

    private suspend fun resolveActualConversationId(rawId: String): String {
        val result = resolveConversationId(rawId)
        return if (result is Result.Success) result.data else rawId
    }

    override suspend fun getConversations(currentUserId: String): Result<List<Conversation>> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/conversations?select=id,match_id,last_message_text,last_message_at,last_message_sender_id,participants:conversation_participants(user_id,last_read_at,profile:profiles(id,display_name,is_online,last_seen_at,photos:profile_photos(photo_url,is_primary)))&order=last_message_at.desc")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<ConversationSupabaseDto>>()
                list.mapNotNull { convDto ->
                    val otherParticipant = convDto.participants.firstOrNull { it.userId != currentUserId }
                    val participantId = otherParticipant?.userId ?: ""
                    val profile = otherParticipant?.profile
                    val participantName = profile?.displayName ?: "User"
                    val primaryPhoto = profile?.photos?.firstOrNull { it.isPrimary }?.photoUrl
                        ?: profile?.photos?.firstOrNull()?.photoUrl

                    val time = convDto.lastMessageAt?.let { DateTimeUtils.parseIsoDate(it) }
                        ?: System.currentTimeMillis()

                    val lastSeenTime = profile?.lastSeenAt?.let { DateTimeUtils.parseIsoDate(it) }

                    Conversation(
                        id = convDto.id,
                        matchId = convDto.matchId,
                        participantUserId = participantId,
                        participantName = participantName,
                        participantPhotoUrl = primaryPhoto,
                        isParticipantOnline = profile?.isOnline ?: false,
                        participantLastSeenAtMillis = lastSeenTime,
                        lastMessageText = convDto.lastMessageText,
                        lastMessageAtMillis = time,
                        lastMessageSenderId = convDto.lastMessageSenderId,
                        unreadCount = 0
                    )
                }
            }
        )
    }

    override suspend fun getMessages(
        conversationId: String,
        limit: Int,
        beforeTimestampIso: String?
    ): Result<List<Message>> {
        val targetConvId = resolveActualConversationId(conversationId)
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    val urlBuilder = StringBuilder("${clientProvider.baseUrl}/rest/v1/messages?conversation_id=eq.$targetConvId&deleted_at=is.null")
                    if (!beforeTimestampIso.isNullOrBlank()) {
                        urlBuilder.append("&created_at=lt.$beforeTimestampIso")
                    }
                    urlBuilder.append("&order=created_at.desc&limit=$limit")
                    url(urlBuilder.toString())
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<MessageSupabaseDto>>()
                list.map { dto ->
                    Message(
                        id = dto.id,
                        conversationId = dto.conversationId,
                        senderId = dto.senderId,
                        content = dto.content,
                        messageType = try { MessageType.valueOf(dto.messageType) } catch (e: Exception) { MessageType.TEXT },
                        mediaUrl = dto.mediaUrl,
                        status = try { MessageStatus.valueOf(dto.status) } catch (e: Exception) { MessageStatus.SENT },
                        createdAtMillis = DateTimeUtils.parseIsoDate(dto.createdAt)
                    )
                }.reversed()
            }
        )
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        content: String,
        messageType: MessageType,
        mediaUrl: String?
    ): Result<Message> {
        val targetConvId = resolveActualConversationId(conversationId)
        val messageId = UUID.randomUUID().toString()
        val bodyMap = mapOf(
            "id" to messageId,
            "conversation_id" to targetConvId,
            "sender_id" to senderId,
            "content" to content,
            "message_type" to messageType.name,
            "media_url" to mediaUrl,
            "status" to "SENT"
        )

        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/messages")
                    contentType(ContentType.Application.Json)
                    header("Prefer", "return=representation")
                    headers(this)
                    setBody(bodyMap)
                }
            },
            parser = { response ->
                val list = response.body<List<MessageSupabaseDto>>()
                val dto = requireNotNull(list.firstOrNull())

                // Update conversation's last message
                clientProvider.safeApiCall(
                    block = { client, headers ->
                        client.patch {
                            url("${clientProvider.baseUrl}/rest/v1/conversations?id=eq.$targetConvId")
                            contentType(ContentType.Application.Json)
                            headers(this)
                            setBody(
                                mapOf(
                                    "last_message_text" to if (messageType == MessageType.IMAGE) "📷 Photo" else content,
                                    "last_message_at" to dto.createdAt,
                                    "last_message_sender_id" to senderId
                                )
                            )
                        }
                    },
                    parser = { }
                )

                Message(
                    id = dto.id,
                    conversationId = dto.conversationId,
                    senderId = dto.senderId,
                    content = dto.content,
                    messageType = try { MessageType.valueOf(dto.messageType) } catch (e: Exception) { MessageType.TEXT },
                    mediaUrl = dto.mediaUrl,
                    status = MessageStatus.SENT,
                    createdAtMillis = DateTimeUtils.parseIsoDate(dto.createdAt)
                )
            }
        )
    }

    override suspend fun uploadChatImage(
        conversationId: String,
        imageBytes: ByteArray
    ): Result<String> {
        val imageId = UUID.randomUUID().toString()
        val path = "$conversationId/$imageId.webp"

        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/storage/v1/object/chat-media/$path")
                    contentType(ContentType("image", "webp"))
                    headers(this)
                    setBody(imageBytes)
                }
            },
            parser = {
                "${clientProvider.baseUrl}/storage/v1/object/public/chat-media/$path"
            }
        )
    }

    override suspend fun markAsRead(conversationId: String, userId: String): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.patch {
                    url("${clientProvider.baseUrl}/rest/v1/conversation_participants?conversation_id=eq.$conversationId&user_id=eq.$userId")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(UpdateLastReadRequest(lastReadAt = "now()"))
                }
            },
            parser = { }
        )
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.patch {
                    url("${clientProvider.baseUrl}/rest/v1/messages?id=eq.$messageId")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(SoftDeleteMessageRequest(deletedAt = "now()"))
                }
            },
            parser = { }
        )
    }
}
