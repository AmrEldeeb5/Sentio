package com.example.klarity.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.klarity.presentation.navigation.NavDestination
import com.example.klarity.presentation.navigation.NavigationRail
import com.example.klarity.presentation.screen.graph.GraphScreen
import com.example.klarity.presentation.state.HomeUiEffect
import com.example.klarity.presentation.state.HomeUiEvent
import com.example.klarity.presentation.state.HomeUiState
import com.example.klarity.presentation.viewmodel.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Home Screen - Klarity Desktop Experience
 *
 * Desktop Philosophy:
 * - Keyboard-first with Command Palette (Ctrl+K)
 * - Multi-pane & multi-window support
 * - Spatial & expansive workspace
 * - Quiet power with subtle, dark surfaces
 *
 * Layout Modes:
 * - Single Pane: Full focus on Notes, Graph, or Tasks
 * - Dual Pane: Notes list + Editor, Tasks + Editor, Graph + AI Chat
 * - Tri-Pane: List + Editor + AI Context
 * - Focus: Minimal UI, just editor
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    // Collect unified UI state with lifecycle awareness for better performance
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Render based on state
    when (val state = uiState) {
        is HomeUiState.Idle,
        is HomeUiState.Loading -> {
            HomeLoadingScreen()
        }
        is HomeUiState.Error -> {
            HomeErrorScreen(
                message = state.message,
                onRetry = state.retryAction
            )
        }
        is HomeUiState.Success -> {
            HomeScreenContent(
                state = state,
                viewModel = viewModel
            )
        }
    }
}

/**
 * Loading screen with skeleton placeholder
 */
