package edu.bluejack25_1.synwc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import edu.bluejack25_1.synwc.data.model.User
import edu.bluejack25_1.synwc.data.repository.AuthRepository
import edu.bluejack25_1.synwc.data.repository.StreakRepository
import edu.bluejack25_1.synwc.data.repository.UserRepository
import edu.bluejack25_1.synwc.domain.service.StreakService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val streakRepository = StreakRepository()
    private val streakService = StreakService()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    private val _userLoggedIn = MutableStateFlow(repository.isUserLoggedIn())
    val userLoggedIn = _userLoggedIn.asStateFlow()

    init {
        // Start periodic streak checking when ViewModel is created
        streakService.startPeriodicStreakCheck()

        // Check streaks immediately if user is already logged in
        if (_userLoggedIn.value) {
            checkStreaksOnAppStart()
        }
    }

    fun login(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please fill all fields."
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            val result = repository.loginWithEmailOrUsername(identifier, password)
            _loading.value = false
            result.onSuccess {
                _success.value = true
                _userLoggedIn.value = true

                // Update login streak and check other streaks on successful login
                updateLoginStreak()
                checkStreaksOnAppStart()
            }.onFailure {
                _errorMessage.value = getFirebaseAuthErrorMessage(it)
            }
        }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _success.value = false

            try {
                // Basic validation
                if (username.length < 3) {
                    _errorMessage.value = "Username must be at least 3 characters"
                    _loading.value = false
                    return@launch
                }

                if (password.length < 6) {
                    _errorMessage.value = "Password must be at least 6 characters"
                    _loading.value = false
                    return@launch
                }

                if (password != confirmPassword) {
                    _errorMessage.value = "Passwords do not match"
                    _loading.value = false
                    return@launch
                }

                // First validate username and email uniqueness
                val userRepository = UserRepository()

                if (userRepository.isUsernameExists(username)) {
                    _errorMessage.value = "Username already exists"
                    _loading.value = false
                    return@launch
                }

                if (userRepository.isEmailExists(email)) {
                    _errorMessage.value = "Email already exists"
                    _loading.value = false
                    return@launch
                }

                // If validation passes, create auth user
                val authResult = Firebase.auth.createUserWithEmailAndPassword(email, password).await()

                val user = User.createNewUser(
                    id = authResult.user?.uid ?: "",
                    name = username.trim(),
                    email = email.trim().lowercase()
                )

                // Create user document with validation
                val createResult = userRepository.createUser(user)

                createResult.onSuccess {
                    _success.value = true
                    _userLoggedIn.value = true
                    _loading.value = false

                    // For new users, check streaks (will initialize them properly)
                    checkStreaksOnAppStart()
                }.onFailure { exception ->
                    // Delete the auth user if user creation fails
                    authResult.user?.delete()?.await()
                    _errorMessage.value = exception.message ?: "Registration failed"
                    _loading.value = false
                }

            } catch (e: Exception) {
                _errorMessage.value = when {
                    e.message?.contains("email address is already in use") == true -> "Email already exists"
                    e.message?.contains("password") == true -> "Password is too weak"
                    else -> e.message ?: "Registration failed"
                }
                _loading.value = false
            }
        }
    }

    fun updateLoginStreak() {
        viewModelScope.launch {
            streakRepository.updateLoginStreak()
        }
    }

    fun updateReflectionStreak() {
        viewModelScope.launch {
            streakRepository.updateReflectionStreak()
        }
    }

    fun updateTodoStreak() {
        viewModelScope.launch {
            streakRepository.updateTodoStreak()
        }
    }

    suspend fun checkAndResetStreaks() {
        streakRepository.checkAndResetStreaks()
    }

    fun checkStreaksOnAppStart() {
        viewModelScope.launch {
            try {
                streakService.checkStreaksOnAppStart()
            } catch (e: Exception) {
                // Log the error but don't show to user as this is a background operation
                println("Streak check failed: ${e.message}")
            }
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

        // Check streaks when auth state is checked and user is logged in
        if (_userLoggedIn.value) {
            checkStreaksOnAppStart()
        }
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
            errorMessage.contains("no user record") -> "No account found with this email or username."
            errorMessage.contains("email address is already in use") -> "Email already registered."
            errorMessage.contains("network error") -> "Network error. Please check your connection."
            errorMessage.contains("WEAK_PASSWORD") -> "Password is too weak. Please use a stronger password."
            errorMessage.contains("No account found with this username") -> "No account found with this username."
            else -> "Authentication failed: ${exception.message}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Stop streak checking when ViewModel is cleared
        streakService.stopPeriodicStreakCheck()
    }
}