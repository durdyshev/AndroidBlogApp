package com.aura.dating.data.profile.repository

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.data.profile.local.ProfileLocalDataSource
import com.aura.dating.data.profile.remote.ProfileRemoteDataSource
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.Interest
import com.aura.dating.domain.profile.model.ProfilePhoto
import com.aura.dating.domain.profile.model.UserPreferences
import com.aura.dating.domain.profile.model.UserProfile
import com.aura.dating.domain.profile.repository.ProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val remoteDataSource: ProfileRemoteDataSource,
    private val localDataSource: ProfileLocalDataSource,
    private val tokenStorage: TokenStorage
) : ProfileRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getMyProfileFlow(): Flow<UserProfile?> {
        return tokenStorage.userIdFlow.flatMapLatest { userId ->
            if (userId.isNullOrBlank()) flowOf(null)
            else localDataSource.getProfileFlow(userId)
        }
    }

    override suspend fun getMyProfile(): Result<UserProfile> {
        val userId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        val result = remoteDataSource.getProfile(userId)
        if (result is Result.Success) {
            localDataSource.saveProfile(result.data)
        } else {
            val cached = localDataSource.getProfileOnce(userId)
            if (cached != null) return Result.Success(cached)
        }
        return result
    }

    override suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return remoteDataSource.getProfile(userId)
    }

    override suspend fun updateProfile(
        displayName: String,
        birthDateMillis: Long,
        gender: Gender,
        bio: String?,
        interestIds: List<String>
    ): Result<UserProfile> {
        val userId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        val result = remoteDataSource.updateProfile(
            userId = userId,
            displayName = displayName,
            birthDateMillis = birthDateMillis,
            gender = gender,
            bio = bio,
            interestIds = interestIds
        )
        if (result is Result.Success) {
            localDataSource.saveProfile(result.data)
        }
        return result
    }

    override suspend fun uploadPhoto(
        imageBytes: ByteArray,
        isPrimary: Boolean
    ): Result<ProfilePhoto> {
        val userId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        val result = remoteDataSource.uploadPhoto(userId, imageBytes, isPrimary)
        if (result is Result.Success) {
            getMyProfile() // Refresh local cached profile
        }
        return result
    }

    override suspend fun deletePhoto(photoId: String, storagePath: String): Result<Unit> {
        val result = remoteDataSource.deletePhoto(photoId, storagePath)
        if (result is Result.Success) {
            getMyProfile()
        }
        return result
    }

    override suspend fun reorderPhotos(photoIds: List<String>): Result<Unit> {
        val result = remoteDataSource.reorderPhotos(photoIds)
        if (result is Result.Success) {
            getMyProfile()
        }
        return result
    }

    override suspend fun setPrimaryPhoto(photoId: String): Result<Unit> {
        val result = remoteDataSource.setPrimaryPhoto(photoId)
        if (result is Result.Success) {
            getMyProfile()
        }
        return result
    }

    override suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences> {
        val userId = if (preferences.userId.isNotBlank()) preferences.userId else tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())
        val result = remoteDataSource.updatePreferences(preferences.copy(userId = userId))
        if (result is Result.Success) {
            getMyProfile()
        }
        return result
    }

    override suspend fun updateLocation(latitude: Double, longitude: Double): Result<Unit> {
        return remoteDataSource.updateLocation(latitude, longitude)
    }

    override suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit> {
        val userId = tokenStorage.getUserId() ?: return Result.Error(AppError.Unauthorized())
        return remoteDataSource.updateOnlineStatus(userId, isOnline)
    }

    override suspend fun getAllInterests(): Result<List<Interest>> {
        return remoteDataSource.getAllInterests()
    }
}
