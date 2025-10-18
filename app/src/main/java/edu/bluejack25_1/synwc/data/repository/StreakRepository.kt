package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.data.model.User
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.tasks.await

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

    suspend fun updateTodoStreak(): Result<Unit> {
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

            // Check if streaks need to be reset (missed more than 1 day)
            if (shouldResetStreak(user.lastLoginDate, currentDate, user.loginStreak)) {
                updates["loginStreak"] = 0
            }

            if (shouldResetStreak(user.lastReflectionDate, currentDate, user.reflectionStreak)) {
                updates["reflectionStreak"] = 0
            }

            if (shouldResetStreak(user.lastTodoDate, currentDate, user.todoStreak)) {
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

    private fun shouldResetStreak(lastActivityDate: String, currentDate: String, currentStreak: Int): Boolean {
        if (lastActivityDate.isEmpty() || currentStreak == 0) {
            return false // No activity yet or already reset
        }

        // If it's not the same day and not consecutive, reset the streak
        return !isSameDay(lastActivityDate, currentDate) && !isConsecutiveDay(lastActivityDate, currentDate)
    }

    // Helper function to check if a date is more than 1 day old
    private fun isMoreThanOneDayOld(lastDate: String, currentDate: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val last = sdf.parse(lastDate)
            val current = sdf.parse(currentDate)

            val diff = current.time - last.time
            val daysDiff = diff / (1000 * 60 * 60 * 24)

            daysDiff > 1
        } catch (e: Exception) {
            false
        }
    }
}