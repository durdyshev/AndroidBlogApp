package com.aura.dating.core.network

import com.aura.dating.BuildConfig
import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.security.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
private data class TokenRefreshResponseDto(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val user: TokenRefreshUserDto? = null
)

@Serializable
private data class TokenRefreshUserDto(
    val id: String
)

@Singleton
class SupabaseClientProvider @Inject constructor(
    private val tokenStorage: TokenStorage
) {
    val baseUrl: String = BuildConfig.SUPABASE_URL.trimEnd('/')
    val anonKey: String = BuildConfig.SUPABASE_ANON_KEY

    private val refreshMutex = Mutex()

    val jsonSerializer = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }

    val httpClient: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(jsonSerializer)
            }
            install(WebSockets)
            install(Logging) {
                level = if (BuildConfig.DEBUG) LogLevel.HEADERS else LogLevel.NONE
            }
        }
    }

    private suspend fun tryRefreshToken(): Boolean {
        return refreshMutex.withLock {
            val refreshToken = tokenStorage.getRefreshToken() ?: return false
            val currentUserId = tokenStorage.getUserId() ?: ""
            try {
                val refreshResponse = httpClient.post {
                    url("$baseUrl/auth/v1/token?grant_type=refresh_token")
                    contentType(ContentType.Application.Json)
                    header("apikey", anonKey)
                    header(HttpHeaders.Authorization, "Bearer $anonKey")
                    setBody("""{"refresh_token":"$refreshToken"}""")
                }
                if (refreshResponse.status.isSuccess()) {
                    val body = refreshResponse.body<TokenRefreshResponseDto>()
                    val newAccessToken = body.accessToken ?: return false
                    val newRefreshToken = body.refreshToken ?: refreshToken
                    val newUserId = body.user?.id ?: currentUserId
                    tokenStorage.saveTokens(newAccessToken, newRefreshToken, newUserId)
                    true
                } else {
                    false
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    suspend fun <T> safeApiCall(
        block: suspend (client: HttpClient, headers: suspend (HttpRequestBuilder) -> Unit) -> HttpResponse,
        parser: suspend (HttpResponse) -> T
    ): Result<T> {
        return try {
            val token = tokenStorage.getAccessToken()
            val headerBlock: suspend (HttpRequestBuilder) -> Unit = { builder ->
                builder.header("apikey", anonKey)
                val currentToken = tokenStorage.getAccessToken()
                val bearerToken = if (!currentToken.isNullOrBlank()) currentToken else anonKey
                if (bearerToken.isNotBlank()) {
                    builder.header(HttpHeaders.Authorization, "Bearer $bearerToken")
                }
            }

            var response = block(httpClient, headerBlock)

            // If 401 Unauthorized or token expired, attempt transparent refresh and retry once!
            if (response.status.value == 401) {
                val rawBody = runCatching { response.bodyAsText() }.getOrDefault("")
                if (rawBody.contains("expired", ignoreCase = true) ||
                    rawBody.contains("JWT", ignoreCase = true) ||
                    rawBody.contains("invalid_token", ignoreCase = true)
                ) {
                    val refreshed = tryRefreshToken()
                    if (refreshed) {
                        val newHeaderBlock: suspend (HttpRequestBuilder) -> Unit = { builder ->
                            builder.header("apikey", anonKey)
                            val freshToken = tokenStorage.getAccessToken()
                            val bearerToken = if (!freshToken.isNullOrBlank()) freshToken else anonKey
                            if (bearerToken.isNotBlank()) {
                                builder.header(HttpHeaders.Authorization, "Bearer $bearerToken")
                            }
                        }
                        response = block(httpClient, newHeaderBlock)
                    }
                }
            }

            if (response.status.isSuccess()) {
                val data = parser(response)
                Result.Success(data)
            } else {
                val rawError = runCatching { response.bodyAsText() }.getOrDefault("")
                val detailedMessage = parseErrorMessage(rawError, response.status.value)
                
                when (response.status.value) {
                    400 -> Result.Error(AppError.ValidationError(detailedMessage))
                    401 -> Result.Error(AppError.Unauthorized(detailedMessage))
                    403 -> Result.Error(AppError.Forbidden(detailedMessage))
                    404 -> Result.Error(AppError.NotFound(detailedMessage))
                    422 -> Result.Error(AppError.ValidationError(detailedMessage))
                    in 500..599 -> Result.Error(
                        AppError.ServerError(statusCode = response.status.value, message = detailedMessage)
                    )
                    else -> Result.Error(
                        AppError.UnknownError(detailedMessage)
                    )
                }
            }
        } catch (e: IOException) {
            Result.Error(AppError.NetworkError(cause = e))
        } catch (e: Exception) {
            Result.Error(AppError.UnknownError(message = e.message ?: "Unexpected error", cause = e))
        }
    }

    private fun parseErrorMessage(rawBody: String, statusCode: Int): String {
        if (rawBody.isBlank()) return "Request failed ($statusCode)"
        return try {
            val json = jsonSerializer.parseToJsonElement(rawBody).jsonObject
            json["msg"]?.jsonPrimitive?.content
                ?: json["message"]?.jsonPrimitive?.content
                ?: json["error_description"]?.jsonPrimitive?.content
                ?: json["error"]?.jsonPrimitive?.content
                ?: rawBody
        } catch (_: Exception) {
            rawBody
        }
    }
}
