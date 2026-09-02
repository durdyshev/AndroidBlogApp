package com.aura.dating.domain.discovery.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.discovery.model.DiscoveryCandidate
import kotlinx.coroutines.flow.Flow

interface DiscoveryRepository {
    val candidatesFlow: Flow<List<DiscoveryCandidate>>
    suspend fun getCandidates(limit: Int = 20, forceRefresh: Boolean = false): Result<List<DiscoveryCandidate>>
    suspend fun removeCandidateLocally(candidateId: String)
    suspend fun searchCandidatesByLocation(
        countryId: String?,
        regionId: String?,
        cityId: String?,
        minAge: Int,
        maxAge: Int,
        gender: String,
        limit: Int = 20,
        offset: Int = 0
    ): Result<List<DiscoveryCandidate>>
}
