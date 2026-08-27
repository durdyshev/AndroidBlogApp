package com.aura.dating.domain.auth.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.auth.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentSessionFlow: Flow<UserSession?>
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun register(email: String, password: String): Result<UserSession>
    suspend fun verifyEmail(email: String, token: String): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun getCurrentSession(): UserSession?
    suspend fun restoreSession(): Result<UserSession>
    suspend fun logout(): Result<Unit>
}
