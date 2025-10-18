package edu.bluejack25_1.synwc.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.bluejack25_1.synwc.data.model.Note
import edu.bluejack25_1.synwc.data.repository.NoteRepository
import edu.bluejack25_1.synwc.data.repository.StreakRepository
import kotlinx.coroutines.launch

class NoteViewModel : ViewModel() {
    private val noteRepository = NoteRepository()
    private val streakRepository = StreakRepository()

    // States
    var notes by mutableStateOf<List<Note>>(emptyList())
        private set

    var filteredNotes by mutableStateOf<List<Note>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var selectedFilter by mutableStateOf(NoteFilter.ALL)
        private set

    var showAddEditDialog by mutableStateOf(false)
        private set

    var editingNote by mutableStateOf<Note?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    // Form fields for add/edit
    var noteTitle by mutableStateOf("")
        private set

    var noteDescription by mutableStateOf("")
        private set

    var notePriority by mutableStateOf(Note.Priority.MEDIUM)
        private set

    // Store current user ID for refreshing
    private var currentUserId: String = ""

    fun loadNotes(userId: String) {
        if (userId.isBlank()) {
            errorMessage = "User ID is required to load notes"
            return
        }

        currentUserId = userId // Store for refresh operations

        viewModelScope.launch {
            isLoading = true
            println("DEBUG: Loading notes for user: $userId")
            try {
                val result = noteRepository.getNotes(userId)
                result.onSuccess { loadedNotes ->
                    println("DEBUG: Successfully loaded ${loadedNotes.size} notes")
                    notes = loadedNotes
                    applyFilters()
                    errorMessage = null
                }.onFailure { exception ->
                    val error = "Failed to load notes: ${exception.message}"
                    println("DEBUG: $error")
                    errorMessage = error
                    notes = emptyList()
                    applyFilters()
                }
            } catch (e: Exception) {
                val error = "Exception loading notes: ${e.message}"
                println("DEBUG: $error")
                errorMessage = error
                notes = emptyList()
                applyFilters()
            }
            isLoading = false
        }
    }

    fun addNote(userId: String) {
        if (userId.isBlank()) {
            errorMessage = "User ID is required to add note. Please make sure you're logged in."
            return
        }

        if (noteTitle.isBlank()) {
            errorMessage = "Please enter a title"
            return
        }

        viewModelScope.launch {
            isLoading = true
            println("DEBUG: Adding note for user: $userId")
            println("DEBUG: Note title: '$noteTitle', description: '$noteDescription'")

            try {
                val newNote = Note(
                    title = noteTitle.trim(),
                    description = noteDescription.trim(),
                    priority = notePriority,
                    isCompleted = false,
                    timestamp = System.currentTimeMillis(),
                    userId = userId
                )

                val result = noteRepository.addNote(newNote)
                result.onSuccess { noteId ->
                    println("DEBUG: Successfully added note with ID: $noteId")

                    updateTodoStreak(userId)

                    refreshNotes()
                    resetForm()
                    showAddEditDialog = false
                    errorMessage = null
                }.onFailure { exception ->
                    val error = "Failed to add note: ${exception.message}"
                    println("DEBUG: $error")
                    errorMessage = error
                }
            } catch (e: Exception) {
                val error = "Exception adding note: ${e.message}"
                println("DEBUG: $error")
                errorMessage = error
            }
            isLoading = false
        }
    }

    fun updateNote(userId: String) {
        if (userId.isBlank()) {
            errorMessage = "User ID is required to update note. Please make sure you're logged in."
            return
        }

        if (noteTitle.isBlank()) {
            errorMessage = "Please enter a title"
            return
        }

        viewModelScope.launch {
            isLoading = true
            editingNote?.let { note ->
                println("DEBUG: Updating note: ${note.id}")

                try {
                    val updatedNote = note.copy(
                        title = noteTitle.trim(),
                        description = noteDescription.trim(),
                        priority = notePriority,
                        timestamp = System.currentTimeMillis(),
                        userId = userId
                    )

                    val result = noteRepository.updateNote(updatedNote)
                    result.onSuccess {
                        println("DEBUG: Successfully updated note")
                        refreshNotes()
                        resetForm()
                        showAddEditDialog = false
                        editingNote = null
                        errorMessage = null
                    }.onFailure { exception ->
                        val error = "Failed to update note: ${exception.message}"
                        println("DEBUG: $error")
                        errorMessage = error
                    }
                } catch (e: Exception) {
                    val error = "Exception updating note: ${e.message}"
                    println("DEBUG: $error")
                    errorMessage = error
                }
            } ?: run {
                errorMessage = "No note selected for editing"
            }
            isLoading = false
        }
    }

