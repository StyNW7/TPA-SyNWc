package edu.bluejack25_1.synwc.ui.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.bluejack25_1.synwc.R
import edu.bluejack25_1.synwc.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Animation states
    val scaleAnimation = remember { Animatable(0f) }
    val alphaAnimation = remember { Animatable(0f) }
    val slideAnimation = remember { Animatable(50f) }
    val logoRotation = remember { Animatable(0f) }
    val glowAnimation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // run all animations concurrently
        launch {
            logoRotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }

        launch {
            scaleAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, delayMillis = 200, easing = FastOutSlowInEasing)
            )
        }

        launch {
            alphaAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, delayMillis = 300)
            )
        }

        launch {
            slideAnimation.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 800, delayMillis = 400, easing = FastOutSlowInEasing)
            )
        }

        launch {
            glowAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1200, delayMillis = 100)
            )
        }

        authViewModel.checkAuthState()

        delay(2000)

        // Navigate based on authentication state
        if (authViewModel.userLoggedIn.value) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("onboarding") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-150).dp)
                .scale(glowAnimation.value)
                .alpha(0.1f)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                )
                .blur(60.dp)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = 120.dp, y = 180.dp)
                .scale(glowAnimation.value)
                .alpha(0.08f)
                .background(
                    MaterialTheme.colorScheme.tertiary,
                    CircleShape
                )
                .blur(50.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(40.dp)
                .offset(y = slideAnimation.value.dp)
        ) {
            // Logo container with glow effect
            Box(
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scaleAnimation.value * glowAnimation.value)
                        .alpha(glowAnimation.value * 0.4f)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                        .blur(30.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_synwc_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scaleAnimation.value)
                        .alpha(alphaAnimation.value)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "SyNWc",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 56.sp,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(alphaAnimation.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 4.dp)
                    .alpha(alphaAnimation.value * 0.8f)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary,
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SyNWc Your Notes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(alphaAnimation.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "With Clarity",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.alpha(alphaAnimation.value * 0.9f),
                textAlign = TextAlign.Center
            )
        }

        // Bottom accent
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        ) {
            Text(
                text = "●",
                color = MaterialTheme.colorScheme.primary.copy(alpha = alphaAnimation.value * 0.6f),
                fontSize = 32.sp,
                modifier = Modifier.alpha(alphaAnimation.value)
            )
        }
    }

}