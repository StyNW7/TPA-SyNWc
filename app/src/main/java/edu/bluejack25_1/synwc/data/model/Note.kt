package edu.bluejack25_1.synwc.data.model

import com.google.firebase.database.Exclude

data class Note(
    val id: String = "",
    val userId: String = "", // Reference to user who owns this note
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "description" to description,
            "timestamp" to timestamp,
            "isCompleted" to isCompleted
        )
    }
    enum class Priority {
        LOW, MEDIUM, HIGH
    }
}