package com.aura.dating.feature.chat.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.ImageCompressor
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.domain.chat.model.Message
import com.aura.dating.domain.chat.usecase.DeleteMessageUseCase
import com.aura.dating.domain.chat.usecase.GetMessagesUseCase
import com.aura.dating.domain.chat.usecase.MarkMessagesAsReadUseCase
import com.aura.dating.domain.chat.usecase.ObserveTypingStatusUseCase
import com.aura.dating.domain.chat.usecase.SendImageMessageUseCase
import com.aura.dating.domain.chat.usecase.SendMessageUseCase
import com.aura.dating.domain.chat.usecase.SendTypingStatusUseCase
import com.aura.dating.domain.matching.usecase.UnmatchUseCase
import com.aura.dating.domain.moderation.model.ReportReason
import com.aura.dating.domain.moderation.model.ReportRequest
import com.aura.dating.domain.moderation.usecase.BlockUserUseCase
import com.aura.dating.domain.moderation.usecase.ReportUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationUiState(
    val conversationId: String = "",
    val matchName: String = "",
    val matchPhotoUrl: String? = null,
    val currentUserId: String = "",
    val messages: List<Message> = emptyList(),
    val isPartnerTyping: Boolean = false,
    val inputText: String = "",
    val isSending: Boolean = false,
    val isUploadingImage: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ConversationEvent {
    data object NavigateBack : ConversationEvent
    data class ShowToast(val message: String) : ConversationEvent
}

@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val sendImageMessageUseCase: SendImageMessageUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val observeTypingStatusUseCase: ObserveTypingStatusUseCase,
    private val sendTypingStatusUseCase: SendTypingStatusUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val unmatchUseCase: UnmatchUseCase,
    private val blockUserUseCase: BlockUserUseCase,
    private val reportUserUseCase: ReportUserUseCase,
    private val tokenStorage: TokenStorage
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])
    private val matchName: String = savedStateHandle["matchName"] ?: "Match"
    private val photoUrl: String? = savedStateHandle["photoUrl"]

    private val _uiState = MutableStateFlow(
        ConversationUiState(
            conversationId = conversationId,
            matchName = matchName,
            matchPhotoUrl = photoUrl
        )
    )
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ConversationEvent>()
    val eventFlow: SharedFlow<ConversationEvent> = _eventFlow.asSharedFlow()

    private var typingJob: Job? = null

    init {
        loadUserId()
        observeMessages()
        observeTyping()
        fetchInitialMessages()
        markAsRead()
    }

    private fun loadUserId() {
        viewModelScope.launch {
            val userId = tokenStorage.getUserId() ?: ""
            _uiState.value = _uiState.value.copy(currentUserId = userId)
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            getMessagesUseCase(conversationId).collect { messageList ->
                _uiState.value = _uiState.value.copy(messages = messageList)
            }
        }
    }

    private fun observeTyping() {
        viewModelScope.launch {
            observeTypingStatusUseCase(conversationId).collect { isTyping ->
                _uiState.value = _uiState.value.copy(isPartnerTyping = isTyping)
            }
        }
    }

    fun fetchInitialMessages() {
        viewModelScope.launch {
            getMessagesUseCase.fetchMessages(conversationId, forceRefresh = true)
        }
    }

    fun markAsRead() {
        viewModelScope.launch {
            markMessagesAsReadUseCase(conversationId)
        }
    }

    fun onInputTextChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)

        // Broadcast typing status
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            sendTypingStatusUseCase(conversationId, true)
            delay(2000)
            sendTypingStatusUseCase(conversationId, false)
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        _uiState.value = _uiState.value.copy(inputText = "")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            sendMessageUseCase(conversationId, text)
            _uiState.value = _uiState.value.copy(isSending = false)
        }
    }

    fun sendImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingImage = true, errorMessage = null)
            try {
                val imageBytes = ImageCompressor.compressImage(context, uri)
                sendImageMessageUseCase(conversationId, imageBytes)
                _uiState.value = _uiState.value.copy(isUploadingImage = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploadingImage = false,
                    errorMessage = "Failed to send photo: ${e.message}"
                )
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            deleteMessageUseCase(messageId)
        }
    }

    fun unmatch(matchId: String) {
        viewModelScope.launch {
            unmatchUseCase(matchId)
            _eventFlow.emit(ConversationEvent.NavigateBack)
        }
    }

    fun blockUser(partnerId: String) {
        viewModelScope.launch {
            blockUserUseCase(partnerId, matchName, photoUrl)
            _eventFlow.emit(ConversationEvent.NavigateBack)
        }
    }

    fun reportUser(partnerId: String, reason: ReportReason, details: String?) {
        viewModelScope.launch {
            reportUserUseCase(ReportRequest(partnerId, reason, details))
            _eventFlow.emit(ConversationEvent.ShowToast("Report submitted"))
        }
    }
}
