package com.aura.dating.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aura.dating.core.database.entity.BlockedUserEntity
import com.aura.dating.core.database.entity.ConversationEntity
import com.aura.dating.core.database.entity.DiscoveryCandidateEntity
import com.aura.dating.core.database.entity.MatchEntity
import com.aura.dating.core.database.entity.MessageEntity
import com.aura.dating.core.database.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE id = :userId LIMIT 1")
    fun getProfile(userId: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :userId LIMIT 1")
    suspend fun getProfileOnce(userId: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :userId")
    suspend fun deleteProfile(userId: String)
}

@Dao
interface DiscoveryDao {
    @Query("SELECT * FROM discovery_candidates WHERE id NOT IN (SELECT blockedUserId FROM blocked_users) ORDER BY isOnline DESC, distanceKm ASC")
    fun getCandidatesFlow(): Flow<List<DiscoveryCandidateEntity>>

    @Query("SELECT * FROM discovery_candidates WHERE id NOT IN (SELECT blockedUserId FROM blocked_users) ORDER BY isOnline DESC, distanceKm ASC")
    suspend fun getCandidatesOnce(): List<DiscoveryCandidateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidates(candidates: List<DiscoveryCandidateEntity>)

    @Query("DELETE FROM discovery_candidates WHERE id = :candidateId")
    suspend fun removeCandidate(candidateId: String)

    @Query("DELETE FROM discovery_candidates")
    suspend fun clearCandidates()
}

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches WHERE isActive = 1 AND matchedUserId NOT IN (SELECT blockedUserId FROM blocked_users) ORDER BY matchedAtMillis DESC")
    fun getMatchesFlow(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Query("UPDATE matches SET isActive = 0 WHERE id = :matchId")
    suspend fun deactivateMatch(matchId: String)

    @Query("DELETE FROM matches WHERE id = :matchId")
    suspend fun deleteMatch(matchId: String)

    @Query("DELETE FROM matches")
    suspend fun clearMatches()
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE participantUserId NOT IN (SELECT blockedUserId FROM blocked_users) ORDER BY lastMessageAtMillis DESC")
    fun getConversationsFlow(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    fun getConversationFlow(conversationId: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getConversationById(conversationId: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE matchId = :matchId LIMIT 1")
    suspend fun getConversationByMatchId(matchId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET lastMessageText = :text, lastMessageAtMillis = :time, lastMessageSenderId = :senderId WHERE id = :conversationId")
    suspend fun updateLastMessage(conversationId: String, text: String, time: Long, senderId: String)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun markAsRead(conversationId: String)

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1, lastMessageText = :text, lastMessageAtMillis = :time, lastMessageSenderId = :senderId WHERE id = :conversationId")
    suspend fun incrementUnreadCount(conversationId: String, text: String, time: Long, senderId: String)

    @Query("SELECT COALESCE(SUM(unreadCount), 0) FROM conversations WHERE participantUserId NOT IN (SELECT blockedUserId FROM blocked_users)")
    fun getTotalUnreadCountFlow(): Flow<Int>

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAtMillis ASC")
    fun getMessagesFlow(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearMessagesForConversation(conversationId: String)
}

@Dao
interface BlockedUserDao {
    @Query("SELECT * FROM blocked_users ORDER BY blockedAtMillis DESC")
    fun getBlockedUsersFlow(): Flow<List<BlockedUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedUser(blockedUser: BlockedUserEntity)

    @Query("SELECT * FROM blocked_users WHERE blockedUserId = :blockedUserId LIMIT 1")
    suspend fun getBlockedUserById(blockedUserId: String): BlockedUserEntity?

    @Query("DELETE FROM blocked_users WHERE blockedUserId = :blockedUserId")
    suspend fun removeBlockedUser(blockedUserId: String)

    @Query("DELETE FROM blocked_users")
    suspend fun clearBlockedUsers()
}
