package com.aura.dating.domain.profile.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.Interest
import com.aura.dating.domain.profile.model.ProfilePhoto
import com.aura.dating.domain.profile.model.UserPreferences
import com.aura.dating.domain.profile.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getMyProfileFlow(): Flow<UserProfile?>
    suspend fun getMyProfile(): Result<UserProfile>
    suspend fun getUserProfile(userId: String): Result<UserProfile>
    suspend fun updateProfile(
        displayName: String,
        birthDateMillis: Long,
        gender: Gender,
        bio: String?,
        interestIds: List<String>
    ): Result<UserProfile>
    suspend fun uploadPhoto(imageBytes: ByteArray, isPrimary: Boolean): Result<ProfilePhoto>
    suspend fun deletePhoto(photoId: String, storagePath: String): Result<Unit>
    suspend fun reorderPhotos(photoIds: List<String>): Result<Unit>
    suspend fun setPrimaryPhoto(photoId: String): Result<Unit>
    suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences>
    suspend fun updateLocation(latitude: Double, longitude: Double): Result<Unit>
    suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit>
    suspend fun getAllInterests(): Result<List<Interest>>
}
