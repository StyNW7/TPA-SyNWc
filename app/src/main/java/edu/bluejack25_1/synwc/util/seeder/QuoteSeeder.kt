package edu.bluejack25_1.synwc.util.seeder

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import kotlinx.coroutines.tasks.await

class QuoteSeeder {
    private val db: FirebaseFirestore = Firebase.firestore

    suspend fun seedQuotes() {
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
            ),
            mapOf(
                "text" to "Do not go where the path may lead, go instead where there is no path and leave a trail.",
                "author" to "Ralph Waldo Emerson",
                "category" to "daily"
            ),
            mapOf(
                "text" to "The best and most beautiful things in the world cannot be seen or even touched - they must be felt with the heart.",
                "author" to "Helen Keller",
                "category" to "daily"
            ),
            mapOf(
                "text" to "Keep your face always toward the sunshine - and shadows will fall behind you.",
                "author" to "Walt Whitman",
                "category" to "daily"
            ),
            mapOf(
                "text" to "You will face many defeats in life, but never let yourself be defeated.",
                "author" to "Maya Angelou",
                "category" to "daily"
            ),
            mapOf(
                "text" to "The greatest glory in living lies not in never falling, but in rising every time we fall.",
                "author" to "Nelson Mandela",
                "category" to "daily"
            ),
            mapOf(
                "text" to "In the end, it's not the years in your life that count. It's the life in your years.",
                "author" to "Abraham Lincoln",
                "category" to "daily"
            ),
            mapOf(
                "text" to "Never let the fear of striking out keep you from playing the game.",
                "author" to "Babe Ruth",
                "category" to "daily"
            ),
            mapOf(
                "text" to "Life is either a daring adventure or nothing at all.",
                "author" to "Helen Keller",
                "category" to "daily"
            ),
            mapOf(
                "text" to "Many of life's failures are people who did not realize how close they were to success when they gave up.",
                "author" to "Thomas A. Edison",
                "category" to "daily"
            ),
            mapOf(
                "text" to "You have brains in your head. You have feet in your shoes. You can steer yourself any direction you choose.",
                "author" to "Dr. Seuss",
                "category" to "daily"
            )
        )

        var successCount = 0
        var errorCount = 0

        quotes.forEachIndexed { index, quoteData ->
            try {
                // Use auto-generated document ID
                db.collection("quotes")
                    .add(quoteData)
                    .await()
                successCount++
                println("✅ Successfully added quote ${index + 1}: ${quoteData["text"]}")
            } catch (e: Exception) {
                errorCount++
                println("❌ Error adding quote ${index + 1}: ${e.message}")
            }
        }

        println("\n🎉 Seeding completed!")
        println("✅ Successfully added: $successCount quotes")
        println("❌ Failed to add: $errorCount quotes")
        println("📊 Total quotes in database: ${successCount}")
    }

    suspend fun clearQuotes() {
        try {
            val snapshot = db.collection("quotes").get().await()
            val batch = db.batch()

            snapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }

            batch.commit().await()
            println("🗑️ All quotes cleared from database")
        } catch (e: Exception) {
            println("❌ Error clearing quotes: ${e.message}")
        }
    }
}