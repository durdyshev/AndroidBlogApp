package com.aura.dating.data.notifications.remote

import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.core.notifications.NotificationType
import com.aura.dating.domain.notifications.model.NotificationItem
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
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
data class NotificationSupabaseDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("actor_id") val actorId: String? = null,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class RegisterDeviceTokenRequest(
    @SerialName("user_id") val userId: String,
    val token: String,
    val platform: String = "ANDROID",
    @SerialName("updated_at") val updatedAt: String = "now()"
)

@Serializable
data class MarkNotificationReadRequest(
    @SerialName("is_read") val isRead: Boolean = true
)

interface NotificationRemoteDataSource {
    suspend fun registerDeviceToken(userId: String, token: String): Result<Unit>
    suspend fun getNotifications(userId: String): Result<List<NotificationItem>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
}

@Singleton
class SupabaseNotificationRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : NotificationRemoteDataSource {

    override suspend fun registerDeviceToken(
        userId: String,
        token: String
    ): Result<Unit> {
        val requestBody = RegisterDeviceTokenRequest(
            userId = userId,
            token = token
        )

        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/device_tokens")
                    contentType(ContentType.Application.Json)
                    header("Prefer", "resolution=merge-duplicates")
                    headers(this)
                    setBody(requestBody)
                }
            },
            parser = { }
        )
    }

    override suspend fun getNotifications(userId: String): Result<List<NotificationItem>> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/notifications?user_id=eq.$userId&order=created_at.desc&limit=30")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<NotificationSupabaseDto>>()
                list.map { dto ->
                    NotificationItem(
                        id = dto.id,
                        userId = dto.userId,
                        actorId = dto.actorId,
                        type = try { NotificationType.valueOf(dto.type) } catch (e: Exception) { NotificationType.SYSTEM },
                        title = dto.title,
                        body = dto.body,
                        isRead = dto.isRead,
                        createdAtMillis = DateTimeUtils.parseIsoDate(dto.createdAt)
                    )
                }
            }
        )
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.patch {
                    url("${clientProvider.baseUrl}/rest/v1/notifications?id=eq.$notificationId")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(MarkNotificationReadRequest(isRead = true))
                }
            },
            parser = { }
        )
    }
}
