package edu.bluejack25_1.synwc.ui.screen.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import edu.bluejack25_1.synwc.R
import edu.bluejack25_1.synwc.ui.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = viewModel(),
    onGetStartedClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState()
    val totalPages = viewModel.getTotalPages()
    val coroutineScope = rememberCoroutineScope()

    // Sync ViewModel state with pager state
    val currentPageFromViewModel by viewModel.currentPage.collectAsState()

    LaunchedEffect(currentPageFromViewModel) {
        pagerState.animateScrollToPage(currentPageFromViewModel)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Decorative background elements
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = (-120).dp, y = (-80).dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    CircleShape
                )
                .blur(80.dp)
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.06f),
                    CircleShape
                )
                .blur(70.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Image Pager with enhanced presentation
            HorizontalPager(
                count = totalPages,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val data = viewModel.getPageData(page)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Glow effect behind image
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    CircleShape
                                )
                                .blur(40.dp)
                        )

                        // Image with shadow and elevated feel
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 12.dp,
                            modifier = Modifier
                                .size(200.dp)
                                .offset(y = (-10).dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = data.imageRes),
                                    contentDescription = data.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Card
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box {
                    // Gradient overlay for depth
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
                                    )
                                )
                            )
                    )

                    val currentPage = pagerState.currentPage
                    val pageData = viewModel.getPageData(currentPage)
                    val isLastPage = currentPage == totalPages - 1

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 36.dp)
                            .fillMaxWidth()
                    ) {
                        // Modern Page Indicators
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(totalPages) { index ->
                                val isSelected = index == currentPage
                                val width by animateDpAsState(
                                    targetValue = if (isSelected) 32.dp else 8.dp,
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                    label = "indicator_width"
                                )

                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .width(width)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isSelected) {
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.secondary
                                                    )
                                                )
                                            } else {
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                                    )
                                                )
                                            }
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Title with modern typography
                        Text(
                            text = pageData.title,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description with refined styling
                        Text(
                            text = pageData.description,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        if (isLastPage) {

                            Button(
                                onClick = {
                                    onGetStartedClick()
                                    navController.navigate("login") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 8.dp,
                                    pressedElevation = 12.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                            ) {
                                Text(
                                    text = "Get Started",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        } else {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.skipToEnd()
                                        }
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "Skip",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                // Next button with elevated design
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.nextPage()
                                        }
                                    },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 8.dp,
                                        pressedElevation = 12.dp
                                    ),
                                    modifier = Modifier
                                        .size(64.dp)
                                        .shadow(
                                            elevation = 12.dp,
                                            shape = CircleShape,
                                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                        )
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_arrow_right),
                                        contentDescription = "Next",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}