package com.aura.dating.data.location.remote

import com.aura.dating.core.common.result.Result
import com.aura.dating.core.network.SupabaseClientProvider
import com.aura.dating.domain.location.model.City
import com.aura.dating.domain.location.model.Country
import com.aura.dating.domain.location.model.Region
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CountryDto(
    val id: String,
    val name: String,
    val code: String? = null
)

@Serializable
data class RegionDto(
    val id: String,
    @SerialName("country_id") val countryId: String,
    val name: String
)

@Serializable
data class CityDto(
    val id: String,
    @SerialName("region_id") val regionId: String,
    val name: String
)

interface LocationRemoteDataSource {
    suspend fun getCountries(): Result<List<Country>>
    suspend fun getRegions(countryId: String): Result<List<Region>>
    suspend fun getCities(regionId: String): Result<List<City>>
}

@Singleton
class SupabaseLocationRemoteDataSource @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) : LocationRemoteDataSource {

    override suspend fun getCountries(): Result<List<Country>> {
        android.util.Log.d("LocationDataSource", "Fetching countries from: ${clientProvider.baseUrl}/rest/v1/countries")
        val result = clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/countries?select=*&order=name.asc")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<CountryDto>>()
                android.util.Log.d("LocationDataSource", "Fetched ${list.size} countries from Supabase")
                list.map { Country(id = it.id, name = it.name, code = it.code) }
            }
        )
        if (result is Result.Error) {
            android.util.Log.e("LocationDataSource", "Failed to fetch countries: ${result.error.message}")
        }
        return result
    }

    override suspend fun getRegions(countryId: String): Result<List<Region>> {
        android.util.Log.d("LocationDataSource", "Fetching regions for country: $countryId")
        val result = clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/regions?country_id=eq.$countryId&select=*&order=name.asc")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<RegionDto>>()
                android.util.Log.d("LocationDataSource", "Fetched ${list.size} regions from Supabase")
                list.map { Region(id = it.id, countryId = it.countryId, name = it.name) }
            }
        )
        if (result is Result.Error) {
            android.util.Log.e("LocationDataSource", "Failed to fetch regions: ${result.error.message}")
        }
        return result
    }

    override suspend fun getCities(regionId: String): Result<List<City>> {
        android.util.Log.d("LocationDataSource", "Fetching cities for region: $regionId")
        val result = clientProvider.safeApiCall(
            block = { client, headers ->
                client.get {
                    url("${clientProvider.baseUrl}/rest/v1/cities?region_id=eq.$regionId&select=*&order=name.asc")
                    headers(this)
                }
            },
            parser = { response ->
                val list = response.body<List<CityDto>>()
                android.util.Log.d("LocationDataSource", "Fetched ${list.size} cities from Supabase")
                list.map { City(id = it.id, regionId = it.regionId, name = it.name) }
            }
        )
        if (result is Result.Error) {
            android.util.Log.e("LocationDataSource", "Failed to fetch cities: ${result.error.message}")
        }
        return result
    }
}
