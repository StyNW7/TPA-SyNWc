package edu.bluejack25_1.synwc.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val joinDate: Long = System.currentTimeMillis(),
    val loginStreak: Int = 0,
    val todoStreak: Int = 0,
    val reflectionStreak: Int = 0,
    val lastLoginDate: String = "", // Start empty for new users
    val lastTodoDate: String = "", // Start empty until first todo completion
    val lastReflectionDate: String = "" // Start empty until first reflection
) {
    companion object {
        fun getCurrentDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        // Helper function to create a new user with proper initial values
        fun createNewUser(id: String, name: String, email: String): User {
            val currentDate = getCurrentDate()
            return User(
                id = id,
                name = name,
                email = email,
                joinDate = System.currentTimeMillis(),
                loginStreak = 1, // Start with 1 for registration day
                todoStreak = 0, // Start at 0, no todos completed yet
                reflectionStreak = 0, // Start at 0, no reflections yet
                lastLoginDate = currentDate, // Set to current date for login
                lastTodoDate = "", // Empty until first todo completion
                lastReflectionDate = "" // Empty until first reflection
            )
        }
    }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "profileImageUrl" to profileImageUrl,
            "joinDate" to joinDate,
            "loginStreak" to loginStreak,
            "todoStreak" to todoStreak,
            "reflectionStreak" to reflectionStreak,
            "lastLoginDate" to lastLoginDate,
            "lastTodoDate" to lastTodoDate,
            "lastReflectionDate" to lastReflectionDate
        )
    }

    // Helper function to check if user has activity recorded for a specific type
    fun hasActivityRecorded(activityType: String): Boolean {
        return when (activityType) {
            "login" -> lastLoginDate.isNotEmpty()
            "todo" -> lastTodoDate.isNotEmpty()
            "reflection" -> lastReflectionDate.isNotEmpty()
            else -> false
        }
    }
}