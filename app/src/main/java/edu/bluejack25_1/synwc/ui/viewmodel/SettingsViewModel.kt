package edu.bluejack25_1.synwc.ui.viewmodel

import android.content.Context
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

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var editedName by mutableStateOf("")
        private set

    var editedEmail by mutableStateOf("")
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
                // Save to local preferences
                preferences.setUserName(editedName)
                preferences.setUserEmail(editedEmail)

                // Simulate API call delay
                kotlinx.coroutines.delay(1000)

                showEditProfileDialog = false
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error updating profile: ${e.message}"
                isLoading = false
            }
        }
    }

    fun loadUserData() {
        isLoading = true
        viewModelScope.launch {
            try {
                // Simulate loading data
                kotlinx.coroutines.delay(1000)
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error loading data: ${e.message}"
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}