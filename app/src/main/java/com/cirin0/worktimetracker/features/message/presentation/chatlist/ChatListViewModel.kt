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
    private var currentUserId: Int? = null

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                isLoadingMore = false,
                error = null,
                loadMoreError = null,
                currentPage = 1
            )

            ensureCurrentUserId()

            when (val response = userRepository.getUsers(page = 1)) {

                is ApiResponse.Success -> {
                    val filteredUsers = response.data.users.filter { it.id != currentUserId }
                    val meta = response.data.meta

                    _state.value = _state.value.copy(
                        users = filteredUsers,
                        isLoading = false,
                        currentPage = meta?.currentPage ?: 1,
                        hasMore = meta?.let { it.currentPage < it.lastPage }
                            ?: filteredUsers.isNotEmpty()
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

    fun loadMoreUsers() {
        val state = _state.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) {
            return
        }

        viewModelScope.launch {
            ensureCurrentUserId()

            val nextPage = _state.value.currentPage + 1
            _state.value = _state.value.copy(isLoadingMore = true, loadMoreError = null)

            when (val response = userRepository.getUsers(page = nextPage)) {
                is ApiResponse.Success -> {
                    val oldUsers = _state.value.users
                    val existingIds = oldUsers.map { it.id }.toSet()
                    val newUsers = response.data.users
                        .filter { it.id != currentUserId }
                        .filter { it.id !in existingIds }

                    val meta = response.data.meta

                    _state.value = _state.value.copy(
                        users = oldUsers + newUsers,
                        isLoadingMore = false,
                        currentPage = meta?.currentPage ?: nextPage,
                        hasMore = meta?.let { it.currentPage < it.lastPage }
                            ?: response.data.users.isNotEmpty()
                    )
                }

                is ApiResponse.Error -> {
                    _state.value = _state.value.copy(
                        isLoadingMore = false,
                        loadMoreError = response.message
                    )
                }

                is ApiResponse.Loading -> {
                    _state.value = _state.value.copy(isLoadingMore = true)
                }
            }
        }
    }

    private suspend fun ensureCurrentUserId() {
        if (currentUserId != null) {
            return
        }

        currentUserId = when (val currentUserResponse = profileRepository.getCurrentUser()) {
            is ApiResponse.Success -> currentUserResponse.data.id
            else -> null
        }
    }
}


