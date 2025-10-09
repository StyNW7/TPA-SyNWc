package edu.bluejack25_1.synwc.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun onGetStarted() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Future: Save onboarding completion to local (DataStore)
            kotlinx.coroutines.delay(500)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}

data class OnboardingUiState(
    val isLoading: Boolean = false
)