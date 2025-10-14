// data/repository/NoteRepository.kt
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
            val snapshot = notesCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val notes = snapshot.documents.map { doc ->
                Note(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                    isCompleted = doc.getBoolean("isCompleted") ?: false,
                    priority = Note.Priority.valueOf(doc.getString("priority") ?: "MEDIUM")
                )
            }
            Result.success(notes)
        } catch (e: Exception) {
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

            notesCollection.document(noteId).set(noteData).await()
            Result.success(noteId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note, userId: String): Result<Unit> {
        return try {
            val noteData = hashMapOf(
                "userId" to userId,
                "title" to note.title,
                "description" to note.description,
                "timestamp" to note.timestamp,
                "isCompleted" to note.isCompleted,
                "priority" to note.priority.name
            )

            notesCollection.document(note.id).set(noteData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            notesCollection.document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleNoteStatus(noteId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            notesCollection.document(noteId).update("isCompleted", isCompleted).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}