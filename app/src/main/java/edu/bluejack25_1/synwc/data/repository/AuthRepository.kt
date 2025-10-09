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

    suspend fun loginUser(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerUser(username: String, email: String, password: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            // Update user profile with username
            authResult.user?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                user.updateProfile(profileUpdates).await()

                // Create user in Realtime Database
                val newUser = User(
                    id = user.uid,
                    name = username,
                    email = email,
                    joinDate = System.currentTimeMillis(),
                    streakCount = 0,
                    lastActiveDate = User.getCurrentDate()
                )
                userRepository.createUser(newUser)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser() = auth.currentUser
    fun isUserLoggedIn() = auth.currentUser != null
    fun logout() = auth.signOut()
}