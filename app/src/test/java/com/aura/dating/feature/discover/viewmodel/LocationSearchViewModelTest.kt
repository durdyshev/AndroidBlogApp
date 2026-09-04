package com.aura.dating.feature.discover.viewmodel

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.discovery.model.DiscoveryCandidate
import com.aura.dating.domain.discovery.repository.DiscoveryRepository
import com.aura.dating.domain.location.model.City
import com.aura.dating.domain.location.model.Country
import com.aura.dating.domain.location.model.Region
import com.aura.dating.domain.location.repository.LocationRepository
import com.aura.dating.domain.matching.model.SwipeActionType
import com.aura.dating.domain.matching.model.SwipeResult
import com.aura.dating.domain.matching.usecase.SwipeUserUseCase
import com.aura.dating.domain.profile.model.Gender
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic

@OptIn(ExperimentalCoroutinesApi::class)
class LocationSearchViewModelTest {

    private val locationRepository: LocationRepository = mockk(relaxed = true)
    private val discoveryRepository: DiscoveryRepository = mockk(relaxed = true)
    private val swipeUserUseCase: SwipeUserUseCase = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LocationSearchViewModel

    private val sampleCountry = Country("c-tm", "Turkmenistan", "TM")
    private val sampleRegion = Region("r-mary", "c-tm", "Mary")
    private val sampleCity = City("ct-bayramaly", "r-mary", "Bayramaly")

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.v(any(), any()) } returns 0

        Dispatchers.setMain(testDispatcher)
        coEvery { locationRepository.getCountries(any()) } returns Result.Success(listOf(sampleCountry))
        coEvery { locationRepository.getRegions(sampleCountry.id, any()) } returns Result.Success(listOf(sampleRegion))
        coEvery { locationRepository.getCities(sampleRegion.id, any()) } returns Result.Success(listOf(sampleCity))

        viewModel = LocationSearchViewModel(
            locationRepository = locationRepository,
            discoveryRepository = discoveryRepository,
            swipeUserUseCase = swipeUserUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `init loads countries automatically`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.countries.size)
        assertEquals("Turkmenistan", state.countries[0].name)
    }

    @Test
    fun `hierarchical selection works correctly and resets downstream selections`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // Select Country
        viewModel.selectCountry(sampleCountry)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(sampleCountry, viewModel.uiState.value.selectedCountry)
        assertEquals(1, viewModel.uiState.value.regions.size)

        // Select Region
        viewModel.selectRegion(sampleRegion)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(sampleRegion, viewModel.uiState.value.selectedRegion)
        assertEquals(1, viewModel.uiState.value.cities.size)

        // Select City
        viewModel.selectCity(sampleCity)
        assertEquals(sampleCity, viewModel.uiState.value.selectedCity)

        // Selecting a new country resets region and city
        val turkey = Country("c-tr", "Turkey", "TR")
        viewModel.selectCountry(turkey)
        assertEquals(turkey, viewModel.uiState.value.selectedCountry)
        assertNull(viewModel.uiState.value.selectedRegion)
        assertNull(viewModel.uiState.value.selectedCity)
    }

    @Test
    fun `executeSearch calls discoveryRepository with selected filters`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectCountry(sampleCountry)
        viewModel.selectRegion(sampleRegion)
        viewModel.selectCity(sampleCity)
        viewModel.onAgeRangeChange(18, 25)
        viewModel.onGenderChange("WOMEN")
        viewModel.onOnlineOnlyChange(true)

        val candidates = listOf(
            DiscoveryCandidate(
                id = "cand-1",
                displayName = "Leyla",
                birthDateMillis = System.currentTimeMillis() - 22L * 365 * 24 * 3600 * 1000,
                age = 22,
                gender = Gender.WOMAN,
                bio = "Living in Bayramaly",
                distanceKm = null,
                isOnline = true,
                lastSeenAtMillis = System.currentTimeMillis(),
                countryName = "Turkmenistan",
                regionName = "Mary",
                cityName = "Bayramaly",
                photos = emptyList(),
                interests = emptyList()
            )
        )

        coEvery {
            discoveryRepository.searchCandidatesByLocation(
                countryId = sampleCountry.id,
                regionId = sampleRegion.id,
                cityId = sampleCity.id,
                minAge = 18,
                maxAge = 25,
                gender = "WOMEN",
                onlyOnline = true,
                limit = 20,
                offset = 0
            )
        } returns Result.Success(candidates)

        viewModel.executeSearch(isNewSearch = true)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("Leyla", state.results[0].displayName)
        assertTrue(state.onlyOnline)
        assertFalse(state.isSearching)
        assertNull(state.errorMessage)

        coVerify {
            discoveryRepository.searchCandidatesByLocation(
                countryId = sampleCountry.id,
                regionId = sampleRegion.id,
                cityId = sampleCity.id,
                minAge = 18,
                maxAge = 25,
                gender = "WOMEN",
                onlyOnline = true,
                limit = 20,
                offset = 0
            )
        }
    }

    @Test
    fun `resetFilters resets onlineOnly and other criteria to defaults`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectCountry(sampleCountry)
        viewModel.onOnlineOnlyChange(true)
        viewModel.onGenderChange("MEN")
        viewModel.onAgeRangeChange(20, 30)

        assertTrue(viewModel.uiState.value.onlyOnline)
        assertEquals("MEN", viewModel.uiState.value.gender)

        viewModel.resetFilters()

        val state = viewModel.uiState.value
        assertFalse(state.onlyOnline)
        assertNull(state.selectedCountry)
        assertEquals(18, state.minAge)
        assertEquals(75, state.maxAge)
        assertEquals("ALL", state.gender)
    }

    @Test
    fun `swipeCandidate removes candidate from results list locally`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val candidate = DiscoveryCandidate(
            id = "cand-1",
            displayName = "Leyla",
            birthDateMillis = System.currentTimeMillis() - 22L * 365 * 24 * 3600 * 1000,
            age = 22,
            gender = Gender.WOMAN,
            bio = null,
            distanceKm = null,
            isOnline = true,
            lastSeenAtMillis = System.currentTimeMillis(),
            photos = emptyList(),
            interests = emptyList()
        )

        coEvery {
            discoveryRepository.searchCandidatesByLocation(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.Success(listOf(candidate))

        coEvery {
            swipeUserUseCase("cand-1", SwipeActionType.LIKE)
        } returns Result.Success(SwipeResult(isMatch = false))

        viewModel.executeSearch(isNewSearch = true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.results.size)

        // When
        viewModel.swipeCandidate(candidate, SwipeActionType.LIKE)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then candidate should be removed immediately from list
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }
}
