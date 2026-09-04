package com.aura.dating.data.chat.local

import com.aura.dating.core.database.dao.ConversationDao
import com.aura.dating.core.database.dao.MessageDao
import com.aura.dating.core.database.entity.ConversationEntity
import com.aura.dating.core.database.entity.MessageEntity
import com.aura.dating.domain.chat.model.Conversation
import com.aura.dating.domain.chat.model.Message
import com.aura.dating.domain.chat.model.MessageStatus
import com.aura.dating.domain.chat.model.MessageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ChatLocalDataSource {
    val conversationsFlow: Flow<List<Conversation>>
    val totalUnreadCountFlow: Flow<Int>
    fun getMessagesFlow(conversationId: String): Flow<List<Message>>
    suspend fun saveConversations(conversations: List<Conversation>)
    suspend fun saveConversation(conversation: Conversation)
    suspend fun saveMessages(messages: List<Message>)
    suspend fun saveMessage(message: Message)
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)
    suspend fun deleteMessage(messageId: String)
    suspend fun updateLastMessage(conversationId: String, text: String, time: Long, senderId: String)
    suspend fun markConversationAsRead(conversationId: String)
    suspend fun incrementUnreadCount(conversationId: String, text: String, time: Long, senderId: String)
}

@Singleton
class RoomChatLocalDataSource @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : ChatLocalDataSource {

    override val totalUnreadCountFlow: Flow<Int> = conversationDao.getTotalUnreadCountFlow()

    override val conversationsFlow: Flow<List<Conversation>> = conversationDao.getConversationsFlow().map { list ->
        list.map { entity ->
            Conversation(
                id = entity.id,
                matchId = entity.matchId,
                participantUserId = entity.participantUserId,
                participantName = entity.participantName,
                participantPhotoUrl = entity.participantPhotoUrl,
                isParticipantOnline = entity.isParticipantOnline,
                lastMessageText = entity.lastMessageText,
                lastMessageAtMillis = entity.lastMessageAtMillis,
                lastMessageSenderId = entity.lastMessageSenderId,
                unreadCount = entity.unreadCount
            )
        }
    }

    override fun getMessagesFlow(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesFlow(conversationId).map { list ->
            list.map { entity ->
                Message(
                    id = entity.id,
                    conversationId = entity.conversationId,
                    senderId = entity.senderId,
                    content = entity.content,
                    messageType = try { MessageType.valueOf(entity.messageType) } catch (e: Exception) { MessageType.TEXT },
                    mediaUrl = entity.mediaUrl,
                    status = try { MessageStatus.valueOf(entity.status) } catch (e: Exception) { MessageStatus.SENT },
                    createdAtMillis = entity.createdAtMillis
                )
            }
        }
    }

    override suspend fun saveConversations(conversations: List<Conversation>) {
        val entities = conversations.map { c ->
            ConversationEntity(
                id = c.id,
                matchId = c.matchId,
                participantUserId = c.participantUserId,
                participantName = c.participantName,
                participantPhotoUrl = c.participantPhotoUrl,
                isParticipantOnline = c.isParticipantOnline,
                lastMessageText = c.lastMessageText,
                lastMessageAtMillis = c.lastMessageAtMillis,
                lastMessageSenderId = c.lastMessageSenderId,
                unreadCount = c.unreadCount
            )
        }
        conversationDao.insertConversations(entities)
    }

    override suspend fun saveConversation(conversation: Conversation) {
        conversationDao.insertConversation(
            ConversationEntity(
                id = conversation.id,
                matchId = conversation.matchId,
                participantUserId = conversation.participantUserId,
                participantName = conversation.participantName,
                participantPhotoUrl = conversation.participantPhotoUrl,
                isParticipantOnline = conversation.isParticipantOnline,
                lastMessageText = conversation.lastMessageText,
                lastMessageAtMillis = conversation.lastMessageAtMillis,
                lastMessageSenderId = conversation.lastMessageSenderId,
                unreadCount = conversation.unreadCount
            )
        )
    }

    override suspend fun saveMessages(messages: List<Message>) {
        val entities = messages.map { m ->
            MessageEntity(
                id = m.id,
                conversationId = m.conversationId,
                senderId = m.senderId,
                content = m.content,
                messageType = m.messageType.name,
                mediaUrl = m.mediaUrl,
                status = m.status.name,
                createdAtMillis = m.createdAtMillis
            )
        }
        messageDao.insertMessages(entities)
    }

    override suspend fun saveMessage(message: Message) {
        messageDao.insertMessage(
            MessageEntity(
                id = message.id,
                conversationId = message.conversationId,
                senderId = message.senderId,
                content = message.content,
                messageType = message.messageType.name,
                mediaUrl = message.mediaUrl,
                status = message.status.name,
                createdAtMillis = message.createdAtMillis
            )
        )
    }

    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        messageDao.updateMessageStatus(messageId, status.name)
    }

    override suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessage(messageId)
    }

    override suspend fun updateLastMessage(
        conversationId: String,
        text: String,
        time: Long,
        senderId: String
    ) {
        conversationDao.updateLastMessage(conversationId, text, time, senderId)
    }

    override suspend fun markConversationAsRead(conversationId: String) {
        conversationDao.markAsRead(conversationId)
    }

    override suspend fun incrementUnreadCount(
        conversationId: String,
        text: String,
        time: Long,
        senderId: String
    ) {
        conversationDao.incrementUnreadCount(conversationId, text, time, senderId)
    }
}
