package edu.bluejack25_1.synwc.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

class OnboardingViewModel : ViewModel() {

    private val pages = listOf(
        OnboardingPage(
            imageRes = edu.bluejack25_1.synwc.R.drawable.ic_book,
            title = "Simple & Minimalist To-do List",
            description = "Write down all your tasks on your to-do list."
        ),
        OnboardingPage(
            imageRes = edu.bluejack25_1.synwc.R.drawable.ic_message,
            title = "Daily Quotes for Inspiration",
            description = "Get fresh motivational quotes every day."
        ),
        OnboardingPage(
            imageRes = edu.bluejack25_1.synwc.R.drawable.ic_quote,
            title = "Reflect & Grow",
            description = "Write reflections and track your daily progress."
        )
    )

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    fun nextPage() {
        if (_currentPage.value < pages.lastIndex) {
            _currentPage.value += 1
        }
    }

    fun skipToEnd() {
        _currentPage.value = pages.lastIndex
    }

    fun getPageData(index: Int): OnboardingPage = pages[index]
    fun getTotalPages(): Int = pages.size
}
