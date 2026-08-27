package com.aura.dating.data.auth.repository

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.data.auth.local.AuthLocalDataSource
import com.aura.dating.data.auth.remote.AuthRemoteDataSource
import com.aura.dating.domain.auth.model.UserSession
import com.aura.dating.domain.auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    override val currentSessionFlow: Flow<UserSession?> = localDataSource.currentSessionFlow

    override suspend fun login(email: String, password: String): Result<UserSession> {
        val result = remoteDataSource.login(email, password)
        if (result is Result.Success) {
            localDataSource.saveSession(result.data)
        }
        return result
    }

    override suspend fun register(email: String, password: String): Result<UserSession> {
        val result = remoteDataSource.register(email, password)
        if (result is Result.Success && result.data.accessToken.isNotBlank()) {
            localDataSource.saveSession(result.data)
        }
        return result
    }

    override suspend fun verifyEmail(email: String, token: String): Result<Unit> {
        return remoteDataSource.verifyOtp(email, token)
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return remoteDataSource.sendPasswordReset(email)
    }

    override suspend fun getCurrentSession(): UserSession? {
        val token = localDataSource.getAccessToken()
        val userId = localDataSource.getUserId()
        if (token.isNullOrBlank() || userId.isNullOrBlank()) return null
        return UserSession(
            userId = userId,
            email = "",
            accessToken = token,
            refreshToken = localDataSource.getRefreshToken() ?: ""
        )
    }

    override suspend fun restoreSession(): Result<UserSession> {
        val refreshToken = localDataSource.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            return Result.Error(AppError.Unauthorized("No stored refresh token"))
        }
        val result = remoteDataSource.refreshToken(refreshToken)
        if (result is Result.Success) {
            localDataSource.saveSession(result.data)
        }
        return result
    }

    override suspend fun logout(): Result<Unit> {
        remoteDataSource.logout()
        localDataSource.clearSession()
        return Result.Success(Unit)
    }
}
