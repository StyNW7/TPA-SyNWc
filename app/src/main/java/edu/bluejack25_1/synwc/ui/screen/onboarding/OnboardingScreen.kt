package edu.bluejack25_1.synwc.ui.screen.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bluejack25_1.synwc.R

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onGetStartedClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your illustration
                contentDescription = "Onboarding Image",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Welcome to SyNWc 🌿",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your daily space for self-love, reflection, and productivity.",
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    viewModel.onGetStarted()
                    onGetStartedClick()
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Get Started", fontSize = 18.sp)
                }
            }
        }
    }
}