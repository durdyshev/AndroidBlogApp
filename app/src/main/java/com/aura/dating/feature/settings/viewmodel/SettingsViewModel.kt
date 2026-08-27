package com.aura.dating.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.dating.domain.auth.usecase.LogoutUseCase
import com.aura.dating.domain.moderation.model.BlockedUser
import com.aura.dating.domain.moderation.usecase.DeleteAccountUseCase
import com.aura.dating.domain.moderation.usecase.GetBlockedUsersUseCase
import com.aura.dating.domain.moderation.usecase.UnblockUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow: SharedFlow<SettingsEvent> = _eventFlow.asSharedFlow()

    init {
        observeBlockedUsers()
        loadBlockedUsers()
    }

    private fun observeBlockedUsers() {
        viewModelScope.launch {
            getBlockedUsersUseCase.blockedUsersFlow.collect { list ->
                _uiState.value = _uiState.value.copy(blockedUsers = list)
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
        _uiState.value = _uiState.value.copy(pushNotificationsEnabled = enabled)
    }

    fun toggleNewMatchesPush(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(newMatchesPush = enabled)
    }

    fun toggleMessagesPush(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(messagesPush = enabled)
    }

    fun toggleLikesPush(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(likesPush = enabled)
    }

    fun toggleShowOnline(show: Boolean) {
        _uiState.value = _uiState.value.copy(showOnlineStatus = show)
    }

    fun toggleShowDistance(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDistance = show)
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
