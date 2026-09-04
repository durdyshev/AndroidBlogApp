package com.aura.dating.feature.profile_creation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.ImageCompressor
import com.aura.dating.domain.location.model.City
import com.aura.dating.domain.location.model.Country
import com.aura.dating.domain.location.model.Region
import com.aura.dating.domain.location.repository.LocationRepository
import com.aura.dating.domain.profile.model.Gender
import com.aura.dating.domain.profile.model.GenderPreference
import com.aura.dating.domain.profile.model.Interest
import com.aura.dating.domain.profile.model.ProfilePhoto
import com.aura.dating.domain.profile.model.UserPreferences
import com.aura.dating.domain.profile.model.UserProfile
import com.aura.dating.domain.profile.usecase.DeletePhotoUseCase
import com.aura.dating.domain.profile.usecase.GetInterestsUseCase
import com.aura.dating.domain.profile.usecase.GetMyProfileUseCase
import com.aura.dating.domain.profile.usecase.UpdatePreferencesUseCase
import com.aura.dating.domain.profile.usecase.UpdateProfileUseCase
import com.aura.dating.domain.profile.usecase.UploadPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ProfileCreationUiState(
    val displayName: String = "",
    val birthDateMillis: Long = Calendar.getInstance().apply { add(Calendar.YEAR, -22) }.timeInMillis,
    val gender: Gender = Gender.MAN,
    val bio: String = "",
    val selectedCountry: Country? = null,
    val selectedRegion: Region? = null,
    val selectedCity: City? = null,
    val countries: List<Country> = emptyList(),
    val regions: List<Region> = emptyList(),
    val cities: List<City> = emptyList(),
    val isLoadingLocations: Boolean = false,
    val photos: List<ProfilePhoto> = emptyList(),
    val availableInterests: List<Interest> = emptyList(),
    val selectedInterestIds: Set<String> = emptySet(),
    val minAgePreference: Int = 18,
    val maxAgePreference: Int = 35,
    val genderPreference: GenderPreference = GenderPreference.ALL,
    val maxDistanceKm: Int = 50,
    val isLoading: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ProfileCreationEvent {
    data object NavigateToAddPhotos : ProfileCreationEvent
    data object NavigateToSelectInterests : ProfileCreationEvent
    data object NavigateToDatingPreferences : ProfileCreationEvent
    data object NavigateToMain : ProfileCreationEvent
}

@HiltViewModel
class CreateProfileViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val getInterestsUseCase: GetInterestsUseCase,
    private val updatePreferencesUseCase: UpdatePreferencesUseCase,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileCreationUiState())
    val uiState: StateFlow<ProfileCreationUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileCreationEvent>()
    val eventFlow: SharedFlow<ProfileCreationEvent> = _eventFlow.asSharedFlow()

    init {
        loadInitialData()
        loadCountries()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val interestsResult = getInterestsUseCase()
            if (interestsResult is Result.Success) {
                _uiState.value = _uiState.value.copy(availableInterests = interestsResult.data)
            }

            val profileResult = getMyProfileUseCase()
            if (profileResult is Result.Success) {
                val profile = profileResult.data
                val country = if (profile.countryId != null) Country(profile.countryId, profile.countryName ?: "") else null
                val region = if (profile.regionId != null && profile.countryId != null) Region(profile.regionId, profile.countryId, profile.regionName ?: "") else null
                val city = if (profile.cityId != null && profile.regionId != null) City(profile.cityId, profile.regionId, profile.cityName ?: "") else null

                _uiState.value = _uiState.value.copy(
                    displayName = profile.displayName,
                    birthDateMillis = profile.birthDateMillis,
                    gender = profile.gender,
                    bio = profile.bio ?: "",
                    photos = profile.photos,
                    selectedInterestIds = profile.interests.map { it.id }.toSet(),
                    selectedCountry = country,
                    selectedRegion = region,
                    selectedCity = city
                )

                if (profile.countryId != null) {
                    loadRegions(profile.countryId)
                }
                if (profile.regionId != null) {
                    loadCities(profile.regionId)
                }
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun loadCountries() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocations = true)
            val result = locationRepository.getCountries()
            if (result is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    countries = result.data,
                    isLoadingLocations = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingLocations = false)
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

    private fun loadRegions(countryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocations = true)
            val result = locationRepository.getRegions(countryId)
            if (result is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    regions = result.data,
                    isLoadingLocations = false
                )
            } else {
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

    private fun loadCities(regionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocations = true)
            val result = locationRepository.getCities(regionId)
            if (result is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    cities = result.data,
                    isLoadingLocations = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingLocations = false)
            }
        }
    }

    fun selectCity(city: City?) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
    }

    fun onDisplayNameChange(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name, errorMessage = null)
    }

    fun onBirthDateChange(millis: Long) {
        _uiState.value = _uiState.value.copy(birthDateMillis = millis, errorMessage = null)
    }

    fun onGenderChange(gender: Gender) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun onBioChange(bio: String) {
        _uiState.value = _uiState.value.copy(bio = bio)
    }

    fun toggleInterest(interestId: String) {
        val current = _uiState.value.selectedInterestIds.toMutableSet()
        if (current.contains(interestId)) {
            current.remove(interestId)
        } else {
            if (current.size < 8) {
                current.add(interestId)
            }
        }
        _uiState.value = _uiState.value.copy(selectedInterestIds = current)
    }

    fun onGenderPreferenceChange(pref: GenderPreference) {
        _uiState.value = _uiState.value.copy(genderPreference = pref)
    }

    fun onAgeRangeChange(min: Int, max: Int) {
        _uiState.value = _uiState.value.copy(minAgePreference = min, maxAgePreference = max)
    }

    fun onDistanceChange(distanceKm: Int) {
        _uiState.value = _uiState.value.copy(maxDistanceKm = distanceKm)
    }

    fun submitBasicInfo() {
        val name = _uiState.value.displayName.trim()
        val birthMillis = _uiState.value.birthDateMillis

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = updateProfileUseCase(
                displayName = name,
                birthDateMillis = birthMillis,
                gender = _uiState.value.gender,
                bio = _uiState.value.bio,
                countryId = _uiState.value.selectedCountry?.id,
                regionId = _uiState.value.selectedRegion?.id,
                cityId = _uiState.value.selectedCity?.id,
                interestIds = _uiState.value.selectedInterestIds.toList()
            )
            _uiState.value = _uiState.value.copy(isLoading = false)

            when (result) {
                is Result.Success -> _eventFlow.emit(ProfileCreationEvent.NavigateToAddPhotos)
                is Result.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.error.message)
            }
        }
    }

    fun uploadPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingPhoto = true, errorMessage = null)
            try {
                val compressedBytes = ImageCompressor.compressImage(context, uri)
                val isFirstPhoto = _uiState.value.photos.isEmpty()
                val result = uploadPhotoUseCase(compressedBytes, isPrimary = isFirstPhoto)
                _uiState.value = _uiState.value.copy(isUploadingPhoto = false)

                when (result) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            photos = _uiState.value.photos + result.data
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(errorMessage = result.error.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingPhoto = false,
                    errorMessage = "Failed to process photo: ${e.message}"
                )
            }
        }
    }

    fun deletePhoto(photo: ProfilePhoto) {
        viewModelScope.launch {
            val result = deletePhotoUseCase(photo.id, photo.storagePath)
            if (result is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    photos = _uiState.value.photos.filter { it.id != photo.id }
                )
            }
        }
    }

    fun proceedToInterests() {
        if (_uiState.value.photos.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please upload at least 1 profile photo.")
            return
        }
        viewModelScope.launch {
            _eventFlow.emit(ProfileCreationEvent.NavigateToSelectInterests)
        }
    }

    fun submitInterests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = updateProfileUseCase(
                displayName = _uiState.value.displayName,
                birthDateMillis = _uiState.value.birthDateMillis,
                gender = _uiState.value.gender,
                bio = _uiState.value.bio,
                interestIds = _uiState.value.selectedInterestIds.toList()
            )
            _uiState.value = _uiState.value.copy(isLoading = false)

            if (result is Result.Success) {
                _eventFlow.emit(ProfileCreationEvent.NavigateToDatingPreferences)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = (result as Result.Error).error.message)
            }
        }
    }

    fun submitPreferences() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // 1. Save profile details & selected interests
            val profileResult = updateProfileUseCase(
                displayName = _uiState.value.displayName,
                birthDateMillis = _uiState.value.birthDateMillis,
                gender = _uiState.value.gender,
                bio = _uiState.value.bio,
                interestIds = _uiState.value.selectedInterestIds.toList()
            )
            if (profileResult is Result.Error) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = profileResult.error.message)
                return@launch
            }

            // 2. Save dating preferences
            val pref = UserPreferences(
                userId = "",
                minAge = _uiState.value.minAgePreference,
                maxAge = _uiState.value.maxAgePreference,
                interestedInGender = _uiState.value.genderPreference,
                maxDistanceKm = _uiState.value.maxDistanceKm
            )
            val prefResult = updatePreferencesUseCase(pref)
            _uiState.value = _uiState.value.copy(isLoading = false)

            if (prefResult is Result.Success) {
                _eventFlow.emit(ProfileCreationEvent.NavigateToMain)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = (prefResult as Result.Error).error.message)
            }
        }
    }
}
