package com.aura.dating.domain.profile.usecase

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.Interest
import com.aura.dating.domain.profile.model.ProfilePhoto
import com.aura.dating.domain.profile.model.UserPreferences
import com.aura.dating.domain.profile.model.UserProfile
import com.aura.dating.domain.profile.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    val profileFlow: Flow<UserProfile?> = profileRepository.getMyProfileFlow()
    suspend operator fun invoke(): Result<UserProfile> = profileRepository.getMyProfile()
}

class GetUserProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String): Result<UserProfile> = profileRepository.getUserProfile(userId)
}

class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        displayName: String,
        birthDateMillis: Long,
        gender: Gender,
        bio: String?,
        interestIds: List<String>
    ): Result<UserProfile> {
        val trimmedName = displayName.trim()
        if (trimmedName.length < 2) {
            return Result.Error(AppError.ValidationError("Name must be at least 2 characters", field = "displayName"))
        }

        val age = DateTimeUtils.calculateAge(birthDateMillis)
        if (age < 18) {
            return Result.Error(AppError.ValidationError("You must be at least 18 years old to use Aura.", field = "birthDate"))
        }

        return profileRepository.updateProfile(
            displayName = trimmedName,
            birthDateMillis = birthDateMillis,
            gender = gender,
            bio = bio?.trim(),
            interestIds = interestIds
        )
    }
}

class UploadPhotoUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(imageBytes: ByteArray, isPrimary: Boolean = false): Result<ProfilePhoto> {
        if (imageBytes.isEmpty()) {
            return Result.Error(AppError.ValidationError("Image file cannot be empty"))
        }
        return profileRepository.uploadPhoto(imageBytes, isPrimary)
    }
}

class DeletePhotoUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(photoId: String, storagePath: String): Result<Unit> {
        return profileRepository.deletePhoto(photoId, storagePath)
    }
}

class ReorderPhotosUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(photoIds: List<String>): Result<Unit> {
        return profileRepository.reorderPhotos(photoIds)
    }
}

class SetPrimaryPhotoUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(photoId: String): Result<Unit> {
        return profileRepository.setPrimaryPhoto(photoId)
    }
}

class UpdatePreferencesUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(preferences: UserPreferences): Result<UserPreferences> {
        if (preferences.minAge < 18) {
            return Result.Error(AppError.ValidationError("Minimum age cannot be lower than 18"))
        }
        if (preferences.maxAge < preferences.minAge) {
            return Result.Error(AppError.ValidationError("Maximum age cannot be less than minimum age"))
        }
        return profileRepository.updatePreferences(preferences)
    }
}

class UpdateLocationUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double): Result<Unit> {
        return profileRepository.updateLocation(latitude, longitude)
    }
}

class GetInterestsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(): Result<List<Interest>> = profileRepository.getAllInterests()
}
