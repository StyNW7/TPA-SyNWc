package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.data.model.User
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

            val userResult = userRepository.getUser(userId)
            if (userResult.isFailure) {
                return Result.failure(userResult.exceptionOrNull() ?: Exception("User not found"))
            }

            val user = userResult.getOrNull()!!
            val lastLoginDate = user.lastLoginDate

            // Check if user already logged in today
            if (lastLoginDate == currentDate) {
                return Result.success(Unit) // Already updated today
            }

            val newStreak = calculateNewStreak(lastLoginDate, currentDate, user.loginStreak)

            val updates = mapOf(
                "loginStreak" to newStreak,
                "lastLoginDate" to currentDate
            )

            userRepository.updateUserStreaks(userId, updates)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReflectionStreak(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val currentDate = User.getCurrentDate()

            val userResult = userRepository.getUser(userId)
            if (userResult.isFailure) {
                return Result.failure(userResult.exceptionOrNull() ?: Exception("User not found"))
            }

            val user = userResult.getOrNull()!!
            val lastReflectionDate = user.lastReflectionDate

            // Check if user already reflected today
            if (lastReflectionDate == currentDate) {
                return Result.success(Unit) // Already reflected today
            }

            val newStreak = calculateNewStreak(lastReflectionDate, currentDate, user.reflectionStreak)

            val updates = mapOf(
                "reflectionStreak" to newStreak,
                "lastReflectionDate" to currentDate
            )

            userRepository.updateUserStreaks(userId, updates)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTodoStreak(userId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val currentDate = User.getCurrentDate()

            val userResult = userRepository.getUser(userId)
            if (userResult.isFailure) {
                return Result.failure(userResult.exceptionOrNull() ?: Exception("User not found"))
            }

            val user = userResult.getOrNull()!!
            val lastTodoDate = user.lastTodoDate

            // Check if user already completed a todo today
            if (lastTodoDate == currentDate) {
                return Result.success(Unit) // Already completed todo today
            }

            val newStreak = calculateNewStreak(lastTodoDate, currentDate, user.todoStreak)

            val updates = mapOf(
                "todoStreak" to newStreak,
                "lastTodoDate" to currentDate
            )

            userRepository.updateUserStreaks(userId, updates)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateNewStreak(lastActivityDate: String, currentDate: String, currentStreak: Int): Int {
        return when {
            // First time activity or no previous activity
            lastActivityDate.isEmpty() -> 1

            // Same day - no change (shouldn't happen due to checks above, but as safety)
            isSameDay(lastActivityDate, currentDate) -> currentStreak

            // Consecutive day - increment streak
            isConsecutiveDay(lastActivityDate, currentDate) -> currentStreak + 1

            // Missed one or more days - reset to 1
            else -> 1
        }
    }

    private fun isConsecutiveDay(lastDate: String, currentDate: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val last = sdf.parse(lastDate)
            val current = sdf.parse(currentDate)

            val calendar = Calendar.getInstance()
            calendar.time = last
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val nextDay = sdf.format(calendar.time)

            nextDay == currentDate
        } catch (e: Exception) {
            false
        }
    }

    private fun isSameDay(lastDate: String, currentDate: String): Boolean {
        return lastDate == currentDate
    }

    suspend fun checkAndResetStreaks(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val currentDate = User.getCurrentDate()

            val userResult = userRepository.getUser(userId)
            if (userResult.isFailure) {
                return Result.failure(userResult.exceptionOrNull() ?: Exception("User not found"))
            }

            val user = userResult.getOrNull()!!
            val updates = mutableMapOf<String, Any>()

            // Check login streak reset (if last login was before yesterday)
            if (user.lastLoginDate.isNotEmpty() && !isSameDay(user.lastLoginDate, currentDate) &&
                !isConsecutiveDay(user.lastLoginDate, currentDate)) {
                updates["loginStreak"] = 0
            }

            // Check reflection streak reset
            if (user.lastReflectionDate.isNotEmpty() && !isSameDay(user.lastReflectionDate, currentDate) &&
                !isConsecutiveDay(user.lastReflectionDate, currentDate)) {
                updates["reflectionStreak"] = 0
            }

            // Check todo streak reset
            if (user.lastTodoDate.isNotEmpty() && !isSameDay(user.lastTodoDate, currentDate) &&
                !isConsecutiveDay(user.lastTodoDate, currentDate)) {
                updates["todoStreak"] = 0
            }

            if (updates.isNotEmpty()) {
                userRepository.updateUserStreaks(userId, updates)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}