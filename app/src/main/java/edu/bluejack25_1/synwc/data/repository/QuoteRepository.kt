package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.data.model.Quote
import kotlinx.coroutines.tasks.await
import java.util.UUID

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
                .whereEqualTo("category", "daily")
                .get()
                .await()

            val quotes = quotesSnapshot.toObjects(Quote::class.java)
            if (quotes.isNotEmpty()) {
                val randomQuote = quotes.random()
                Result.success(randomQuote)
            } else {
                // Return a default quote if no quotes found
                Result.success(
                    Quote(
                        id = "default",
                        text = "The only way to do great work is to love what you do.",
                        author = "Steve Jobs",
                        category = "daily"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveFavoriteQuote(quote: Quote): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val quoteId = UUID.randomUUID().toString()
            val favoriteQuote = quote.copy(id = quoteId, isFavorite = true)

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

            val quotes = snapshot.toObjects(Quote::class.java)
            Result.success(quotes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchQuotes(query: String): Result<List<Quote>> {
        return try {
            val snapshot = db.collection(QUOTES_COLLECTION)
                .whereGreaterThanOrEqualTo("text", query)
                .whereLessThanOrEqualTo("text", query + "\uf8ff")
                .get()
                .await()

            val quotes = snapshot.toObjects(Quote::class.java)
            Result.success(quotes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}