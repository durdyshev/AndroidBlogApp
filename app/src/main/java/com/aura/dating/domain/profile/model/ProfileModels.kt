package com.aura.dating.domain.profile.model

import kotlinx.serialization.Serializable

enum class Gender {
    MAN, WOMAN, NON_BINARY, OTHER
}

enum class GenderPreference {
    ALL, WOMEN, MEN, NON_BINARY
}

@Serializable
data class ProfilePhoto(
    val id: String,
    val userId: String,
    val photoUrl: String,
    val storagePath: String,
    val displayOrder: Int = 0,
    val isPrimary: Boolean = false
)

@Serializable
data class Interest(
    val id: String,
    val name: String,
    val category: String,
    val icon: String? = null
)

@Serializable
data class UserPreferences(
    val userId: String,
    val minAge: Int = 18,
    val maxAge: Int = 50,
    val interestedInGender: GenderPreference = GenderPreference.ALL,
    val maxDistanceKm: Int = 50,
    val showOnlyOnline: Boolean = false
)

@Serializable
data class UserProfile(
    val id: String,
    val displayName: String,
    val birthDateMillis: Long,
    val gender: Gender,
    val bio: String? = null,
    val photos: List<ProfilePhoto> = emptyList(),
    val interests: List<Interest> = emptyList(),
    val preferences: UserPreferences? = null,
    val isOnline: Boolean = false,
    val lastSeenAtMillis: Long = System.currentTimeMillis(),
    val distanceKm: Double? = null
)
