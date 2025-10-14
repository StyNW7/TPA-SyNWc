package edu.bluejack25_1.synwc.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack25_1.synwc.data.preferences.AppPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(context: Context) : ViewModel() {
    private val preferences = AppPreferences(context)

    // Theme state
    val themeMode = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

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

    var editedName by mutableStateOf("")
        private set

    var editedEmail by mutableStateOf("")
        private set

    // Simple approach - just use one mutable state without backing property
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    // Actions
    fun setThemeMode(theme: String) {
        viewModelScope.launch {
            preferences.setThemeMode(theme)
        }
    }

    fun showEditProfile() {
        editedName = userName.value
        editedEmail = userEmail.value
        showEditProfileDialog = true
    }

    fun hideEditProfile() {
        showEditProfileDialog = false
    }

    fun updateEditedName(name: String) {
        editedName = name
    }

    fun updateEditedEmail(email: String) {
        editedEmail = email
    }

    fun saveProfile() {
        viewModelScope.launch {
            preferences.setUserName(editedName)
            preferences.setUserEmail(editedEmail)
            showEditProfileDialog = false
        }
    }

    fun showImagePicker() {
        showImagePicker = true
    }

    fun hideImagePicker() {
        showImagePicker = false
    }

    // Function to update the selected image URI - using different name to avoid conflict
    fun updateImageUri(uri: Uri?) {
        selectedImageUri = uri
    }

    fun uploadProfileImage() {
        viewModelScope.launch {
            // Here you would upload to Cloudinary
            // For now, we'll just save the local URI
            selectedImageUri?.let { uri ->
                preferences.setProfileImageUrl(uri.toString())
            }
            showImagePicker = false
            selectedImageUri = null
        }
    }
}