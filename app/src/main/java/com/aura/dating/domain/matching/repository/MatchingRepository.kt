package com.aura.dating.domain.matching.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.matching.model.Match
import com.aura.dating.domain.matching.model.SwipeActionType
import com.aura.dating.domain.matching.model.SwipeResult
import kotlinx.coroutines.flow.Flow

interface MatchingRepository {
    val matchesFlow: Flow<List<Match>>
    suspend fun swipe(targetUserId: String, action: SwipeActionType): Result<SwipeResult>
    suspend fun getMatches(forceRefresh: Boolean = false): Result<List<Match>>
    suspend fun unmatch(matchId: String): Result<Unit>
}
