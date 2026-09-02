package com.aura.dating.domain.discovery.model

import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.Interest
import com.aura.dating.domain.profile.model.ProfilePhoto
import kotlinx.serialization.Serializable

@Serializable
data class DiscoveryCandidate(
    val id: String,
    val displayName: String,
    val birthDateMillis: Long,
    val age: Int,
    val gender: Gender,
    val bio: String?,
    val distanceKm: Double?,
    val isOnline: Boolean,
    val lastSeenAtMillis: Long,
    val countryName: String? = null,
    val regionName: String? = null,
    val cityName: String? = null,
    val photos: List<ProfilePhoto>,
    val interests: List<Interest>
)

data class DiscoveryFilter(
    val minAge: Int = 18,
    val maxAge: Int = 50,
    val maxDistanceKm: Int = 50,
    val genderPreference: String = "ALL",
    val showOnlyOnline: Boolean = false
)
