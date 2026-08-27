package com.aura.dating.data.discovery.remote

import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.data.profile.remote.InterestDto
import com.aura.dating.data.profile.remote.PhotoDto
import com.aura.dating.domain.discovery.model.DiscoveryCandidate
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.Interest
import com.aura.dating.domain.profile.model.ProfilePhoto
import io.ktor.client.call.body
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
data class DiscoveryCandidateDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("birth_date") val birthDate: String,
    val gender: String,
    val bio: String? = null,
    @SerialName("distance_km") val distanceKm: Double? = null,
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("last_seen_at") val lastSeenAt: String? = null,
    val photos: List<PhotoDto> = emptyList(),
    val interests: List<InterestDto> = emptyList()
)

@Serializable
data class DiscoveryCandidatesRequest(
    val p_limit: Int
)

interface DiscoveryRemoteDataSource {
    suspend fun getCandidates(limit: Int = 20): Result<List<DiscoveryCandidate>>
}

@Singleton
class SupabaseDiscoveryRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : DiscoveryRemoteDataSource {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun getCandidates(limit: Int): Result<List<DiscoveryCandidate>> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/rpc/get_discovery_candidates")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(DiscoveryCandidatesRequest(p_limit = limit))
                }
            },
            parser = { response ->
                val list = response.body<List<DiscoveryCandidateDto>>()
                list.map { dto ->
                    val birthMillis = try {
                        dateFormat.parse(dto.birthDate)?.time ?: (System.currentTimeMillis() - 25L * 365 * 24 * 3600 * 1000)
                    } catch (e: Exception) {
                        System.currentTimeMillis() - 25L * 365 * 24 * 3600 * 1000
                    }
                    val age = DateTimeUtils.calculateAge(birthMillis)

                    DiscoveryCandidate(
                        id = dto.id,
                        displayName = dto.displayName,
                        birthDateMillis = birthMillis,
                        age = age,
                        gender = try { Gender.valueOf(dto.gender) } catch (e: Exception) { Gender.OTHER },
                        bio = dto.bio,
                        distanceKm = dto.distanceKm,
                        isOnline = dto.isOnline,
                        lastSeenAtMillis = dto.lastSeenAt?.let { DateTimeUtils.parseIsoDate(it) } ?: System.currentTimeMillis(),
                        photos = dto.photos.map {
                            ProfilePhoto(
                                id = it.id,
                                userId = it.userId,
                                photoUrl = it.photoUrl,
                                storagePath = it.storagePath,
                                displayOrder = it.displayOrder,
                                isPrimary = it.isPrimary
                            )
                        },
                        interests = dto.interests.map {
                            Interest(it.id, it.name, it.category, it.icon)
                        }
                    )
                }
            }
        )
    }
}
