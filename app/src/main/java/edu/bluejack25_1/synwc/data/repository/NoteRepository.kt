package edu.bluejack25_1.synwc.data.repository

import edu.bluejack25_1.synwc.data.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NoteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val notesCollection = db.collection("notes")
    private val auth = Firebase.auth
    private val streakRepository = StreakRepository()

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw Exception("User not authenticated")
    }

    suspend fun getNotes(userId: String): Result<List<Note>> {
        return try {
            println("DEBUG: Fetching notes for user: $userId")
            val snapshot = notesCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val notes = snapshot.documents.mapNotNull { doc ->
                try {
                    Note(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        priority = try {
                            Note.Priority.valueOf(doc.getString("priority") ?: "MEDIUM")
                        } catch (e: Exception) {
                            Note.Priority.MEDIUM
                        }
                    )
                } catch (e: Exception) {
                    println("DEBUG: Error parsing note document ${doc.id}: ${e.message}")
                    null
                }
            }

            // Sort manually on client side
            val sortedNotes = notes.sortedByDescending { it.timestamp }

            println("DEBUG: Successfully fetched ${sortedNotes.size} notes")
            Result.success(sortedNotes)
        } catch (e: Exception) {
            println("DEBUG: Error fetching notes: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addNote(note: Note): Result<String> {
        return try {
            val noteId = if (note.id.isBlank()) UUID.randomUUID().toString() else note.id
            val noteData = note.toMap()

            println("DEBUG: Adding note to Firestore: $noteData")
            notesCollection.document(noteId).set(noteData).await()
            println("DEBUG: Successfully added note with ID: $noteId")
            Result.success(noteId)
        } catch (e: Exception) {
            println("DEBUG: Error adding note: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note): Result<Unit> {
        return try {
            if (note.id.isBlank()) {
                throw IllegalArgumentException("Note ID cannot be blank for update")
            }

            val noteData = note.toMap()

            println("DEBUG: Updating note ${note.id} in Firestore")
            notesCollection.document(note.id).set(noteData).await()
            println("DEBUG: Successfully updated note ${note.id}")

            // Update todo streak if note is being marked as completed
            if (note.isCompleted) {
                val userId = getCurrentUserId()
                streakRepository.updateTodoStreak(userId)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error updating note: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            if (noteId.isBlank()) {
                throw IllegalArgumentException("Note ID cannot be blank for deletion")
            }

            println("DEBUG: Deleting note $noteId from Firestore")
            notesCollection.document(noteId).delete().await()
            println("DEBUG: Successfully deleted note $noteId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error deleting note: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun toggleNoteStatus(noteId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            if (noteId.isBlank()) {
                throw IllegalArgumentException("Note ID cannot be blank for status toggle")
            }

            println("DEBUG: Toggling note $noteId status to $isCompleted")
            notesCollection.document(noteId).update("isCompleted", isCompleted).await()
            println("DEBUG: Successfully toggled note $noteId status")

            // Update todo streak if note is being marked as completed
            if (isCompleted) {
                val userId = getCurrentUserId()
                streakRepository.updateTodoStreak(userId)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error toggling note status: ${e.message}")
            Result.failure(e)
        }
    }

    // Helper function to get notes for current user
    suspend fun getNotesForCurrentUser(): Result<List<Note>> {
        return try {
            val userId = getCurrentUserId()
            getNotes(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}