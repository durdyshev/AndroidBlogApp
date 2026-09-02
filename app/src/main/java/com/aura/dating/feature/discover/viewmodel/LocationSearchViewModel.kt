package com.aura.dating.feature.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationSearchUiState(
    val countries: List<Country> = emptyList(),
    val regions: List<Region> = emptyList(),
    val cities: List<City> = emptyList(),
    val selectedCountry: Country? = null,
    val selectedRegion: Region? = null,
    val selectedCity: City? = null,
    val minAge: Int = 18,
    val maxAge: Int = 100,
    val gender: String = "ALL", // "ALL", "WOMEN", "MEN", "NON_BINARY"
    val isLoadingLocations: Boolean = false,
    val isSearching: Boolean = false,
    val isPaginating: Boolean = false,
    val results: List<DiscoveryCandidate> = emptyList(),
    val hasMore: Boolean = true,
    val currentOffset: Int = 0,
    val errorMessage: String? = null,
    val matchResult: SwipeResult? = null
)

sealed interface LocationSearchEvent {
    data class NavigateToResults(
        val title: String,
        val subtitle: String
    ) : LocationSearchEvent
    data class ShowToast(val message: String) : LocationSearchEvent
}

@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val discoveryRepository: DiscoveryRepository,
    private val swipeUserUseCase: SwipeUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationSearchUiState())
    val uiState: StateFlow<LocationSearchUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LocationSearchEvent>()
    val eventFlow: SharedFlow<LocationSearchEvent> = _eventFlow.asSharedFlow()

    private val pageSize = 20

    init {
        loadCountries()
    }

    fun loadCountries(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            android.util.Log.d("LocationSearchVM", "loadCountries called (forceRefresh=$forceRefresh)")
            _uiState.value = _uiState.value.copy(isLoadingLocations = true, errorMessage = null)
            val result = locationRepository.getCountries(forceRefresh = forceRefresh)
            if (result is Result.Success) {
                android.util.Log.d("LocationSearchVM", "Loaded ${result.data.size} countries")
                _uiState.value = _uiState.value.copy(
                    countries = result.data,
                    isLoadingLocations = false
                )
            } else if (result is Result.Error) {
                android.util.Log.e("LocationSearchVM", "Error loading countries: ${result.error.message}")
                _uiState.value = _uiState.value.copy(
                    isLoadingLocations = false,
                    errorMessage = result.error.message
                )
            }
        }
    }

    fun selectCountry(country: Country?) {
        if (_uiState.value.selectedCountry?.id == country?.id) return
        _uiState.value = _uiState.value.copy(
            selectedCountry = country,
            selectedRegion = null,
            selectedCity = null,
            regions = emptyList(),
            cities = emptyList()
        )
        if (country != null) {
            loadRegions(country.id)
        }
    }

    fun loadRegions(countryId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            android.util.Log.d("LocationSearchVM", "loadRegions called for countryId: $countryId")
            _uiState.value = _uiState.value.copy(isLoadingLocations = true)
            val result = locationRepository.getRegions(countryId, forceRefresh = forceRefresh)
            if (result is Result.Success) {
                android.util.Log.d("LocationSearchVM", "Loaded ${result.data.size} regions")
                _uiState.value = _uiState.value.copy(
                    regions = result.data,
                    isLoadingLocations = false
                )
            } else if (result is Result.Error) {
                android.util.Log.e("LocationSearchVM", "Error loading regions: ${result.error.message}")
                _uiState.value = _uiState.value.copy(isLoadingLocations = false)
            }
        }
    }

    fun selectRegion(region: Region?) {
        if (_uiState.value.selectedRegion?.id == region?.id) return
        _uiState.value = _uiState.value.copy(
            selectedRegion = region,
            selectedCity = null,
            cities = emptyList()
        )
        if (region != null) {
            loadCities(region.id)
        }
    }

    fun loadCities(regionId: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            android.util.Log.d("LocationSearchVM", "loadCities called for regionId: $regionId")
            _uiState.value = _uiState.value.copy(isLoadingLocations = true)
            val result = locationRepository.getCities(regionId, forceRefresh = forceRefresh)
            if (result is Result.Success) {
                android.util.Log.d("LocationSearchVM", "Loaded ${result.data.size} cities")
                _uiState.value = _uiState.value.copy(
                    cities = result.data,
                    isLoadingLocations = false
                )
            } else if (result is Result.Error) {
                android.util.Log.e("LocationSearchVM", "Error loading cities: ${result.error.message}")
                _uiState.value = _uiState.value.copy(isLoadingLocations = false)
            }
        }
    }

    fun selectCity(city: City?) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
    }

    fun onAgeRangeChange(min: Int, max: Int) {
        _uiState.value = _uiState.value.copy(
            minAge = min.coerceAtLeast(18),
            maxAge = max.coerceAtLeast(min)
        )
    }

    fun onGenderChange(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun removeCountryFilter() {
        selectCountry(null)
    }

    fun removeRegionFilter() {
        selectRegion(null)
    }

    fun removeCityFilter() {
        selectCity(null)
    }

    fun resetFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCountry = null,
            selectedRegion = null,
            selectedCity = null,
            regions = emptyList(),
            cities = emptyList(),
            minAge = 18,
            maxAge = 100,
            gender = "ALL"
        )
    }

    fun executeSearch(isNewSearch: Boolean = true) {
        viewModelScope.launch {
            if (isNewSearch) {
                _uiState.value = _uiState.value.copy(
                    isSearching = true,
                    currentOffset = 0,
                    results = emptyList(),
                    hasMore = true,
                    errorMessage = null
                )
            } else {
                if (!_uiState.value.hasMore || _uiState.value.isPaginating) return@launch
                _uiState.value = _uiState.value.copy(isPaginating = true)
            }

            val offset = if (isNewSearch) 0 else _uiState.value.currentOffset

            val result = discoveryRepository.searchCandidatesByLocation(
                countryId = _uiState.value.selectedCountry?.id,
                regionId = _uiState.value.selectedRegion?.id,
                cityId = _uiState.value.selectedCity?.id,
                minAge = _uiState.value.minAge,
                maxAge = _uiState.value.maxAge,
                gender = _uiState.value.gender,
                limit = pageSize,
                offset = offset
            )

            if (result is Result.Success) {
                val newItems = result.data
                val updatedList = if (isNewSearch) newItems else _uiState.value.results + newItems
                _uiState.value = _uiState.value.copy(
                    results = updatedList,
                    currentOffset = offset + newItems.size,
                    hasMore = newItems.size >= pageSize,
                    isSearching = false,
                    isPaginating = false,
                    errorMessage = null
                )

                if (isNewSearch) {
                    val title = buildSearchTitle()
                    val subtitle = buildSearchSubtitle()
                    _eventFlow.emit(LocationSearchEvent.NavigateToResults(title, subtitle))
                }
            } else if (result is Result.Error) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    isPaginating = false,
                    errorMessage = result.error.message
                )
            }
        }
    }

    fun loadNextPage() {
        if (!_uiState.value.isSearching && !_uiState.value.isPaginating && _uiState.value.hasMore) {
            executeSearch(isNewSearch = false)
        }
    }

    fun swipeCandidate(candidate: DiscoveryCandidate, action: SwipeActionType) {
        viewModelScope.launch {
            // Remove candidate locally from results list for responsive UX
            val updated = _uiState.value.results.filterNot { it.id == candidate.id }
            _uiState.value = _uiState.value.copy(results = updated)

            val result = swipeUserUseCase(candidate.id, action)
            if (result is Result.Success && result.data.isMatch) {
                _uiState.value = _uiState.value.copy(matchResult = result.data)
            }
        }
    }

    fun clearMatchDialog() {
        _uiState.value = _uiState.value.copy(matchResult = null)
    }

    fun buildSearchTitle(): String {
        return when {
            _uiState.value.selectedCity != null -> "People in ${_uiState.value.selectedCity?.name}"
            _uiState.value.selectedRegion != null -> "People in ${_uiState.value.selectedRegion?.name}"
            _uiState.value.selectedCountry != null -> "People in ${_uiState.value.selectedCountry?.name}"
            else -> "People Worldwide"
        }
    }

    fun buildSearchSubtitle(): String {
        val genderLabel = when (_uiState.value.gender) {
            "WOMEN" -> "Women"
            "MEN" -> "Men"
            "NON_BINARY" -> "Non-Binary"
            else -> "Everyone"
        }
        val ageLabel = "${_uiState.value.minAge}–${_uiState.value.maxAge}"
        return "$ageLabel · $genderLabel"
    }
}
