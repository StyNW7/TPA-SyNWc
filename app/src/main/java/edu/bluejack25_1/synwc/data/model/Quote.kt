package edu.bluejack25_1.synwc.data.model

data class Quote(
    val id: String = "",
    val text: String = "",
    val author: String = "",
    val isFavorite: Boolean = false,
    val category: String = "daily"
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "text" to text,
            "author" to author,
            "isFavorite" to isFavorite,
            "category" to category
        )
    }
}