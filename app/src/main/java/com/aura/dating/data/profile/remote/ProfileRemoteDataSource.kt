package com.aura.dating.data.profile.remote

import com.aura.dating.core.common.result.Result
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.GenderPreference
import com.aura.dating.domain.profile.model.Interest
import com.aura.dating.domain.profile.model.ProfilePhoto
import com.aura.dating.domain.profile.model.UserPreferences
import com.aura.dating.domain.profile.model.UserProfile
import io.ktor.client.call.body
import io.ktor.client.request.delete
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ProfileDto(
    val id: String = "",
    @SerialName("display_name") val displayName: String = "",
    @SerialName("birth_date") val birthDate: String = "2000-01-01",
    val gender: String = "OTHER",
    val bio: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("profile_photos") val photos: List<PhotoDto> = emptyList(),
    @SerialName("user_interests") val userInterests: List<UserInterestDto> = emptyList(),
    @SerialName("user_preferences") val preferences: List<PreferencesDto> = emptyList()
)

@Serializable
data class PhotoDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("photo_url") val photoUrl: String = "",
    @SerialName("storage_path") val storagePath: String = "",
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("is_primary") val isPrimary: Boolean = false
)

@Serializable
data class InterestDto(
    val id: String,
    val name: String,
    val category: String,
    val icon: String? = null
)

@Serializable
data class UserInterestDto(
    val interests: InterestDto? = null
)

@Serializable
data class PreferencesDto(
    @SerialName("user_id") val userId: String,
    @SerialName("min_age") val minAge: Int = 18,
    @SerialName("max_age") val maxAge: Int = 50,
    @SerialName("interested_in_gender") val interestedInGender: String = "ALL",
    @SerialName("max_distance_km") val maxDistanceKm: Int = 50,
    @SerialName("show_only_online") val showOnlyOnline: Boolean = false
)

@Serializable
data class UpdateProfileRequest(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("birth_date") val birthDate: String,
    val gender: String,
    val bio: String? = null,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class UserInterestInsertRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("interest_id") val interestId: String
)

@Serializable
data class InsertPhotoRequest(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("photo_url") val photoUrl: String,
    @SerialName("storage_path") val storagePath: String,
    @SerialName("is_primary") val isPrimary: Boolean
)

@Serializable
data class UpdateDisplayOrderRequest(
    @SerialName("display_order") val displayOrder: Int
)

@Serializable
data class UpdatePrimaryPhotoRequest(
    @SerialName("is_primary") val isPrimary: Boolean
)

@Serializable
data class UpdatePreferencesRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("min_age") val minAge: Int,
    @SerialName("max_age") val maxAge: Int,
    @SerialName("interested_in_gender") val interestedInGender: String,
    @SerialName("max_distance_km") val maxDistanceKm: Int,
    @SerialName("show_only_online") val showOnlyOnline: Boolean
)

@Serializable
data class UpdateLocationRequest(
    val p_latitude: Double,
    val p_longitude: Double
)

interface ProfileRemoteDataSource {
    suspend fun getProfile(userId: String): Result<UserProfile>
    suspend fun updateProfile(
        userId: String,
        displayName: String,
        birthDateMillis: Long,
        gender: Gender,
        bio: String?,
        interestIds: List<String>
    ): Result<UserProfile>
    suspend fun uploadPhoto(userId: String, imageBytes: ByteArray, isPrimary: Boolean): Result<ProfilePhoto>
    suspend fun deletePhoto(photoId: String, storagePath: String): Result<Unit>
    suspend fun reorderPhotos(photoIds: List<String>): Result<Unit>
    suspend fun setPrimaryPhoto(photoId: String): Result<Unit>
    suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences>
    suspend fun updateLocation(latitude: Double, longitude: Double): Result<Unit>
    suspend fun getAllInterests(): Result<List<Interest>>
}

