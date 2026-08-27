package com.aura.dating.feature.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.location.LocationProvider
import com.aura.dating.domain.auth.usecase.GetSessionUseCase
import com.aura.dating.domain.profile.usecase.GetMyProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashNavigationState {
    data object Loading : SplashNavigationState
    data object NavigateToWelcome : SplashNavigationState
    data object NavigateToLocationPermission : SplashNavigationState
    data object NavigateToCreateProfile : SplashNavigationState
    data object NavigateToMain : SplashNavigationState
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val getSessionUseCase: GetSessionUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Loading)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    fun checkInitialDestination() {
        viewModelScope.launch {
            val session = getSessionUseCase.getCachedSession()
            if (session == null) {
                _navigationState.value = SplashNavigationState.NavigateToWelcome
                return@launch
            }

            if (!locationProvider.hasLocationPermission()) {
                _navigationState.value = SplashNavigationState.NavigateToLocationPermission
                return@launch
            }

            val profileResult = getMyProfileUseCase()
            if (profileResult is Result.Success && profileResult.data.displayName.isNotBlank()) {
                _navigationState.value = SplashNavigationState.NavigateToMain
            } else {
                _navigationState.value = SplashNavigationState.NavigateToCreateProfile
            }
        }
    }
}
