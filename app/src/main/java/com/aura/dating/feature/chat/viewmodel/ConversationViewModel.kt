package com.aura.dating.feature.chat.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.common.utils.ImageCompressor
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.domain.chat.model.Message
import com.aura.dating.domain.chat.usecase.DeleteMessageUseCase
import com.aura.dating.domain.chat.usecase.GetMessagesUseCase
import com.aura.dating.domain.chat.usecase.MarkMessagesAsReadUseCase
import com.aura.dating.domain.chat.usecase.ObserveTypingStatusUseCase
import com.aura.dating.domain.chat.usecase.ResolveConversationIdUseCase
import com.aura.dating.domain.chat.usecase.SendImageMessageUseCase
import com.aura.dating.domain.chat.usecase.SendMessageUseCase
import com.aura.dating.domain.chat.usecase.SendTypingStatusUseCase
import com.aura.dating.domain.matching.usecase.UnmatchUseCase
import com.aura.dating.domain.moderation.model.ReportReason
import com.aura.dating.domain.moderation.model.ReportRequest
import com.aura.dating.domain.moderation.usecase.BlockUserUseCase
import com.aura.dating.domain.moderation.usecase.ReportUserUseCase
import com.aura.dating.domain.chat.repository.ChatRepository
import com.aura.dating.domain.profile.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationUiState(
    val conversationId: String = "",
    val matchName: String = "",
    val matchPhotoUrl: String? = null,
    val currentUserId: String = "",
    val partnerUserId: String = "",
    val isPartnerOnline: Boolean = false,
    val partnerLastSeenAtMillis: Long? = null,
    val messages: List<Message> = emptyList(),
    val isPartnerTyping: Boolean = false,
    val inputText: String = "",
    val isSending: Boolean = false,
    val isUploadingImage: Boolean = false,
    val isLoadingOlderMessages: Boolean = false,
    val hasMoreOlderMessages: Boolean = true,
    val errorMessage: String? = null
)

sealed interface ConversationEvent {
    data object NavigateBack : ConversationEvent
    data class ShowToast(val message: String) : ConversationEvent
}

