package edu.bluejack25_1.synwc.data.model

import com.google.firebase.database.Exclude

data class Quote(
    val id: String = "",
    val text: String = "",
    val author: String = ""
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "text" to text,
            "author" to author
        )
    }
}