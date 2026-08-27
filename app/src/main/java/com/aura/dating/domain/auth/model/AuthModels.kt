package com.aura.dating.domain.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long? = null
)

data class AuthCredentials(
    val email: String,
    val password: String
)
