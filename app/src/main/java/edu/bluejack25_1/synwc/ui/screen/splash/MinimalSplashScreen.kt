//package edu.bluejack25_1.synwc.ui.screen.splash
//
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import kotlinx.coroutines.delay
//
//@Composable
//fun MinimalSplashScreen(
//    navController: NavController
//) {
//    val alphaAnimation = remember { Animatable(0f) }
//
//    LaunchedEffect(key1 = true) {
//        alphaAnimation.animateTo(
//            targetValue = 1f,
//            animationSpec = tween(durationMillis = 1500)
//        )
//
//        delay(2000)
//
//        navController.navigate("main") {
//            popUpTo("splash") { inclusive = true }
//        }
//    }
//
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier.padding(24.dp)
//            ) {
//                // Simple dot/logo
//                Box(
//                    modifier = Modifier
//                        .size(80.dp)
//                        .alpha(alphaAnimation.value)
//                        .background(
//                            color = MaterialTheme.colorScheme.primary,
//                            shape = MaterialTheme.shapes.medium
//                        )
//                )
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                Text(
//                    text = "SyNWc",
//                    style = MaterialTheme.typography.displayMedium,
//                    color = MaterialTheme.colorScheme.onBackground,
//                    fontSize = 42.sp,
//                    modifier = Modifier.alpha(alphaAnimation.value)
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text(
//                    text = "Your thoughts, organized",
//                    style = MaterialTheme.typography.bodyLarge,
//                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
//                    modifier = Modifier.alpha(alphaAnimation.value)
//                )
//            }
//        }
//    }
//}