package com.aura.dating.domain.matching.usecase

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.discovery.repository.DiscoveryRepository
import com.aura.dating.domain.matching.model.Match
import com.aura.dating.domain.matching.model.SwipeActionType
import com.aura.dating.domain.matching.model.SwipeResult
import com.aura.dating.domain.matching.repository.MatchingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SwipeUserUseCase @Inject constructor(
    private val matchingRepository: MatchingRepository,
    private val discoveryRepository: DiscoveryRepository
) {
    suspend operator fun invoke(targetUserId: String, action: SwipeActionType): Result<SwipeResult> {
        // Optimistically remove from discovery stack
        discoveryRepository.removeCandidateLocally(targetUserId)
        return matchingRepository.swipe(targetUserId, action)
    }
}

class GetMatchesUseCase @Inject constructor(
    private val matchingRepository: MatchingRepository
) {
    val matchesFlow: Flow<List<Match>> = matchingRepository.matchesFlow

    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<Match>> {
        return matchingRepository.getMatches(forceRefresh = forceRefresh)
    }
}

class UnmatchUseCase @Inject constructor(
    private val matchingRepository: MatchingRepository
) {
    suspend operator fun invoke(matchId: String): Result<Unit> {
        return matchingRepository.unmatch(matchId)
    }
}
