package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.data.model.Quote
import kotlinx.coroutines.tasks.await
import java.util.*

class QuoteRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth = Firebase.auth

    companion object {
        private const val QUOTES_COLLECTION = "quotes"
        private const val USER_QUOTES_COLLECTION = "user_quotes"
    }

    suspend fun getDailyQuote(): Result<Quote> {
        return try {
            val quotesSnapshot = db.collection(QUOTES_COLLECTION)
                .get()
                .await()

            val quotes = quotesSnapshot.documents.mapNotNull { doc ->
                try {
                    Quote(
                        id = doc.id,
                        text = doc.getString("text") ?: "",
                        author = doc.getString("author") ?: "Unknown",
                        category = doc.getString("category") ?: "daily"
                    )
                } catch (e: Exception) {
                    null
                }
            }

            if (quotes.isNotEmpty()) {
                // Get a random quote
                val randomQuote = quotes.random()
                Result.success(randomQuote)
            } else {
                // Return default quotes if no quotes found in database
                val defaultQuotes = getDefaultQuotes()
                val randomQuote = defaultQuotes.random()
                Result.success(randomQuote)
            }
        } catch (e: Exception) {
            // Return default quotes in case of error
            val defaultQuotes = getDefaultQuotes()
            val randomQuote = defaultQuotes.random()
            Result.success(randomQuote)
        }
    }

    suspend fun saveFavoriteQuote(quote: Quote): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val quoteId = UUID.randomUUID().toString()

            val favoriteQuote = quote.copy(id = quoteId)

            db.collection(USER_QUOTES_COLLECTION)
                .document(userId)
                .collection("favorites")
                .document(quoteId)
                .set(favoriteQuote.toMap())
                .await()

            Result.success(quoteId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFavoriteQuotes(): Result<List<Quote>> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = db.collection(USER_QUOTES_COLLECTION)
                .document(userId)
                .collection("favorites")
                .get()
                .await()

            val quotes = snapshot.documents.mapNotNull { doc ->
                try {
                    Quote(
                        id = doc.id,
                        text = doc.getString("text") ?: "",
                        author = doc.getString("author") ?: "Unknown",
                        category = doc.getString("category") ?: "favorite"
                    )
                } catch (e: Exception) {
                    null
                }
            }
            Result.success(quotes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchQuotes(query: String): Result<List<Quote>> {
        return try {
            if (query.isBlank()) {
                return Result.success(emptyList())
            }

            val snapshot = db.collection(QUOTES_COLLECTION)
                .get()
                .await()

            val allQuotes = snapshot.documents.mapNotNull { doc ->
                try {
                    Quote(
                        id = doc.id,
                        text = doc.getString("text") ?: "",
                        author = doc.getString("author") ?: "Unknown",
                        category = doc.getString("category") ?: "daily"
                    )
                } catch (e: Exception) {
                    null
                }
            }

            // Filter quotes locally based on search query
            val filteredQuotes = allQuotes.filter { quote ->
                quote.text.contains(query, ignoreCase = true) ||
                        quote.author.contains(query, ignoreCase = true)
            }

            Result.success(filteredQuotes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getDefaultQuotes(): List<Quote> {
        return listOf(
            Quote(
                id = "default1",
                text = "The only way to do great work is to love what you do.",
                author = "Steve Jobs",
                category = "daily"
            ),
            Quote(
                id = "default2",
                text = "Life is what happens to you while you're busy making other plans.",
                author = "John Lennon",
                category = "daily"
            ),
            Quote(
                id = "default3",
                text = "The future belongs to those who believe in the beauty of their dreams.",
                author = "Eleanor Roosevelt",
                category = "daily"
            ),
            Quote(
                id = "default4",
                text = "It is during our darkest moments that we must focus to see the light.",
                author = "Aristotle",
                category = "daily"
            ),
            Quote(
                id = "default5",
                text = "Whoever is happy will make others happy too.",
                author = "Anne Frank",
                category = "daily"
            )
        )
    }
}