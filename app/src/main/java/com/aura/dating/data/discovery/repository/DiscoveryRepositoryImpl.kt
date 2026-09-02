package com.aura.dating.data.discovery.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.data.discovery.local.DiscoveryLocalDataSource
import com.aura.dating.data.discovery.remote.DiscoveryRemoteDataSource
import com.aura.dating.domain.discovery.model.DiscoveryCandidate
import com.aura.dating.domain.discovery.repository.DiscoveryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryRepositoryImpl @Inject constructor(
    private val remoteDataSource: DiscoveryRemoteDataSource,
    private val localDataSource: DiscoveryLocalDataSource
) : DiscoveryRepository {

    override val candidatesFlow: Flow<List<DiscoveryCandidate>> = localDataSource.candidatesFlow

    override suspend fun getCandidates(
        limit: Int,
        forceRefresh: Boolean
    ): Result<List<DiscoveryCandidate>> {
        val cached = localDataSource.getCandidatesOnce()
        if (cached.isNotEmpty() && !forceRefresh) {
            return Result.Success(cached)
        }

        val remoteResult = remoteDataSource.getCandidates(limit)
        if (remoteResult is Result.Success) {
            if (forceRefresh) {
                localDataSource.clear()
            }
            localDataSource.saveCandidates(remoteResult.data)
            return remoteResult
        }

        if (cached.isNotEmpty()) {
            return Result.Success(cached)
        }

        return remoteResult
    }

    override suspend fun removeCandidateLocally(candidateId: String) {
        localDataSource.removeCandidate(candidateId)
    }

    override suspend fun searchCandidatesByLocation(
        countryId: String?,
        regionId: String?,
        cityId: String?,
        minAge: Int,
        maxAge: Int,
        gender: String,
        limit: Int,
        offset: Int
    ): Result<List<DiscoveryCandidate>> {
        return remoteDataSource.searchCandidatesByLocation(
            countryId = countryId,
            regionId = regionId,
            cityId = cityId,
            minAge = minAge,
            maxAge = maxAge,
            gender = gender,
            limit = limit,
            offset = offset
        )
    }
}
