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
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.example.sentio.presentation.state.HomeUiEffect
import com.example.sentio.presentation.state.HomeUiEvent
import com.example.sentio.presentation.theme.KlarityColors
import com.example.sentio.presentation.viewmodel.HomeViewModel
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
    val notes by viewModel.notes.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedNoteId by viewModel.selectedNoteId.collectAsState()
    val expandedFolderIds by viewModel.expandedFolderIds.collectAsState()
    val pinnedSectionExpanded by viewModel.pinnedSectionExpanded.collectAsState()
    
    val selectedNote = notes.find { it.id == selectedNoteId }
    val pinnedNotes = notes.filter { it.isPinned }
    val recentNotes = notes.sortedByDescending { it.updatedAt }.take(10)
    
    var showSlashMenu by remember { mutableStateOf(false) }
    var currentNavDestination by remember { mutableStateOf(NavDestination.HOME) }
    
    // Workspace layout state
    var workspaceConfig by remember { mutableStateOf(WorkspacePresets.notesDefault) }
    
    // Command Palette state
    var showCommandPalette by remember { mutableStateOf(false) }
    
    // Top command bar state
    var topSearchQuery by remember { mutableStateOf("") }
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
            NavDestination.TASKS -> WorkspacePresets.tasksFull
            NavDestination.SETTINGS -> workspaceConfig
        }
    }

    // Main layout with keyboard handling
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KlarityColors.BgPrimary)
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
                // Top Command Bar
                TopCommandBar(
                    currentPath = breadcrumbPath,
                    searchQuery = topSearchQuery,
                    onSearchQueryChange = { topSearchQuery = it },
                    onCommandPaletteOpen = { showCommandPalette = true },
                    syncStatus = SyncStatus.SYNCED,
                    currentTheme = currentTheme,
                    onThemeChange = { currentTheme = it },
                    aiModelName = "GPT-4",
                    aiTemperature = 0.7f
                )
                
                // Workspace Top Bar with layout controls
                WorkspaceTopBar(
                    currentMode = workspaceConfig.mode,
                    currentDestination = currentNavDestination,
                    onLayoutModeChange = { mode ->
                        workspaceConfig = when (mode) {
                            WorkspaceLayoutMode.SINGLE_PANE -> workspaceConfig.copy(
                                mode = mode,
                                leftPane = null,
                                rightPane = null
                            )
                            WorkspaceLayoutMode.DUAL_PANE -> workspaceConfig.copy(
                                mode = mode,
                                leftPane = PaneType.NOTES_LIST,
                                rightPane = null
                            )
                            WorkspaceLayoutMode.TRI_PANE -> WorkspacePresets.notesDefault
                            WorkspaceLayoutMode.FOCUS -> workspaceConfig.copy(
                                mode = mode,
                                leftPane = null,
                                rightPane = null
                            )
                        }
                    },
                    onToggleLeftPane = {
                        workspaceConfig = if (workspaceConfig.leftPane != null) {
                            workspaceConfig.copy(leftPane = null)
                        } else {
                            workspaceConfig.copy(leftPane = PaneType.NOTES_LIST)
                        }
                    },
                    onToggleRightPane = {
                        workspaceConfig = if (workspaceConfig.rightPane != null) {
                            workspaceConfig.copy(rightPane = null)
                        } else {
                            workspaceConfig.copy(rightPane = PaneType.TASKS)
                        }
                    }
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
                                    PaneType.NOTES_LIST -> NotesListPane(
                                        notes = notes,
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
                                        onAskAI = { note ->
                                            // TODO: Open AI chat with note context
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
                                // Right pane content
                                when (workspaceConfig.rightPane) {
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
