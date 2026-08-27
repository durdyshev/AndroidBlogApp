package com.aura.dating.data.discovery.local

import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.database.dao.DiscoveryDao
import com.aura.dating.core.database.entity.DiscoveryCandidateEntity
import com.aura.dating.domain.discovery.model.DiscoveryCandidate
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.Interest
import com.aura.dating.domain.profile.model.ProfilePhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface DiscoveryLocalDataSource {
    val candidatesFlow: Flow<List<DiscoveryCandidate>>
    suspend fun getCandidatesOnce(): List<DiscoveryCandidate>
    suspend fun saveCandidates(candidates: List<DiscoveryCandidate>)
    suspend fun removeCandidate(candidateId: String)
    suspend fun clear()
}

@Singleton
class RoomDiscoveryLocalDataSource @Inject constructor(
    private val discoveryDao: DiscoveryDao
) : DiscoveryLocalDataSource {

    override val candidatesFlow: Flow<List<DiscoveryCandidate>> = discoveryDao.getCandidatesFlow().map { list ->
        list.map { entity ->
            val age = DateTimeUtils.calculateAge(entity.birthDateMillis)
            DiscoveryCandidate(
                id = entity.id,
                displayName = entity.displayName,
                birthDateMillis = entity.birthDateMillis,
                age = age,
                gender = try { Gender.valueOf(entity.gender) } catch (e: Exception) { Gender.OTHER },
                bio = entity.bio,
                distanceKm = entity.distanceKm,
                isOnline = entity.isOnline,
                lastSeenAtMillis = entity.lastSeenAtMillis,
                photos = entity.photos.mapIndexed { index, url ->
                    ProfilePhoto(
                        id = index.toString(),
                        userId = entity.id,
                        photoUrl = url,
                        storagePath = "",
                        displayOrder = index,
                        isPrimary = index == 0
                    )
                },
                interests = entity.interests.map { Interest(it, it, "General", null) }
            )
        }
    }

    override suspend fun getCandidatesOnce(): List<DiscoveryCandidate> {
        return discoveryDao.getCandidatesOnce().map { entity ->
            val age = DateTimeUtils.calculateAge(entity.birthDateMillis)
            DiscoveryCandidate(
                id = entity.id,
                displayName = entity.displayName,
                birthDateMillis = entity.birthDateMillis,
                age = age,
                gender = try { Gender.valueOf(entity.gender) } catch (e: Exception) { Gender.OTHER },
                bio = entity.bio,
                distanceKm = entity.distanceKm,
                isOnline = entity.isOnline,
                lastSeenAtMillis = entity.lastSeenAtMillis,
                photos = entity.photos.mapIndexed { index, url ->
                    ProfilePhoto(
                        id = index.toString(),
                        userId = entity.id,
                        photoUrl = url,
                        storagePath = "",
                        displayOrder = index,
                        isPrimary = index == 0
                    )
                },
                interests = entity.interests.map { Interest(it, it, "General", null) }
            )
        }
    }

    override suspend fun saveCandidates(candidates: List<DiscoveryCandidate>) {
        val entities = candidates.map { c ->
            DiscoveryCandidateEntity(
                id = c.id,
                displayName = c.displayName,
                birthDateMillis = c.birthDateMillis,
                gender = c.gender.name,
                bio = c.bio,
                photos = c.photos.map { it.photoUrl },
                interests = c.interests.map { it.name },
                distanceKm = c.distanceKm,
                isOnline = c.isOnline,
                lastSeenAtMillis = c.lastSeenAtMillis,
                cachedAtMillis = System.currentTimeMillis()
            )
        }
        discoveryDao.insertCandidates(entities)
    }

    override suspend fun removeCandidate(candidateId: String) {
        discoveryDao.removeCandidate(candidateId)
    }

    override suspend fun clear() {
        discoveryDao.clearCandidates()
    }
}
