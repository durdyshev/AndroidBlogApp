package com.aura.dating.data.chat.repository

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.data.chat.local.ChatLocalDataSource
import com.aura.dating.data.chat.realtime.ChatRealtimeManager
import com.aura.dating.data.chat.remote.ChatRemoteDataSource
import com.aura.dating.domain.chat.model.Conversation
import com.aura.dating.domain.chat.model.Message
import com.aura.dating.domain.chat.model.MessageStatus
import com.aura.dating.domain.chat.model.MessageType
import com.aura.dating.domain.chat.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: ChatRemoteDataSource,
    private val localDataSource: ChatLocalDataSource,
    private val realtimeManager: ChatRealtimeManager,
    private val tokenStorage: TokenStorage
) : ChatRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override val conversationsFlow: Flow<List<Conversation>> = localDataSource.conversationsFlow
    override val totalUnreadCountFlow: Flow<Int> = localDataSource.totalUnreadCountFlow

    override suspend fun resolveConversationId(rawId: String): Result<String> {
        return remoteDataSource.resolveConversationId(rawId)
    }

    override fun getMessagesFlow(conversationId: String): Flow<List<Message>> {
        // Connect and observe incoming realtime messages, saving them locally into Room
        realtimeManager.observeMessages(conversationId)
            .onEach { message ->
                localDataSource.saveMessage(message)
            }
            .launchIn(scope)

        return localDataSource.getMessagesFlow(conversationId)
    }

    override suspend fun getConversations(forceRefresh: Boolean): Result<List<Conversation>> {
        val currentUserId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        val remoteResult = remoteDataSource.getConversations(currentUserId)
        if (remoteResult is Result.Success) {
            localDataSource.saveConversations(remoteResult.data)
            return remoteResult
        }
        return remoteResult
    }

    override suspend fun getMessages(
        conversationId: String,
        limit: Int,
        beforeTimestampIso: String?,
        forceRefresh: Boolean
    ): Result<List<Message>> {
        val remoteResult = remoteDataSource.getMessages(conversationId, limit, beforeTimestampIso)
        if (remoteResult is Result.Success) {
            localDataSource.saveMessages(remoteResult.data)
            return remoteResult
        }
        return remoteResult
    }

    override suspend fun sendMessage(
        conversationId: String,
        content: String
    ): Result<Message> {
        val senderId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        val tempId = UUID.randomUUID().toString()
        val optimisticMessage = Message(
            id = tempId,
            conversationId = conversationId,
            senderId = senderId,
            content = content,
            messageType = MessageType.TEXT,
            status = MessageStatus.SENDING,
            createdAtMillis = System.currentTimeMillis()
        )
        // Optimistic UI save
        localDataSource.saveMessage(optimisticMessage)
        localDataSource.updateLastMessage(conversationId, content, optimisticMessage.createdAtMillis, senderId)

        val result = remoteDataSource.sendMessage(
            conversationId = conversationId,
            senderId = senderId,
            content = content,
            messageType = MessageType.TEXT
        )

        if (result is Result.Success) {
            localDataSource.deleteMessage(tempId)
            localDataSource.saveMessage(result.data)
            scope.launch {
                realtimeManager.emitMessage(conversationId, result.data)
            }
            return result
        } else {
            localDataSource.updateMessageStatus(tempId, MessageStatus.FAILED)
            return result
        }
    }

    override suspend fun sendImageMessage(
        conversationId: String,
        imageBytes: ByteArray
    ): Result<Message> {
        val senderId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        val uploadResult = remoteDataSource.uploadChatImage(conversationId, imageBytes)
        if (uploadResult is Result.Error) {
            return Result.Error(uploadResult.error)
        }

        val mediaUrl = (uploadResult as Result.Success).data
        val result = remoteDataSource.sendMessage(
            conversationId = conversationId,
            senderId = senderId,
            content = "",
            messageType = MessageType.IMAGE,
            mediaUrl = mediaUrl
        )

        if (result is Result.Success) {
            localDataSource.saveMessage(result.data)
            localDataSource.updateLastMessage(conversationId, "📷 Photo", result.data.createdAtMillis, senderId)
            scope.launch {
                realtimeManager.emitMessage(conversationId, result.data)
            }
            return result
        }
        return result
    }

    override suspend fun markMessagesAsRead(conversationId: String): Result<Unit> {
        localDataSource.markConversationAsRead(conversationId)
        val userId = tokenStorage.getUserId() ?: return Result.Success(Unit)
        return remoteDataSource.markAsRead(conversationId, userId)
    }

    override fun observeTypingStatus(conversationId: String): Flow<Boolean> {
        return realtimeManager.observeTyping(conversationId)
    }

    override suspend fun sendTypingStatus(
        conversationId: String,
        isTyping: Boolean
    ): Result<Unit> {
        realtimeManager.emitTyping(conversationId, isTyping)
        return Result.Success(Unit)
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
        localDataSource.deleteMessage(messageId)
        return remoteDataSource.deleteMessage(messageId)
    }
}
