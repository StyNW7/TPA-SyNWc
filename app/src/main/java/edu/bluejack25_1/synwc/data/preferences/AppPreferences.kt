package edu.bluejack25_1.synwc.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferences(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
        private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    // Theme preferences
    val themeMode: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[THEME_MODE] ?: "system"
        }

    suspend fun setThemeMode(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = theme
        }
    }

    // User profile preferences
    val userName: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[USER_NAME] ?: ""
        }

    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }

    val userEmail: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL] ?: ""
        }

    suspend fun setUserEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
        }
    }

    val profileImageUrl: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PROFILE_IMAGE_URL] ?: ""
        }

    suspend fun setProfileImageUrl(imageUrl: String) {
        dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_URL] = imageUrl
        }
    }

    // First launch flag
    val isFirstLaunch: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH] ?: true
        }

    suspend fun setFirstLaunch(isFirstLaunch: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = isFirstLaunch
        }
    }

    // Clear all preferences (for logout)
    suspend fun clearPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

}