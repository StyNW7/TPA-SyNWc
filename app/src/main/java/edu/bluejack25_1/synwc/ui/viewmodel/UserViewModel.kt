package edu.bluejack25_1.synwc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack25_1.synwc.data.repository.UserRepository
import edu.bluejack25_1.synwc.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun updateStreak(newStreak: Int) {
        viewModelScope.launch {
            _loading.value = true
            val result = userRepository.updateStreak(
                _currentUser.value?.id ?: return@launch,
                newStreak
            )
            _loading.value = false
            result.onFailure {
                _errorMessage.value = "Failed to update streak: ${it.message}"
            }
        }
    }

    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            _loading.value = true
            val result = userRepository.updateUser(user)
            _loading.value = false
            result.onFailure {
                _errorMessage.value = "Failed to update profile: ${it.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}