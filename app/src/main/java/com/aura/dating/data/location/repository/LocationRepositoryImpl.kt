package com.aura.dating.data.location.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.data.location.remote.LocationRemoteDataSource
import com.aura.dating.domain.location.model.City
import com.aura.dating.domain.location.model.Country
import com.aura.dating.domain.location.model.Region
import com.aura.dating.domain.location.repository.LocationRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val remoteDataSource: LocationRemoteDataSource
) : LocationRepository {

    private var cachedCountries: List<Country>? = null
    private val cachedRegionsByCountry = ConcurrentHashMap<String, List<Region>>()
    private val cachedCitiesByRegion = ConcurrentHashMap<String, List<City>>()

    override suspend fun getCountries(forceRefresh: Boolean): Result<List<Country>> {
        if (!forceRefresh && !cachedCountries.isNullOrEmpty()) {
            return Result.Success(cachedCountries!!)
        }
        val result = remoteDataSource.getCountries()
        if (result is Result.Success && result.data.isNotEmpty()) {
            cachedCountries = result.data
        }
        return result
    }

    override suspend fun getRegions(countryId: String, forceRefresh: Boolean): Result<List<Region>> {
        val cached = cachedRegionsByCountry[countryId]
        if (!forceRefresh && !cached.isNullOrEmpty()) {
            return Result.Success(cached)
        }
        val result = remoteDataSource.getRegions(countryId)
        if (result is Result.Success && result.data.isNotEmpty()) {
            cachedRegionsByCountry[countryId] = result.data
        }
        return result
    }

    override suspend fun getCities(regionId: String, forceRefresh: Boolean): Result<List<City>> {
        val cached = cachedCitiesByRegion[regionId]
        if (!forceRefresh && !cached.isNullOrEmpty()) {
            return Result.Success(cached)
        }
        val result = remoteDataSource.getCities(regionId)
        if (result is Result.Success && result.data.isNotEmpty()) {
            cachedCitiesByRegion[regionId] = result.data
        }
        return result
    }
}
