package com.aura.dating.data.location.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.data.location.remote.LocationRemoteDataSource
import com.aura.dating.domain.location.model.City
import com.aura.dating.domain.location.model.Country
import com.aura.dating.domain.location.model.Region
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationRepositoryTest {

    private val remoteDataSource: LocationRemoteDataSource = mockk()
    private lateinit var repository: LocationRepositoryImpl

    @Before
    fun setUp() {
        repository = LocationRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getCountries fetches from remote and caches result`() = runTest {
        val countries = listOf(
            Country("1", "Turkmenistan", "TM"),
            Country("2", "Turkey", "TR")
        )
        coEvery { remoteDataSource.getCountries() } returns Result.Success(countries)

        // First call - should hit remote
        val result1 = repository.getCountries(forceRefresh = false)
        assertTrue(result1 is Result.Success)
        assertEquals(2, (result1 as Result.Success).data.size)

        // Second call - should return cached result without hitting remote again
        val result2 = repository.getCountries(forceRefresh = false)
        assertTrue(result2 is Result.Success)
        assertEquals(2, (result2 as Result.Success).data.size)

        coVerify(exactly = 1) { remoteDataSource.getCountries() }
    }

    @Test
    fun `getRegions fetches and caches by countryId`() = runTest {
        val countryId = "tm-1"
        val regions = listOf(
            Region("r1", countryId, "Mary"),
            Region("r2", countryId, "Ahal")
        )
        coEvery { remoteDataSource.getRegions(countryId) } returns Result.Success(regions)

        val result1 = repository.getRegions(countryId, forceRefresh = false)
        assertTrue(result1 is Result.Success)
        assertEquals("Mary", (result1 as Result.Success).data[0].name)

        val result2 = repository.getRegions(countryId, forceRefresh = false)
        assertTrue(result2 is Result.Success)

        coVerify(exactly = 1) { remoteDataSource.getRegions(countryId) }
    }

    @Test
    fun `getCities fetches and caches by regionId`() = runTest {
        val regionId = "mary-1"
        val cities = listOf(
            City("c1", regionId, "Bayramaly"),
            City("c2", regionId, "Mary")
        )
        coEvery { remoteDataSource.getCities(regionId) } returns Result.Success(cities)

        val result1 = repository.getCities(regionId, forceRefresh = false)
        assertTrue(result1 is Result.Success)
        assertEquals("Bayramaly", (result1 as Result.Success).data[0].name)

        val result2 = repository.getCities(regionId, forceRefresh = false)
        assertTrue(result2 is Result.Success)

        coVerify(exactly = 1) { remoteDataSource.getCities(regionId) }
    }
}
