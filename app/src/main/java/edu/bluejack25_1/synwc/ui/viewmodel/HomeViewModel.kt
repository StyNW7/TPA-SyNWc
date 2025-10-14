package edu.bluejack25_1.synwc.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack25_1.synwc.data.model.Quote
import edu.bluejack25_1.synwc.data.model.Reflection
import edu.bluejack25_1.synwc.data.repository.QuoteRepository
import edu.bluejack25_1.synwc.data.repository.ReflectionRepository
import edu.bluejack25_1.synwc.data.repository.StreakRepository
import edu.bluejack25_1.synwc.data.repository.UserRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val quoteRepository = QuoteRepository()
    private val reflectionRepository = ReflectionRepository()
    private val streakRepository = StreakRepository()
    private val userRepository = UserRepository()

    // States
    var currentQuote by mutableStateOf<Quote?>(null)
        private set

    var favoriteQuotes by mutableStateOf<List<Quote>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
        private set

    var searchResults by mutableStateOf<List<Quote>>(emptyList())
        private set

    var dailyQuestion by mutableStateOf("")
        private set

    var reflectionAnswer by mutableStateOf("")
        private set

    var reflectionHistory by mutableStateOf<List<Reflection>>(emptyList())
        private set

    var showReflectionHistory by mutableStateOf(false)
        private set

    var showFavoriteQuotes by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Initialize
    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            isLoading = true
            updateLoginStreak()
            loadDailyQuote()
            loadDailyReflectionQuestion()
            loadReflectionHistory()
            loadFavoriteQuotes()
            isLoading = false
        }
    }

    // Quote Functions
    fun loadDailyQuote() {
        viewModelScope.launch {
            val result = quoteRepository.getDailyQuote()
            result.onSuccess { quote ->
                currentQuote = quote
            }.onFailure {
                errorMessage = "Failed to load quote: ${it.message}"
            }
        }
    }

    fun saveFavoriteQuote() {
        viewModelScope.launch {
            currentQuote?.let { quote ->
                val result = quoteRepository.saveFavoriteQuote(quote)
                result.onSuccess {
                    loadFavoriteQuotes()
                }.onFailure {
                    errorMessage = "Failed to save favorite quote: ${it.message}"
                }
            }
        }
    }

    fun loadFavoriteQuotes() {
        viewModelScope.launch {
            val result = quoteRepository.getFavoriteQuotes()
            result.onSuccess { quotes ->
                favoriteQuotes = quotes
            }.onFailure {
                errorMessage = "Failed to load favorite quotes: ${it.message}"
            }
        }
    }

    fun searchQuotes() {
        if (searchQuery.isBlank()) return

        viewModelScope.launch {
            val result = quoteRepository.searchQuotes(searchQuery)
            result.onSuccess { quotes ->
                searchResults = quotes
            }.onFailure {
                errorMessage = "Failed to search quotes: ${it.message}"
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    // Reflection Functions
    fun loadDailyReflectionQuestion() {
        viewModelScope.launch {
            val result = reflectionRepository.getDailyReflectionQuestion()
            result.onSuccess { question ->
                dailyQuestion = question
            }.onFailure {
                errorMessage = "Failed to load reflection question: ${it.message}"
            }
        }
    }

    fun updateReflectionAnswer(answer: String) {
        reflectionAnswer = answer
    }

    fun saveReflection() {
        if (reflectionAnswer.isBlank()) {
            errorMessage = "Please write your reflection first"
            return
        }

        viewModelScope.launch {
            val result = reflectionRepository.saveReflection(dailyQuestion, reflectionAnswer)
            result.onSuccess {
                updateReflectionStreak()
                reflectionAnswer = ""
                loadReflectionHistory()
                errorMessage = null
            }.onFailure {
                errorMessage = "Failed to save reflection: ${it.message}"
            }
        }
    }

    fun loadReflectionHistory() {
        viewModelScope.launch {
            val result = reflectionRepository.getReflectionsHistory()
            result.onSuccess { reflections ->
                reflectionHistory = reflections
            }.onFailure {
                errorMessage = "Failed to load reflection history: ${it.message}"
            }
        }
    }

    // Streak Functions
    private fun updateLoginStreak() {
        viewModelScope.launch {
            streakRepository.updateLoginStreak()
        }
    }

    private fun updateReflectionStreak() {
        viewModelScope.launch {
            streakRepository.updateReflectionStreak()
        }
    }

    // UI State Functions
    fun toggleReflectionHistory() {
        showReflectionHistory = !showReflectionHistory
    }

    fun toggleFavoriteQuotes() {
        showFavoriteQuotes = !showFavoriteQuotes
    }

    fun clearError() {
        errorMessage = null
    }
}