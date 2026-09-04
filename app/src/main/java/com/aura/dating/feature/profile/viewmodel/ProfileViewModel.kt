package com.aura.dating.feature.profile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.ImageCompressor
import com.aura.dating.domain.moderation.model.ReportReason
import com.aura.dating.domain.moderation.model.ReportRequest
import com.aura.dating.domain.moderation.usecase.BlockUserUseCase
import com.aura.dating.domain.moderation.usecase.ReportUserUseCase
import com.aura.dating.domain.profile.model.Interest
import com.aura.dating.domain.profile.model.ProfilePhoto
import com.aura.dating.domain.profile.model.UserProfile
import com.aura.dating.domain.profile.usecase.DeletePhotoUseCase
import com.aura.dating.domain.profile.usecase.GetInterestsUseCase
import com.aura.dating.domain.profile.usecase.GetMyProfileUseCase
import com.aura.dating.domain.profile.usecase.GetUserProfileUseCase
import com.aura.dating.domain.profile.usecase.ReorderPhotosUseCase
import com.aura.dating.domain.profile.usecase.SetPrimaryPhotoUseCase
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
import javax.inject.Inject

import com.aura.dating.core.preferences.AppSettingsStorage
import com.aura.dating.domain.location.model.City
import com.aura.dating.domain.location.model.Country
import com.aura.dating.domain.location.model.Region
import com.aura.dating.domain.location.repository.LocationRepository

data class ProfileUiState(
    val myProfile: UserProfile? = null,
    val selectedUserProfile: UserProfile? = null,
    val availableInterests: List<Interest> = emptyList(),
    val countries: List<Country> = emptyList(),
    val regions: List<Region> = emptyList(),
    val cities: List<City> = emptyList(),
    val showDistance: Boolean = true,
    val isLoadingLocations: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed interface ProfileEvent {
    data class ShowToast(val message: String) : ProfileEvent
    data object NavigateBack : ProfileEvent
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val reorderPhotosUseCase: ReorderPhotosUseCase,
    private val setPrimaryPhotoUseCase: SetPrimaryPhotoUseCase,
    private val getInterestsUseCase: GetInterestsUseCase,
    private val blockUserUseCase: BlockUserUseCase,
    private val reportUserUseCase: ReportUserUseCase,
    private val locationRepository: LocationRepository,
    private val appSettingsStorage: AppSettingsStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProfileEvent>()
    val eventFlow: SharedFlow<ProfileEvent> = _eventFlow.asSharedFlow()

    init {
        loadProfile()
        observeProfile()
        observeSettings()
        loadInterests()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appSettingsStorage.showDistanceFlow.collect { showDistance ->
                _uiState.value = _uiState.value.copy(showDistance = showDistance)
            }
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            getMyProfileUseCase.profileFlow.collect { profile ->
                _uiState.value = _uiState.value.copy(myProfile = profile)
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = getMyProfileUseCase()
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result is Result.Success) {
                _uiState.value = _uiState.value.copy(myProfile = result.data)
            }
        }
    }

    private fun loadInterests() {
        viewModelScope.launch {
            val result = getInterestsUseCase()
            if (result is Result.Success) {
                _uiState.value = _uiState.value.copy(availableInterests = result.data)
            }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedUserProfile = null)
            val result = getUserProfileUseCase(userId)
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result is Result.Success) {
                _uiState.value = _uiState.value.copy(selectedUserProfile = result.data)
            } else if (result is Result.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = result.error.message)
            }
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

    fun loadRegions(countryId: String) {
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

    fun loadCities(regionId: String) {
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

    fun updateProfile(
        displayName: String,
        bio: String,
        countryId: String? = null,
        regionId: String? = null,
        cityId: String? = null
    ) {
        val current = _uiState.value.myProfile ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = updateProfileUseCase(
                displayName = displayName,
                birthDateMillis = current.birthDateMillis,
                gender = current.gender,
                bio = bio,
                countryId = countryId,
                regionId = regionId,
                cityId = cityId,
                interestIds = current.interests.map { it.id }
            )
            _uiState.value = _uiState.value.copy(isLoading = false)

            if (result is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    myProfile = result.data,
                    successMessage = "Profile updated successfully"
                )
                _eventFlow.emit(ProfileEvent.NavigateBack)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = (result as Result.Error).error.message)
            }
        }
    }

    fun uploadPhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingPhoto = true, errorMessage = null)
            try {
                val compressed = ImageCompressor.compressImage(context, uri)
                val result = uploadPhotoUseCase(compressed, isPrimary = _uiState.value.myProfile?.photos.isNullOrEmpty())
                _uiState.value = _uiState.value.copy(isUploadingPhoto = false)

                if (result is Result.Success) {
                    loadProfile()
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = (result as Result.Error).error.message)
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
            deletePhotoUseCase(photo.id, photo.storagePath)
            loadProfile()
        }
    }

    fun setPrimaryPhoto(photo: ProfilePhoto) {
        viewModelScope.launch {
            setPrimaryPhotoUseCase(photo.id)
            loadProfile()
        }
    }

    fun updateInterests(selectedIds: List<String>) {
        val current = _uiState.value.myProfile ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = updateProfileUseCase(
                displayName = current.displayName,
                birthDateMillis = current.birthDateMillis,
                gender = current.gender,
                bio = current.bio,
                interestIds = selectedIds
            )
            _uiState.value = _uiState.value.copy(isLoading = false)
            if (result is Result.Success) {
                loadProfile()
                _eventFlow.emit(ProfileEvent.NavigateBack)
            }
        }
    }

    fun blockUser(userId: String, name: String, photoUrl: String?) {
        viewModelScope.launch {
            blockUserUseCase(userId, name, photoUrl)
            _eventFlow.emit(ProfileEvent.NavigateBack)
        }
    }

    fun reportUser(userId: String, reason: ReportReason, details: String?) {
        viewModelScope.launch {
            reportUserUseCase(ReportRequest(userId, reason, details))
            _eventFlow.emit(ProfileEvent.ShowToast("Report submitted"))
        }
    }
}
