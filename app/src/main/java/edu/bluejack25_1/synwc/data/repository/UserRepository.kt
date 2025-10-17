package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.data.model.User
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class UserRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private val auth = Firebase.auth

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    // Add this method to find email by username
    suspend fun findEmailByUsername(username: String): String? {
        return try {
            val query = db.collection(USERS_COLLECTION)
                .whereEqualTo("name", username.trim())
                .get()
                .await()

            if (!query.isEmpty && query.documents.isNotEmpty()) {
                val user = query.documents[0].toObject(User::class.java)
                user?.email
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Check if email already exists
    suspend fun isEmailExists(email: String): Boolean {
        return try {
            val query = db.collection(USERS_COLLECTION)
                .whereEqualTo("email", email.trim().lowercase())
                .get()
                .await()

            !query.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Check if username already exists
    suspend fun isUsernameExists(username: String): Boolean {
        return try {
            val query = db.collection(USERS_COLLECTION)
                .whereEqualTo("name", username.trim())
                .get()
                .await()

            !query.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Create user with validation
    suspend fun createUserWithValidation(user: User): Result<Unit> {
        return try {
            // Validate email uniqueness
            if (isEmailExists(user.email)) {
                return Result.failure(Exception("Email already exists"))
            }

            // Validate username uniqueness
            if (isUsernameExists(user.name)) {
                return Result.failure(Exception("Username already exists"))
            }

            // Initialize with proper streak data
            val newUser = user.copy(
                loginStreak = 1,
                todoStreak = 0,
                reflectionStreak = 0,
                lastLoginDate = User.getCurrentDate(),
                lastTodoDate = "", // Empty means no todo activity yet
                lastReflectionDate = "" // Empty means no reflection activity yet
            )

            db.collection(USERS_COLLECTION)
                .document(newUser.id)
                .set(newUser.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            // Initialize with proper streak data
            val newUser = user.copy(
                loginStreak = 1,
                todoStreak = 0,
                reflectionStreak = 0,
                lastLoginDate = User.getCurrentDate(),
                lastTodoDate = "", // Empty means no todo activity yet
                lastReflectionDate = "" // Empty means no reflection activity yet
            )

            db.collection(USERS_COLLECTION)
                .document(newUser.id)
                .set(newUser.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val document = db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Failed to parse user data"))
                }
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            db.collection(USERS_COLLECTION)
                .document(user.id)
                .set(user.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(userId: String, name: String, email: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "name" to name.trim(),
                "email" to email.trim().lowercase()
            )
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileImage(userId: String, imageUrl: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "profileImageUrl" to imageUrl
            )
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserStreaks(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            db.collection(USERS_COLLECTION)
                .document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw Exception("User not authenticated")
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}