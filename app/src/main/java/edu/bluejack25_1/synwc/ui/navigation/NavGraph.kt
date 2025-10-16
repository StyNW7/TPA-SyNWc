package edu.bluejack25_1.synwc.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.bluejack25_1.synwc.ui.screen.admin.QuoteSeederScreen
import edu.bluejack25_1.synwc.ui.screen.onboarding.OnboardingScreen
import edu.bluejack25_1.synwc.ui.screen.auth.LoginScreen
import edu.bluejack25_1.synwc.ui.screen.auth.RegisterScreen
import edu.bluejack25_1.synwc.ui.screen.home.HomeScreen
import edu.bluejack25_1.synwc.ui.screen.settings.SettingsScreen
import edu.bluejack25_1.synwc.ui.screen.splash.SplashScreen
import edu.bluejack25_1.synwc.ui.screen.todo.ToDoScreen
import edu.bluejack25_1.synwc.ui.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("onboarding") {
            OnboardingScreen(
                navController = navController,
                onGetStartedClick = {
                    navController.navigate("login")
                }
            )
        }
        composable("login") {
            LoginScreen(
                navController = navController
            )
        }
        composable("register") {
            RegisterScreen(
                navController = navController
            )
        }
        composable("home") {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("todo") {
            ToDoScreen(navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
//      composable("login") {
//          QuoteSeederScreen(navController = navController)
//      }
    }
}