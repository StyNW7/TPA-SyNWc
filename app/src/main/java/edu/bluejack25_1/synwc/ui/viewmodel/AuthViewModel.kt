package edu.bluejack25_1.synwc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import edu.bluejack25_1.synwc.data.repository.AuthRepository
import edu.bluejack25_1.synwc.data.repository.StreakRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val streakRepository = StreakRepository()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    private val _userLoggedIn = MutableStateFlow(repository.isUserLoggedIn())
    val userLoggedIn = _userLoggedIn.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill all fields."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Invalid email format."
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            val result = repository.loginUser(email, password)
            _loading.value = false
            result.onSuccess {
                _success.value = true
                _userLoggedIn.value = true
            }.onFailure {
                _errorMessage.value = getFirebaseAuthErrorMessage(it)
            }
        }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _errorMessage.value = "Please fill all fields."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Invalid email format."
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "Password should be at least 6 characters."
            return
        }
        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match."
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            val result = repository.registerUser(username, email, password)
            _loading.value = false
            result.onSuccess {
                _success.value = true
                _userLoggedIn.value = true
            }.onFailure {
                _errorMessage.value = getFirebaseAuthErrorMessage(it)
            }
        }
    }

    fun updateLoginStreak() {
        viewModelScope.launch {
            streakRepository.updateLoginStreak()
        }
    }

    fun logout(settingsViewModel: SettingsViewModel? = null) {
        viewModelScope.launch {
            try {
                Firebase.auth.signOut()
                _userLoggedIn.value = false

                // Clear settings data when logging out
                settingsViewModel?.clearUserData()
            } catch (e: Exception) {
                _errorMessage.value = "Logout failed: ${e.message}"
            }
        }
    }

    fun checkAuthState() {
        _userLoggedIn.value = repository.isUserLoggedIn()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun resetSuccess() {
        _success.value = false
    }

    private fun getFirebaseAuthErrorMessage(exception: Throwable): String {
        val errorMessage = exception.message ?: "Unknown error occurred"

        return when {
            errorMessage.contains("badly formatted") -> "Invalid email format."
            errorMessage.contains("password is invalid") -> "Invalid password."
            errorMessage.contains("no user record") -> "No account found with this email."
            errorMessage.contains("email address is already in use") -> "Email already registered."
            errorMessage.contains("network error") -> "Network error. Please check your connection."
            errorMessage.contains("WEAK_PASSWORD") -> "Password is too weak. Please use a stronger password."
            else -> "Authentication failed: ${exception.message}"
        }
    }
}