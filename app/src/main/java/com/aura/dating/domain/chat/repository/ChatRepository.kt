package com.aura.dating.domain.chat.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.chat.model.Conversation
import com.aura.dating.domain.chat.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    val conversationsFlow: Flow<List<Conversation>>
    val totalUnreadCountFlow: Flow<Int>
    suspend fun resolveConversationId(rawId: String): Result<String>
    fun getMessagesFlow(conversationId: String): Flow<List<Message>>
    suspend fun getConversations(forceRefresh: Boolean = false): Result<List<Conversation>>
    suspend fun getMessages(
        conversationId: String,
        limit: Int = 30,
        beforeTimestampIso: String? = null,
        forceRefresh: Boolean = false
    ): Result<List<Message>>
    suspend fun sendMessage(conversationId: String, content: String): Result<Message>
    suspend fun sendImageMessage(conversationId: String, imageBytes: ByteArray): Result<Message>
    suspend fun markMessagesAsRead(conversationId: String): Result<Unit>
    fun observeTypingStatus(conversationId: String): Flow<Boolean>
    suspend fun sendTypingStatus(conversationId: String, isTyping: Boolean): Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
}
