package edu.bluejack25_1.synwc.ui.viewmodel

import androidx.lifecycle.ViewModel
import edu.bluejack25_1.synwc.R
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
            imageRes = R.drawable.ic_book_nobg,
            title = "Simple & Minimalist To-do",
            description = "Write down all your tasks on your to-do list."
        ),
        OnboardingPage(
            imageRes = R.drawable.ic_message_nobg,
            title = "Daily Quotes for Inspiration",
            description = "Get fresh motivational quotes every day."
        ),
        OnboardingPage(
            imageRes = R.drawable.ic_quote_nobg,
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
