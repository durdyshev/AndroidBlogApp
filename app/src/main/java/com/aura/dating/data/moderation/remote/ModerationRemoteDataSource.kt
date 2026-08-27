package com.aura.dating.data.moderation.remote

import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.data.profile.remote.ProfileDto
import com.aura.dating.domain.moderation.model.BlockedUser
import com.aura.dating.domain.moderation.model.ReportRequest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
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
data class BlockSupabaseDto(
    val id: String,
    @SerialName("blocker_id") val blockerId: String,
    @SerialName("blocked_id") val blockedId: String,
    @SerialName("created_at") val createdAt: String,
    val blocked: ProfileDto? = null
)

@Serializable
data class InsertBlockRequest(
    @SerialName("blocker_id") val blockerId: String,
    @SerialName("blocked_id") val blockedId: String
)

@Serializable
data class InsertReportRequest(
    @SerialName("reporter_id") val reporterId: String,
    @SerialName("reported_id") val reportedId: String,
    val reason: String,
    val details: String? = null
)

interface ModerationRemoteDataSource {
    suspend fun blockUser(blockerId: String, blockedId: String): Result<Unit>
    suspend fun unblockUser(blockerId: String, blockedId: String): Result<Unit>
    suspend fun getBlockedUsers(blockerId: String): Result<List<BlockedUser>>
    suspend fun reportUser(reporterId: String, request: ReportRequest): Result<Unit>
    suspend fun softDeleteAccount(): Result<Unit>
}

@Singleton
class SupabaseModerationRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : ModerationRemoteDataSource {

    override suspend fun blockUser(
        blockerId: String,
        blockedId: String
    ): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/blocks")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(InsertBlockRequest(blockerId = blockerId, blockedId = blockedId))
                }
            },
            parser = { }
        )
    }

    override suspend fun unblockUser(
        blockerId: String,
        blockedId: String
    ): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.delete {
                    url("${clientProvider.baseUrl}/rest/v1/blocks?blocker_id=eq.$blockerId&blocked_id=eq.$blockedId")
                    headers(this)
                }
            },
            parser = { }
        )
    }

    override suspend fun getBlockedUsers(blockerId: String): Result<List<BlockedUser>> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/blocks?blocker_id=eq.$blockerId&select=*,blocked:profiles!blocks_blocked_id_fkey(*,profile_photos(*))&order=created_at.desc")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<BlockSupabaseDto>>()
                list.map { dto ->
                    val profile = dto.blocked
                    val photoUrl = profile?.photos?.firstOrNull { it.isPrimary }?.photoUrl
                        ?: profile?.photos?.firstOrNull()?.photoUrl

                    BlockedUser(
                        id = dto.id,
                        blockedUserId = dto.blockedId,
                        displayName = profile?.displayName ?: "User",
                        photoUrl = photoUrl,
                        blockedAtMillis = DateTimeUtils.parseIsoDate(dto.createdAt)
                    )
                }
            }
        )
    }

    override suspend fun reportUser(
        reporterId: String,
        request: ReportRequest
    ): Result<Unit> {
        val requestBody = InsertReportRequest(
            reporterId = reporterId,
            reportedId = request.reportedUserId,
            reason = request.reason.name,
            details = request.details
        )

        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/reports")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(requestBody)
                }
            },
            parser = { }
        )
    }

    override suspend fun softDeleteAccount(): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/rpc/soft_delete_user_account")
                    contentType(ContentType.Application.Json)
                    headers(this)
                }
            },
            parser = { }
        )
    }
}
