package com.aura.dating.domain.location.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.location.model.City
import com.aura.dating.domain.location.model.Country
import com.aura.dating.domain.location.model.Region

interface LocationRepository {
    suspend fun getCountries(forceRefresh: Boolean = false): Result<List<Country>>
    suspend fun getRegions(countryId: String, forceRefresh: Boolean = false): Result<List<Region>>
    suspend fun getCities(regionId: String, forceRefresh: Boolean = false): Result<List<City>>
}
