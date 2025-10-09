package edu.bluejack25_1.synwc.data.model

import com.google.firebase.database.Exclude

data class Note(
    val id: String = "",
    val userId: String = "", // Reference to user who owns this note
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "content" to content,
            "timestamp" to timestamp,
            "isCompleted" to isCompleted
        )
    }
}