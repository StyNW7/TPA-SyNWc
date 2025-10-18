package edu.bluejack25_1.synwc.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack25_1.synwc.data.model.Quote
import edu.bluejack25_1.synwc.data.model.Reflection
import edu.bluejack25_1.synwc.data.model.User
import edu.bluejack25_1.synwc.data.repository.QuoteRepository
import edu.bluejack25_1.synwc.data.repository.StreakRepository
import edu.bluejack25_1.synwc.data.repository.ReflectionRepository
import edu.bluejack25_1.synwc.data.repository.UserRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {

    private val quoteRepository = QuoteRepository()
    private val reflectionRepository = ReflectionRepository()
    private val streakRepository = StreakRepository()
    private val userRepository = UserRepository()

    // Quote States
    var currentQuote by mutableStateOf<Quote?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var searchResults by mutableStateOf<List<Quote>>(emptyList())
        private set

    var showFavoriteQuotes by mutableStateOf(false)
        private set

    var favoriteQuotes by mutableStateOf<List<Quote>>(emptyList())
        private set

    // Reflection States
    var dailyQuestion by mutableStateOf("")
        private set

    var reflectionAnswer by mutableStateOf("")
        private set

    var reflectionHistory by mutableStateOf<List<Reflection>>(emptyList())
        private set

    var showReflectionHistory by mutableStateOf(false)
        private set

    var hasReflectedToday by mutableStateOf(false)
        private set

    var questionHistory by mutableStateOf<List<Pair<String, String>>>(emptyList())
        private set

    // User State
    var currentUser by mutableStateOf<User?>(null)
        private set

    // Common States
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    private var lastQuestionDate by mutableStateOf("")

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {

                // Load all data in parallel
                val quoteJob = launch { loadDailyQuote() }
                val reflectionJob = launch {
                    checkAndUpdateDailyQuestion()
                    checkIfReflectedToday()
                    loadReflectionHistory()
                }
                val favoritesJob = launch { loadFavoriteQuotes() }
                val userJob = launch { loadCurrentUser() }

                // Wait for all to complete
                quoteJob.join()
                reflectionJob.join()
                favoritesJob.join()
                userJob.join()

            } catch (e: Exception) {
                errorMessage = "Failed to load initial data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun checkAndUpdateDailyQuestion() {
        val currentDate = getCurrentDate()

        if (lastQuestionDate != currentDate) {
            loadDailyReflectionQuestion()
            lastQuestionDate = currentDate
        } else if (dailyQuestion.isEmpty()) {
            loadDailyReflectionQuestion()
        }

    }

    // User Functions
    fun loadCurrentUser() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val currentUserId = userRepository.getCurrentUserId()
                val result = userRepository.getUser(currentUserId)
                result.onSuccess { user ->
                    currentUser = user
                }.onFailure { exception ->
                    errorMessage = "Failed to load user data: ${exception.message}"
                    currentUser = null
                }
            } catch (e: Exception) {
                errorMessage = "Exception loading user data: ${e.message}"
                currentUser = null
            } finally {
                isLoading = false
            }
        }
    }

    fun updateUserStreaks() {
        viewModelScope.launch {
            try {
                streakRepository.updateReflectionStreak()
                loadCurrentUser()
            } catch (e: Exception) {
                errorMessage = "Failed to update streaks: ${e.message}"
            }
        }
    }

    // Quote Functions
    fun loadDailyQuote() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = quoteRepository.getDailyQuote()
                result.onSuccess { quote ->
                    currentQuote = quote
                }.onFailure { exception ->
                    errorMessage = "Failed to load quote: ${exception.message}"
                }
            } catch (e: Exception) {
                errorMessage = "Exception loading quote: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveFavoriteQuote() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            currentQuote?.let { quote ->
                try {
                    val result = quoteRepository.saveFavoriteQuote(quote)
                    result.onSuccess {
                        successMessage = "Quote saved to favorites!"
                        loadFavoriteQuotes()
                    }.onFailure { exception ->
                        errorMessage = "Failed to save favorite: ${exception.message}"
                    }
                } catch (e: Exception) {
                    errorMessage = "Exception saving favorite: ${e.message}"
                }
            } ?: run {
                errorMessage = "No quote to save as favorite"
            }
            isLoading = false
        }
    }

    fun loadFavoriteQuotes() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = quoteRepository.getFavoriteQuotes()
                result.onSuccess { quotes ->
                    favoriteQuotes = quotes
                }.onFailure { exception ->
                    errorMessage = "Failed to load favorites: ${exception.message}"
                }
            } catch (e: Exception) {
                errorMessage = "Exception loading favorites: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun searchQuotes() {
        if (searchQuery.isBlank()) {
            searchResults = emptyList()
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = quoteRepository.searchQuotes(searchQuery)
                result.onSuccess { quotes ->
                    searchResults = quotes
                }.onFailure { exception ->
                    errorMessage = "Failed to search quotes: ${exception.message}"
                    searchResults = emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Exception searching quotes: ${e.message}"
                searchResults = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        if (query.isBlank()) {
            searchResults = emptyList()
        }
    }

    fun toggleFavoriteQuotes() {
        showFavoriteQuotes = !showFavoriteQuotes
        if (showFavoriteQuotes) {
            loadFavoriteQuotes()
        }
    }

    // Reflection Functions
    fun loadDailyReflectionQuestion() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = reflectionRepository.getDailyQuestion()
                result.onSuccess { question ->
                    dailyQuestion = question
                    reflectionAnswer = "" // Clear previous answer
                    lastQuestionDate = getCurrentDate()
                }.onFailure { exception ->
                    errorMessage = "Failed to load reflection question: ${exception.message}"
                    // Set a fallback question if loading fails
                    dailyQuestion = getFallbackQuestion()
                }
            } catch (e: Exception) {
                errorMessage = "Exception loading reflection question: ${e.message}"
                dailyQuestion = getFallbackQuestion()
            } finally {
                isLoading = false
            }
        }
    }

    private fun getFallbackQuestion(): String {
        // Use date-based fallback to ensure consistency
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val questions = listOf(
            "What are you grateful for today?",
            "What was the highlight of your day?",
            "What challenged you today and how did you overcome it?",
            "What did you learn today?",
            "How did you show kindness today?"
        )
        return questions[dayOfYear % questions.size]
    }

    fun saveReflection() {
        if (reflectionAnswer.isBlank()) {
            errorMessage = "Please write your reflection before saving"
            return
        }

        if (reflectionAnswer.length < 10) {
            errorMessage = "Please write a more detailed reflection (at least 10 characters)"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = reflectionRepository.saveReflection(dailyQuestion, reflectionAnswer)
                result.onSuccess {
                    successMessage = "Reflection saved successfully!"
                    reflectionAnswer = "" // Clear answer after saving
                    hasReflectedToday = true
                    loadReflectionHistory() // Refresh history

                    // Update reflection streak after saving reflection
                    updateUserStreaks()
                }.onFailure { exception ->
                    errorMessage = "Failed to save reflection: ${exception.message}"
                }
            } catch (e: Exception) {
                errorMessage = "Exception saving reflection: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadReflectionHistory() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = reflectionRepository.getReflectionHistory()
                result.onSuccess { reflections ->
                    reflectionHistory = reflections
                }.onFailure { exception ->
                    errorMessage = "Failed to load reflection history: ${exception.message}"
                }
            } catch (e: Exception) {
                errorMessage = "Exception loading reflection history: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadQuestionHistory() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = reflectionRepository.getQuestionHistory()
                result.onSuccess { questions ->
                    questionHistory = questions
                }.onFailure { exception ->
                    errorMessage = "Failed to load question history: ${exception.message}"
                }
            } catch (e: Exception) {
                errorMessage = "Exception loading question history: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun checkIfReflectedToday() {
        try {
            val result = reflectionRepository.hasReflectedToday()
            result.onSuccess { hasReflected ->
                hasReflectedToday = hasReflected
            }.onFailure {
                hasReflectedToday = false
            }
        } catch (e: Exception) {
            hasReflectedToday = false
        }
    }

    fun updateReflectionAnswer(answer: String) {
        reflectionAnswer = answer
        // Clear error when user starts typing
        if (errorMessage?.contains("reflection") == true) {
            errorMessage = null
        }
    }

    fun toggleReflectionHistory() {
        showReflectionHistory = !showReflectionHistory
        if (showReflectionHistory) {
            loadReflectionHistory()
        }
    }

    // Streak Functions
    fun checkAndResetStreaks() {
        viewModelScope.launch {
            try {
                streakRepository.checkAndResetStreaks()
                // Reload user data to get updated streaks
                loadCurrentUser()
            } catch (e: Exception) {
                errorMessage = "Failed to check streaks: ${e.message}"
            }
        }
    }

    fun updateTodoStreak() {
        viewModelScope.launch {
            try {
                streakRepository.updateTodoStreak()
                loadCurrentUser()
            } catch (e: Exception) {
                errorMessage = "Failed to update todo streak: ${e.message}"
            }
        }
    }

    // Common Functions
    fun clearError() {
        errorMessage = null
    }

    fun clearSuccessMessage() {
        successMessage = null
    }

    fun refreshAllData() {
        loadInitialData()
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}