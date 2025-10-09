package edu.bluejack25_1.synwc.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import edu.bluejack25_1.synwc.data.DatabaseReference
import edu.bluejack25_1.synwc.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {

    suspend fun createUser(user: User): Result<Unit> {
        return try {
            DatabaseReference.usersRef()
                .child(user.id)
                .setValue(user.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            DatabaseReference.usersRef()
                .child(user.id)
                .setValue(user.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val snapshot = DatabaseReference.usersRef()
                .child(userId)
                .get()
                .await()

            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        try {
            val currentUserId = DatabaseReference.getCurrentUserId()

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    trySend(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }

            DatabaseReference.usersRef()
                .child(currentUserId)
                .addValueEventListener(listener)

            awaitClose {
                DatabaseReference.usersRef()
                    .child(currentUserId)
                    .removeEventListener(listener)
            }
        } catch (e: Exception) {
            trySend(null)
            awaitClose { }
        }
    }

    suspend fun updateStreak(userId: String, newStreak: Int): Result<Unit> {
        return try {
            val updates = mapOf(
                "streakCount" to newStreak,
                "lastActiveDate" to User.getCurrentDate()
            )
            DatabaseReference.usersRef()
                .child(userId)
                .updateChildren(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}