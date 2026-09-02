package com.aura.dating.feature.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.location.LocationProvider
import com.aura.dating.domain.discovery.model.DiscoveryCandidate
import com.aura.dating.domain.discovery.model.DiscoveryFilter
import com.aura.dating.domain.discovery.usecase.GetDiscoveryCandidatesUseCase
import com.aura.dating.domain.matching.model.SwipeActionType
import com.aura.dating.domain.matching.model.SwipeResult
import com.aura.dating.domain.matching.usecase.SwipeUserUseCase
import com.aura.dating.domain.profile.model.GenderPreference
import com.aura.dating.domain.profile.model.UserPreferences
import com.aura.dating.domain.profile.usecase.UpdateLocationUseCase
import com.aura.dating.domain.profile.usecase.UpdatePreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val candidates: List<DiscoveryCandidate> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val filter: DiscoveryFilter = DiscoveryFilter(),
    val currentMatchCelebration: SwipeResult? = null,
    val errorMessage: String? = null
)

sealed interface DiscoverEvent {
    data class OpenConversation(val conversationId: String, val name: String, val photoUrl: String?) : DiscoverEvent
    data class ShowToast(val message: String) : DiscoverEvent
}

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val getDiscoveryCandidatesUseCase: GetDiscoveryCandidatesUseCase,
    private val swipeUserUseCase: SwipeUserUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val updatePreferencesUseCase: UpdatePreferencesUseCase,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<DiscoverEvent>()
    val eventFlow: SharedFlow<DiscoverEvent> = _eventFlow.asSharedFlow()

    init {
        observeCandidates()
        updateLocationAndFetchCandidates()
    }

    private fun observeCandidates() {
        viewModelScope.launch {
            getDiscoveryCandidatesUseCase.candidatesFlow.collect { list ->
                _uiState.value = _uiState.value.copy(candidates = list)
            }
        }
    }

    fun updateLocationAndFetchCandidates(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Fast location update from cache (instant)
            val fastLocation = locationProvider.getLastKnownLocation()
            if (fastLocation != null) {
                launch { updateLocationUseCase(fastLocation.latitude, fastLocation.longitude) }
            }

            // Fetch discovery candidates immediately without blocking
            val result = getDiscoveryCandidatesUseCase(forceRefresh = forceRefresh)
            _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)

            if (result is Result.Error) {
                _uiState.value = _uiState.value.copy(errorMessage = result.error.message)
            }

            // Acquire high-accuracy GPS in background without blocking screen load
            launch {
                try {
                    val freshLocation = locationProvider.getCurrentLocation()
                    if (freshLocation != null && freshLocation != fastLocation) {
                        updateLocationUseCase(freshLocation.latitude, freshLocation.longitude)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun onSwipe(candidateId: String, action: SwipeActionType) {
        viewModelScope.launch {
            val result = swipeUserUseCase(candidateId, action)
            if (result is Result.Success && result.data.isMatch) {
                _uiState.value = _uiState.value.copy(currentMatchCelebration = result.data)
            }
        }
    }

    fun dismissMatchCelebration() {
        _uiState.value = _uiState.value.copy(currentMatchCelebration = null)
    }

    fun applyFilters(
        minAge: Int,
        maxAge: Int,
        maxDistanceKm: Int,
        gender: String,
        online: Boolean
    ) {
        viewModelScope.launch {
            val genderPref = try { GenderPreference.valueOf(gender) } catch (e: Exception) { GenderPreference.ALL }
            val preferences = UserPreferences(
                userId = "",
                minAge = minAge,
                maxAge = maxAge,
                interestedInGender = genderPref,
                maxDistanceKm = maxDistanceKm,
                showOnlyOnline = online
            )
            updatePreferencesUseCase(preferences)
            _uiState.value = _uiState.value.copy(
                filter = DiscoveryFilter(
                    minAge = minAge,
                    maxAge = maxAge,
                    maxDistanceKm = maxDistanceKm,
                    genderPreference = gender,
                    showOnlyOnline = online
                )
            )
            updateLocationAndFetchCandidates(forceRefresh = true)
        }
    }
}
