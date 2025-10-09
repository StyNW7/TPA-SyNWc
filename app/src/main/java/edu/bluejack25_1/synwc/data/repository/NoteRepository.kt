package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.data.model.Note
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NoteRepository {
    private val database: FirebaseDatabase = Firebase.database
    private val auth = Firebase.auth

    companion object {
        private const val NOTES_PATH = "notes"
    }

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw Exception("User not authenticated")
    }

    suspend fun createNote(note: Note): Result<String> {
        return try {
            val userId = getCurrentUserId()
            val noteId = UUID.randomUUID().toString()
            val noteWithId = note.copy(id = noteId, userId = userId)

            database.reference
                .child(NOTES_PATH)
                .child(userId)
                .child(noteId)
                .setValue(noteWithId.toMap())
                .await()

            Result.success(noteId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            database.reference
                .child(NOTES_PATH)
                .child(userId)
                .child(note.id)
                .setValue(note.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            database.reference
                .child(NOTES_PATH)
                .child(userId)
                .child(noteId)
                .removeValue()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNotesFlow(): Flow<List<Note>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = mutableListOf<Note>()
                snapshot.children.forEach { child ->
                    val note = child.getValue(Note::class.java)
                    note?.let { notes.add(it) }
                }
                // Sort by timestamp (newest first)
                notes.sortByDescending { it.timestamp }
                trySend(notes)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        database.reference
            .child(NOTES_PATH)
            .child(userId)
            .addValueEventListener(listener)

        awaitClose {
            database.reference
                .child(NOTES_PATH)
                .child(userId)
                .removeEventListener(listener)
        }
    }
}