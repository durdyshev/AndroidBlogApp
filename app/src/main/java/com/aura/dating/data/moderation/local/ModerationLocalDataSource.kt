package com.aura.dating.data.moderation.local

import com.aura.dating.core.database.dao.BlockedUserDao
import com.aura.dating.core.database.entity.BlockedUserEntity
import com.aura.dating.domain.moderation.model.BlockedUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ModerationLocalDataSource {
    val blockedUsersFlow: Flow<List<BlockedUser>>
    suspend fun saveBlockedUser(blockedUser: BlockedUser)
    suspend fun getBlockedUserById(blockedUserId: String): BlockedUser?
    suspend fun removeBlockedUser(blockedUserId: String)
    suspend fun clear()
}

@Singleton
class RoomModerationLocalDataSource @Inject constructor(
    private val blockedUserDao: BlockedUserDao
) : ModerationLocalDataSource {

    override val blockedUsersFlow: Flow<List<BlockedUser>> = blockedUserDao.getBlockedUsersFlow().map { list ->
        list.map { entity ->
            BlockedUser(
                id = entity.id,
                blockedUserId = entity.blockedUserId,
                displayName = entity.displayName,
                photoUrl = entity.photoUrl,
                blockedAtMillis = entity.blockedAtMillis
            )
        }
    }

    override suspend fun saveBlockedUser(blockedUser: BlockedUser) {
        blockedUserDao.insertBlockedUser(
            BlockedUserEntity(
                id = blockedUser.id,
                blockedUserId = blockedUser.blockedUserId,
                displayName = blockedUser.displayName,
                photoUrl = blockedUser.photoUrl,
                blockedAtMillis = blockedUser.blockedAtMillis
            )
        )
    }

    override suspend fun getBlockedUserById(blockedUserId: String): BlockedUser? {
        val entity = blockedUserDao.getBlockedUserById(blockedUserId) ?: return null
        return BlockedUser(
            id = entity.id,
            blockedUserId = entity.blockedUserId,
            displayName = entity.displayName,
            photoUrl = entity.photoUrl,
            blockedAtMillis = entity.blockedAtMillis
        )
    }

    override suspend fun removeBlockedUser(blockedUserId: String) {
        blockedUserDao.removeBlockedUser(blockedUserId)
    }

    override suspend fun clear() {
        blockedUserDao.clearBlockedUsers()
    }
}