@Singleton
class SupabaseProfileRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : ProfileRemoteDataSource {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun getProfile(userId: String): Result<UserProfile> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/profiles?id=eq.$userId&select=*,profile_photos(*),user_interests(interests(*))")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<ProfileDto>>()
                val dto = requireNotNull(list.firstOrNull()) { "Profile not found" }
                mapDtoToDomain(dto)
            }
        )
    }

    override suspend fun updateProfile(
        userId: String,
        displayName: String,
        birthDateMillis: Long,
        gender: Gender,
        bio: String?,
        interestIds: List<String>
    ): Result<UserProfile> {
        val birthDateStr = dateFormat.format(Date(birthDateMillis))
        val requestBody = UpdateProfileRequest(
            id = userId,
            displayName = displayName,
            birthDate = birthDateStr,
            gender = gender.name,
            bio = bio,
            updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        )

        val profileResult = clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/profiles")
                    contentType(ContentType.Application.Json)
                    header("Prefer", "resolution=merge-duplicates,return=representation")
                    headers(this)
                    setBody(requestBody)
                }
            },
            parser = { response ->
                val list = response.body<List<ProfileDto>>()
                requireNotNull(list.firstOrNull())
            }
        )

        if (profileResult is Result.Success) {
            // Update interests mapping
            clientProvider.safeApiCall(
                block = { client, headers ->
                    client.delete {
                        url("${clientProvider.baseUrl}/rest/v1/user_interests?user_id=eq.$userId")
                        headers(this)
                    }
                },
                parser = { }
            )

            if (interestIds.isNotEmpty()) {
                val userInterestsRows = interestIds.map { UserInterestInsertRequest(userId = userId, interestId = it) }
                clientProvider.safeApiCall(
                    block = { client, headers ->
                        client.post {
                            url("${clientProvider.baseUrl}/rest/v1/user_interests")
                            contentType(ContentType.Application.Json)
                            headers(this)
                            setBody(userInterestsRows)
                        }
                    },
                    parser = { }
                )
            }

            return getProfile(userId)
        }

        return Result.Error((profileResult as Result.Error).error)
    }

    override suspend fun uploadPhoto(
        userId: String,
        imageBytes: ByteArray,
        isPrimary: Boolean
    ): Result<ProfilePhoto> {
        val photoId = UUID.randomUUID().toString()
        val storagePath = "$userId/profile/$photoId.webp"
        val publicUrl = "${clientProvider.baseUrl}/storage/v1/object/public/profile-photos/$storagePath"

        // 1. Upload to Supabase Storage
        val uploadResult = clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/storage/v1/object/profile-photos/$storagePath")
                    contentType(ContentType("image", "webp"))
                    headers(this)
                    setBody(imageBytes)
                }
            },
            parser = { }
        )

        if (uploadResult is Result.Error) {
            return Result.Error(uploadResult.error)
        }

        // 2. Insert record into profile_photos table
        val insertBody = InsertPhotoRequest(
            id = photoId,
            userId = userId,
            photoUrl = publicUrl,
            storagePath = storagePath,
            isPrimary = isPrimary
        )

        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/profile_photos")
                    contentType(ContentType.Application.Json)
                    header("Prefer", "return=representation")
                    headers(this)
                    setBody(insertBody)
                }
            },
            parser = { response ->
                val list = response.body<List<PhotoDto>>()
                val dto = requireNotNull(list.firstOrNull())
                ProfilePhoto(
                    id = dto.id,
                    userId = dto.userId,
                    photoUrl = dto.photoUrl,
                    storagePath = dto.storagePath,
                    displayOrder = dto.displayOrder,
                    isPrimary = dto.isPrimary
                )
            }
        )
    }

    override suspend fun deletePhoto(photoId: String, storagePath: String): Result<Unit> {
        clientProvider.safeApiCall(
            block = { client, headers ->
                client.delete {
                    url("${clientProvider.baseUrl}/storage/v1/object/profile-photos/$storagePath")
                    headers(this)
                }
            },
            parser = { }
        )

        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.delete {
                    url("${clientProvider.baseUrl}/rest/v1/profile_photos?id=eq.$photoId")
                    headers(this)
                }
            },
            parser = { }
        )
    }

    override suspend fun reorderPhotos(photoIds: List<String>): Result<Unit> {
        photoIds.forEachIndexed { index, id ->
            clientProvider.safeApiCall(
                block = { client, headers ->
                    client.patch {
                        url("${clientProvider.baseUrl}/rest/v1/profile_photos?id=eq.$id")
                        contentType(ContentType.Application.Json)
                        headers(this)
                        setBody(UpdateDisplayOrderRequest(displayOrder = index))
                    }
                },
                parser = { }
            )
        }
        return Result.Success(Unit)
    }

    override suspend fun setPrimaryPhoto(photoId: String): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.patch {
                    url("${clientProvider.baseUrl}/rest/v1/profile_photos?id=eq.$photoId")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(UpdatePrimaryPhotoRequest(isPrimary = true))
                }
            },
            parser = { }
        )
    }

    override suspend fun updatePreferences(preferences: UserPreferences): Result<UserPreferences> {
        val requestBody = UpdatePreferencesRequest(
            userId = preferences.userId,
            minAge = preferences.minAge,
            maxAge = preferences.maxAge,
            interestedInGender = preferences.interestedInGender.name,
            maxDistanceKm = preferences.maxDistanceKm,
            showOnlyOnline = preferences.showOnlyOnline
        )

        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/user_preferences")
                    contentType(ContentType.Application.Json)
                    header("Prefer", "resolution=merge-duplicates,return=representation")
                    headers(this)
                    setBody(requestBody)
                }
            },
            parser = { response ->
                val list = response.body<List<PreferencesDto>>()
                val dto = requireNotNull(list.firstOrNull())
                UserPreferences(
                    userId = dto.userId,
                    minAge = dto.minAge,
                    maxAge = dto.maxAge,
                    interestedInGender = try { GenderPreference.valueOf(dto.interestedInGender) } catch (e: Exception) { GenderPreference.ALL },
                    maxDistanceKm = dto.maxDistanceKm,
                    showOnlyOnline = dto.showOnlyOnline
                )
            }
        )
    }

    override suspend fun updateLocation(latitude: Double, longitude: Double): Result<Unit> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.post {
                    url("${clientProvider.baseUrl}/rest/v1/rpc/update_user_location")
                    contentType(ContentType.Application.Json)
                    headers(this)
                    setBody(UpdateLocationRequest(p_latitude = latitude, p_longitude = longitude))
                }
            },
            parser = { }
        )
    }

    override suspend fun getAllInterests(): Result<List<Interest>> {
        return clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/interests?select=*")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<InterestDto>>()
                list.map { Interest(it.id, it.name, it.category, it.icon) }
            }
        )
    }

    private fun mapDtoToDomain(dto: ProfileDto): UserProfile {
        val birthMillis = try {
            dateFormat.parse(dto.birthDate)?.time ?: (System.currentTimeMillis() - 25L * 365 * 24 * 3600 * 1000)
        } catch (e: Exception) {
            System.currentTimeMillis() - 25L * 365 * 24 * 3600 * 1000
        }

        return UserProfile(
            id = dto.id,
            displayName = dto.displayName,
            birthDateMillis = birthMillis,
            gender = try { Gender.valueOf(dto.gender) } catch (e: Exception) { Gender.OTHER },
            bio = dto.bio,
            photos = dto.photos.map {
                ProfilePhoto(
                    id = it.id,
                    userId = it.userId,
                    photoUrl = it.photoUrl,
                    storagePath = it.storagePath,
                    displayOrder = it.displayOrder,
                    isPrimary = it.isPrimary
                )
            }.sortedBy { it.displayOrder },
            interests = dto.userInterests.mapNotNull { it.interests }.map {
                Interest(it.id, it.name, it.category, it.icon)
            },
            preferences = dto.preferences.firstOrNull()?.let {
                UserPreferences(
                    userId = it.userId,
                    minAge = it.minAge,
                    maxAge = it.maxAge,
                    interestedInGender = try { GenderPreference.valueOf(it.interestedInGender) } catch (e: Exception) { GenderPreference.ALL },
                    maxDistanceKm = it.maxDistanceKm,
                    showOnlyOnline = it.showOnlyOnline
                )
            },
            isOnline = dto.isOnline
        )
    }
}
