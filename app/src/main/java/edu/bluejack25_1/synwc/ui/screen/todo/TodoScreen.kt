package edu.bluejack25_1.synwc.ui.screen.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import edu.bluejack25_1.synwc.data.model.Note
import edu.bluejack25_1.synwc.ui.component.BeautifulBottomNavigationBar
import edu.bluejack25_1.synwc.ui.viewmodel.AuthViewModel
import edu.bluejack25_1.synwc.ui.viewmodel.NoteViewModel
import edu.bluejack25_1.synwc.ui.viewmodel.UserViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.launch
import edu.bluejack25_1.synwc.ui.viewmodel.NoteFilter
import edu.bluejack25_1.synwc.ui.viewmodel.SettingsViewModel
import edu.bluejack25_1.synwc.ui.viewmodel.SettingsViewModelFactory
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ToDoScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    noteViewModel: NoteViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val userLoading by userViewModel.loading.collectAsState()
    val userLoggedIn by authViewModel.userLoggedIn.collectAsState()

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context)
    )

    val notes = noteViewModel.filteredNotes
    val isLoading = noteViewModel.isLoading
    val errorMessage = noteViewModel.errorMessage
    val selectedFilter = noteViewModel.selectedFilter
    val showAddEditDialog = noteViewModel.showAddEditDialog
    val editingNote = noteViewModel.editingNote
    val searchQuery = noteViewModel.searchQuery

    val noteTitle = noteViewModel.noteTitle
    val noteDescription = noteViewModel.noteDescription
    val notePriority = noteViewModel.notePriority

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val currentUserId = currentUser?.id ?: ""

    LaunchedEffect(Unit) {
        if (userLoggedIn) {
            if (currentUser == null && !userLoading) {
                println("DEBUG: Loading current user...")
                userViewModel.loadCurrentUser()
            }
        }
    }

    LaunchedEffect(currentUser, userLoggedIn) {
        if (userLoggedIn && currentUser != null) {
            println("DEBUG: User loaded, loading notes for user: ${currentUser!!.id}")
            noteViewModel.loadNotes(currentUser!!.id)
        }
    }

    if (!userLoggedIn) {
        LaunchedEffect(userLoggedIn) {
            navController.navigate("login") {
                popUpTo("todo") { inclusive = true }
            }
        }
        return
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = "Dismiss"
                )
                if (result == SnackbarResult.Dismissed) {
                    noteViewModel.clearError()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "To Do",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.logout(settingsViewModel)
                            navController.navigate("login") {
                                popUpTo("settings") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            BeautifulBottomNavigationBar(navController = navController)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (currentUserId.isNotBlank()) {
                        noteViewModel.showAddNoteDialog()
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please wait while we load your user information")
                        }
                        userViewModel.loadCurrentUser()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier.shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            ) {
                if (userLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Task", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {

            Box(
                modifier = Modifier
                    .size(280.dp)
                    .offset(x = (-100).dp, y = 150.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        CircleShape
                    )
                    .blur(70.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // User Loading Indicator
                if (userLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Search and Filter Section
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { noteViewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search your tasks...") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { noteViewModel.updateSearchQuery("") }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = selectedFilter == NoteFilter.ALL,
                                onClick = { noteViewModel.setFilter(NoteFilter.ALL) },
                                label = { Text("All", fontWeight = FontWeight.Medium) },
                                leadingIcon = if (selectedFilter == NoteFilter.ALL) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            FilterChip(
                                selected = selectedFilter == NoteFilter.ACTIVE,
                                onClick = { noteViewModel.setFilter(NoteFilter.ACTIVE) },
                                label = { Text("Active", fontWeight = FontWeight.Medium) },
                                leadingIcon = if (selectedFilter == NoteFilter.ACTIVE) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            FilterChip(
                                selected = selectedFilter == NoteFilter.COMPLETED,
                                onClick = { noteViewModel.setFilter(NoteFilter.COMPLETED) },
                                label = { Text("Done", fontWeight = FontWeight.Medium) },
                                leadingIcon = if (selectedFilter == NoteFilter.COMPLETED) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Stats
                if (currentUser != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                count = notes.size.toString(),
                                label = "Total Tasks",
                                icon = Icons.Default.FormatListBulleted,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(50.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )

                            StatItem(
                                count = notes.count { !it.isCompleted }.toString(),
                                label = "Active",
                                icon = Icons.Default.PlayCircle,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(50.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )

                            StatItem(
                                count = notes.count { it.isCompleted }.toString(),
                                label = "Completed",
                                icon = Icons.Default.CheckCircle,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes List
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when {
                        userLoading -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "Loading your information...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        isLoading && notes.isEmpty() -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "Loading your tasks...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        notes.isEmpty() -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.TaskAlt,
                                        contentDescription = "No tasks",
                                        modifier = Modifier.size(50.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    "No tasks yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Tap the button below to create your first task",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = notes,
                                    key = { it.id }
                                ) { note ->
                                    NoteItem(
                                        note = note,
                                        onToggleStatus = { isCompleted ->
                                            noteViewModel.toggleNoteStatus(note.id, isCompleted)
                                        },
                                        onEdit = {
                                            if (currentUserId.isNotBlank()) {
                                                noteViewModel.showEditNoteDialog(note)
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Please wait while we load your user information")
                                                }
                                            }
                                        },
                                        onDelete = {
                                            noteViewModel.deleteNote(note.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Note Dialog
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { noteViewModel.hideAddEditDialog() },
            icon = {
                Icon(
                    if (editingNote != null) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    if (editingNote != null) "Edit Task" else "New Task",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                AddEditNoteForm(
                    noteViewModel = noteViewModel,
                    noteTitle = noteTitle,
                    noteDescription = noteDescription,
                    notePriority = notePriority
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentUserId.isNotBlank()) {
                            if (editingNote != null) {
                                noteViewModel.updateNote(currentUserId)
                            } else {
                                noteViewModel.addNote(currentUserId)
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("User information not available. Please try again.")
                                noteViewModel.hideAddEditDialog()
                            }
                        }
                    },
                    enabled = !isLoading && currentUserId.isNotBlank() && noteTitle.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (editingNote != null) "Update Task" else "Add Task",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { noteViewModel.hideAddEditDialog() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", fontSize = 15.sp)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }
}

@Composable
fun StatItem(
    count: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun NoteItem(
    note: Note,
    onToggleStatus: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (note.isCompleted) 2.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (note.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (note.isCompleted) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Custom Checkbox
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .offset(y = 2.dp),
                shape = CircleShape,
                color = if (note.isCompleted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = if (!note.isCompleted) {
                    androidx.compose.foundation.BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                } else null,
                onClick = { onToggleStatus(!note.isCompleted) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (note.isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Note Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (note.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2
                )

                if (note.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = note.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (note.isCompleted) 0.5f else 0.8f
                        ),
                        textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 3,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Priority Indicator
                    PriorityIndicator(priority = note.priority)

                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = getRelativeTime(note.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Actions
            Box {
                IconButton(
                    onClick = { showDropdown = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Task") },
                        onClick = {
                            onEdit()
                            showDropdown = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Task") },
                        onClick = {
                            onDelete()
                            showDropdown = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityIndicator(priority: Note.Priority) {
    val (color, text, icon) = when (priority) {
        Note.Priority.LOW -> Triple(
            MaterialTheme.colorScheme.tertiary,
            "Low",
            Icons.Default.SignalCellularAlt1Bar
        )
        Note.Priority.MEDIUM -> Triple(
            MaterialTheme.colorScheme.secondary,
            "Medium",
            Icons.Default.SignalCellularAlt2Bar
        )
        Note.Priority.HIGH -> Triple(
            MaterialTheme.colorScheme.error,
            "High",
            Icons.Default.PriorityHigh
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditNoteForm(
    noteViewModel: NoteViewModel,
    noteTitle: String,
    noteDescription: String,
    notePriority: Note.Priority
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Title Field
        OutlinedTextField(
            value = noteTitle,
            onValueChange = { noteViewModel.updateNoteTitle(it) },
            label = { Text("Task Title *") },
            placeholder = { Text("What needs to be done?") },
            leadingIcon = {
                Icon(
                    Icons.Default.Title,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            isError = noteTitle.isBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            supportingText = {
                if (noteTitle.isBlank()) {
                    Text(
                        "Title is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )

        // Description Field
        OutlinedTextField(
            value = noteDescription,
            onValueChange = { noteViewModel.updateNoteDescription(it) },
            label = { Text("Description (Optional)") },
            placeholder = { Text("Add more details...") },
            leadingIcon = {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            singleLine = false,
            maxLines = 5,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        // Priority Selection
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Priority Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Note.Priority.entries.forEach { priority ->
                    val (color, icon) = when (priority) {
                        Note.Priority.LOW -> Pair(MaterialTheme.colorScheme.tertiary, Icons.Default.SignalCellularAlt1Bar)
                        Note.Priority.MEDIUM -> Pair(MaterialTheme.colorScheme.secondary, Icons.Default.SignalCellularAlt2Bar)
                        Note.Priority.HIGH -> Pair(MaterialTheme.colorScheme.error, Icons.Default.PriorityHigh)
                    }

                    Surface(
                        onClick = { noteViewModel.updateNotePriority(priority) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = if (notePriority == priority) {
                            color.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        border = if (notePriority == priority) {
                            androidx.compose.foundation.BorderStroke(2.dp, color)
                        } else null,
                        tonalElevation = if (notePriority == priority) 2.dp else 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = priority.name,
                                tint = if (notePriority == priority) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = priority.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (notePriority == priority) FontWeight.Bold else FontWeight.Medium,
                                color = if (notePriority == priority) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        diff < 604800000 -> "${diff / 86400000}d ago"
        else -> "${diff / 604800000}w ago"
    }
}