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

    fun loadCurrentUser() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val userId = userRepository.getCurrentUserId()
                val result = userRepository.getUser(userId)
                result.onSuccess { user ->
                    _currentUser.value = user
                }.onFailure {
                    _errorMessage.value = "Failed to load user: ${it.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "User not authenticated"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateStreak(newStreak: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val userId = userRepository.getCurrentUserId()
                val result = userRepository.updateStreak(userId, newStreak)
                result.onSuccess {
                    // Reload user data after update
                    loadCurrentUser()
                }.onFailure {
                    _errorMessage.value = "Failed to update streak: ${it.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "User not authenticated"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}