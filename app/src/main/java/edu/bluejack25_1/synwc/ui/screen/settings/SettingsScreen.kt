package edu.bluejack25_1.synwc.ui.screen.settings

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import edu.bluejack25_1.synwc.ui.component.BeautifulBottomNavigationBar
import edu.bluejack25_1.synwc.ui.viewmodel.SettingsViewModel
import edu.bluejack25_1.synwc.ui.viewmodel.SettingsViewModelFactory
import edu.bluejack25_1.synwc.ui.viewmodel.AuthViewModel
import edu.bluejack25_1.synwc.ui.viewmodel.UserViewModel
import edu.bluejack25_1.synwc.util.ImagePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    context: Context = LocalContext.current
) {

    val currentUser by userViewModel.currentUser.collectAsState()
    val userLoggedIn by authViewModel.userLoggedIn.collectAsState()

    // Use the factory pattern for ViewModel initialization
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )

    val themeMode by settingsViewModel.themeMode.collectAsState()
//    val themeMode by settingsViewModel.themeMode.collectAsState(initial = "system")

    val userName by settingsViewModel.userName.collectAsState(initial = "")
    val userEmail by settingsViewModel.userEmail.collectAsState(initial = "")
    val profileImageUrl by settingsViewModel.profileImageUrl.collectAsState(initial = "")

    // Access mutable state directly
    val isLoading = settingsViewModel.isLoading
    val errorMessage = settingsViewModel.errorMessage

    // Track when profile image updates to force recomposition
    var imageUpdateTrigger by remember { mutableStateOf(0) }

    // Initialize ImagePicker
    val imagePicker = ImagePicker()
    val pickImageLauncher = imagePicker.rememberImagePicker { uri ->
        settingsViewModel.updateImageUri(uri)
        if (uri != null) {
            // Automatically upload when image is selected
            settingsViewModel.uploadProfileImage()
        }
    }

    // Load user data when screen appears
    LaunchedEffect(Unit) {
        authViewModel.checkAuthState()
        if (userLoggedIn) {
            userViewModel.loadCurrentUser()
            settingsViewModel.loadUserData()
        }
    }

    // Observe profile image URL changes and trigger recomposition
    LaunchedEffect(profileImageUrl) {
        if (profileImageUrl.isNotEmpty()) {
            imageUpdateTrigger++
        }
    }

    // Handle successful image upload
    LaunchedEffect(isLoading) {
        if (!isLoading && settingsViewModel.selectedImageUri == null) {
            // Image upload completed successfully, trigger refresh
            imageUpdateTrigger++
        }
    }

    // Handle errors with snackbar
    if (!errorMessage.isNullOrEmpty()) {
        LaunchedEffect(errorMessage) {
            // You can show a snackbar here
            println("Settings Error: $errorMessage")
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
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout(settingsViewModel)
                        navController.navigate("login") {
                            popUpTo("settings") { inclusive = true }
                        }
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
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Profile Section
                item {
                    SettingsSection(title = "Profile") {
                        ProfileCard(
                            userName = userName,
                            userEmail = userEmail,
                            profileImageUrl = profileImageUrl,
                            isLoading = isLoading,
                            onEditProfile = { settingsViewModel.showEditProfile() },
                            onChangePhoto = { settingsViewModel.showImagePicker() }
                        )
                    }
                }

                // Appearance Section
                item {
                    SettingsSection(title = "Appearance") {
                        ThemeSelector(
                            currentTheme = themeMode,
                            onThemeSelected = { theme -> settingsViewModel.setThemeMode(theme) }
                        )
                    }
                }

                // About Section
                item {
                    SettingsSection(title = "About") {
                        AboutSection()
                    }
                }

                // Sync Section
//                item {
//                    SettingsSection(title = "Data") {
//                        SyncSection(
//                            onSyncData = {
//                                settingsViewModel.loadUserData()
//                                imageUpdateTrigger++
//                            },
//                            isLoading = isLoading
//                        )
//                    }
//                }

                // Debug Section
//                item {
//                    SettingsSection(title = "Debug Info") {
//                        DebugInfoCard(
//                            profileImageUrl = profileImageUrl,
//                            isLoading = isLoading,
//                            errorMessage = errorMessage,
//                            imageUpdateTrigger = imageUpdateTrigger,
//                            onReload = {
//                                settingsViewModel.loadUserData()
//                                imageUpdateTrigger++
//                            }
//                        )
//                    }
//                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Uploading image...", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Edit profile dialog
            if (settingsViewModel.showEditProfileDialog) {
                EditProfileDialog(
                    settingsViewModel = settingsViewModel,
                    currentName = userName,
                    currentEmail = userEmail,
                    isLoading = isLoading,
                    errorMessage = errorMessage
                )
            }

            // Image picker dialog
            if (settingsViewModel.showImagePicker) {
                ImagePickerDialog(
                    onPickFromGallery = {
                        pickImageLauncher()
                        settingsViewModel.hideImagePicker()
                    },
                    onDismiss = { settingsViewModel.hideImagePicker() }
                )
            }
        }
    }
}

