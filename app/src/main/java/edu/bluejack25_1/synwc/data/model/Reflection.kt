package edu.bluejack25_1.synwc.data.model

import com.google.firebase.database.Exclude
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Reflection(
    val id: String = "",
    val userId: String = "", // Reference to user who owns this reflection
    val question: String = "",
    val answer: String = "",
    val date: String = getCurrentDate()
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
            "userId" to userId,
            "question" to question,
            "answer" to answer,
            "date" to date
        )
    }
}