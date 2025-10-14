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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import edu.bluejack25_1.synwc.R
import edu.bluejack25_1.synwc.ui.viewmodel.SettingsViewModel
import edu.bluejack25_1.synwc.util.ImagePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    context: Context = LocalContext.current,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val themeMode by settingsViewModel.themeMode.collectAsState(initial = "system")
    val userName by settingsViewModel.userName.collectAsState(initial = "")
    val userEmail by settingsViewModel.userEmail.collectAsState(initial = "")
    val profileImageUrl by settingsViewModel.profileImageUrl.collectAsState(initial = "")

    val imagePicker = ImagePicker()
    val pickImage = imagePicker.rememberImagePicker { uri ->
        // CHANGED: Use the new function name
        settingsViewModel.updateImageUri(uri)
        if (uri != null) {
            settingsViewModel.uploadProfileImage()
        }
    }

    // Handle image picker dialog
    if (settingsViewModel.showImagePicker) {
        AlertDialog(
            onDismissRequest = { settingsViewModel.hideImagePicker() },
            title = { Text("Change Profile Picture") },
            text = { Text("Choose how you want to set your profile picture") },
            confirmButton = {
                Button(
                    onClick = {
                        pickImage()
                        settingsViewModel.hideImagePicker()
                    }
                ) {
                    Text("Choose from Gallery")
                }
            },
            dismissButton = {
                TextButton(onClick = { settingsViewModel.hideImagePicker() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit profile dialog
    if (settingsViewModel.showEditProfileDialog) {
        EditProfileDialog(
            settingsViewModel = settingsViewModel,
            currentName = userName,
            currentEmail = userEmail
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
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
        }
    }
}

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
fun ProfileCard(
    userName: String,
    userEmail: String,
    profileImageUrl: String,
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
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onChangePhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUrl.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUrl),
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile picture",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Edit icon overlay
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
                    modifier = Modifier.size(48.dp)
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
fun EditProfileDialog(
    settingsViewModel: SettingsViewModel,
    currentName: String,
    currentEmail: String
) {
    AlertDialog(
        onDismissRequest = { settingsViewModel.hideEditProfile() },
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(
                    value = settingsViewModel.editedName,
                    onValueChange = { settingsViewModel.updateEditedName(it) },
                    label = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = settingsViewModel.editedEmail,
                    onValueChange = { settingsViewModel.updateEditedEmail(it) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { settingsViewModel.saveProfile() },
                enabled = settingsViewModel.editedName.isNotEmpty() && settingsViewModel.editedEmail.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { settingsViewModel.hideEditProfile() }) {
                Text("Cancel")
            }
        }
    )
}