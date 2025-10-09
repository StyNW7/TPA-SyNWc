package edu.bluejack25_1.synwc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import edu.bluejack25_1.synwc.ui.navigation.AppNavGraph
import edu.bluejack25_1.synwc.ui.theme.SyNWcTheme

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Check if Firebase is properly initialized
        try {
            val auth = Firebase.auth
            println("Firebase Auth initialized: ${auth.app.name}")
        } catch (e: Exception) {
            println("Firebase initialization error: ${e.message}")
        }

        setContent {
            SyNWcTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController)
                }
            }
        }
    }
}