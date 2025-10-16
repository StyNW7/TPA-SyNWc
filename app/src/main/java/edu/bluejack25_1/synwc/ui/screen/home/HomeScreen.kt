package edu.bluejack25_1.synwc.ui.screen.home

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import edu.bluejack25_1.synwc.data.model.Quote
import edu.bluejack25_1.synwc.ui.component.BeautifulBottomNavigationBar
import edu.bluejack25_1.synwc.ui.viewmodel.AuthViewModel
import edu.bluejack25_1.synwc.ui.viewmodel.HomeViewModel
import edu.bluejack25_1.synwc.ui.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val userLoggedIn by authViewModel.userLoggedIn.collectAsState()

    // Get state directly from HomeViewModel
    val dailyQuestion = homeViewModel.dailyQuestion
    val reflectionAnswer = homeViewModel.reflectionAnswer
    val reflectionHistory = homeViewModel.reflectionHistory
    val showHistory = homeViewModel.showReflectionHistory
    val hasReflectedToday = homeViewModel.hasReflectedToday
    val currentQuote = homeViewModel.currentQuote
    val searchQuery = homeViewModel.searchQuery
    val searchResults = homeViewModel.searchResults
    val showFavorites = homeViewModel.showFavoriteQuotes
    val favoriteQuotes = homeViewModel.favoriteQuotes
    val errorMessage = homeViewModel.errorMessage
    val successMessage = homeViewModel.successMessage
    val isLoading = homeViewModel.isLoading

    // Handle auto-dismiss messages
    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null) {
            delay(3000)
            homeViewModel.clearSuccessMessage()
        }
    }

    // Show messages as Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            homeViewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let { success ->
            snackbarHostState.showSnackbar(success)
        }
    }

    // Load user data when screen appears and check streaks
    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
        if (userLoggedIn) {
            userViewModel.loadCurrentUser()
            homeViewModel.refreshAllData()

            // Update login streak when user opens the app
            authViewModel.updateLoginStreak()
        }
    }

    // Redirect to login if not logged in
    if (!userLoggedIn) {
        LaunchedEffect(userLoggedIn) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "SyNWc",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },

                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        homeViewModel.refreshAllData()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },

                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BeautifulBottomNavigationBar(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        // Show loading indicator if data is loading
        if (isLoading && currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome Section
                item {
                    WelcomeSection(currentUser = currentUser)
                }

                // Streak Section
                item {
                    StreakSection(currentUser = currentUser)
                }

                // Reflection Status Banner
                if (hasReflectedToday) {
                    item {
                        ReflectionStatusBanner(hasReflectedToday = true)
                    }
                }

                // Quote Section
                item {
                    QuoteSection(
                        homeViewModel = homeViewModel,
                        currentQuote = currentQuote,
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        showFavorites = showFavorites,
                        favoriteQuotes = favoriteQuotes,
                        isLoading = isLoading
                    )
                }

                // Reflection Section
                item {
                    ReflectionSection(
                        homeViewModel = homeViewModel,
                        dailyQuestion = dailyQuestion,
                        reflectionAnswer = reflectionAnswer,
                        reflectionHistory = reflectionHistory,
                        showHistory = showHistory,
                        hasReflectedToday = hasReflectedToday,
                        isLoading = isLoading
                    )
                }

                // Add bottom spacer
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun WelcomeSection(currentUser: edu.bluejack25_1.synwc.data.model.User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Welcome back, ${currentUser?.name ?: "User"}!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "Here's your daily inspiration and reflection",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun StreakSection(currentUser: edu.bluejack25_1.synwc.data.model.User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Your Streaks 🔥",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BeautifulStreakItem(
                    title = "Login",
                    count = currentUser?.loginStreak ?: 0,
                    icon = Icons.Rounded.Person,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    ),
                    description = "Daily login streak"
                )
                BeautifulStreakItem(
                    title = "To-Do",
                    count = currentUser?.todoStreak ?: 0,
                    icon = Icons.Rounded.CheckCircle,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.secondaryContainer
                    ),
                    description = "Tasks completed"
                )
                BeautifulStreakItem(
                    title = "Reflection",
                    count = currentUser?.reflectionStreak ?: 0,
                    icon = Icons.Rounded.Lightbulb,
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.tertiary,
                        MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    description = "Daily reflections"
                )
            }
        }
    }
}

