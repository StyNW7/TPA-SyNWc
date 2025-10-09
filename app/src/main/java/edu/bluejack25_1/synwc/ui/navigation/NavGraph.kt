package edu.bluejack25_1.synwc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.bluejack25_1.synwc.ui.screen.onboarding.OnboardingScreen
import edu.bluejack25_1.synwc.ui.screen.auth.LoginScreen
import edu.bluejack25_1.synwc.ui.screen.auth.RegisterScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                navController = navController,
                onGetStartedClick = { navController.navigate("login") }
            )
        }
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("home") { /* TODO: your home screen */ }
    }
}