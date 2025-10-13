package edu.bluejack25_1.synwc.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import edu.bluejack25_1.synwc.data.model.User  // Add this import
import edu.bluejack25_1.synwc.ui.viewmodel.AuthViewModel
import edu.bluejack25_1.synwc.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val userLoggedIn by authViewModel.userLoggedIn.collectAsState()
    val loading by userViewModel.loading.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()

    // Load user data when screen appears
    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
        if (userLoggedIn) {
            userViewModel.loadCurrentUser()
        }
    }

    // Handle error messages
    errorMessage?.let {
        LaunchedEffect(it) {
            // You can show a snackbar or dialog for errors
            println("User loading error: $it")
        }
    }

    // Redirect to login if not logged in
    if (!userLoggedIn) {
        LaunchedEffect(userLoggedIn) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
        return  // Important: return early to avoid showing home content
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("SyNWc Home") },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        userViewModel.clearError()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome to SyNWc!",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Display user information
            when {
                loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading user data...")
                    }
                }
                currentUser != null -> {
                    UserProfileCard(user = currentUser!!)

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val newStreak = currentUser!!.streakCount + 1
                            userViewModel.updateStreak(newStreak)
                        },
                        enabled = !loading
                    ) {
                        Text("Increase Streak")
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No user data found")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { userViewModel.loadCurrentUser() }) {
                            Text("Retry Loading")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "User Profile",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Name: ${user.name}", style = MaterialTheme.typography.bodyLarge)
            Text("Email: ${user.email}", style = MaterialTheme.typography.bodyLarge)
            Text("Streak: ${user.streakCount} days", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Member since: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(user.joinDate))}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text("Last active: ${user.lastActiveDate}", style = MaterialTheme.typography.bodyMedium)

            // Show profile image if available
            user.profileImageUrl?.takeIf { it.isNotEmpty() }?.let { imageUrl ->
                Spacer(modifier = Modifier.height(16.dp))
                Text("Profile image: $imageUrl", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}