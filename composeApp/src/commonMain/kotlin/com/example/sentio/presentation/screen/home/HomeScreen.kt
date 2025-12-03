package com.example.sentio.presentation.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sentio.domain.models.Note
import com.example.sentio.presentation.state.HomeUiEvent
import com.example.sentio.presentation.theme.searchBarBg
import com.example.sentio.presentation.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNoteClick: (String) -> Unit,
    onCreateNote: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedNoteId by viewModel.selectedNoteId.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar
        Sidebar(
            notes = notes,
            searchQuery = searchQuery,
            selectedNoteId = selectedNoteId,
            onSearchQueryChange = { viewModel.onEvent(HomeUiEvent.SearchQueryChanged(it)) },
            onNoteClick = onNoteClick,
            onCreateNote = { viewModel.onEvent(HomeUiEvent.CreateNote) },
            modifier = Modifier.width(270.dp).fillMaxHeight()
        )

        // Main content area

    }
}

@Composable
private fun Sidebar(
    notes: List<Note>,
    searchQuery: String,
    selectedNoteId: String?,
    onSearchQueryChange: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header - no extra padding needed, part of main padding
            WorkspaceHeader()

            HorizontalDivider(modifier = Modifier.height(16.dp))

            // Search bar
            SearchBarWithCreate(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onCreateNote = onCreateNote
            )

            Spacer(modifier = Modifier.height(16.dp))

            // View controls - if you add them later
            // ViewControls()
            // Spacer(modifier = Modifier.height(16.dp))

            // Cluster toggle
            ClusterToggle()

            Spacer(modifier = Modifier.height(20.dp))

            // Notes list with groups
            NotesList(
                notes = notes,
                selectedNoteId = selectedNoteId,
                onNoteClick = onNoteClick,
                modifier = Modifier.weight(1f)
            )

            // Footer with count
            NotesFooter(
                noteCount = notes.size,
                selectedCount = 0
            )
        }
    }
}

@Composable
private fun WorkspaceHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile icon
        Surface(
            modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("S", style = MaterialTheme.typography.titleMedium)
            }
        }

        Column {
            Text(
                text = "Sentio Workspace",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "user@sentio.ai",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchBarWithCreate(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CustomSearchBar(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f)
        )

        // Custom Add Button matching the image (Rounded Square)
        Surface(
            modifier = Modifier
                .size(52.dp)
                .clickable(onClick = onCreateNote),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create note",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(52.dp),
        placeholder = {
            Text(
                text = "Search notes...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.searchBarBg,
            unfocusedContainerColor = MaterialTheme.colorScheme.searchBarBg,
            disabledContainerColor = MaterialTheme.colorScheme.searchBarBg,
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        )
    )
}

@Composable
private fun ClusterToggle(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Cluster by Topic",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Let AI organize your notes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = true,
            onCheckedChange = { }
        )
    }
}

@Composable
private fun GroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun CompactNoteItem(
    note: Note,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.searchBarBg else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = note.preview(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NotesFooter(
    noteCount: Int,
    selectedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$noteCount notes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (selectedCount > 0) {
            Text(
                text = "$selectedCount selected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun NotesList(
    notes: List<Note>,
    selectedNoteId: String?,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp) // Tighter spacing
    ) {
        // Group headers
        item {
            GroupHeader("PROJECT PHOENIX UPDATES")
        }

        items(notes.filter { /* filter by group */ true }) { note ->
            CompactNoteItem(
                note = note,
                onClick = { onNoteClick(note.id) },
                isSelected = note.id == selectedNoteId
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            GroupHeader("AI RESEARCH NOTES")
        }

        // More groups...
    }
}

@Composable
private fun EmptyState(onCreateNote: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Sentio",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your unified developer operating system",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onCreateNote) {
            Text("Create Your First Note")
        }
    }
}