@Composable
fun BeautifulStreakItem(
    title: String,
    count: Int,
    icon: ImageVector,
    gradientColors: List<ComposeColor>,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        // Gradient background with icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    clip = false
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = ComposeColor.White,
                modifier = Modifier.size(32.dp)
            )

            // Small flame indicator for active streaks
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .background(ComposeColor.Red, CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🔥",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Streak count with nice styling
        Text(
            "$count",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Title
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Description
        Text(
            description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun ReflectionStatusBanner(hasReflectedToday: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (hasReflectedToday) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (hasReflectedToday) Icons.Default.CheckCircle else Icons.Default.Lightbulb,
                contentDescription = null,
                tint = if (hasReflectedToday) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                if (hasReflectedToday) "You've completed today's reflection! ✨"
                else "Don't forget to complete your daily reflection!",
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasReflectedToday) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun StreakItem(title: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = CircleShape
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "$count days",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuoteSection(
    homeViewModel: HomeViewModel,
    currentQuote: Quote?,
    searchQuery: String,
    searchResults: List<Quote>,
    showFavorites: Boolean,
    favoriteQuotes: List<Quote>,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Daily Quote 💭",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row {
                    IconButton(
                        onClick = { homeViewModel.loadDailyQuote() },
                        modifier = Modifier.size(36.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh quote",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = { homeViewModel.toggleFavoriteQuotes() },
                        modifier = Modifier.size(36.dp),
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Favorite quotes",
                            tint = if (showFavorites) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showFavorites) {
                FavoriteQuotesSection(favoriteQuotes = favoriteQuotes)
            } else {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { homeViewModel.updateSearchQuery(it) },
                    label = { Text("Search quotes...") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = { homeViewModel.searchQuotes() },
                            enabled = !isLoading && searchQuery.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    },
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Current Quote or Search Results
                if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                    SearchResultsSection(searchResults = searchResults, homeViewModel = homeViewModel)
                } else {
                    CurrentQuoteSection(currentQuote = currentQuote, homeViewModel = homeViewModel, isLoading = isLoading)
                }
            }
        }
    }
}

@Composable
fun CurrentQuoteSection(
    currentQuote: Quote?,
    homeViewModel: HomeViewModel,
    isLoading: Boolean
) {
    Column {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            currentQuote?.let { quote ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "\"${quote.text}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "- ${quote.author}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { homeViewModel.saveFavoriteQuote() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Favorites")
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No quote available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsSection(
    searchResults: List<Quote>,
    homeViewModel: HomeViewModel
) {
    Column {
        Text(
            "Search Results (${searchResults.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searchResults) { quote ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "\"${quote.text}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "- ${quote.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteQuotesSection(favoriteQuotes: List<Quote>) {
    Column {
        Text(
            "Favorite Quotes (${favoriteQuotes.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (favoriteQuotes.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favoriteQuotes) { quote ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "\"${quote.text}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "- ${quote.author}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                "No favorite quotes yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun ReflectionSection(
    homeViewModel: HomeViewModel,
    dailyQuestion: String,
    reflectionAnswer: String,
    reflectionHistory: List<edu.bluejack25_1.synwc.data.model.Reflection>,
    showHistory: Boolean,
    hasReflectedToday: Boolean,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Daily Reflection 🤔",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { homeViewModel.toggleReflectionHistory() },
                    modifier = Modifier.size(36.dp),
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "Reflection history",
                        tint = if (showHistory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showHistory) {
                ReflectionHistorySection(reflectionHistory = reflectionHistory)
            } else {
                CurrentReflectionSection(
                    dailyQuestion = dailyQuestion,
                    reflectionAnswer = reflectionAnswer,
                    homeViewModel = homeViewModel,
                    hasReflectedToday = hasReflectedToday,
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun CurrentReflectionSection(
    dailyQuestion: String,
    reflectionAnswer: String,
    homeViewModel: HomeViewModel,
    hasReflectedToday: Boolean,
    isLoading: Boolean
) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Display today's date
                Text(
                    "Today's Reflection - ${getFormattedDate()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Today's Question:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    dailyQuestion.ifEmpty { "Loading today's question..." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = reflectionAnswer,
            onValueChange = { homeViewModel.updateReflectionAnswer(it) },
            label = { Text("Your reflection...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = false,
            maxLines = 4,
            enabled = !isLoading && !hasReflectedToday,
            placeholder = {
                if (hasReflectedToday) {
                    Text("You've already reflected today. Come back tomorrow for a new question!")
                } else {
                    Text("Write your thoughts here...")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { homeViewModel.saveReflection() },
            modifier = Modifier.fillMaxWidth(),
            enabled = reflectionAnswer.isNotBlank() && !isLoading && !hasReflectedToday,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (hasReflectedToday) "Already Reflected Today" else "Save Reflection")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (!hasReflectedToday) {
                    homeViewModel.loadDailyReflectionQuestion()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            enabled = !isLoading && !hasReflectedToday
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Question")
        }
    }
}

@Composable
fun ReflectionHistorySection(reflectionHistory: List<edu.bluejack25_1.synwc.data.model.Reflection>) {
    Column {
        Text(
            "Reflection History (${reflectionHistory.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (reflectionHistory.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reflectionHistory) { reflection ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                reflection.date,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                reflection.question,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                reflection.answer,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                "No reflection history yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

// Add this helper function to the HomeScreen.kt file:
private fun getFormattedDate(): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date())
}