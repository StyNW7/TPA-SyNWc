package edu.bluejack25_1.synwc.data.repository

import edu.bluejack25_1.synwc.data.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NoteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val notesCollection = db.collection("notes")

    suspend fun getNotes(userId: String): Result<List<Note>> {
        return try {
            println("DEBUG: Fetching notes for user: $userId")
            val snapshot = notesCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val notes = snapshot.documents.mapNotNull { doc ->
                try {
                    Note(
                        id = doc.id,
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
            println("DEBUG: Successfully fetched ${notes.size} notes")
            Result.success(notes)
        } catch (e: Exception) {
            println("DEBUG: Error fetching notes: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addNote(note: Note, userId: String): Result<String> {
        return try {
            val noteId = if (note.id.isBlank()) UUID.randomUUID().toString() else note.id
            val noteData = hashMapOf(
                "userId" to userId,
                "title" to note.title,
                "description" to note.description,
                "timestamp" to note.timestamp,
                "isCompleted" to note.isCompleted,
                "priority" to note.priority.name
            )

            println("DEBUG: Adding note to Firestore: $noteData")
            notesCollection.document(noteId).set(noteData).await()
            println("DEBUG: Successfully added note with ID: $noteId")
            Result.success(noteId)
        } catch (e: Exception) {
            println("DEBUG: Error adding note: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note, userId: String): Result<Unit> {
        return try {
            if (note.id.isBlank()) {
                throw IllegalArgumentException("Note ID cannot be blank for update")
            }

            val noteData = hashMapOf(
                "userId" to userId,
                "title" to note.title,
                "description" to note.description,
                "timestamp" to note.timestamp,
                "isCompleted" to note.isCompleted,
                "priority" to note.priority.name
            )

            println("DEBUG: Updating note ${note.id} in Firestore")
            notesCollection.document(note.id).set(noteData).await()
            println("DEBUG: Successfully updated note ${note.id}")
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
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error toggling note status: ${e.message}")
            Result.failure(e)
        }
    }
}