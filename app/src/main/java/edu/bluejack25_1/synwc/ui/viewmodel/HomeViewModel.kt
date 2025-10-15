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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {
    private val quoteRepository = QuoteRepository()
    private val reflectionRepository = ReflectionRepository()

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

    // Common States
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            isLoading = true
            try {
                loadDailyQuote()
                loadDailyReflectionQuestion()
                loadReflectionHistory()
                loadFavoriteQuotes()
            } catch (e: Exception) {
                errorMessage = "Failed to load initial data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Quote Functions
    fun loadDailyQuote() {
        viewModelScope.launch {
            isLoading = true
            try {
                val result = quoteRepository.getDailyQuote()
                result.onSuccess { quote ->
                    currentQuote = quote
                    errorMessage = null
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
            currentQuote?.let { quote ->
                try {
                    val result = quoteRepository.saveFavoriteQuote(quote)
                    result.onSuccess { quoteId ->
                        errorMessage = null
                        // Reload favorites to include the new one
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
            try {
                val result = quoteRepository.getFavoriteQuotes()
                result.onSuccess { quotes ->
                    favoriteQuotes = quotes
                    errorMessage = null
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
            try {
                val result = quoteRepository.searchQuotes(searchQuery)
                result.onSuccess { quotes ->
                    searchResults = quotes
                    errorMessage = null
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
            try {
                val result = reflectionRepository.getDailyQuestion()
                result.onSuccess { question ->
                    dailyQuestion = question
                    reflectionAnswer = "" // Clear previous answer
                    errorMessage = null
                }.onFailure { exception ->
                    errorMessage = "Failed to load reflection question: ${exception.message}"
                    // Set a default question if loading fails
                    dailyQuestion = "What are you grateful for today?"
                }
            } catch (e: Exception) {
                errorMessage = "Exception loading reflection question: ${e.message}"
                dailyQuestion = "What made you smile today?"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveReflection() {
        if (reflectionAnswer.isBlank()) {
            errorMessage = "Please write your reflection before saving"
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val result = reflectionRepository.saveReflection(dailyQuestion, reflectionAnswer)
                result.onSuccess {
                    errorMessage = null
                    reflectionAnswer = "" // Clear answer after saving
                    loadReflectionHistory() // Refresh history
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
            try {
                val result = reflectionRepository.getReflectionHistory()
                result.onSuccess { reflections ->
                    reflectionHistory = reflections
                    errorMessage = null
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

    fun updateReflectionAnswer(answer: String) {
        reflectionAnswer = answer
    }

    fun toggleReflectionHistory() {
        showReflectionHistory = !showReflectionHistory
        if (showReflectionHistory) {
            loadReflectionHistory()
        }
    }

    // Common Functions
    fun clearError() {
        errorMessage = null
    }
}