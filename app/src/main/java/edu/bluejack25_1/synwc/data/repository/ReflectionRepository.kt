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
        private const val DAILY_QUESTIONS_COLLECTION = "daily_questions"
        private const val USER_PREFERENCES_COLLECTION = "user_preferences"

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
            "What are you proud of accomplishing today?",
            "What emotions did you experience today?",
            "What was the most meaningful moment of your day?",
            "How did you grow as a person today?",
            "What inspired you today?",
            "What would you do differently if you could relive today?",
            "How did you handle stress or challenges today?",
            "What are you thankful for in your life right now?",
            "What did you do today that brought you joy?",
            "How did you connect with others today?",
            "What are your intentions for tomorrow?"
        )
    }

    suspend fun getDailyQuestion(): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val currentDate = getCurrentDate()

            // Check if we already have a question for today
            val existingQuestion = getTodaysQuestionFromStorage(userId, currentDate)
            if (existingQuestion != null) {
                return Result.success(existingQuestion)
            }

            // Get yesterday's question to avoid repetition
            val yesterdayQuestion = getYesterdaysQuestion(userId)

            // Generate new question (avoid repeating yesterday's)
            val newQuestion = generateNewQuestion(yesterdayQuestion)

            // Save today's question
            saveTodaysQuestion(userId, currentDate, newQuestion)

            Result.success(newQuestion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getTodaysQuestionFromStorage(userId: String, currentDate: String): String? {
        return try {
            val snapshot = db.collection(DAILY_QUESTIONS_COLLECTION)
                .document(userId)
                .collection("questions")
                .document(currentDate)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.getString("question")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun getYesterdaysQuestion(userId: String): String? {
        return try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            val snapshot = db.collection(DAILY_QUESTIONS_COLLECTION)
                .document(userId)
                .collection("questions")
                .document(yesterdayDate)
                .get()
                .await()

            if (snapshot.exists()) {
                snapshot.getString("question")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun generateNewQuestion(previousQuestion: String?): String {
        val availableQuestions = if (previousQuestion != null) {
            reflectionQuestions.filter { it != previousQuestion }
        } else {
            reflectionQuestions
        }

        if (availableQuestions.isEmpty()) {
            return reflectionQuestions.random()
        }

        // Use the current date as seed for consistent daily randomness
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val random = Random(dayOfYear.toLong())

        return availableQuestions[random.nextInt(availableQuestions.size)]
    }

    private suspend fun saveTodaysQuestion(userId: String, date: String, question: String) {
        try {
            val questionData = mapOf(
                "question" to question,
                "date" to date,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection(DAILY_QUESTIONS_COLLECTION)
                .document(userId)
                .collection("questions")
                .document(date)
                .set(questionData)
                .await()

            // Clean up old questions (keep only last 30 days)
            cleanupOldQuestions(userId)
        } catch (e: Exception) {
            // If saving fails, we'll just generate a new question next time
        }
    }

    private suspend fun cleanupOldQuestions(userId: String) {
        try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -30) // Keep only 30 days of history
            val cutoffDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            val snapshot = db.collection(DAILY_QUESTIONS_COLLECTION)
                .document(userId)
                .collection("questions")
                .get()
                .await()

            for (document in snapshot.documents) {
                val date = document.id
                if (date < cutoffDate) {
                    document.reference.delete().await()
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
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

            // Update reflection streak - use the new function
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
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
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
            }

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

    suspend fun getQuestionHistory(): Result<List<Pair<String, String>>> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = db.collection(DAILY_QUESTIONS_COLLECTION)
                .document(userId)
                .collection("questions")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val questions = snapshot.documents.mapNotNull { doc ->
                try {
                    val date = doc.id
                    val question = doc.getString("question") ?: ""
                    Pair(date, question)
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}