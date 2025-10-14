// ui/screen/todo/ToDoScreen.kt
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import edu.bluejack25_1.synwc.data.model.Note
import edu.bluejack25_1.synwc.ui.component.BeautifulBottomNavigationBar
import edu.bluejack25_1.synwc.ui.viewmodel.AuthViewModel
import edu.bluejack25_1.synwc.ui.viewmodel.NoteFilter
import edu.bluejack25_1.synwc.ui.viewmodel.NoteViewModel
import edu.bluejack25_1.synwc.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel(),
    noteViewModel: NoteViewModel = viewModel()
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val userLoggedIn by authViewModel.userLoggedIn.collectAsState()

    // Collect states from NoteViewModel
    val notes by noteViewModel.filteredNotes
    val isLoading by noteViewModel.isLoading
    val errorMessage by noteViewModel.errorMessage
    val selectedFilter by noteViewModel.selectedFilter
    val showAddEditDialog by noteViewModel.showAddEditDialog
    val editingNote by noteViewModel.editingNote
    val searchQuery by noteViewModel.searchQuery

    // Load notes when screen appears or user changes
    LaunchedEffect(currentUser) {
        if (userLoggedIn && currentUser != null) {
            noteViewModel.loadNotes(currentUser!!.id)
        }
    }

    // Handle authentication
    if (!userLoggedIn) {
        LaunchedEffect(userLoggedIn) {
            navController.navigate("login") {
                popUpTo("todo") { inclusive = true }
            }
        }
        return
    }

    // Handle errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            // You can show a snackbar here
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "To-Do List 📝",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { noteViewModel.showAddNoteDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { noteViewModel.updateSearchQuery(it) },
                        label = { Text("Search notes...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { noteViewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = selectedFilter == NoteFilter.ALL,
                            onClick = { noteViewModel.setFilter(NoteFilter.ALL) },
                            label = { Text("All") }
                        )
                        FilterChip(
                            selected = selectedFilter == NoteFilter.ACTIVE,
                            onClick = { noteViewModel.setFilter(NoteFilter.ACTIVE) },
                            label = { Text("Active") }
                        )
                        FilterChip(
                            selected = selectedFilter == NoteFilter.COMPLETED,
                            onClick = { noteViewModel.setFilter(NoteFilter.COMPLETED) },
                            label = { Text("Completed") }
                        )
                    }
                }
            }

            // Stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        count = notes.size.toString(),
                        label = "Total"
                    )
                    StatItem(
                        count = notes.count { !it.isCompleted }.toString(),
                        label = "Active"
                    )
                    StatItem(
                        count = notes.count { it.isCompleted }.toString(),
                        label = "Completed"
                    )
                }
            }

            // Notes List
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.NoteAdd,
                            contentDescription = "No notes",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No notes found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Tap the + button to add your first note",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteItem(
                            note = note,
                            onToggleStatus = { isCompleted ->
                                noteViewModel.toggleNoteStatus(note.id, isCompleted)
                            },
                            onEdit = { noteViewModel.showEditNoteDialog(note) },
                            onDelete = { noteViewModel.deleteNote(note.id) }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Note Dialog
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { noteViewModel.hideAddEditDialog() },
            title = {
                Text(if (editingNote != null) "Edit Note" else "Add New Note")
            },
            text = {
                AddEditNoteForm(noteViewModel = noteViewModel)
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editingNote != null) {
                            noteViewModel.updateNote(currentUser?.id ?: "")
                        } else {
                            noteViewModel.addNote(currentUser?.id ?: "")
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (editingNote != null) "Update" else "Add")
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = { noteViewModel.hideAddEditDialog() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                verticalAlignment = Alignment.Top
            ) {
                // Checkbox
                Checkbox(
                    checked = note.isCompleted,
                    onCheckedChange = onToggleStatus,
                    modifier = Modifier.align(Alignment.Top)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Note Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    if (note.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = note.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Priority Indicator
                        PriorityIndicator(priority = note.priority)

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = getRelativeTime(note.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Actions
                DropdownMenu(
                    expanded = remember { mutableStateOf(false) }.value,
                    onDismissRequest = { /* Handle dismiss */ }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEdit()
                            // Close dropdown
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            // Close dropdown
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityIndicator(priority: Note.Priority) {
    val (color, text) = when (priority) {
        Note.Priority.LOW -> MaterialTheme.colorScheme.primary to "Low"
        Note.Priority.MEDIUM -> MaterialTheme.colorScheme.secondary to "Medium"
        Note.Priority.HIGH -> MaterialTheme.colorScheme.error to "High"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AddEditNoteForm(noteViewModel: NoteViewModel) {
    val noteTitle by noteViewModel.noteTitle
    val noteDescription by noteViewModel.noteDescription
    val notePriority by noteViewModel.notePriority

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = noteTitle,
            onValueChange = { noteViewModel.updateNoteTitle(it) },
            label = { Text("Title *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        OutlinedTextField(
            value = noteDescription,
            onValueChange = { noteViewModel.updateNoteDescription(it) },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            singleLine = false,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        // Priority Selection
        Column {
            Text(
                "Priority",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Note.Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = notePriority == priority,
                        onClick = { noteViewModel.updateNotePriority(priority) },
                        label = { Text(priority.name) }
                    )
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