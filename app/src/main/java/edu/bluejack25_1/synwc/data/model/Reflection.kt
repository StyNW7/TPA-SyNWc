package edu.bluejack25_1.synwc.data.model

data class Reflection(
    val id: String = "",
    val userId: String = "",
    val question: String = "",
    val answer: String = "",
    val date: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "question" to question,
            "answer" to answer,
            "date" to date
        )
    }
}