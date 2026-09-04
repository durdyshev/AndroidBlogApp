package com.aura.dating.feature.matches.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.dating.domain.chat.model.Conversation
import com.aura.dating.domain.chat.usecase.GetConversationsUseCase
import com.aura.dating.domain.matching.model.Match
import com.aura.dating.domain.matching.usecase.GetMatchesUseCase
import com.aura.dating.domain.matching.usecase.UnmatchUseCase
import com.aura.dating.domain.chat.usecase.GetTotalUnreadCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchesUiState(
    val matches: List<Match> = emptyList(),
    val conversations: List<Conversation> = emptyList(),
    val totalUnreadCount: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val getMatchesUseCase: GetMatchesUseCase,
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getTotalUnreadCountUseCase: GetTotalUnreadCountUseCase,
    private val unmatchUseCase: UnmatchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        observeData()
        loadData()
    }

    private fun observeData() {
        viewModelScope.launch {
            getMatchesUseCase.matchesFlow.collect { matchesList ->
                _uiState.value = _uiState.value.copy(matches = matchesList)
            }
        }
        viewModelScope.launch {
            getConversationsUseCase.conversationsFlow.collect { convsList ->
                _uiState.value = _uiState.value.copy(conversations = convsList)
            }
        }
        viewModelScope.launch {
            getTotalUnreadCountUseCase().collect { unread ->
                _uiState.value = _uiState.value.copy(totalUnreadCount = unread)
            }
        }
    }

    fun loadData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            getMatchesUseCase(forceRefresh = forceRefresh)
            getConversationsUseCase(forceRefresh = forceRefresh)
            _uiState.value = _uiState.value.copy(isLoading = false, isRefreshing = false)
        }
    }

    fun unmatch(matchId: String) {
        viewModelScope.launch {
            unmatchUseCase(matchId)
        }
    }
}
