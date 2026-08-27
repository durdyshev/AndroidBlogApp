package com.aura.dating.data.matching.remote

import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.data.profile.remote.ProfileDto
import com.aura.dating.domain.matching.model.Match
import com.aura.dating.domain.matching.model.SwipeActionType
import com.aura.dating.domain.matching.model.SwipeResult
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.ProfilePhoto
import com.aura.dating.domain.profile.model.UserProfile
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ProcessSwipeRpcResponse(
    @SerialName("is_match") val isMatch: Boolean,
    @SerialName("match_id") val matchId: String? = null,
    @SerialName("matched_user") val matchedUser: MatchedUserDto? = null
)

@Serializable
data class MatchedUserDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("birth_date") val birthDate: String,
    val photos: List<String> = emptyList()
)

@Serializable
data class MatchSupabaseDto(
    val id: String,
    @SerialName("user1_id") val user1Id: String,
    @SerialName("user2_id") val user2Id: String,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String,
    val user1: ProfileDto? = null,
    val user2: ProfileDto? = null
)

@Serializable
data class ProcessSwipeRequest(
    val p_target_id: String,
    val p_action: String
)

@Serializable
data class UnmatchRequest(
    val p_match_id: String
)

interface MatchingRemoteDataSource {
    suspend fun processSwipe(targetUserId: String, action: SwipeActionType): Result<SwipeResult>
    suspend fun getMatches(currentUserId: String): Result<List<Match>>
    suspend fun unmatch(matchId: String): Result<Unit>
}

@Singleton
class SupabaseMatchingRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : MatchingRemoteDataSource {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun processSwipe(
        targetUserId: String,
        action: SwipeActionType
    ): Result<SwipeResult> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/rpc/process_swipe")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(ProcessSwipeRequest(p_target_id = targetUserId, p_action = action.name))
                }
            },
            parser = { response ->
                val body = response.body<ProcessSwipeRpcResponse>()
                val matchedProfile = body.matchedUser?.let { dto ->
                    val birthMillis = try {
                        dateFormat.parse(dto.birthDate)?.time ?: (System.currentTimeMillis() - 25L * 365 * 24 * 3600 * 1000)
                    } catch (e: Exception) {
                        System.currentTimeMillis() - 25L * 365 * 24 * 3600 * 1000
                    }
                    UserProfile(
                        id = dto.id,
                        displayName = dto.displayName,
                        birthDateMillis = birthMillis,
                        gender = Gender.OTHER,
                        photos = dto.photos.mapIndexed { index, url ->
                            ProfilePhoto(
                                id = index.toString(),
                                userId = dto.id,
                                photoUrl = url,
                                storagePath = "",
                                displayOrder = index,
                                isPrimary = index == 0
                            )
                        }
                    )
                }

                SwipeResult(
                    isMatch = body.isMatch,
                    matchId = body.matchId,
                    matchedUser = matchedProfile
                )
            }
        )
    }

    override suspend fun getMatches(currentUserId: String): Result<List<Match>> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/matches?select=*,user1:profiles!matches_user1_id_fkey(*,profile_photos(*)),user2:profiles!matches_user2_id_fkey(*,profile_photos(*))&is_active=eq.true&order=created_at.desc")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<MatchSupabaseDto>>()
                list.mapNotNull { matchDto ->
                    val partner = if (matchDto.user1Id == currentUserId) matchDto.user2 else matchDto.user1
                    if (partner == null) return@mapNotNull null

                    val birthMillis = try {
                        dateFormat.parse(partner.birthDate)?.time ?: (System.currentTimeMillis() - 25L * 365 * 24 * 3600 * 1000)
                    } catch (e: Exception) {
                        System.currentTimeMillis() - 25L * 365 * 24 * 3600 * 1000
                    }
                    val age = DateTimeUtils.calculateAge(birthMillis)
                    val primaryPhoto = partner.photos.firstOrNull { it.isPrimary }?.photoUrl
                        ?: partner.photos.firstOrNull()?.photoUrl

                    Match(
                        id = matchDto.id,
                        matchedUserId = partner.id,
                        matchedUserName = partner.displayName,
                        matchedUserAge = age,
                        matchedUserPhotoUrl = primaryPhoto,
                        matchedUserDistanceKm = null,
                        matchedAtMillis = DateTimeUtils.parseIsoDate(matchDto.createdAt),
                        isActive = matchDto.isActive
                    )
                }
            }
        )
    }

    override suspend fun unmatch(matchId: String): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/rpc/unmatch_user")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(UnmatchRequest(p_match_id = matchId))
                }
            },
            parser = { }
        )
    }
}
