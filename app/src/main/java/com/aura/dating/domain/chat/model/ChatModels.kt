package com.aura.dating.domain.chat.model

import kotlinx.serialization.Serializable

enum class MessageType {
    TEXT, IMAGE, SYSTEM
}

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}

@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Serializable
data class Conversation(
    val id: String,
    val matchId: String? = null,
    val participantUserId: String,
    val participantName: String,
    val participantPhotoUrl: String?,
    val isParticipantOnline: Boolean = false,
    val participantLastSeenAtMillis: Long? = null,
    val lastMessageText: String? = null,
    val lastMessageAtMillis: Long = System.currentTimeMillis(),
    val lastMessageSenderId: String? = null,
    val unreadCount: Int = 0
)

data class TypingIndicator(
    val conversationId: String,
    val userId: String,
    val isTyping: Boolean
)
