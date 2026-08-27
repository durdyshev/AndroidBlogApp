package com.aura.dating.domain.discovery.usecase

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.discovery.model.DiscoveryCandidate
import com.aura.dating.domain.discovery.repository.DiscoveryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDiscoveryCandidatesUseCase @Inject constructor(
    private val discoveryRepository: DiscoveryRepository
) {
    val candidatesFlow: Flow<List<DiscoveryCandidate>> = discoveryRepository.candidatesFlow

    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<DiscoveryCandidate>> {
        return discoveryRepository.getCandidates(limit = 20, forceRefresh = forceRefresh)
    }
}
