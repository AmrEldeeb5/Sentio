package com.example.sentio.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.sentio.presentation.state.HomeUiEffect
import com.example.sentio.presentation.state.HomeUiEvent
import com.example.sentio.presentation.theme.SentioColors
import com.example.sentio.presentation.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Home Screen - Composed of multiple panels
 *
 * Layout:
 * - TopBar (top)
 * - AIToolbeltSidebar (left)
 * - FileExplorerPanel (left-center)
 * - EditorPanel (center)
 * - NotesTreeSidebar (right)
 * - AIContextSidebar (far right, toggleable)
 */
@Composable
fun HomeScreen(
    onNoteDoubleClick: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedNoteId by viewModel.selectedNoteId.collectAsState()
    val expandedFolderIds by viewModel.expandedFolderIds.collectAsState()
    val pinnedSectionExpanded by viewModel.pinnedSectionExpanded.collectAsState()
    
    val selectedNote = notes.find { it.id == selectedNoteId }
    var showSlashMenu by remember { mutableStateOf(false) }
    var showContextSidebar by remember { mutableStateOf(true) }

    // Handle navigation effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToEditor -> onNoteDoubleClick(effect.noteId)
                is HomeUiEffect.ShowSnackbar -> { /* TODO: Show snackbar */ }
                is HomeUiEffect.ShowError -> { /* TODO: Show error */ }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SentioColors.BgPrimary)) {
        // Top Bar
        TopBar(
            showContextSidebar = showContextSidebar,
            onToggleContextSidebar = { showContextSidebar = !showContextSidebar }
        )

        // Main Content Row
        Row(modifier = Modifier.fillMaxSize().weight(1f)) {
            // Left: AI Toolbelt Sidebar
            AIToolbeltSidebar()

            // File Explorer Panel
            FileExplorerPanel(
                folders = folders,
                notes = notes,
                expandedFolderIds = expandedFolderIds,
                selectedNoteId = selectedNoteId,
                onToggleFolder = { viewModel.onEvent(HomeUiEvent.ToggleFolder(it)) },
                onNoteSelect = { viewModel.onEvent(HomeUiEvent.SelectNote(it)) },
                onCreateFolder = { viewModel.onEvent(HomeUiEvent.CreateFolder(it)) }
            )

            // Center: Editor Panel
            EditorPanel(
                selectedNote = selectedNote,
                folders = folders,
                showSlashMenu = showSlashMenu,
                onToggleSlashMenu = { showSlashMenu = !showSlashMenu },
                onTitleChange = { title ->
                    selectedNote?.let { viewModel.onEvent(HomeUiEvent.UpdateNoteTitle(it.id, title)) }
                },
                onContentChange = { content ->
                    selectedNote?.let { viewModel.onEvent(HomeUiEvent.UpdateNoteContent(it.id, content)) }
                },
                onTogglePin = {
                    selectedNote?.let { viewModel.onEvent(HomeUiEvent.ToggleNotePin(it.id)) }
                },
                onDelete = {
                    selectedNote?.let { viewModel.onEvent(HomeUiEvent.DeleteNote(it.id)) }
                },
                modifier = Modifier.weight(1f)
            )

            // Right: Notes Sidebar with Tree View
            NotesTreeSidebar(
                notes = notes,
                folders = folders,
                expandedFolderIds = expandedFolderIds,
                pinnedSectionExpanded = pinnedSectionExpanded,
                searchQuery = searchQuery,
                selectedNoteId = selectedNoteId,
                onSearchQueryChange = { viewModel.onEvent(HomeUiEvent.SearchQueryChanged(it)) },
                onNoteSelect = { viewModel.onEvent(HomeUiEvent.SelectNote(it)) },
                onCreateNote = { viewModel.onEvent(HomeUiEvent.CreateNote) },
                onToggleFolder = { viewModel.onEvent(HomeUiEvent.ToggleFolder(it)) },
                onTogglePinnedSection = { viewModel.onEvent(HomeUiEvent.TogglePinnedSection) },
                onTogglePin = { noteId -> viewModel.onEvent(HomeUiEvent.ToggleNotePin(noteId)) },
                onDeleteNote = { noteId -> viewModel.onEvent(HomeUiEvent.DeleteNote(noteId)) }
            )

            // AI Context Sidebar (4th panel - toggleable)
            AnimatedVisibility(
                visible = showContextSidebar && selectedNote != null,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                AIContextSidebar(note = selectedNote)
            }
        }
    }
}
