package edu.bluejack25_1.synwc.data.model

import com.google.firebase.database.Exclude
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    @Exclude val password: String = "", // Excluded from Firebase serialization
    val profileImageUrl: String? = null,
    val joinDate: Long = System.currentTimeMillis(),
    val streakCount: Int = 0,
    val lastActiveDate: String = getCurrentDate()
) {
    companion object {
        fun getCurrentDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "profileImageUrl" to profileImageUrl,
            "joinDate" to joinDate,
            "streakCount" to streakCount,
            "lastActiveDate" to lastActiveDate
        )
    }
}