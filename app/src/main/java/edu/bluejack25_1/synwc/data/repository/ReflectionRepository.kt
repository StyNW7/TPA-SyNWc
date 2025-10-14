package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.data.model.Reflection
import kotlinx.coroutines.tasks.await
import java.util.*

class ReflectionRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth = Firebase.auth

    companion object {
        private const val REFLECTIONS_COLLECTION = "reflections"
        private const val REFLECTION_QUESTIONS_COLLECTION = "reflection_questions"
    }

    suspend fun getDailyReflectionQuestion(): Result<String> {
        return try {
            val snapshot = db.collection(REFLECTION_QUESTIONS_COLLECTION)
                .get()
                .await()

            val questions = snapshot.documents.map { it.getString("question") ?: "" }
            if (questions.isNotEmpty()) {
                val randomQuestion = questions.random()
                Result.success(randomQuestion)
            } else {
                Result.success("What are you grateful for today?")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveReflection(question: String, answer: String): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val reflectionId = UUID.randomUUID().toString()

            val reflection = Reflection(
                id = reflectionId,
                userId = userId,
                question = question,
                answer = answer
            )

            db.collection(REFLECTIONS_COLLECTION)
                .document(userId)
                .collection("user_reflections")
                .document(reflectionId)
                .set(reflection.toMap())
                .await()

            Result.success(reflectionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReflectionsHistory(): Result<List<Reflection>> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = db.collection(REFLECTIONS_COLLECTION)
                .document(userId)
                .collection("user_reflections")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val reflections = snapshot.toObjects(Reflection::class.java)
            Result.success(reflections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}