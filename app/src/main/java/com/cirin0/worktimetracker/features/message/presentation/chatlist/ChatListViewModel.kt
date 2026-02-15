package com.cirin0.worktimetracker.features.message.presentation.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cirin0.worktimetracker.core.network.ApiResponse
import com.cirin0.worktimetracker.features.message.data.repository.UserRepository
import com.cirin0.worktimetracker.features.profile.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Get current user ID first
            val currentUserId =
                when (val currentUserResponse = profileRepository.getCurrentUser()) {
                    is ApiResponse.Success -> currentUserResponse.data.id
                    else -> null
                }

            when (val response = userRepository.getUsers()) {

                is ApiResponse.Success -> {
                    // Filter out current user from the list
                    val filteredUsers = if (currentUserId != null) {
                        response.data.filter { it.id != currentUserId }
                    } else {
                        response.data
                    }
                    println(userRepository.getUsers())

                    _state.value = _state.value.copy(
                        users = filteredUsers,
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
}