@Composable
private fun HomeLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Text(
                text = "Loading your workspace...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error screen with retry option
 */
@Composable
private fun HomeErrorScreen(
    message: String,
    onRetry: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayMedium
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text(
                        text = "Try Again",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Main content when state is Success
 */
@Composable
private fun HomeScreenContent(
    state: HomeUiState.Success,
    viewModel: HomeViewModel
) {
    // Derived state from success state
    val notes = state.notes
    val folders = state.folders
    val searchQuery = state.searchQuery
    val selectedNoteId = state.selectedNoteId
    val expandedFolderIds = state.expandedFolderIds

    val selectedNote = remember(notes, selectedNoteId) {
        notes.find { it.id == selectedNoteId }
    }
    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val recentNotes = remember(notes) { notes.sortedByDescending { it.updatedAt }.take(10) }

    var showSlashMenu by remember { mutableStateOf(false) }
    var currentNavDestination by remember { mutableStateOf(NavDestination.HOME) }
    
    // Workspace layout state
    var workspaceConfig by remember { mutableStateOf(WorkspacePresets.notesDefault) }
    
    // Command Palette state
    var showCommandPalette by remember { mutableStateOf(false) }
    
    // Top command bar state - synced with ViewModel search
    var currentTheme by remember { mutableStateOf(ThemeMode.DARK) }
    
    // Multi-select state for notes
    var selectedNoteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Breadcrumb path
    val breadcrumbPath = remember(currentNavDestination, selectedNote) {
        buildList {
            add("Klarity")
            add(currentNavDestination.label)
            selectedNote?.let { add(it.title.ifEmpty { "Untitled" }) }
        }
    }

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
    
    // Handle Command Palette commands
    fun handleCommand(command: CommandItem) {
        when (command) {
            CommandItem.GoToHome -> currentNavDestination = NavDestination.HOME
            CommandItem.GoToNotes -> currentNavDestination = NavDestination.NOTES
            CommandItem.GoToTasks -> currentNavDestination = NavDestination.TASKS
            CommandItem.GoToSettings -> currentNavDestination = NavDestination.SETTINGS
            CommandItem.CreateNote -> viewModel.onEvent(HomeUiEvent.CreateNote)
            CommandItem.ToggleSidebar -> {
                workspaceConfig = if (workspaceConfig.leftPane != null) {
                    workspaceConfig.copy(leftPane = null)
                } else {
                    workspaceConfig.copy(leftPane = PaneType.NOTES_LIST)
                }
            }
            CommandItem.ToggleRightPanel -> {
                workspaceConfig = if (workspaceConfig.rightPane != null) {
                    workspaceConfig.copy(rightPane = null)
                } else {
                    workspaceConfig.copy(rightPane = PaneType.TASKS)
                }
            }
            CommandItem.ZenMode -> {
                workspaceConfig = workspaceConfig.copy(
                    mode = WorkspaceLayoutMode.FOCUS,
                    leftPane = null,
                    rightPane = null
                )
            }
            else -> { /* TODO: Handle other commands */ }
        }
    }
    
    // Update workspace config based on navigation destination
    LaunchedEffect(currentNavDestination) {
        workspaceConfig = when (currentNavDestination) {
            NavDestination.HOME -> WorkspacePresets.notesDefault.copy(
                centerPane = null // Use dashboard instead
            )
            NavDestination.NOTES -> if (workspaceConfig.mode == WorkspaceLayoutMode.FOCUS) {
                workspaceConfig
            } else {
                WorkspacePresets.notesDefault
            }
            NavDestination.GRAPH -> WorkspacePresets.notesDefault.copy(
                centerPane = PaneType.GRAPH,
                leftPane = null,
                rightPane = null
            )
            NavDestination.TASKS -> WorkspacePresets.tasksFull
            NavDestination.SETTINGS -> workspaceConfig
        }
    }

    // Main layout with keyboard handling
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onKeyEvent { event ->
                // Command Palette shortcut: Ctrl+K
                if (event.type == KeyEventType.KeyDown &&
                    event.key == Key.K &&
                    event.isCtrlPressed
                ) {
                    showCommandPalette = true
                    true
                } else {
                    false
                }
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Navigation Rail (always visible, 72px)
            NavigationRail(
                currentDestination = currentNavDestination,
                onDestinationSelected = { destination ->
                    currentNavDestination = destination
                }
            )

            // Main Workspace Area
            Column(modifier = Modifier.fillMaxSize().weight(1f)) {
                // Top Command Bar - contextual and state-aware
                TopCommandBar(
                    currentPath = breadcrumbPath,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query ->
                        viewModel.onEvent(HomeUiEvent.SearchQueryChanged(query))
                        // Auto-navigate to Notes when searching
                        if (query.isNotEmpty() && currentNavDestination == NavDestination.HOME) {
                            currentNavDestination = NavDestination.NOTES
                        }
                    },
                    onCommandPaletteOpen = { showCommandPalette = true },
                    syncStatus = SyncStatus.SYNCED,
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it },
                    aiModelName = "GPT-4",
                    aiTemperature = 0.7f,
                    // Contextual state
                    hasNotes = notes.isNotEmpty(),
                    isEditingNote = selectedNote != null,
                    noteCount = notes.size
                )

                // Main content area
                Row(modifier = Modifier.weight(1f)) {
                    // Adaptive Workspace or Dashboard
                    if (currentNavDestination == NavDestination.HOME) {
                        // 3-Column Home Dashboard
                        HomeDashboard(
                            recentNotes = recentNotes,
                            recentItems = emptyList(), // TODO: Populate with recent items
                            focusItems = emptyList(), // TODO: Populate with focus items
                            onNoteClick = { note ->
                                viewModel.onEvent(HomeUiEvent.SelectNote(note.id))
                                currentNavDestination = NavDestination.NOTES
                            },
                            onRecentItemClick = { /* TODO */ },
                            onFocusItemClick = { /* TODO */ },
                            onFocusItemToggle = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Adaptive Multi-Pane Workspace
                        AdaptiveWorkspace(
                            config = workspaceConfig,
                            onConfigChange = { workspaceConfig = it },
                            leftPaneContent = { modifier ->
                                // Left pane content based on type
                                when (workspaceConfig.leftPane) {
                                    PaneType.FILE_EXPLORER -> FileExplorerPanel(
                                        folders = folders,
                                        notes = notes,
                                        expandedFolderIds = expandedFolderIds,
                                        selectedNoteId = selectedNoteId,
                                        projectName = "Klarity",
                                        onToggleFolder = { folderId ->
                                            viewModel.onEvent(HomeUiEvent.ToggleFolder(folderId))
                                        },
                                        onNoteSelect = { noteId ->
                                            viewModel.onEvent(HomeUiEvent.SelectNote(noteId))
                                        },
                                        onCreateFolder = { folderName ->
                                            viewModel.onEvent(HomeUiEvent.CreateFolder(folderName))
                                        },
                                        onRenameFolder = { folderId, newName ->
                                            viewModel.onEvent(HomeUiEvent.RenameFolder(folderId, newName))
                                        },
                                        onDeleteFolder = { folderId ->
                                            viewModel.onEvent(HomeUiEvent.DeleteFolder(folderId))
                                        },
                                        onMoveNoteToFolder = { noteId, folderId ->
                                            viewModel.onEvent(HomeUiEvent.MoveNoteToFolder(noteId, folderId))
                                        }
                                    )
                                    PaneType.TASKS -> TasksPane(modifier = modifier)
                                    else -> {}
                                }
                            },
                            centerPaneContent = { modifier ->
                                // Center pane content based on type and destination
                                when (workspaceConfig.centerPane ?: PaneType.EDITOR) {
                                    PaneType.EDITOR -> EditorPanel(
                                        selectedNote = selectedNote,
                                        folders = folders,
                                        showSlashMenu = showSlashMenu,
                                        onToggleSlashMenu = { showSlashMenu = !showSlashMenu },
                                        onCreateNote = { viewModel.onEvent(HomeUiEvent.CreateNote) },
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
                                        onWikiLinkClick = { noteName ->
                                            // Find note by title and navigate to it
                                            val linkedNote = notes.find { 
                                                it.title.equals(noteName, ignoreCase = true) 
                                            }
                                            if (linkedNote != null) {
                                                viewModel.onEvent(HomeUiEvent.SelectNote(linkedNote.id))
                                            } else {
                                                // Create new note with this title
                                                viewModel.onEvent(HomeUiEvent.CreateNoteWithTitle(noteName))
                                            }
                                        },
                                        modifier = modifier
                                    )
                                    PaneType.GRAPH -> GraphScreen(
                                        notes = notes,
                                        selectedNoteId = selectedNoteId,
                                        onNoteSelected = { noteId ->
                                            viewModel.onEvent(HomeUiEvent.SelectNote(noteId))
                                            currentNavDestination = NavDestination.NOTES
                                        },
                                        modifier = modifier
                                    )
                                    PaneType.TASKS -> TasksPane(modifier = modifier)
                                    else -> EditorPanel(
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
                                        onWikiLinkClick = { noteName ->
                                            // Find note by title and navigate to it
                                            val linkedNote = notes.find { 
                                                it.title.equals(noteName, ignoreCase = true) 
                                            }
                                            if (linkedNote != null) {
                                                viewModel.onEvent(HomeUiEvent.SelectNote(linkedNote.id))
                                            } else {
                                                // Create new note with this title
                                                viewModel.onEvent(HomeUiEvent.CreateNoteWithTitle(noteName))
                                            }
                                        },
                                        modifier = modifier
                                    )
                                }
                            },
                            rightPaneContent = { modifier ->
                                // Right pane content - Notes List
                                when (workspaceConfig.rightPane) {
                                    PaneType.NOTES_LIST -> NotesListPane(
                                        notes = notes,
                                        folders = folders,
                                        selectedNoteId = selectedNoteId,
                                        selectedNoteIds = selectedNoteIds,
                                        searchQuery = searchQuery,
                                        onSearchQueryChange = { viewModel.onEvent(HomeUiEvent.SearchQueryChanged(it)) },
                                        onNoteClick = { note -> 
                                            viewModel.onEvent(HomeUiEvent.SelectNote(note.id))
                                            selectedNoteIds = emptySet()
                                        },
                                        onNoteSelect = { note, ctrlPressed, shiftPressed ->
                                            if (ctrlPressed) {
                                                selectedNoteIds = if (note.id in selectedNoteIds) {
                                                    selectedNoteIds - note.id
                                                } else {
                                                    selectedNoteIds + note.id
                                                }
                                            } else {
                                                selectedNoteIds = setOf(note.id)
                                            }
                                        },
                                        onCreateNote = { viewModel.onEvent(HomeUiEvent.CreateNote) },
                                        onTogglePin = { noteId -> viewModel.onEvent(HomeUiEvent.ToggleNotePin(noteId)) },
                                        onDeleteNote = { noteId -> viewModel.onEvent(HomeUiEvent.DeleteNote(noteId)) },
                                        onAskAI = { note -> },
                                        modifier = modifier
                                    )
                                    PaneType.TASKS -> TasksPane(modifier = modifier)
                                    else -> {}
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Command Palette Overlay
        CommandPalette(
            isOpen = showCommandPalette,
            onDismiss = { showCommandPalette = false },
            onCommandSelected = { command ->
                handleCommand(command)
            }
        )
    }
}
