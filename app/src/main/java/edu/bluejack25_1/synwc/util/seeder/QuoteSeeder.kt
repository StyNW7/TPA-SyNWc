package edu.bluejack25_1.synwc.util.seeder

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class QuoteSeeder {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun seedQuotes(): Boolean {
        val quotes = listOf(
            mapOf(
                "text" to "The only way to do great work is to love what you do.",
                "author" to "Steve Jobs",
                "category" to "daily"
            ),
            mapOf(
                "text" to "Life is what happens to you while you're busy making other plans.",
                "author" to "John Lennon",
                "category" to "daily"
            ),
            mapOf(
                "text" to "The future belongs to those who believe in the beauty of their dreams.",
                "author" to "Eleanor Roosevelt",
                "category" to "daily"
            ),
            mapOf(
                "text" to "It is during our darkest moments that we must focus to see the light.",
                "author" to "Aristotle",
                "category" to "daily"
            ),
            mapOf(
                "text" to "Whoever is happy will make others happy too.",
                "author" to "Anne Frank",
                "category" to "daily"
            ),
            mapOf(
                "text" to "Be yourself; everyone else is already taken.",
                "author" to "Oscar Wilde",
                "category" to "daily"
            ),
            mapOf(
                "text" to "You must be the change you wish to see in the world.",
                "author" to "Mahatma Gandhi",
                "category" to "daily"
            ),
            mapOf(
                "text" to "Spread love everywhere you go. Let no one ever come to you without leaving happier.",
                "author" to "Mother Teresa",
                "category" to "daily"
            ),
            mapOf(
                "text" to "The only thing we have to fear is fear itself.",
                "author" to "Franklin D. Roosevelt",
                "category" to "daily"
            ),
            mapOf(
                "text" to "It is never too late to be what you might have been.",
                "author" to "George Eliot",
                "category" to "daily"
            )
        )

        var successCount = 0
        var errorCount = 0

        // First, let's check if we can read from Firestore
        try {
            println("Checking Firestore connection...")
            val testDoc = db.collection("test").document("connection_test")
            testDoc.set(mapOf("test" to true)).await()
            testDoc.delete().await()
            println("Firestore connection successful")
        } catch (e: Exception) {
            println("Firestore connection failed: ${e.message}")
            return false
        }

        quotes.forEachIndexed { index, quoteData ->
            try {
                // Use specific document ID instead of auto-generated
                val documentId = "quote_${index + 1}"
                db.collection("quotes")
                    .document(documentId)
                    .set(quoteData)
                    .await()
                successCount++
                println("Successfully added quote $documentId: ${quoteData["text"]?.take(50)}...")
            } catch (e: Exception) {
                errorCount++
                println("Error adding quote ${index + 1}: ${e.message}")
                e.printStackTrace()
            }
        }

        println("\nSeeding completed!")
        println("Successfully added: $successCount quotes")
        println("Failed to add: $errorCount quotes")

        // Verify by reading back
        if (successCount > 0) {
            try {
                println("\nVerifying quotes in database...")
                val snapshot = db.collection("quotes").get().await()
                println("Found ${snapshot.documents.size} quotes in database")
                snapshot.documents.forEach { doc ->
                    println("   - ${doc.id}: ${doc.getString("text")?.take(30)}...")
                }
            } catch (e: Exception) {
                println("Error verifying quotes: ${e.message}")
            }
        }

        return successCount > 0
    }

    suspend fun checkExistingQuotes(): Int {
        return try {
            val snapshot = db.collection("quotes").get().await()
            val count = snapshot.documents.size
            println("Found $count existing quotes in database")
            snapshot.documents.forEach { doc ->
                println("   - ${doc.id}: ${doc.getString("text")?.take(30)}...")
            }
            count
        } catch (e: Exception) {
            println("Error checking existing quotes: ${e.message}")
            -1
        }
    }

    suspend fun clearQuotes(): Boolean {
        return try {
            val snapshot = db.collection("quotes").get().await()
            val batch = db.batch()

            snapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }

            batch.commit().await()
            println("🗑All quotes cleared from database")
            true
        } catch (e: Exception) {
            println("Error clearing quotes: ${e.message}")
            false
        }
    }
}