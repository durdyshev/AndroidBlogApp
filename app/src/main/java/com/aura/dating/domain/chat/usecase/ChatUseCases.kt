package com.aura.dating.domain.chat.usecase

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.chat.model.Conversation
import com.aura.dating.domain.chat.model.Message
import com.aura.dating.domain.chat.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    val conversationsFlow: Flow<List<Conversation>> = chatRepository.conversationsFlow
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<Conversation>> {
        return chatRepository.getConversations(forceRefresh)
    }
}

class GetMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(conversationId: String): Flow<List<Message>> {
        return chatRepository.getMessagesFlow(conversationId)
    }

    suspend fun fetchMessages(
        conversationId: String,
        limit: Int = 30,
        beforeTimestampIso: String? = null,
        forceRefresh: Boolean = false
    ): Result<List<Message>> {
        return chatRepository.getMessages(
            conversationId = conversationId,
            limit = limit,
            beforeTimestampIso = beforeTimestampIso,
            forceRefresh = forceRefresh
        )
    }
}

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String, content: String): Result<Message> {
        val trimmed = content.trim()
        if (trimmed.isBlank()) {
            return Result.Error(AppError.ValidationError("Message cannot be empty"))
        }
        return chatRepository.sendMessage(conversationId, trimmed)
    }
}

class SendImageMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String, imageBytes: ByteArray): Result<Message> {
        if (imageBytes.isEmpty()) {
            return Result.Error(AppError.ValidationError("Image data cannot be empty"))
        }
        return chatRepository.sendImageMessage(conversationId, imageBytes)
    }
}

class MarkMessagesAsReadUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String): Result<Unit> {
        return chatRepository.markMessagesAsRead(conversationId)
    }
}

class ObserveTypingStatusUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(conversationId: String): Flow<Boolean> {
        return chatRepository.observeTypingStatus(conversationId)
    }
}

class SendTypingStatusUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String, isTyping: Boolean): Result<Unit> {
        return chatRepository.sendTypingStatus(conversationId, isTyping)
    }
}

class DeleteMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(messageId: String): Result<Unit> {
        return chatRepository.deleteMessage(messageId)
    }
}

class ResolveConversationIdUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(rawId: String): Result<String> {
        return chatRepository.resolveConversationId(rawId)
    }
}

class GetTotalUnreadCountUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): Flow<Int> = chatRepository.totalUnreadCountFlow
}
