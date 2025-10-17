package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.data.model.User
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AuthRepository {
    private val auth: FirebaseAuth = Firebase.auth
    private val userRepository = UserRepository()
    private val streakRepository = StreakRepository()

    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()

            // Update login streak after successful login
            streakRepository.updateLoginStreak()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithEmailOrUsername(identifier: String, password: String): Result<Unit> {
        return try {
            // First, check if the identifier is an email
            if (isValidEmail(identifier)) {
                // If it's a valid email format, try to login directly
                return loginUser(identifier, password)
            } else {
                // If it's not an email, assume it's a username and find the associated email
                val userEmail = userRepository.findEmailByUsername(identifier)
                if (userEmail != null) {
                    return loginUser(userEmail, password)
                } else {
                    return Result.failure(Exception("No account found with this username"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(username: String, email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            // Update user profile with username in Firebase Auth
            authResult.user?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                user.updateProfile(profileUpdates).await()

                // Create user in Firestore with proper streak initialization
                val newUser = User.createNewUser(
                    id = user.uid,
                    name = username,
                    email = email
                )

                val createUserResult = userRepository.createUser(newUser)
                if (createUserResult.isFailure) {
                    // If Firestore creation fails, delete the auth user to maintain consistency
                    user.delete().await()
                    return Result.failure(createUserResult.exceptionOrNull()!!)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser() = auth.currentUser
    fun isUserLoggedIn() = auth.currentUser != null
    fun logout() = auth.signOut()

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return emailRegex.matches(email)
    }
}