package com.aura.dating.data.matching.repository

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.data.matching.local.MatchingLocalDataSource
import com.aura.dating.data.matching.remote.MatchingRemoteDataSource
import com.aura.dating.domain.matching.model.Match
import com.aura.dating.domain.matching.model.SwipeActionType
import com.aura.dating.domain.matching.model.SwipeResult
import com.aura.dating.domain.matching.repository.MatchingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchingRepositoryImpl @Inject constructor(
    private val remoteDataSource: MatchingRemoteDataSource,
    private val localDataSource: MatchingLocalDataSource,
    private val tokenStorage: TokenStorage
) : MatchingRepository {

    override val matchesFlow: Flow<List<Match>> = localDataSource.matchesFlow

    override suspend fun swipe(
        targetUserId: String,
        action: SwipeActionType
    ): Result<SwipeResult> {
        val result = remoteDataSource.processSwipe(targetUserId, action)
        if (result is Result.Success && result.data.isMatch && result.data.matchId != null) {
            val user = result.data.matchedUser
            if (user != null) {
                localDataSource.saveMatch(
                    Match(
                        id = result.data.matchId,
                        matchedUserId = user.id,
                        matchedUserName = user.displayName,
                        matchedUserAge = com.aura.dating.core.common.utils.DateTimeUtils.calculateAge(user.birthDateMillis),
                        matchedUserPhotoUrl = user.photos.firstOrNull()?.photoUrl,
                        matchedUserDistanceKm = null,
                        matchedAtMillis = System.currentTimeMillis(),
                        isActive = true
                    )
                )
            }
        }
        return result
    }

    override suspend fun getMatches(forceRefresh: Boolean): Result<List<Match>> {
        val userId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        val remoteResult = remoteDataSource.getMatches(userId)
        if (remoteResult is Result.Success) {
            if (forceRefresh) {
                localDataSource.clear()
            }
            localDataSource.saveMatches(remoteResult.data)
            return remoteResult
        }
        return remoteResult
    }

    override suspend fun unmatch(matchId: String): Result<Unit> {
        localDataSource.deactivateMatch(matchId)
        return remoteDataSource.unmatch(matchId)
    }
}