@Composable
fun ProfileCard(
    userName: String,
    userEmail: String,
    profileImageUrl: String,
    isLoading: Boolean,
    onEditProfile: () -> Unit,
    onChangePhoto: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Image with better loading states
                ProfileImage(
                    profileImageUrl = profileImageUrl,
                    isLoading = isLoading,
                    onChangePhoto = onChangePhoto
                )

                Spacer(modifier = Modifier.width(16.dp))

                // User Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = userName.ifEmpty { "Your Name" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = userEmail.ifEmpty { "your.email@example.com" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Edit Button
                IconButton(
                    onClick = onEditProfile,
                    modifier = Modifier.size(48.dp),
                    enabled = !isLoading
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit profile",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileImage(
    profileImageUrl: String,
    isLoading: Boolean,
    onChangePhoto: () -> Unit
) {
    var imageLoadError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                enabled = !isLoading,
                onClick = onChangePhoto
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            // Show loading indicator while uploading
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                strokeWidth = 2.dp
            )
        } else if (profileImageUrl.isNotEmpty() && !imageLoadError) {
            // Show profile image with proper caching
            Image(
                painter = rememberAsyncImagePainter(
                    model = profileImageUrl,
                    onSuccess = {
                        println("Profile image loaded successfully: $profileImageUrl")
                        imageLoadError = false
                    },
                    onError = {
                        println("Failed to load profile image: $profileImageUrl")
                        imageLoadError = true
                    }
                ),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // Show app logo as default when no image is set
            // Replace with your actual app logo - you can use a vector asset or drawable
            Icon(
                Icons.Default.AccountCircle, // Replace with your app logo icon
                contentDescription = "Default profile picture",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Edit icon overlay (only show when not loading)
        if (!isLoading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Change photo",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun ImagePickerDialog(
    onPickFromGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Profile Picture") },
        text = { Text("Choose how you want to set your profile picture") },
        confirmButton = {
            Button(onClick = onPickFromGallery) {
                Text("Choose from Gallery")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// The rest of your composable functions remain the same (SettingsSection, ThemeSelector, ThemeOption, AboutSection, SyncSection, EditProfileDialog)
@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
    }
}

@Composable
fun ThemeSelector(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                supportingContent = {
                    Text("Choose your preferred theme mode")
                },
                leadingContent = {
                    Icon(
                        Icons.Default.DarkMode,
                        contentDescription = "Theme",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThemeOption(
                    title = "Light",
                    icon = Icons.Default.LightMode,
                    isSelected = currentTheme == "light",
                    onClick = { onThemeSelected("light") }
                )

                ThemeOption(
                    title = "Dark",
                    icon = Icons.Default.DarkMode,
                    isSelected = currentTheme == "dark",
                    onClick = { onThemeSelected("dark") }
                )

                ThemeOption(
                    title = "System",
                    icon = Icons.Default.Settings,
                    isSelected = currentTheme == "system",
                    onClick = { onThemeSelected("system") }
                )
            }
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier.size(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = CircleShape,
            elevation = if (isSelected) CardDefaults.cardElevation(4.dp) else CardDefaults.cardElevation(1.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
fun AboutSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        "About SyNWc",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "About",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            Text(
                text = "SyNWc (Sync Your Notes With Clarity) is a modern note-taking app designed to help you organize your thoughts, track your progress, and reflect on your journey.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App version
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Version",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SyncSection(
    onSyncData: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        "Data Sync",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                supportingContent = {
                    Text("Sync your data with the cloud")
                },
                leadingContent = {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    Button(
                        onClick = onSyncData,
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Sync Now")
                    }
                }
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    settingsViewModel: SettingsViewModel,
    currentName: String,
    currentEmail: String,
    isLoading: Boolean,
    errorMessage: String?
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) settingsViewModel.hideEditProfile() },
        title = { Text("Edit Profile") },
        text = {
            Column {
                if (!errorMessage.isNullOrEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = settingsViewModel.editedName,
                    onValueChange = { settingsViewModel.updateEditedName(it) },
                    label = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = settingsViewModel.editedEmail,
                    onValueChange = { settingsViewModel.updateEditedEmail(it) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { settingsViewModel.saveProfile() },
                enabled = settingsViewModel.editedName.isNotEmpty() &&
                        settingsViewModel.editedEmail.isNotEmpty() &&
                        !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { settingsViewModel.hideEditProfile() },
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}