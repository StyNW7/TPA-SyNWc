package edu.bluejack25_1.synwc.ui.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.bluejack25_1.synwc.R
import edu.bluejack25_1.synwc.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Animation states
    val scaleAnimation = remember { Animatable(0f) }
    val alphaAnimation = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Start animations
        scaleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, delayMillis = 200)
        )

        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, delayMillis = 300)
        )

        // Check authentication state while showing splash
        authViewModel.checkAuthState()

        // Wait for minimum display time (2 seconds)
        delay(2000)

        // Navigate based on authentication state
        if (authViewModel.userLoggedIn.value) {
            // User is logged in, go to home
            navController.navigate("home") {
                // Remove splash screen from back stack so user can't go back
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // User is not logged in, go to onboarding or login
            // You can choose between "onboarding" or "login" here
            navController.navigate("onboarding") {
                // Remove splash screen from back stack so user can't go back
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            // App Logo/Icon with animation
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnimation.value)
                    .alpha(alphaAnimation.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Name with animation
            Text(
                text = "SyNWc",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                modifier = Modifier.alpha(alphaAnimation.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Tagline
            Text(
                text = "Sync Your Notes With Clarity",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                modifier = Modifier.alpha(alphaAnimation.value),
                textAlign = TextAlign.Center
            )
        }
    }
}