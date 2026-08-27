package com.aura.dating.data.profile.local

import com.aura.dating.core.database.dao.ProfileDao
import com.aura.dating.core.database.entity.ProfileEntity
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.Interest
import com.aura.dating.domain.profile.model.ProfilePhoto
import com.aura.dating.domain.profile.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileLocalDataSource {
    fun getProfileFlow(userId: String): Flow<UserProfile?>
    suspend fun getProfileOnce(userId: String): UserProfile?
    suspend fun saveProfile(profile: UserProfile)
    suspend fun deleteProfile(userId: String)
}

@Singleton
class RoomProfileLocalDataSource @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileLocalDataSource {

    override fun getProfileFlow(userId: String): Flow<UserProfile?> {
        return profileDao.getProfile(userId).map { entity ->
            entity?.let { mapEntityToDomain(it) }
        }
    }

    override suspend fun getProfileOnce(userId: String): UserProfile? {
        return profileDao.getProfileOnce(userId)?.let { mapEntityToDomain(it) }
    }

    override suspend fun saveProfile(profile: UserProfile) {
        val entity = ProfileEntity(
            id = profile.id,
            displayName = profile.displayName,
            birthDateMillis = profile.birthDateMillis,
            gender = profile.gender.name,
            bio = profile.bio,
            photos = profile.photos.map { it.photoUrl },
            interests = profile.interests.map { it.name },
            isOnline = profile.isOnline,
            lastSeenAtMillis = profile.lastSeenAtMillis,
            updatedAtMillis = System.currentTimeMillis()
        )
        profileDao.insertProfile(entity)
    }

    override suspend fun deleteProfile(userId: String) {
        profileDao.deleteProfile(userId)
    }

    private fun mapEntityToDomain(entity: ProfileEntity): UserProfile {
        return UserProfile(
            id = entity.id,
            displayName = entity.displayName,
            birthDateMillis = entity.birthDateMillis,
            gender = try { Gender.valueOf(entity.gender) } catch (e: Exception) { Gender.OTHER },
            bio = entity.bio,
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
            interests = entity.interests.map { Interest(it, it, "General", null) },
            isOnline = entity.isOnline,
            lastSeenAtMillis = entity.lastSeenAtMillis
        )
    }
}
