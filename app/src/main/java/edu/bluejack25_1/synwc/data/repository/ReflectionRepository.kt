package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.data.model.Reflection
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ReflectionRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth = Firebase.auth
    private val streakRepository = StreakRepository()

    companion object {
        private const val REFLECTIONS_COLLECTION = "reflections"
        private val reflectionQuestions = listOf(
            "What are you grateful for today?",
            "What was the highlight of your day?",
            "What challenged you today and how did you overcome it?",
            "What did you learn today?",
            "How did you show kindness today?",
            "What are you looking forward to tomorrow?",
            "What made you smile today?",
            "How did you take care of yourself today?",
            "What would you like to improve about today?",
            "What are you proud of accomplishing today?"
        )
    }

    suspend fun getDailyQuestion(): Result<String> {
        return try {
            // Get a random question based on the current date for consistency
            val calendar = Calendar.getInstance()
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val random = Random(dayOfYear.toLong()) // Same question for the same day
            val question = reflectionQuestions[random.nextInt(reflectionQuestions.size)]
            Result.success(question)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveReflection(question: String, answer: String): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val reflectionId = UUID.randomUUID().toString()
            val currentDate = getCurrentDate()

            val reflection = Reflection(
                id = reflectionId,
                userId = userId,
                question = question,
                answer = answer,
                date = currentDate
            )

            // Save reflection to Firestore
            db.collection(REFLECTIONS_COLLECTION)
                .document(reflectionId)
                .set(reflection.toMap())
                .await()

            // Update reflection streak
            streakRepository.updateReflectionStreak()

            Result.success(reflectionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReflectionHistory(): Result<List<Reflection>> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = db.collection(REFLECTIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val reflections = snapshot.documents.mapNotNull { doc ->
                try {
                    Reflection(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        question = doc.getString("question") ?: "",
                        answer = doc.getString("answer") ?: "",
                        date = doc.getString("date") ?: getCurrentDate()
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.date }

            Result.success(reflections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasReflectedToday(): Result<Boolean> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val currentDate = getCurrentDate()

            val snapshot = db.collection(REFLECTIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("date", currentDate)
                .get()
                .await()

            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}