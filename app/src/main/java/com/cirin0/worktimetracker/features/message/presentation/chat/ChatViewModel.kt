package com.cirin0.worktimetracker.features.message.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.auth.data.repository.AuthRepository
import com.cirin0.worktimetracker.features.message.data.repository.MessageRepository
import com.cirin0.worktimetracker.features.profile.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var subscriptionJob: Job? = null

    fun initChat(receiverId: Int) {
        viewModelScope.launch {
            val currentUser = profileRepository.getCurrentUser()
            if (currentUser is ApiResponse.Success) {
                _state.value = _state.value.copy(
                    currentUserId = currentUser.data.id,
                    receiverId = receiverId
                )

                val token = authRepository.getToken()
                messageRepository.connectPusher(token)

                loadMessages()

                subscribeToMessages()
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val response = messageRepository.getMessages(_state.value.receiverId)) {
                is ApiResponse.Success -> {
                    _state.value = _state.value.copy(
                        messages = response.data,
                        isLoading = false
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = response.message
                    )
                }

                is ApiResponse.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun subscribeToMessages() {
        subscriptionJob?.cancel()

        subscriptionJob = viewModelScope.launch {
            val currentUserId = _state.value.currentUserId
            val receiverId = _state.value.receiverId

            messageRepository.subscribeToMessages(currentUserId)
                .collect { newMessage ->
                    val isFromChatPartner =
                        newMessage.senderId == receiverId && newMessage.receiverId == currentUserId
                    val isToChatPartner =
                        newMessage.senderId == currentUserId && newMessage.receiverId == receiverId

                    if (isFromChatPartner || isToChatPartner) {
                        val isDuplicate = _state.value.messages.any { it.id == newMessage.id }

                        if (!isDuplicate) {
                            val updatedMessages = _state.value.messages + newMessage
                            _state.value = _state.value.copy(messages = updatedMessages)
                        }
                    }
                }
        }
    }

    fun cleanup() {
        subscriptionJob?.cancel()
        subscriptionJob = null
        if (_state.value.currentUserId != 0) {
            messageRepository.unsubscribeFromMessages(_state.value.currentUserId)
        }
    }

    fun onMessageTextChange(text: String) {
        _state.value = _state.value.copy(messageText = text)
    }

    fun sendMessage() {
        val messageText = _state.value.messageText.trim()
        if (messageText.isEmpty() || _state.value.isSending) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true)

            when (val response = messageRepository.sendMessage(
                message = messageText,
                receiverId = _state.value.receiverId
            )) {
                is ApiResponse.Success -> {
                    val updatedMessages = _state.value.messages + response.data
                    _state.value = _state.value.copy(
                        messages = updatedMessages,
                        messageText = "",
                        isSending = false
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isSending = false,
                        error = response.message
                    )
                }

                is ApiResponse.Loading -> {
                    _state.value = _state.value.copy(isSending = true)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}