    fun deleteNote(noteId: String) {
        if (noteId.isBlank()) {
            errorMessage = "Note ID is required for deletion"
            return
        }

        viewModelScope.launch {
            isLoading = true
            println("DEBUG: Deleting note: $noteId")
            try {
                val result = noteRepository.deleteNote(noteId)
                result.onSuccess {
                    println("DEBUG: Successfully deleted note")
                    errorMessage = null
                    refreshNotes()
                }.onFailure { exception ->
                    val error = "Failed to delete note: ${exception.message}"
                    println("DEBUG: $error")
                    errorMessage = error
                }
            } catch (e: Exception) {
                val error = "Exception deleting note: ${e.message}"
                println("DEBUG: $error")
                errorMessage = error
            }
            isLoading = false
        }
    }

    fun toggleNoteStatus(noteId: String, isCompleted: Boolean) {
        if (noteId.isBlank()) {
            errorMessage = "Note ID is required to toggle status"
            return
        }

        viewModelScope.launch {
            println("DEBUG: Toggling note $noteId to $isCompleted")
            try {
                val result = noteRepository.toggleNoteStatus(noteId, isCompleted)
                result.onSuccess {
                    println("DEBUG: Successfully toggled note status")
                    errorMessage = null
                    refreshNotes()
                }.onFailure { exception ->
                    val error = "Failed to update note status: ${exception.message}"
                    println("DEBUG: $error")
                    errorMessage = error
                }
            } catch (e: Exception) {
                val error = "Exception toggling note status: ${e.message}"
                println("DEBUG: $error")
                errorMessage = error
            }
        }
    }

    private fun refreshNotes() {
        if (currentUserId.isNotBlank()) {
            loadNotes(currentUserId)
        }
    }

    private suspend fun updateTodoStreak(userId: String) {
        try {
            val result = streakRepository.updateTodoStreak()
            result.onSuccess {
                println("DEBUG: Successfully updated todo streak")
            }.onFailure { exception ->
                println("DEBUG: Failed to update todo streak: ${exception.message}")
            }
        } catch (e: Exception) {
            println("DEBUG: Exception updating todo streak: ${e.message}")
        }
    }

    fun setFilter(filter: NoteFilter) {
        selectedFilter = filter
        applyFilters()
    }

    private fun applyFilters() {
        filteredNotes = when (selectedFilter) {
            NoteFilter.ALL -> notes
            NoteFilter.ACTIVE -> notes.filter { !it.isCompleted }
            NoteFilter.COMPLETED -> notes.filter { it.isCompleted }
        }.filter { note ->
            note.title.contains(searchQuery, ignoreCase = true) ||
                    note.description.contains(searchQuery, ignoreCase = true)
        }
        println("DEBUG: Applied filters. Total notes: ${notes.size}, Filtered: ${filteredNotes.size}")
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun showAddNoteDialog() {
        resetForm()
        editingNote = null
        showAddEditDialog = true
    }

    fun showEditNoteDialog(note: Note) {
        editingNote = note
        noteTitle = note.title
        noteDescription = note.description
        notePriority = note.priority
        showAddEditDialog = true
    }

    fun hideAddEditDialog() {
        showAddEditDialog = false
        editingNote = null
        resetForm()
    }

    fun updateNoteTitle(title: String) {
        noteTitle = title
    }

    fun updateNoteDescription(description: String) {
        noteDescription = description
    }

    fun updateNotePriority(priority: Note.Priority) {
        notePriority = priority
    }

    private fun resetForm() {
        noteTitle = ""
        noteDescription = ""
        notePriority = Note.Priority.MEDIUM
    }

    fun clearError() {
        errorMessage = null
    }
}

enum class NoteFilter {
    ALL, ACTIVE, COMPLETED
}