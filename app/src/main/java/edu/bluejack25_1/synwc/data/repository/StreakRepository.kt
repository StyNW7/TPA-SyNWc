package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.data.model.User
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class StreakRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth = Firebase.auth
    private val userRepository = UserRepository()

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    suspend fun updateLoginStreak(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val currentDate = User.getCurrentDate()

            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()
            val user = userDoc.toObject(User::class.java)

            user?.let {
                val lastLoginDate = it.lastLoginDate
                val currentStreak = it.loginStreak

                val newStreak = if (isConsecutiveDay(lastLoginDate, currentDate)) {
                    currentStreak + 1
                } else if (isSameDay(lastLoginDate, currentDate)) {
                    currentStreak
                } else {
                    1
                }

                val updates = mapOf(
                    "loginStreak" to newStreak,
                    "lastLoginDate" to currentDate
                )

                db.collection(USERS_COLLECTION)
                    .document(userId)
                    .update(updates)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReflectionStreak(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val currentDate = User.getCurrentDate()

            val userDoc = db.collection(USERS_COLLECTION).document(userId).get().await()
            val user = userDoc.toObject(User::class.java)

            user?.let {
                val lastReflectionDate = it.lastReflectionDate
                val currentStreak = it.reflectionStreak

                val newStreak = if (isConsecutiveDay(lastReflectionDate, currentDate)) {
                    currentStreak + 1
                } else if (isSameDay(lastReflectionDate, currentDate)) {
                    currentStreak
                } else {
                    1
                }

                val updates = mapOf(
                    "reflectionStreak" to newStreak,
                    "lastReflectionDate" to currentDate
                )

                db.collection(USERS_COLLECTION)
                    .document(userId)
                    .update(updates)
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isConsecutiveDay(lastDate: String, currentDate: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val last = sdf.parse(lastDate)
            val current = sdf.parse(currentDate)
            val difference = (current.time - last.time) / (1000 * 60 * 60 * 24)
            difference == 1L
        } catch (e: Exception) {
            false
        }
    }

    private fun isSameDay(lastDate: String, currentDate: String): Boolean {
        return lastDate == currentDate
    }

    suspend fun updateTodoStreak(userId: String): Result<Unit> {
        return try {
            // Get current user
            val userResult = userRepository.getUser(userId)
            if (userResult.isFailure) {
                return Result.failure(userResult.exceptionOrNull() ?: Exception("User not found"))
            }

            val user = userResult.getOrNull()!!
            val currentDate = User.getCurrentDate()

            // Check if user already updated todo today
            if (user.lastTodoDate == currentDate) {
                return Result.success(Unit) // Already updated today, no change needed
            }

            // Check if it's consecutive day (yesterday)
            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentDate)!!
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            val newStreak = if (user.lastTodoDate == yesterday) {
                // Consecutive day - increment streak
                user.todoStreak + 1
            } else {
                // Not consecutive - reset to 1
                1
            }

            // Update user streak
            val updates = mapOf(
                "todoStreak" to newStreak,
                "lastTodoDate" to currentDate
            )

            val updateResult = userRepository.updateUserStreaks(userId, updates)
            if (updateResult.isFailure) {
                return Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to update streak"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}