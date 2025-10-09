package edu.bluejack25_1.synwc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.bluejack25_1.synwc.ui.screen.onboarding.OnboardingScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onGetStartedClick = { navController.navigate("login") }
            )
        }
    }
}