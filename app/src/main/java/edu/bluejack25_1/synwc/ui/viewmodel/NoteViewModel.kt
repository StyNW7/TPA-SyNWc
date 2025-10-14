// ui/viewmodel/NoteViewModel.kt
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

    fun loadNotes(userId: String) {
        viewModelScope.launch {
            isLoading = true
            val result = noteRepository.getNotes(userId)
            result.onSuccess { loadedNotes ->
                notes = loadedNotes
                applyFilters()
                errorMessage = null
            }.onFailure {
                errorMessage = "Failed to load notes: ${it.message}"
            }
            isLoading = false
        }
    }

    fun addNote(userId: String) {
        if (noteTitle.isBlank()) {
            errorMessage = "Please enter a title"
            return
        }

        viewModelScope.launch {
            isLoading = true
            val newNote = Note(
                title = noteTitle,
                description = noteDescription,
                priority = notePriority,
                isCompleted = false
            )

            val result = noteRepository.addNote(newNote, userId)
            result.onSuccess {
                updateTodoStreak()
                loadNotes(userId)
                resetForm()
                showAddEditDialog = false
                errorMessage = null
            }.onFailure {
                errorMessage = "Failed to add note: ${it.message}"
            }
            isLoading = false
        }
    }

    fun updateNote(userId: String) {
        if (noteTitle.isBlank()) {
            errorMessage = "Please enter a title"
            return
        }

        viewModelScope.launch {
            isLoading = true
            editingNote?.let { note ->
                val updatedNote = note.copy(
                    title = noteTitle,
                    description = noteDescription,
                    priority = notePriority
                )

                val result = noteRepository.updateNote(updatedNote, userId)
                result.onSuccess {
                    loadNotes(userId)
                    resetForm()
                    showAddEditDialog = false
                    editingNote = null
                    errorMessage = null
                }.onFailure {
                    errorMessage = "Failed to update note: ${it.message}"
                }
            }
            isLoading = false
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            isLoading = true
            val result = noteRepository.deleteNote(noteId)
            result.onSuccess {
                loadNotes("") // userId will be handled in the actual call
                errorMessage = null
            }.onFailure {
                errorMessage = "Failed to delete note: ${it.message}"
            }
            isLoading = false
        }
    }

    fun toggleNoteStatus(noteId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            val result = noteRepository.toggleNoteStatus(noteId, isCompleted)
            result.onSuccess {
                loadNotes("") // userId will be handled in the actual call
                if (isCompleted) {
                    updateTodoStreak()
                }
            }.onFailure {
                errorMessage = "Failed to update note status: ${it.message}"
            }
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

    private fun updateTodoStreak() {
        viewModelScope.launch {
            streakRepository.updateTodoStreak()
        }
    }

    fun clearError() {
        errorMessage = null
    }
}

enum class NoteFilter {
    ALL, ACTIVE, COMPLETED
}