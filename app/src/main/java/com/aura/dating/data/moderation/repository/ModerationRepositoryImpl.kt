package com.aura.dating.data.moderation.repository

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.data.auth.local.AuthLocalDataSource
import com.aura.dating.data.moderation.local.ModerationLocalDataSource
import com.aura.dating.data.moderation.remote.ModerationRemoteDataSource
import com.aura.dating.domain.moderation.model.BlockedUser
import com.aura.dating.domain.moderation.model.ReportRequest
import com.aura.dating.domain.moderation.repository.ModerationRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModerationRepositoryImpl @Inject constructor(
    private val remoteDataSource: ModerationRemoteDataSource,
    private val localDataSource: ModerationLocalDataSource,
    private val authLocalDataSource: AuthLocalDataSource,
    private val tokenStorage: TokenStorage
) : ModerationRepository {

    override val blockedUsersFlow: Flow<List<BlockedUser>> = localDataSource.blockedUsersFlow

    override suspend fun blockUser(
        blockedUserId: String,
        displayName: String,
        photoUrl: String?
    ): Result<Unit> {
        val blockerId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        val blockedUser = BlockedUser(
            id = UUID.randomUUID().toString(),
            blockedUserId = blockedUserId,
            displayName = displayName,
            photoUrl = photoUrl,
            blockedAtMillis = System.currentTimeMillis()
        )
        localDataSource.saveBlockedUser(blockedUser)

        return remoteDataSource.blockUser(blockerId, blockedUserId)
    }

    override suspend fun unblockUser(blockedUserId: String): Result<Unit> {
        val blockerId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        localDataSource.removeBlockedUser(blockedUserId)
        return remoteDataSource.unblockUser(blockerId, blockedUserId)
    }

    override suspend fun getBlockedUsers(forceRefresh: Boolean): Result<List<BlockedUser>> {
        val blockerId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        val remoteResult = remoteDataSource.getBlockedUsers(blockerId)
        if (remoteResult is Result.Success) {
            val mergedList = remoteResult.data.map { remoteUser ->
                val cached = localDataSource.getBlockedUserById(remoteUser.blockedUserId)
                val finalName = if (remoteUser.displayName != "User" && remoteUser.displayName.isNotBlank()) {
                    remoteUser.displayName
                } else {
                    cached?.displayName ?: remoteUser.displayName
                }
                val finalPhoto = remoteUser.photoUrl ?: cached?.photoUrl
                remoteUser.copy(displayName = finalName, photoUrl = finalPhoto)
            }
            localDataSource.clear()
            mergedList.forEach { localDataSource.saveBlockedUser(it) }
            return Result.Success(mergedList)
        }
        return remoteResult
    }

    override suspend fun reportUser(request: ReportRequest): Result<Unit> {
        val reporterId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        return remoteDataSource.reportUser(reporterId, request)
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val result = remoteDataSource.softDeleteAccount()
        if (result is Result.Success) {
            authLocalDataSource.clearSession()
            localDataSource.clear()
        }
        return result
    }
}
