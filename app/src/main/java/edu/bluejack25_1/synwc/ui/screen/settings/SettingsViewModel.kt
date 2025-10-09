//package edu.bluejack25_1.synwc.ui.screen.settings
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.viewModelScope
//import edu.bluejack25_1.synwc.data.local.ThemePreferences
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.launch
//
//class SettingsViewModel(application: Application) : AndroidViewModel(application) {
//    private val themePrefs = ThemePreferences(application)
//
//    val isDarkMode = themePrefs.isDarkMode
//        .stateIn(viewModelScope, SharingStarted.Lazily, false)
//
//    fun toggleTheme(enabled: Boolean) {
//        viewModelScope.launch {
//            themePrefs.setDarkMode(enabled)
//        }
//    }
//}
