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
    var projectName by remember { mutableStateOf("My Workspace") }
    var focusMode by remember { mutableStateOf(false) }

    // Handle effects (snackbar, errors)
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeUiEffect.NavigateToEditor -> { /* No longer navigating - editing in place */ }
                is HomeUiEffect.ShowSnackbar -> { /* TODO: Show snackbar */ }
                is HomeUiEffect.ShowError -> { /* TODO: Show error */ }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(SentioColors.BgPrimary)) {
        // Top Bar (hidden in focus mode)
        AnimatedVisibility(
            visible = !focusMode,
            enter = fadeIn() + androidx.compose.animation.expandVertically(),
            exit = fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            TopBar(
                showContextSidebar = showContextSidebar,
                onToggleContextSidebar = { showContextSidebar = !showContextSidebar }
            )
        }

        // Main Content Row
        Row(modifier = Modifier.fillMaxSize().weight(1f)) {
            // Left: AI Toolbelt Sidebar (hidden in focus mode)
            AnimatedVisibility(
                visible = !focusMode,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            ) {
                AIToolbeltSidebar()
            }

            // File Explorer Panel (hidden in focus mode)
            AnimatedVisibility(
                visible = !focusMode,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
            ) {
                FileExplorerPanel(
                    folders = folders,
                    notes = notes,
                    expandedFolderIds = expandedFolderIds,
                    selectedNoteId = selectedNoteId,
                    projectName = projectName,
                    onProjectNameChange = { projectName = it },
                    onToggleFolder = { viewModel.onEvent(HomeUiEvent.ToggleFolder(it)) },
                    onNoteSelect = { viewModel.onEvent(HomeUiEvent.SelectNote(it)) },
                    onCreateFolder = { viewModel.onEvent(HomeUiEvent.CreateFolder(it)) },
                    onRenameFolder = { id, name -> viewModel.onEvent(HomeUiEvent.RenameFolder(id, name)) },
                    onDeleteFolder = { viewModel.onEvent(HomeUiEvent.DeleteFolder(it)) },
                    onMoveNoteToFolder = { noteId, folderId -> viewModel.onEvent(HomeUiEvent.MoveNoteToFolder(noteId, folderId)) }
                )
            }

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
                onStatusChange = { status ->
                    selectedNote?.let { viewModel.onEvent(HomeUiEvent.UpdateNoteStatus(it.id, status)) }
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
