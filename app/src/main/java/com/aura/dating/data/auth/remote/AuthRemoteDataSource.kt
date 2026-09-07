package com.aura.dating.data.auth.remote

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.domain.auth.model.UserSession
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SupabaseAuthRequest(
    val email: String,
    val password: String? = null
)

@Serializable
data class VerifyOtpRequest(
    val type: String = "signup",
    val email: String,
    val token: String
)

@Serializable
data class ResendOtpRequest(
    val type: String = "signup",
    val email: String
)

@Serializable
data class PasswordResetRequest(
    val email: String
)

@Serializable
data class SupabaseTokenRefreshRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class SupabaseAuthResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    val user: SupabaseUserDto? = null,
    val id: String? = null,
    val email: String? = null
)

@Serializable
data class SupabaseUserDto(
    val id: String,
    val email: String? = null
)

interface AuthRemoteDataSource {
    suspend fun login(email: String, password: String): Result<UserSession>
    suspend fun register(email: String, password: String): Result<UserSession>
    suspend fun verifyOtp(email: String, token: String): Result<UserSession>
    suspend fun resendOtp(email: String): Result<Unit>
    suspend fun sendPasswordReset(email: String): Result<Unit>
    suspend fun refreshToken(refreshToken: String): Result<UserSession>
    suspend fun logout(): Result<Unit>
}

@Singleton
class SupabaseAuthRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : AuthRemoteDataSource {

    override suspend fun login(email: String, password: String): Result<UserSession> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/auth/v1/token?grant_type=password")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(SupabaseAuthRequest(email, password))
                }
            },
            parser = { response ->
                val body = response.body<SupabaseAuthResponse>()
                val userId = body.user?.id ?: body.id ?: ""
                val userEmail = body.user?.email ?: body.email ?: email
                val accessToken = requireNotNull(body.accessToken) { "Access token missing" }
                val refreshToken = requireNotNull(body.refreshToken) { "Refresh token missing" }

                UserSession(
                    userId = userId,
                    email = userEmail,
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAt = System.currentTimeMillis() + ((body.expiresIn ?: 3600) * 1000)
                )
            }
        )
    }

    override suspend fun register(email: String, password: String): Result<UserSession> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/auth/v1/signup")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(SupabaseAuthRequest(email, password))
                }
            },
            parser = { response ->
                val body = response.body<SupabaseAuthResponse>()
                val userId = body.user?.id ?: body.id ?: ""
                val userEmail = body.user?.email ?: body.email ?: email
                val accessToken = body.accessToken ?: ""
                val refreshToken = body.refreshToken ?: ""

                UserSession(
                    userId = userId,
                    email = userEmail,
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAt = System.currentTimeMillis() + ((body.expiresIn ?: 3600) * 1000)
                )
            }
        )
    }

    override suspend fun verifyOtp(email: String, token: String): Result<UserSession> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/auth/v1/verify")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(VerifyOtpRequest(type = "signup", email = email, token = token))
                }
            },
            parser = { response ->
                val body = response.body<SupabaseAuthResponse>()
                val userId = body.user?.id ?: body.id ?: ""
                val userEmail = body.user?.email ?: body.email ?: email
                val accessToken = requireNotNull(body.accessToken) { "Access token missing in verification response" }
                val refreshToken = body.refreshToken ?: ""

                UserSession(
                    userId = userId,
                    email = userEmail,
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    expiresAt = System.currentTimeMillis() + ((body.expiresIn ?: 3600) * 1000)
                )
            }
        )
    }

    override suspend fun resendOtp(email: String): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/auth/v1/resend")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(ResendOtpRequest(type = "signup", email = email))
                }
            },
            parser = { }
        )
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/auth/v1/recover")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(PasswordResetRequest(email = email))
                }
            },
            parser = { }
        )
    }

    override suspend fun refreshToken(refreshToken: String): Result<UserSession> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/auth/v1/token?grant_type=refresh_token")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(SupabaseTokenRefreshRequest(refreshToken))
                }
            },
            parser = { response ->
                val body = response.body<SupabaseAuthResponse>()
                val userId = body.user?.id ?: body.id ?: ""
                val userEmail = body.user?.email ?: body.email ?: ""
                val accessToken = requireNotNull(body.accessToken) { "Access token missing" }
                val newRefreshToken = requireNotNull(body.refreshToken) { "Refresh token missing" }

                UserSession(
                    userId = userId,
                    email = userEmail,
                    accessToken = accessToken,
                    refreshToken = newRefreshToken,
                    expiresAt = System.currentTimeMillis() + ((body.expiresIn ?: 3600) * 1000)
                )
            }
        )
    }

    override suspend fun logout(): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/auth/v1/logout")
                    headers(this)
                }
            },
            parser = { }
        )
    }
}
