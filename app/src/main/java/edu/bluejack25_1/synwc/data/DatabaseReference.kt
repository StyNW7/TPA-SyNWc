package edu.bluejack25_1.synwc.data

import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.google.firebase.Firebase

object DatabaseReference {
    private val database = Firebase.database
    private val auth = Firebase.auth

    // Root references
    fun usersRef() = database.getReference("users")
    fun notesRef() = database.getReference("notes")
    fun reflectionsRef() = database.getReference("reflections")
    fun quotesRef() = database.getReference("quotes")

    // User-specific references
    fun currentUserRef() = usersRef().child(getCurrentUserId())
    fun currentUserNotesRef() = notesRef().child(getCurrentUserId())
    fun currentUserReflectionsRef() = reflectionsRef().child(getCurrentUserId())

    // Helper method to get current user ID
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw Exception("User not authenticated")
    }

    // Generate unique IDs
    fun generateId(): String {
        return database.reference.push().key ?: System.currentTimeMillis().toString()
    }
}