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
    @SerialName("country_name") val countryName: String? = null,
    @SerialName("region_name") val regionName: String? = null,
    @SerialName("city_name") val cityName: String? = null,
    val photos: List<PhotoDto> = emptyList(),
    val interests: List<InterestDto> = emptyList()
)

@Serializable
data class DiscoveryCandidatesRequest(
    val p_limit: Int
)

@Serializable
data class SearchCandidatesByLocationRequest(
    val p_country_id: String? = null,
    val p_region_id: String? = null,
    val p_city_id: String? = null,
    val p_min_age: Int = 18,
    val p_max_age: Int = 75,
    val p_gender: String = "ALL",
    val p_only_online: Boolean = false,
    val p_limit: Int = 20,
    val p_offset: Int = 0
)

@Serializable
data class SearchCandidatesByLocationLegacyRequest(
    val p_country_id: String? = null,
    val p_region_id: String? = null,
    val p_city_id: String? = null,
    val p_min_age: Int = 18,
    val p_max_age: Int = 75,
    val p_gender: String = "ALL",
    val p_limit: Int = 20,
    val p_offset: Int = 0
)

interface DiscoveryRemoteDataSource {
    suspend fun getCandidates(limit: Int = 20): Result<List<DiscoveryCandidate>>
    suspend fun searchCandidatesByLocation(
        countryId: String?,
        regionId: String?,
        cityId: String?,
        minAge: Int,
        maxAge: Int,
        gender: String,
        onlyOnline: Boolean = false,
        limit: Int,
        offset: Int
    ): Result<List<DiscoveryCandidate>>
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
                mapDtosToDomain(list)
            }
        )
    }

    override suspend fun searchCandidatesByLocation(
        countryId: String?,
        regionId: String?,
        cityId: String?,
        minAge: Int,
        maxAge: Int,
        gender: String,
        onlyOnline: Boolean,
        limit: Int,
        offset: Int
    ): Result<List<DiscoveryCandidate>> {
        android.util.Log.d(
            "DiscoveryRemote",
            "searchCandidatesByLocation called: country=$countryId, region=$regionId, city=$cityId, minAge=$minAge, maxAge=$maxAge, gender=$gender, onlyOnline=$onlyOnline, limit=$limit, offset=$offset"
        )

        // 1. Try modern 9-param signature (supports server-side p_only_online)
        val modernResult = clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/rpc/search_candidates_by_location")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(
                        SearchCandidatesByLocationRequest(
                            p_country_id = countryId,
                            p_region_id = regionId,
                            p_city_id = cityId,
                            p_min_age = minAge,
                            p_max_age = maxAge,
                            p_gender = gender,
                            p_only_online = onlyOnline,
                            p_limit = limit,
                            p_offset = offset
                        )
                    )
                }
            },
            parser = { response ->
                val list = response.body<List<DiscoveryCandidateDto>>()
                android.util.Log.d("DiscoveryRemote", "Modern RPC success: received ${list.size} candidates")
                val domainList = mapDtosToDomain(list)
                if (onlyOnline) domainList.filter { it.isOnline } else domainList
            }
        )

        if (modernResult is Result.Success) {
            return modernResult
        }

        val modernError = (modernResult as? Result.Error)?.error?.message
        android.util.Log.w(
            "DiscoveryRemote",
            "Modern RPC call failed ($modernError). Attempting legacy 8-param fallback..."
        )

        // 2. Fallback to legacy 8-param signature
        val legacyResult = clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/rpc/search_candidates_by_location")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(
                        SearchCandidatesByLocationLegacyRequest(
                            p_country_id = countryId,
                            p_region_id = regionId,
                            p_city_id = cityId,
                            p_min_age = minAge,
                            p_max_age = maxAge,
                            p_gender = gender,
                            p_limit = limit,
                            p_offset = offset
                        )
                    )
                }
            },
            parser = { response ->
                val list = response.body<List<DiscoveryCandidateDto>>()
                android.util.Log.d("DiscoveryRemote", "Legacy RPC success: received ${list.size} candidates")
                val domainList = mapDtosToDomain(list)
                if (onlyOnline) {
                    domainList.filter { it.isOnline }
                } else {
                    domainList
                }
            }
        )

        if (legacyResult is Result.Error) {
            android.util.Log.e("DiscoveryRemote", "Legacy RPC fallback also failed: ${legacyResult.error.message}")
        }

        return legacyResult
    }

    private fun mapDtosToDomain(list: List<DiscoveryCandidateDto>): List<DiscoveryCandidate> {
        return list.map { dto ->
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
                countryName = dto.countryName,
                regionName = dto.regionName,
                cityName = dto.cityName,
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
}
