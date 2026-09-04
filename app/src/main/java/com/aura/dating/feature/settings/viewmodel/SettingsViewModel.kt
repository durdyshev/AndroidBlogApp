package com.aura.dating.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.dating.core.preferences.AppSettingsStorage
import com.aura.dating.domain.auth.usecase.LogoutUseCase
import com.aura.dating.domain.moderation.model.BlockedUser
import com.aura.dating.domain.moderation.usecase.DeleteAccountUseCase
import com.aura.dating.domain.moderation.usecase.GetBlockedUsersUseCase
import com.aura.dating.domain.moderation.usecase.UnblockUserUseCase
import com.aura.dating.core.presence.PresenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val blockedUsers: List<BlockedUser> = emptyList(),
    val pushNotificationsEnabled: Boolean = true,
    val newMatchesPush: Boolean = true,
    val messagesPush: Boolean = true,
    val likesPush: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val showDistance: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SettingsEvent {
    data object NavigateToWelcome : SettingsEvent
    data class ShowToast(val message: String) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getBlockedUsersUseCase: GetBlockedUsersUseCase,
    private val unblockUserUseCase: UnblockUserUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val appSettingsStorage: AppSettingsStorage,
    private val presenceManager: PresenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow: SharedFlow<SettingsEvent> = _eventFlow.asSharedFlow()

    init {
        observeBlockedUsers()
        observePreferences()
        loadBlockedUsers()
    }

    private fun observeBlockedUsers() {
        viewModelScope.launch {
            getBlockedUsersUseCase.blockedUsersFlow.collect { list ->
                _uiState.value = _uiState.value.copy(blockedUsers = list)
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                listOf(
                    appSettingsStorage.pushNotificationsEnabledFlow,
                    appSettingsStorage.newMatchesPushFlow,
                    appSettingsStorage.messagesPushFlow,
                    appSettingsStorage.likesPushFlow,
                    appSettingsStorage.showOnlineStatusFlow,
                    appSettingsStorage.showDistanceFlow
                )
            ) { values ->
                _uiState.value.copy(
                    pushNotificationsEnabled = values[0],
                    newMatchesPush = values[1],
                    messagesPush = values[2],
                    likesPush = values[3],
                    showOnlineStatus = values[4],
                    showDistance = values[5]
                )
            }.collect { updatedState ->
                _uiState.value = updatedState
            }
        }
    }

    fun loadBlockedUsers() {
        viewModelScope.launch {
            getBlockedUsersUseCase(forceRefresh = true)
        }
    }

    fun unblockUser(blockedUserId: String) {
        viewModelScope.launch {
            unblockUserUseCase(blockedUserId)
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsStorage.setPushNotificationsEnabled(enabled)
        }
    }

    fun toggleNewMatchesPush(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsStorage.setNewMatchesPushEnabled(enabled)
        }
    }

    fun toggleMessagesPush(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsStorage.setMessagesPushEnabled(enabled)
        }
    }

    fun toggleLikesPush(enabled: Boolean) {
        viewModelScope.launch {
            appSettingsStorage.setLikesPushEnabled(enabled)
        }
    }

    fun toggleShowOnline(show: Boolean) {
        viewModelScope.launch {
            appSettingsStorage.setShowOnlineStatus(show)
            presenceManager.setOnlineStatus(show)
        }
    }

    fun toggleShowDistance(show: Boolean) {
        viewModelScope.launch {
            appSettingsStorage.setShowDistance(show)
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _eventFlow.emit(SettingsEvent.NavigateToWelcome)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = deleteAccountUseCase()
            _uiState.value = _uiState.value.copy(isLoading = false)

            if (result is com.aura.dating.core.common.result.Result.Success) {
                _eventFlow.emit(SettingsEvent.NavigateToWelcome)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = (result as com.aura.dating.core.common.result.Result.Error).error.message)
            }
        }
    }
}
