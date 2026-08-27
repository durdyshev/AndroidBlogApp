package com.aura.dating.domain.matching.model

import com.aura.dating.domain.profile.model.UserProfile
import kotlinx.serialization.Serializable

enum class SwipeActionType {
    PASS,
    LIKE,
    SUPER_LIKE
}

@Serializable
data class SwipeResult(
    val isMatch: Boolean,
    val matchId: String? = null,
    val matchedUser: UserProfile? = null
)

@Serializable
data class Match(
    val id: String,
    val matchedUserId: String,
    val matchedUserName: String,
    val matchedUserAge: Int,
    val matchedUserPhotoUrl: String?,
    val matchedUserDistanceKm: Double?,
    val matchedAtMillis: Long,
    val isActive: Boolean = true
)
