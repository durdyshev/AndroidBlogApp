package com.aura.dating.data.auth.local

import com.aura.dating.core.security.TokenStorage
import com.aura.dating.domain.auth.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

interface AuthLocalDataSource {
    val currentSessionFlow: Flow<UserSession?>
    suspend fun saveSession(session: UserSession)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun getUserId(): String?
    suspend fun clearSession()
}

@Singleton
class DataStoreAuthLocalDataSource @Inject constructor(
    private val tokenStorage: TokenStorage
) : AuthLocalDataSource {

    override val currentSessionFlow: Flow<UserSession?> = combine(
        tokenStorage.accessTokenFlow,
        tokenStorage.userIdFlow
    ) { token, userId ->
        if (!token.isNullOrBlank() && !userId.isNullOrBlank()) {
            UserSession(
                userId = userId,
                email = "",
                accessToken = token,
                refreshToken = ""
            )
        } else {
            null
        }
    }

    override suspend fun saveSession(session: UserSession) {
        tokenStorage.saveTokens(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            userId = session.userId
        )
    }

    override suspend fun getAccessToken(): String? = tokenStorage.getAccessToken()

    override suspend fun getRefreshToken(): String? = tokenStorage.getRefreshToken()

    override suspend fun getUserId(): String? = tokenStorage.getUserId()

    override suspend fun clearSession() {
        tokenStorage.clearTokens()
    }
}
