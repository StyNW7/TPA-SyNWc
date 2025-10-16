package edu.bluejack25_1.synwc.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack25_1.synwc.data.preferences.AppPreferences
import edu.bluejack25_1.synwc.data.repository.UserRepository
import edu.bluejack25_1.synwc.util.CloudinaryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(context: Context) : ViewModel() {
    private val preferences = AppPreferences(context)
    private val userRepository = UserRepository()
    private val cloudinaryManager = CloudinaryManager(context)

    // Theme state - use StateFlow for immediate updates
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    init {
        // Load initial theme mode and observe changes
        viewModelScope.launch {
            preferences.themeMode.collect { mode ->
                _themeMode.value = mode
                Log.d("SettingsViewModel", "Theme mode updated to: $mode")
            }
        }
    }

    // User profile state
    val userName = preferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userEmail = preferences.userEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val profileImageUrl = preferences.profileImageUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // UI state
    var showEditProfileDialog by mutableStateOf(false)
        private set

    var showImagePicker by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var editedName by mutableStateOf("")
        private set

    var editedEmail by mutableStateOf("")
        private set

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    // Theme actions
    fun setThemeMode(theme: String) {
        viewModelScope.launch {
            Log.d("SettingsViewModel", "Setting theme mode to: $theme")
            preferences.setThemeMode(theme)
            // The theme will update automatically throughout the app via the observer
        }
    }

    // ... rest of your existing methods (showEditProfile, saveProfile, uploadProfileImage, etc.)
    fun showEditProfile() {
        editedName = userName.value
        editedEmail = userEmail.value
        showEditProfileDialog = true
        errorMessage = null
    }

    fun hideEditProfile() {
        showEditProfileDialog = false
        errorMessage = null
    }

    fun updateEditedName(name: String) {
        editedName = name
    }

    fun updateEditedEmail(email: String) {
        editedEmail = email
    }

    fun saveProfile() {
        if (editedName.isEmpty() || editedEmail.isEmpty()) {
            errorMessage = "Name and email cannot be empty"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId()
                Log.d("SettingsViewModel", "Updating profile for user: $userId")

                // Update to Firestore
                val result = userRepository.updateUserProfile(userId, editedName, editedEmail)

                result.onSuccess {
                    // Save to local preferences
                    preferences.setUserName(editedName)
                    preferences.setUserEmail(editedEmail)

                    Log.d("SettingsViewModel", "Profile updated successfully")
                    showEditProfileDialog = false
                    isLoading = false
                }.onFailure { exception ->
                    errorMessage = "Failed to update profile: ${exception.message}"
                    Log.e("SettingsViewModel", "Profile update failed: ${exception.message}", exception)
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Error updating profile: ${e.message}"
                Log.e("SettingsViewModel", "Profile update error: ${e.message}", e)
                isLoading = false
            }
        }
    }

    fun showImagePicker() {
        showImagePicker = true
        errorMessage = null
        Log.d("SettingsViewModel", "Showing image picker")
    }

    fun hideImagePicker() {
        showImagePicker = false
        errorMessage = null
        Log.d("SettingsViewModel", "Hiding image picker")
    }

    fun updateImageUri(uri: Uri?) {
        selectedImageUri = uri
        Log.d("SettingsViewModel", "Image URI updated: $uri")
    }

    fun uploadProfileImage() {
        selectedImageUri?.let { uri ->
            isLoading = true
            errorMessage = null
            Log.d("SettingsViewModel", "Starting image upload for URI: $uri")

            viewModelScope.launch {
                try {
                    // Try the InputStream method first (more reliable for content URIs)
                    val uploadResult = cloudinaryManager.uploadImageWithInputStream(uri)

                    uploadResult.onSuccess { imageUrl ->
                        Log.d("SettingsViewModel", "Image uploaded to Cloudinary: $imageUrl")

                        // Ensure HTTPS URL
                        val secureImageUrl = ensureHttpsUrl(imageUrl)
                        Log.d("SettingsViewModel", "Using secure URL: $secureImageUrl")

                        // Update Firestore with new image URL
                        val userId = userRepository.getCurrentUserId()
                        Log.d("SettingsViewModel", "Updating profile image for user: $userId")

                        val updateResult = userRepository.updateProfileImage(userId, secureImageUrl)

                        updateResult.onSuccess {
                            // Save to local preferences
                            preferences.setProfileImageUrl(secureImageUrl)

                            Log.d("SettingsViewModel", "Profile image updated successfully")
                            showImagePicker = false
                            selectedImageUri = null
                            isLoading = false
                        }.onFailure { exception ->
                            errorMessage = "Failed to update profile image: ${exception.message}"
                            Log.e("SettingsViewModel", "Profile image update failed: ${exception.message}", exception)
                            isLoading = false
                        }
                    }.onFailure { exception ->
                        // Fallback: Try the file method if InputStream fails
                        Log.w("SettingsViewModel", "InputStream method failed, trying file method: ${exception.message}")
                        tryFileUploadMethod(uri)
                    }
                } catch (e: Exception) {
                    errorMessage = "Error uploading image: ${e.message}"
                    Log.e("SettingsViewModel", "Image upload error: ${e.message}", e)
                    isLoading = false
                }
            }
        } ?: run {
            Log.e("SettingsViewModel", "No image URI selected for upload")
            showImagePicker = false
        }
    }

    private suspend fun tryFileUploadMethod(uri: Uri) {
        try {
            val uploadResult = cloudinaryManager.uploadImage(uri)

            uploadResult.onSuccess { imageUrl ->
                Log.d("SettingsViewModel", "Image uploaded to Cloudinary (file method): $imageUrl")

                // Ensure HTTPS URL
                val secureImageUrl = ensureHttpsUrl(imageUrl)

                val userId = userRepository.getCurrentUserId()
                val updateResult = userRepository.updateProfileImage(userId, secureImageUrl)

                updateResult.onSuccess {
                    preferences.setProfileImageUrl(secureImageUrl)
                    showImagePicker = false
                    selectedImageUri = null
                    isLoading = false
                }.onFailure { exception ->
                    errorMessage = "Failed to update profile image: ${exception.message}"
                    isLoading = false
                }
            }.onFailure { exception ->
                errorMessage = "Failed to upload image: ${exception.message}"
                Log.e("SettingsViewModel", "File upload method also failed: ${exception.message}", exception)
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Both upload methods failed: ${e.message}"
            isLoading = false
        }
    }

    private fun ensureHttpsUrl(url: String): String {
        return if (url.startsWith("http://")) {
            url.replace("http://", "https://")
        } else {
            url
        }
    }

    fun loadUserData() {
        isLoading = true
        viewModelScope.launch {
            try {
                val userId = userRepository.getCurrentUserId()
                Log.d("SettingsViewModel", "Loading user data for: $userId")

                val result = userRepository.getUser(userId)

                result.onSuccess { user ->
                    // Update local preferences with Firestore data
                    preferences.setUserName(user.name)
                    preferences.setUserEmail(user.email)
                    user.profileImageUrl?.let {
                        preferences.setProfileImageUrl(it)
                        Log.d("SettingsViewModel", "Loaded profile image URL: $it")
                    }
                    Log.d("SettingsViewModel", "User data loaded successfully")
                    isLoading = false
                }.onFailure { exception ->
                    errorMessage = "Failed to load user data: ${exception.message}"
                    Log.e("SettingsViewModel", "User data load failed: ${exception.message}", exception)
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Authentication error: ${e.message}"
                Log.e("SettingsViewModel", "User data load error: ${e.message}", e)
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun updateProfileImageUrl(url: String) {
        viewModelScope.launch {
            preferences.setProfileImageUrl(url)
        }
    }
}