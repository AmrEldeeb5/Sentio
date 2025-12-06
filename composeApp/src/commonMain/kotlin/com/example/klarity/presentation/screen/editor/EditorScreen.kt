package com.example.klarity.presentation.screen.editor

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Folder
import com.example.klarity.domain.models.Note
import com.example.klarity.presentation.state.EditorUiEvent
import com.example.klarity.presentation.state.EditorUiState
import com.example.klarity.presentation.state.EditorViewMode
import com.example.klarity.presentation.theme.KlarityColors
import com.example.klarity.presentation.viewmodel.EditorViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EditorScreen(
    noteId: String,
    onBack: () -> Unit,
    onNavigateToNote: (String) -> Unit = {},
    viewModel: EditorViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val pinnedNotes by viewModel.pinnedNotes.collectAsState()
    
    // Sidebar search state
    var sidebarSearchQuery by remember { mutableStateOf("") }
    
    // Local UI state
    var viewMode by remember { mutableStateOf(EditorViewMode.SINGLE_PANE) }
    var showRightSidebar by remember { mutableStateOf(false) }
    var showAISuggestion by remember { mutableStateOf(true) }

    LaunchedEffect(noteId) {
        if (noteId != "new") {
            viewModel.onEvent(EditorUiEvent.LoadNote(noteId))
        } else {
            viewModel.onEvent(EditorUiEvent.CreateNewNote)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = KlarityColors.BgPrimary
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Editor Header with view mode toggles
            EditorHeader(
                viewMode = viewMode,
                onViewModeChange = { viewMode = it },
                showRightSidebar = showRightSidebar,
                onToggleRightSidebar = { showRightSidebar = !showRightSidebar },
                onBack = onBack,
                isPinned = (uiState as? EditorUiState.Success)?.note?.isPinned ?: false,
                isFavorite = (uiState as? EditorUiState.Success)?.note?.isFavorite ?: false,
                onTogglePin = { viewModel.onEvent(EditorUiEvent.TogglePin) },
                onToggleFavorite = { viewModel.onEvent(EditorUiEvent.ToggleFavorite) }
            )

            // Main content
            when (val state = uiState) {
                is EditorUiState.Idle -> {}
                is EditorUiState.Loading -> LoadingState()
                is EditorUiState.Error -> ErrorState(state.message, state.retryAction)
                is EditorUiState.Success -> {
                    EditorMainContent(
                        note = state.note,
                        isSaving = state.isSaving,
                        hasUnsavedChanges = state.hasUnsavedChanges,
                        lastSavedAt = state.lastSavedAt,
                        viewMode = viewMode,
                        showRightSidebar = showRightSidebar,
                        showAISuggestion = showAISuggestion,
                        onDismissAISuggestion = { showAISuggestion = false },
                        onTitleChange = { viewModel.onEvent(EditorUiEvent.UpdateTitle(it)) },
                        onContentChange = { viewModel.onEvent(EditorUiEvent.UpdateContent(it)) },
                        onSave = { viewModel.onEvent(EditorUiEvent.Save) },
                        onFormatBold = { start, end -> 
                            viewModel.onEvent(EditorUiEvent.FormatBold(start, end)) 
                        },
                        onFormatItalic = { start, end -> 
                            viewModel.onEvent(EditorUiEvent.FormatItalic(start, end)) 
                        },
                        onFormatCode = { start, end -> 
                            viewModel.onEvent(EditorUiEvent.FormatCode(start, end)) 
                        },
                        onFormatLink = { start, end, url -> 
                            viewModel.onEvent(EditorUiEvent.FormatLink(start, end, url)) 
                        },
                        onInsertCodeBlock = { pos ->
                            viewModel.onEvent(EditorUiEvent.InsertCodeBlock(pos))
                        },
                        folderPath = null,
                        // Sidebar data
                        allNotes = allNotes,
                        pinnedNotes = pinnedNotes,
                        sidebarSearchQuery = sidebarSearchQuery,
                        onSidebarSearchQueryChange = { sidebarSearchQuery = it },
                        onNoteClick = onNavigateToNote,
                        onToggleNotePin = { noteToToggle ->
                            viewModel.toggleNotePinById(noteToToggle.id)
                        }
                    )
                }
                is EditorUiState.NewNote -> {
                    EditorMainContent(
                        note = Note(
                            title = state.title,
                            content = state.content,
                            folderId = null
                        ),
                        isSaving = state.isSaving,
                        hasUnsavedChanges = true,
                        lastSavedAt = null,
                        viewMode = viewMode,
                        showRightSidebar = showRightSidebar,
                        showAISuggestion = showAISuggestion,
                        onDismissAISuggestion = { showAISuggestion = false },
                        onTitleChange = { viewModel.onEvent(EditorUiEvent.UpdateTitle(it)) },
                        onContentChange = { viewModel.onEvent(EditorUiEvent.UpdateContent(it)) },
                        onSave = { viewModel.onEvent(EditorUiEvent.Save) },
                        onFormatBold = { start, end -> 
                            viewModel.onEvent(EditorUiEvent.FormatBold(start, end)) 
                        },
                        onFormatItalic = { start, end -> 
                            viewModel.onEvent(EditorUiEvent.FormatItalic(start, end)) 
                        },
                        onFormatCode = { start, end -> 
                            viewModel.onEvent(EditorUiEvent.FormatCode(start, end)) 
                        },
                        onFormatLink = { start, end, url -> 
                            viewModel.onEvent(EditorUiEvent.FormatLink(start, end, url)) 
                        },
                        onInsertCodeBlock = { pos ->
                            viewModel.onEvent(EditorUiEvent.InsertCodeBlock(pos))
                        },
                        folderPath = null,
                        // Sidebar data
                        allNotes = allNotes,
                        pinnedNotes = pinnedNotes,
                        sidebarSearchQuery = sidebarSearchQuery,
                        onSidebarSearchQueryChange = { sidebarSearchQuery = it },
                        onNoteClick = onNavigateToNote,
                        onToggleNotePin = { noteToToggle ->
                            viewModel.toggleNotePinById(noteToToggle.id)
                        }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// EDITOR HEADER
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EditorHeader(
    viewMode: EditorViewMode,
    onViewModeChange: (EditorViewMode) -> Unit,
    showRightSidebar: Boolean,
    onToggleRightSidebar: () -> Unit,
    onBack: () -> Unit,
    isPinned: Boolean,
    isFavorite: Boolean,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        color = KlarityColors.BgSecondary,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Back button and logo
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = KlarityColors.TextSecondary
                    )
                }
                
                // Logo
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(KlarityColors.AccentPrimary, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "K",
                            color = KlarityColors.BgPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Klarity",
                        color = KlarityColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            // Center - View mode toggles
            Row(
                modifier = Modifier
                    .background(KlarityColors.BgTertiary, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ViewModeButton(
                    label = "≡≡≡",
                    tooltip = "Tri-Pane",
                    isSelected = viewMode == EditorViewMode.TRI_PANE,
                    onClick = { onViewModeChange(EditorViewMode.TRI_PANE) }
                )
                ViewModeButton(
                    label = "≡≡",
                    tooltip = "Dual-Pane",
                    isSelected = viewMode == EditorViewMode.DUAL_PANE,
                    onClick = { onViewModeChange(EditorViewMode.DUAL_PANE) }
                )
                ViewModeButton(
                    label = "≡",
                    tooltip = "Single",
                    isSelected = viewMode == EditorViewMode.SINGLE_PANE,
                    onClick = { onViewModeChange(EditorViewMode.SINGLE_PANE) }
                )
                ViewModeButton(
                    label = "○",
                    tooltip = "Zen",
                    isSelected = viewMode == EditorViewMode.ZEN,
                    onClick = { onViewModeChange(EditorViewMode.ZEN) }
                )
            }

            // Right side - Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Pin",
                        tint = if (isPinned) KlarityColors.AccentPrimary else KlarityColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) KlarityColors.Error else KlarityColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(Modifier.width(8.dp))
                
                // Toggle right sidebar
                IconButton(
                    onClick = onToggleRightSidebar,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = "Toggle sidebar",
                        tint = if (showRightSidebar) KlarityColors.AccentPrimary else KlarityColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Share button
                Button(
                    onClick = { /* Share */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KlarityColors.AccentPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Share", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ViewModeButton(
    label: String,
    tooltip: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) KlarityColors.AccentPrimary else Color.Transparent
    val contentColor = if (isSelected) KlarityColors.BgPrimary else KlarityColors.TextSecondary
    
    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            AnimatedVisibility(visible = isSelected) {
                Text(
                    text = tooltip,
                    color = contentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// EDITOR MAIN CONTENT
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EditorMainContent(
    note: Note,
    isSaving: Boolean,
    hasUnsavedChanges: Boolean,
    lastSavedAt: Long?,
    viewMode: EditorViewMode,
    showRightSidebar: Boolean,
    showAISuggestion: Boolean,
    onDismissAISuggestion: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onFormatBold: (Int, Int) -> Unit,
    onFormatItalic: (Int, Int) -> Unit,
    onFormatCode: (Int, Int) -> Unit,
    onFormatLink: (Int, Int, String) -> Unit,
    onInsertCodeBlock: (Int) -> Unit,
    folderPath: List<Folder>?,
    // Sidebar data
    allNotes: List<Note>,
    pinnedNotes: List<Note>,
    sidebarSearchQuery: String,
    onSidebarSearchQueryChange: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onToggleNotePin: (Note) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Main editor area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // Editor pane with toolbar
            EditorPane(
                note = note,
                viewMode = viewMode,
                showAISuggestion = showAISuggestion,
                onDismissAISuggestion = onDismissAISuggestion,
                onTitleChange = onTitleChange,
                onContentChange = onContentChange,
                onSave = onSave,
                onFormatBold = onFormatBold,
                onFormatItalic = onFormatItalic,
                onFormatCode = onFormatCode,
                onFormatLink = onFormatLink,
                onInsertCodeBlock = onInsertCodeBlock,
                folderPath = folderPath,
                modifier = Modifier.weight(1f)
            )

            // Footer
            EditorFooter(
                wordCount = note.wordCount(),
                isSaving = isSaving,
                hasUnsavedChanges = hasUnsavedChanges,
                lastSavedAt = lastSavedAt,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt
            )
        }

        // Right sidebar
        AnimatedVisibility(
            visible = showRightSidebar,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            RightSidebar(
                currentNoteId = note.id,
                allNotes = allNotes,
                pinnedNotes = pinnedNotes,
                searchQuery = sidebarSearchQuery,
                onSearchQueryChange = onSidebarSearchQueryChange,
                onNoteClick = onNoteClick,
                onTogglePin = onToggleNotePin,
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// EDITOR PANE
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EditorPane(
    note: Note,
    viewMode: EditorViewMode,
    showAISuggestion: Boolean,
    onDismissAISuggestion: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onFormatBold: (Int, Int) -> Unit,
    onFormatItalic: (Int, Int) -> Unit,
    onFormatCode: (Int, Int) -> Unit,
    onFormatLink: (Int, Int, String) -> Unit,
    onInsertCodeBlock: (Int) -> Unit,
    folderPath: List<Folder>?,
    modifier: Modifier = Modifier
) {
    // Use note.id as key so TextFieldValue is only recreated when loading a different note
    var titleValue by remember(note.id) { 
        mutableStateOf(TextFieldValue(note.title, TextRange(note.title.length))) 
    }
    var contentValue by remember(note.id) { 
        mutableStateOf(TextFieldValue(note.content, TextRange(note.content.length))) 
    }
    var showLinkDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Only notify ViewModel of changes, don't auto-save here to avoid cursor reset
    LaunchedEffect(titleValue.text) {
        if (titleValue.text != note.title) {
            onTitleChange(titleValue.text)
        }
    }
    
    LaunchedEffect(contentValue.text) {
        if (contentValue.text != note.content) {
            onContentChange(contentValue.text)
        }
    }
    
    // Debounced auto-save
    LaunchedEffect(titleValue.text, contentValue.text) {
        kotlinx.coroutines.delay(2000)
        onSave()
    }

    // Link dialog
    if (showLinkDialog) {
        LinkDialog(
            onDismiss = { showLinkDialog = false },
            onConfirm = { url ->
                onFormatLink(
                    contentValue.selection.start,
                    contentValue.selection.end,
                    url
                )
                showLinkDialog = false
            }
        )
    }

    val isZenMode = viewMode == EditorViewMode.ZEN
    val horizontalPadding by animateDpAsState(
        targetValue = if (isZenMode) 120.dp else 24.dp,
        animationSpec = tween(200)
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Toolbar (hidden in Zen mode)
        AnimatedVisibility(visible = !isZenMode) {
            EditorToolbar(
                onBold = { 
                    onFormatBold(contentValue.selection.start, contentValue.selection.end) 
                },
                onItalic = { 
                    onFormatItalic(contentValue.selection.start, contentValue.selection.end) 
                },
                onCode = { 
                    onFormatCode(contentValue.selection.start, contentValue.selection.end) 
                },
                onLink = { showLinkDialog = true },
                onCodeBlock = { 
                    onInsertCodeBlock(contentValue.selection.start) 
                }
            )
        }

        // Breadcrumbs (hidden in Zen mode)
        AnimatedVisibility(visible = !isZenMode) {
            Breadcrumbs(
                folderPath = folderPath,
                noteTitle = note.title.ifBlank { "Untitled" },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        // Editor content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Suggestion Placeholder
            AnimatedVisibility(visible = showAISuggestion && !isZenMode) {
                AISuggestionCard(
                    onDismiss = onDismissAISuggestion
                )
            }

            // Title
            val customSelectionColors = TextSelectionColors(
                handleColor = KlarityColors.AccentPrimary,
                backgroundColor = KlarityColors.EditorSelection
            )
            
            CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
                BasicTextField(
                    value = titleValue,
                    onValueChange = { titleValue = it },
                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                        color = KlarityColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isZenMode) 36.sp else 32.sp
                    ),
                    cursorBrush = SolidColor(KlarityColors.EditorCursor),
                    decorationBox = { innerTextField ->
                        Box {
                            if (titleValue.text.isEmpty()) {
                                Text(
                                    "Untitled",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        color = KlarityColors.TextTertiary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (isZenMode) 36.sp else 32.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // Content
                BasicTextField(
                    value = contentValue,
                    onValueChange = { contentValue = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = KlarityColors.TextPrimary,
                        lineHeight = 28.sp,
                        fontSize = if (isZenMode) 18.sp else 16.sp
                    ),
                    cursorBrush = SolidColor(KlarityColors.EditorCursor),
                    decorationBox = { innerTextField ->
                        Box {
                            if (contentValue.text.isEmpty()) {
                                Text(
                                    "Start writing your thoughts...",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = KlarityColors.TextTertiary,
                                        lineHeight = 28.sp,
                                        fontSize = if (isZenMode) 18.sp else 16.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 300.dp)
                        .focusRequester(focusRequester)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// TOOLBAR
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EditorToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onCode: () -> Unit,
    onLink: () -> Unit,
    onCodeBlock: () -> Unit
) {
    Surface(
        color = KlarityColors.BgSecondary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextToolbarButton(
                text = "B",
                label = "Bold (Ctrl+B)",
                isBold = true,
                onClick = onBold
            )
            TextToolbarButton(
                text = "I",
                label = "Italic (Ctrl+I)",
                isItalic = true,
                onClick = onItalic
            )
            TextToolbarButton(
                text = "</>",
                label = "Code (Ctrl+E)",
                onClick = onCode
            )
            TextToolbarButton(
                text = "🔗",
                label = "Link (Ctrl+K)",
                onClick = onLink
            )
            
            VerticalDivider(
                modifier = Modifier
                    .height(20.dp)
                    .padding(horizontal = 8.dp),
                color = KlarityColors.BorderPrimary
            )
            
            TextToolbarButton(
                text = "{ }",
                label = "Code Block",
                onClick = onCodeBlock
            )
            
            Spacer(Modifier.weight(1f))
            
            // AI Sparkles button
            Surface(
                onClick = { /* AI Actions */ },
                color = KlarityColors.AccentAI.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "✨",
                        fontSize = 14.sp
                    )
                    Text(
                        "AI",
                        color = KlarityColors.AccentAI,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TextToolbarButton(
    text: String,
    label: String,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Surface(
        onClick = onClick,
        color = if (isHovered) KlarityColors.BgTertiary else Color.Transparent,
        shape = RoundedCornerShape(6.dp),
        interactionSource = interactionSource,
        modifier = Modifier.hoverable(interactionSource)
    ) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isHovered) KlarityColors.TextPrimary else KlarityColors.TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (isItalic) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
            )
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Surface(
        onClick = onClick,
        color = if (isHovered) KlarityColors.BgTertiary else Color.Transparent,
        shape = RoundedCornerShape(6.dp),
        interactionSource = interactionSource,
        modifier = Modifier.hoverable(interactionSource)
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (isHovered) KlarityColors.TextPrimary else KlarityColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// BREADCRUMBS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun Breadcrumbs(
    folderPath: List<Folder>?,
    noteTitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Root
        BreadcrumbItem(text = "Notes", isClickable = true)
        
        // Folder path
        folderPath?.forEach { folder ->
            BreadcrumbSeparator()
            BreadcrumbItem(
                text = folder.icon?.let { "$it ${folder.name}" } ?: folder.name,
                isClickable = true
            )
        }
        
        // Current note
        BreadcrumbSeparator()
        BreadcrumbItem(
            text = noteTitle,
            isClickable = false,
            isCurrent = true
        )
    }
}

@Composable
private fun BreadcrumbItem(
    text: String,
    isClickable: Boolean,
    isCurrent: Boolean = false
) {
    Text(
        text = text,
        color = if (isCurrent) KlarityColors.TextPrimary else KlarityColors.TextTertiary,
        fontSize = 13.sp,
        fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = if (isClickable) Modifier.clickable { /* Navigate */ } else Modifier
    )
}

@Composable
private fun BreadcrumbSeparator() {
    Text(
        text = "/",
        color = KlarityColors.TextTertiary.copy(alpha = 0.5f),
        fontSize = 13.sp
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// AI SUGGESTION CARD
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AISuggestionCard(
    onDismiss: () -> Unit
) {
    Surface(
        color = KlarityColors.AccentAIGlow,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, KlarityColors.AccentAI.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(KlarityColors.AccentAI.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "✨",
                        fontSize = 18.sp
                    )
                }
                Column {
                    Text(
                        "AI Suggestion Available",
                        color = KlarityColors.TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        "Type /ai or press Ctrl+J to ask AI for help with your writing",
                        color = KlarityColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Dismiss",
                    tint = KlarityColors.TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// EDITOR FOOTER
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EditorFooter(
    wordCount: Int,
    isSaving: Boolean,
    hasUnsavedChanges: Boolean,
    lastSavedAt: Long?,
    createdAt: kotlinx.datetime.Instant,
    updatedAt: kotlinx.datetime.Instant
) {
    Surface(
        color = KlarityColors.BgSecondary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left - Dates, Word count and reading time
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Created: ${formatDate(createdAt)}",
                    color = KlarityColors.TextTertiary,
                    fontSize = 12.sp
                )
                Text(
                    text = "•",
                    color = KlarityColors.TextTertiary.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Text(
                    text = "Updated: ${formatDate(updatedAt)}",
                    color = KlarityColors.TextTertiary,
                    fontSize = 12.sp
                )
                Text(
                    text = "•",
                    color = KlarityColors.TextTertiary.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Text(
                    text = "$wordCount words",
                    color = KlarityColors.TextTertiary,
                    fontSize = 12.sp
                )
                Text(
                    text = "${(wordCount / 200).coerceAtLeast(1)} min read",
                    color = KlarityColors.TextTertiary,
                    fontSize = 12.sp
                )
            }

            // Center - Graph mapping status (placeholder)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(KlarityColors.AccentPrimary, CircleShape)
                )
                Text(
                    text = "Graph mapped",
                    color = KlarityColors.TextTertiary,
                    fontSize = 12.sp
                )
            }

            // Right - Save status
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isSaving -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = KlarityColors.AccentPrimary
                        )
                        Text(
                            text = "Saving...",
                            color = KlarityColors.TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                    hasUnsavedChanges -> {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(KlarityColors.Warning, CircleShape)
                        )
                        Text(
                            text = "Unsaved changes",
                            color = KlarityColors.Warning,
                            fontSize = 12.sp
                        )
                    }
                    else -> {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = KlarityColors.Success,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "All changes saved",
                            color = KlarityColors.TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// RIGHT SIDEBAR
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RightSidebar(
    currentNoteId: String,
    allNotes: List<Note>,
    pinnedNotes: List<Note>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onTogglePin: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter notes based on search
    val filteredNotes = remember(allNotes, searchQuery) {
        if (searchQuery.isBlank()) allNotes
        else allNotes.filter { 
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.content.contains(searchQuery, ignoreCase = true)
        }
    }
    
    // All notes sorted by updatedAt (excluding current and pinned)
    val recentNotes = remember(filteredNotes, currentNoteId) {
        filteredNotes
            .filter { it.id != currentNoteId && !it.isPinned }
            .sortedByDescending { it.updatedAt }
    }
    
    // Filtered pinned notes (excluding current)
    val filteredPinnedNotes = remember(pinnedNotes, currentNoteId, searchQuery) {
        pinnedNotes
            .filter { it.id != currentNoteId }
            .filter { 
                searchQuery.isBlank() ||
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.content.contains(searchQuery, ignoreCase = true)
            }
    }

    Surface(
        color = KlarityColors.BgSecondary,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Search Bar
            Surface(
                color = KlarityColors.BgTertiary,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = KlarityColors.TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = KlarityColors.TextPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(KlarityColors.AccentPrimary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search notes...",
                                    color = KlarityColors.TextTertiary,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = KlarityColors.TextTertiary,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onSearchQueryChange("") }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Pinned section
            if (filteredPinnedNotes.isNotEmpty()) {
                SidebarSection(title = "📌 Pinned") {
                    filteredPinnedNotes.forEach { note ->
                        SidebarNoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onTogglePin = { onTogglePin(note) }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Recent section
            SidebarSection(title = "🕐 Recent") {
                if (recentNotes.isEmpty()) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "No matching notes" else "No recent notes",
                        color = KlarityColors.TextTertiary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    recentNotes.forEach { note ->
                        SidebarNoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onTogglePin = { onTogglePin(note) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // All notes count
            Text(
                text = "${allNotes.size} notes total",
                color = KlarityColors.TextTertiary,
                fontSize = 12.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun SidebarSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            color = KlarityColors.TextTertiary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SidebarNoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Surface(
        onClick = onClick,
        color = if (isHovered) KlarityColors.BgTertiary else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title.ifBlank { "Untitled Note" },
                    color = KlarityColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.content.take(100).replace("\n", " "),
                    color = KlarityColors.TextTertiary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Pin button (visible on hover)
            AnimatedVisibility(visible = isHovered) {
                Surface(
                    onClick = { onTogglePin() },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (note.isPinned) "📌" else "📍",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// LINK DIALOG
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun LinkDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Insert Link", color = KlarityColors.TextPrimary)
        },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                placeholder = { Text("https://...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KlarityColors.AccentPrimary,
                    cursorColor = KlarityColors.AccentPrimary
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(url) },
                enabled = url.isNotBlank()
            ) {
                Text("Insert", color = KlarityColors.AccentPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = KlarityColors.TextSecondary)
            }
        },
        containerColor = KlarityColors.BgSecondary
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// LOADING & ERROR STATES
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = KlarityColors.AccentPrimary)
    }
}

@Composable
private fun ErrorState(
    message: String,
    retryAction: (() -> Unit)?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "⚠️",
                fontSize = 48.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                color = KlarityColors.Error,
                fontSize = 16.sp
            )
            retryAction?.let { retry ->
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = retry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KlarityColors.AccentPrimary
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// DATE FORMATTING
// ══════════════════════════════════════════════════════════════════════════════

private fun formatDate(instant: kotlinx.datetime.Instant): String {
    val now = kotlinx.datetime.Clock.System.now()
    val duration = now - instant
    
    return when {
        duration.inWholeMinutes < 1 -> "Just now"
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m ago"
        duration.inWholeHours < 24 -> "${duration.inWholeHours}h ago"
        duration.inWholeDays < 7 -> "${duration.inWholeDays}d ago"
        duration.inWholeDays < 30 -> "${duration.inWholeDays / 7}w ago"
        duration.inWholeDays < 365 -> "${duration.inWholeDays / 30}mo ago"
        else -> "${duration.inWholeDays / 365}y ago"
    }
}


