package edu.bluejack25_1.synwc.data.model

data class Note(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "description" to description,
            "timestamp" to timestamp,
            "isCompleted" to isCompleted,
            "priority" to priority.name
        )
    }

    enum class Priority {
        LOW, MEDIUM, HIGH
    }
}