@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resolveConversationIdUseCase: ResolveConversationIdUseCase,
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
    private val tokenStorage: TokenStorage,
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val initialConversationId: String = checkNotNull(savedStateHandle["conversationId"])
    private val matchName: String = savedStateHandle["matchName"] ?: "Match"
    private val photoUrl: String? = savedStateHandle["photoUrl"]

    private var resolvedConversationId: String = initialConversationId
    private var liveSyncJob: Job? = null

    private val _uiState = MutableStateFlow(
        ConversationUiState(
            conversationId = initialConversationId,
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
        initChatSession()
    }

    private fun loadUserId() {
        viewModelScope.launch {
            val userId = tokenStorage.getUserId() ?: ""
            _uiState.value = _uiState.value.copy(currentUserId = userId)
        }
    }

    private fun initChatSession() {
        viewModelScope.launch {
            val res = resolveConversationIdUseCase(initialConversationId)
            val actualId = if (res is Result.Success) res.data else initialConversationId
            resolvedConversationId = actualId
            _uiState.value = _uiState.value.copy(conversationId = actualId)

            resolvePartnerInfo(actualId)
            observeMessages(actualId)
            observeTyping(actualId)
            fetchInitialMessages(actualId)
            markAsRead(actualId)
            startLiveSync(actualId)
        }
    }

    private suspend fun resolvePartnerInfo(actualConvId: String) {
        val convsRes = chatRepository.getConversations(forceRefresh = false)
        if (convsRes is Result.Success) {
            val conv = convsRes.data.firstOrNull { it.id == actualConvId || it.id == initialConversationId || it.participantName == matchName }
            if (conv != null && conv.participantUserId.isNotBlank()) {
                _uiState.value = _uiState.value.copy(
                    partnerUserId = conv.participantUserId,
                    isPartnerOnline = conv.isParticipantOnline,
                    partnerLastSeenAtMillis = conv.participantLastSeenAtMillis
                )
                return
            }
        }

        if (initialConversationId.length == 36 && initialConversationId != actualConvId) {
            _uiState.value = _uiState.value.copy(partnerUserId = initialConversationId)
            refreshPartnerPresence()
        }
    }

    private suspend fun refreshPartnerPresence() {
        val partnerId = _uiState.value.partnerUserId
        if (partnerId.isNotBlank()) {
            val profileRes = profileRepository.getUserProfile(partnerId)
            if (profileRes is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    isPartnerOnline = profileRes.data.isOnline,
                    partnerLastSeenAtMillis = profileRes.data.lastSeenAtMillis
                )
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 30
    }

    private fun startLiveSync(convId: String) {
        liveSyncJob?.cancel()
        liveSyncJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                getMessagesUseCase.fetchMessages(
                    conversationId = convId,
                    limit = PAGE_SIZE,
                    beforeTimestampIso = null,
                    forceRefresh = true
                )
                refreshPartnerPresence()
            }
        }
    }

    private fun observeMessages(convId: String) {
        viewModelScope.launch {
            getMessagesUseCase(convId).collect { messageList ->
                _uiState.value = _uiState.value.copy(messages = messageList)
            }
        }
    }

    private fun observeTyping(convId: String) {
        viewModelScope.launch {
            observeTypingStatusUseCase(convId).collect { isTyping ->
                _uiState.value = _uiState.value.copy(isPartnerTyping = isTyping)
            }
        }
    }

    fun fetchInitialMessages(convId: String = resolvedConversationId) {
        viewModelScope.launch {
            val res = getMessagesUseCase.fetchMessages(
                conversationId = convId,
                limit = PAGE_SIZE,
                beforeTimestampIso = null,
                forceRefresh = true
            )
            if (res is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    hasMoreOlderMessages = res.data.size >= PAGE_SIZE
                )
            }
        }
    }

    fun loadOlderMessages() {
        val currentState = _uiState.value
        if (currentState.isLoadingOlderMessages || !currentState.hasMoreOlderMessages) return

        val oldestMessage = currentState.messages.firstOrNull() ?: return
        val oldestIso = DateTimeUtils.formatToIsoUtc(oldestMessage.createdAtMillis)
        val convId = resolvedConversationId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingOlderMessages = true)
            val res = getMessagesUseCase.fetchMessages(
                conversationId = convId,
                limit = PAGE_SIZE,
                beforeTimestampIso = oldestIso,
                forceRefresh = true
            )
            val hasMore = if (res is Result.Success) {
                res.data.size >= PAGE_SIZE
            } else {
                currentState.hasMoreOlderMessages
            }
            _uiState.value = _uiState.value.copy(
                isLoadingOlderMessages = false,
                hasMoreOlderMessages = hasMore
            )
        }
    }

    fun markAsRead(convId: String = resolvedConversationId) {
        viewModelScope.launch {
            markMessagesAsReadUseCase(convId)
        }
    }

    fun onInputTextChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)

        // Broadcast typing status
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            sendTypingStatusUseCase(resolvedConversationId, true)
            delay(2000)
            sendTypingStatusUseCase(resolvedConversationId, false)
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        val targetConvId = resolvedConversationId
        _uiState.value = _uiState.value.copy(inputText = "")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            val sendResult = sendMessageUseCase(targetConvId, text)
            _uiState.value = _uiState.value.copy(isSending = false)
            if (sendResult is Result.Error) {
                Log.e("hata",sendResult.error.message)
                _eventFlow.emit(ConversationEvent.ShowToast("Mesaj iletilemedi: ${sendResult.error.message}"))
            }
        }
    }

    fun sendImage(context: Context, uri: Uri) {
        val targetConvId = resolvedConversationId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingImage = true, errorMessage = null)
            try {
                val imageBytes = ImageCompressor.compressImage(context, uri)
                val sendResult = sendImageMessageUseCase(targetConvId, imageBytes)
                _uiState.value = _uiState.value.copy(isUploadingImage = false)
                if (sendResult is Result.Error) {
                    _eventFlow.emit(ConversationEvent.ShowToast("Fotoğraf iletilemedi: ${sendResult.error.message}"))
                }
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

    fun unmatch(conversationId: String) {
        val targetId = resolvedConversationId
        viewModelScope.launch {
            val result = unmatchUseCase(targetId)
            if (result is Result.Success) {
                _eventFlow.emit(ConversationEvent.ShowToast("Unmatched successfully"))
                _eventFlow.emit(ConversationEvent.NavigateBack)
            } else if (result is Result.Error) {
                _eventFlow.emit(ConversationEvent.ShowToast(result.error.message))
            }
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            val result = blockUserUseCase(userId, matchName, photoUrl)
            if (result is Result.Success) {
                _eventFlow.emit(ConversationEvent.ShowToast("User blocked"))
                _eventFlow.emit(ConversationEvent.NavigateBack)
            } else if (result is Result.Error) {
                _eventFlow.emit(ConversationEvent.ShowToast(result.error.message))
            }
        }
    }

    fun reportUser(userId: String, reason: ReportReason, details: String?) {
        viewModelScope.launch {
            val result = reportUserUseCase(
                ReportRequest(
                    reportedUserId = userId,
                    reason = reason,
                    details = details
                )
            )
            if (result is Result.Success) {
                _eventFlow.emit(ConversationEvent.ShowToast("Report submitted. Thank you."))
            } else if (result is Result.Error) {
                _eventFlow.emit(ConversationEvent.ShowToast(result.error.message))
            }
        }
    }
}
