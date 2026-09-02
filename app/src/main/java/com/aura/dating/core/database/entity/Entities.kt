package com.aura.dating.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val birthDateMillis: Long,
    val gender: String,
    val bio: String?,
    val photos: List<String>,
    val interests: List<String>,
    val isOnline: Boolean,
    val lastSeenAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(tableName = "discovery_candidates")
data class DiscoveryCandidateEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val birthDateMillis: Long,
    val gender: String,
    val bio: String?,
    val photos: List<String>,
    val interests: List<String>,
    val distanceKm: Double?,
    val isOnline: Boolean,
    val lastSeenAtMillis: Long,
    val cachedAtMillis: Long
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val matchedUserId: String,
    val matchedUserName: String,
    val matchedUserAge: Int,
    val matchedUserPhotoUrl: String?,
    val matchedUserDistanceKm: Double?,
    val matchedAtMillis: Long,
    val isActive: Boolean
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val matchId: String? = null,
    val participantUserId: String,
    val participantName: String,
    val participantPhotoUrl: String?,
    val isParticipantOnline: Boolean,
    val lastMessageText: String?,
    val lastMessageAtMillis: Long,
    val lastMessageSenderId: String?,
    val unreadCount: Int
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val messageType: String,
    val mediaUrl: String?,
    val status: String,
    val createdAtMillis: Long
)

@Entity(tableName = "blocked_users")
data class BlockedUserEntity(
    @PrimaryKey val id: String,
    val blockedUserId: String,
    val displayName: String,
    val photoUrl: String?,
    val blockedAtMillis: Long
)
