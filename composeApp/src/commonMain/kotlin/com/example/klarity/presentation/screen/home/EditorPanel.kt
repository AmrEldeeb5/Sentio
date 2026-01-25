package com.example.klarity.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Folder
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.NoteStatus
import com.example.klarity.presentation.theme.KlarityColors
import com.example.klarity.presentation.theme.KlarityTheme
import com.example.klarity.presentation.theme.KlarityMotion
import com.example.klarity.presentation.components.organicPulsingGlow
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Inline AI Suggestion Data Structure
 * Represents a GitHub Copilot-style suggestion
 */
data class InlineAISuggestion(
    val text: String,           // The suggested completion text
    val cursorPosition: Int,    // Where to insert the suggestion
    val confidence: Float       // 0.0 to 1.0
)

/**
 * Editor Panel - Main content area for editing notes
 * Supports keyboard shortcuts:
 * - Ctrl+/ : Open slash command menu
 * - Ctrl+B : Bold
 * - Ctrl+I : Italic
 * - Ctrl+E : Code
 * - Ctrl+K : Link
 * - Tab : Accept AI suggestion
 * - Escape : Dismiss AI suggestion
 */
@Composable
fun EditorPanel(
    selectedNote: Note?,
    folders: List<Folder>,
    showSlashMenu: Boolean,
    onToggleSlashMenu: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onCreateNote: () -> Unit = {},
    onStatusChange: (NoteStatus) -> Unit = {},
    onWikiLinkClick: (noteName: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Track formatting state for toolbar callbacks
    var formatBoldTrigger by remember { mutableStateOf(0) }
    var formatItalicTrigger by remember { mutableStateOf(0) }
    var formatCodeTrigger by remember { mutableStateOf(0) }
    var formatLinkTrigger by remember { mutableStateOf(0) }
    var isPreviewMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                    when (keyEvent.key) {
                        Key.Slash -> {
                            onToggleSlashMenu()
                            true
                        }
                        Key.B -> {
                            formatBoldTrigger++
                            true
                        }
                        Key.I -> {
                            formatItalicTrigger++
                            true
                        }
                        Key.E -> {
                            formatCodeTrigger++
                            true
                        }
                        Key.K -> {
                            formatLinkTrigger++
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .focusable()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Toolbar with pin/delete and formatting
            EditorToolbar(
                isPinned = selectedNote?.isPinned ?: false,
                noteStatus = selectedNote?.status ?: NoteStatus.NONE,
                isPreviewMode = isPreviewMode,
                onTogglePreview = { isPreviewMode = !isPreviewMode },
                onTogglePin = onTogglePin,
                onDelete = onDelete,
                onStatusChange = onStatusChange,
                hasNote = selectedNote != null,
                onBold = { formatBoldTrigger++ },
                onItalic = { formatItalicTrigger++ },
                onCode = { formatCodeTrigger++ },
                onLink = { formatLinkTrigger++ },
                onSlashMenu = onToggleSlashMenu
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (selectedNote != null) {
                    val folder = folders.find { it.id == selectedNote.folderId }

                    // Breadcrumbs
                    Breadcrumbs(
                        projectName = "Klarity",
                        folderName = folder?.name ?: "Uncategorized",
                        noteName = selectedNote.title.ifBlank { "Untitled" }
                    )

                    // Editor Content (editable or preview)
                    if (isPreviewMode) {
                        MarkdownPreviewContent(
                            note = selectedNote,
                            onWikiLinkClick = onWikiLinkClick
                        )
                    } else {
                        EditableEditorContent(
                            note = selectedNote,
                            onTitleChange = onTitleChange,
                            onContentChange = onContentChange,
                            onToggleSlashMenu = onToggleSlashMenu,
                            formatBoldTrigger = formatBoldTrigger,
                            formatItalicTrigger = formatItalicTrigger,
                            formatCodeTrigger = formatCodeTrigger,
                            formatLinkTrigger = formatLinkTrigger
                        )
                    }
                } else {
                    // Instant Editor - Click anywhere to start writing
                    InstantEditorState(
                        onCreateNote = onCreateNote
                    )
                }
            }

            // Footer
            EditorFooter(wordCount = selectedNote?.wordCount() ?: 0)
        }

        // Slash Menu (floating)
        AnimatedVisibility(
            visible = showSlashMenu,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        ) {
            SlashMenu(onDismiss = onToggleSlashMenu)
        }
    }
}

@Composable
fun EditorToolbar(
    isPinned: Boolean = false,
    noteStatus: NoteStatus = NoteStatus.NONE,
    isPreviewMode: Boolean = false,
    onTogglePreview: () -> Unit = {},
    onTogglePin: () -> Unit = {},
    onDelete: () -> Unit = {},
    onStatusChange: (NoteStatus) -> Unit = {},
    hasNote: Boolean = false,
    onBold: () -> Unit = {},
    onItalic: () -> Unit = {},
    onCode: () -> Unit = {},
    onLink: () -> Unit = {},
    onSlashMenu: () -> Unit = {}
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    
    // Material 3 Toolbar with IconButtons
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
            // Left: Formatting buttons (Material 3 IconButtons)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBold) {
                    Text(
                        "B",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onItalic) {
                    Text(
                        "I",
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCode) {
                    Text(
                        "</>",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onLink) {
                    Text(
                        "🔗",
                        fontSize = 14.sp
                    )
                }

                // Vertical divider
                androidx.compose.material3.VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                FilledTonalIconButton(
                    onClick = onSlashMenu,
                    colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text("✨", fontSize = 14.sp)
                }
                
                // Vertical divider
                androidx.compose.material3.VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Preview toggle
                IconToggleButton(
                    checked = isPreviewMode,
                    onCheckedChange = { onTogglePreview() }
                ) {
                    Text(
                        if (isPreviewMode) "✏️" else "👁",
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = {}) {
                    Text("⋯", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Right: Status, Pin, Delete, Settings
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (hasNote) {
                    // Status selector
                    Box {
                        NoteStatusSelector(
                            currentStatus = noteStatus,
                            onClick = { showStatusMenu = true }
                        )
                        androidx.compose.material3.DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            NoteStatus.entries.forEach { status ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(getStatusIcon(status), fontSize = 14.sp)
                                            Text(getStatusLabel(status), fontSize = 13.sp)
                                        }
                                    },
                                    onClick = {
                                        onStatusChange(status)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }

                    IconToggleButton(
                        checked = isPinned,
                        onCheckedChange = { onTogglePin() }
                    ) {
                        Text(
                            if (isPinned) "📌" else "📍",
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Text("🗑", fontSize = 14.sp)
                    }
                }
                IconButton(onClick = {}) {
                    Text("⚙", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
    }
}

// Status helper functions
fun getStatusIcon(status: NoteStatus): String = when (status) {
    NoteStatus.NONE -> "○"
    NoteStatus.IN_PROGRESS -> "⏳"
    NoteStatus.COMPLETED -> "✓"
    NoteStatus.ON_HOLD -> "⏸"
    NoteStatus.ARCHIVED -> "📦"
}

fun getStatusLabel(status: NoteStatus): String = when (status) {
    NoteStatus.NONE -> "No Status"
    NoteStatus.IN_PROGRESS -> "In Progress"
    NoteStatus.COMPLETED -> "Completed"
    NoteStatus.ON_HOLD -> "On Hold"
    NoteStatus.ARCHIVED -> "Archived"
}

@Composable
fun getStatusColor(status: NoteStatus): Color = when (status) {
    NoteStatus.NONE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    NoteStatus.IN_PROGRESS -> KlarityColors.StatusInProgress
    NoteStatus.COMPLETED -> KlarityColors.StatusCompleted
    NoteStatus.ON_HOLD -> KlarityColors.StatusOnHold
    NoteStatus.ARCHIVED -> KlarityColors.StatusArchived
}

@Composable
fun NoteStatusSelector(
    currentStatus: NoteStatus,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val statusColor = getStatusColor(currentStatus)

    Surface(
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(6.dp),
        color = if (isHovered) statusColor.copy(alpha = 0.15f) else statusColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(getStatusIcon(currentStatus), fontSize = 12.sp, color = statusColor)
            Text(
                getStatusLabel(currentStatus),
                fontSize = 11.sp,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
            Text("▼", fontSize = 8.sp, color = statusColor.copy(alpha = 0.6f))
        }
    }
}

/**
 * Breadcrumbs component - now using shared EditorBreadcrumbs from components package.
 * @deprecated Use [com.example.klarity.presentation.components.EditorBreadcrumbs] directly
 */
@Composable
fun Breadcrumbs(
    projectName: String,
    folderName: String,
    noteName: String
) {
    // Delegate to shared component
    com.example.klarity.presentation.components.EditorBreadcrumbs(
        projectName = projectName,
        folderName = folderName,
        itemName = noteName,
        onProjectClick = null,
        onFolderClick = null
    )
}

@Composable
fun EditableEditorContent(
    note: Note,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onToggleSlashMenu: () -> Unit = {},
    formatBoldTrigger: Int = 0,
    formatItalicTrigger: Int = 0,
    formatCodeTrigger: Int = 0,
    formatLinkTrigger: Int = 0
) {
    // Use TextFieldValue to track selection
    var titleValue by remember(note.id) { 
        mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(note.title)) 
    }
    var contentValue by remember(note.id) { 
        mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(note.content)) 
    }

    // AI Suggestion state
    var currentSuggestion by remember { mutableStateOf<InlineAISuggestion?>(null) }
    var isLoadingSuggestion by remember { mutableStateOf(false) }
    var suggestionJob by remember { mutableStateOf<Job?>(null) }

    // Debounced save - only save after user stops typing
    LaunchedEffect(titleValue.text) {
        if (titleValue.text != note.title) {
            kotlinx.coroutines.delay(500)
            onTitleChange(titleValue.text)
        }
    }

    LaunchedEffect(contentValue.text) {
        if (contentValue.text != note.content) {
            kotlinx.coroutines.delay(500)
            onContentChange(contentValue.text)
        }
    }

    // Debounced AI suggestion request
    LaunchedEffect(contentValue.text, contentValue.selection) {
        // Cancel previous request
        suggestionJob?.cancel()
        currentSuggestion = null
        isLoadingSuggestion = false
        
        // Only request if there's content and cursor is at end of some text
        if (contentValue.text.isNotEmpty() && contentValue.selection.collapsed) {
            suggestionJob = this.launch {
                delay(500) // Debounce
                isLoadingSuggestion = true
                
                // Simulate AI suggestion (replace with actual API call)
                delay(300) // Simulate network request
                
                // Mock suggestion logic - only suggest after punctuation or line breaks
                val cursorPos = contentValue.selection.start
                if (cursorPos > 0 && cursorPos == contentValue.text.length) {
                    val lastChar = contentValue.text.getOrNull(cursorPos - 1)
                    if (lastChar in listOf('.', '!', '?', '\n', ':', ',')) {
                        currentSuggestion = InlineAISuggestion(
                            text = " This is an AI suggestion to continue your thought.",
                            cursorPosition = cursorPos,
                            confidence = 0.87f
                        )
                    }
                }
                
                isLoadingSuggestion = false
            }
        }
    }

    // Accept suggestion handler
    fun acceptSuggestion(suggestion: InlineAISuggestion) {
        val newText = contentValue.text.substring(0, suggestion.cursorPosition) +
                suggestion.text +
                contentValue.text.substring(suggestion.cursorPosition)
        contentValue = contentValue.copy(
            text = newText,
            selection = androidx.compose.ui.text.TextRange(
                suggestion.cursorPosition + suggestion.text.length
            )
        )
        currentSuggestion = null
    }

    // Dismiss suggestion handler
    fun dismissSuggestion() {
        currentSuggestion = null
    }

    // Format text helper
    fun formatSelection(prefix: String, suffix: String) {
        val selection = contentValue.selection
        if (selection.start != selection.end) {
            val selectedText = contentValue.text.substring(selection.start, selection.end)
            val newText = contentValue.text.replaceRange(selection.start, selection.end, "$prefix$selectedText$suffix")
            contentValue = contentValue.copy(
                text = newText,
                selection = androidx.compose.ui.text.TextRange(
                    selection.start + prefix.length,
                    selection.end + prefix.length
                )
            )
        } else {
            // No selection - insert at cursor
            val newText = contentValue.text.substring(0, selection.start) + 
                "$prefix$suffix" + 
                contentValue.text.substring(selection.start)
            contentValue = contentValue.copy(
                text = newText,
                selection = androidx.compose.ui.text.TextRange(selection.start + prefix.length)
            )
        }
    }

    // React to formatting triggers from toolbar/keyboard
    LaunchedEffect(formatBoldTrigger) {
        if (formatBoldTrigger > 0) formatSelection("**", "**")
    }
    LaunchedEffect(formatItalicTrigger) {
        if (formatItalicTrigger > 0) formatSelection("_", "_")
    }
    LaunchedEffect(formatCodeTrigger) {
        if (formatCodeTrigger > 0) formatSelection("`", "`")
    }
    LaunchedEffect(formatLinkTrigger) {
        if (formatLinkTrigger > 0) formatSelection("[", "](url)")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .padding(bottom = 128.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Editable Title
        BasicTextField(
            value = titleValue,
            onValueChange = { titleValue = it },
            textStyle = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 40.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.tertiary),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box {
                    if (titleValue.text.isEmpty()) {
                        Text(
                            "Untitled",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            lineHeight = 40.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Editable Content with keyboard shortcuts and AI suggestions
        Box(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = contentValue,
                onValueChange = { newValue ->
                    contentValue = newValue
                    // Dismiss suggestion on any edit
                    if (currentSuggestion != null) {
                        dismissSuggestion()
                    }
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 28.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.tertiary),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 300.dp)
                    .onKeyEvent { keyEvent ->
                        when {
                            // Tab: Accept suggestion
                            keyEvent.key == Key.Tab && keyEvent.type == KeyEventType.KeyDown && currentSuggestion != null -> {
                                acceptSuggestion(currentSuggestion!!)
                                true
                            }
                            // Escape: Dismiss suggestion
                            keyEvent.key == Key.Escape && keyEvent.type == KeyEventType.KeyDown && currentSuggestion != null -> {
                                dismissSuggestion()
                                true
                            }
                            else -> false
                        }
                    }
                    .semantics {
                        if (currentSuggestion != null) {
                            contentDescription = "AI suggestion available. Press Tab to accept, Escape to dismiss"
                        }
                    },
                decorationBox = { innerTextField ->
                    Box {
                        if (contentValue.text.isEmpty()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    "Start typing your note, or press ",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 16.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        "Ctrl+/",
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(" for commands...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 16.sp)
                            }
                        }
                        
                        // Main text field
                        innerTextField()
                        
                        // Ghost text suggestion overlay
                        currentSuggestion?.let { suggestion ->
                            GhostTextSuggestion(
                                suggestion = suggestion,
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    lineHeight = 28.sp
                                ),
                                contentText = contentValue.text,
                                cursorPosition = contentValue.selection.start
                            )
                        }
                        
                        // Loading indicator
                        if (isLoadingSuggestion) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "⋯",
                                    fontSize = 14.sp,
                                    color = KlarityTheme.extendedColors.accentAI.copy(alpha = 0.6f),
                                    modifier = Modifier.organicPulsingGlow(
                                        color = KlarityTheme.extendedColors.accentAI,
                                        intensity = 0.3f,
                                        pulseSpeed = 1000
                                    )
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * Ghost Text Suggestion Component
 * Renders AI suggestions as inline ghost text with fade-in animation
 */
@Composable
private fun GhostTextSuggestion(
    suggestion: InlineAISuggestion,
    textStyle: TextStyle,
    contentText: String,
    cursorPosition: Int
) {
    // Fade-in animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(suggestion) {
        visible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = KlarityMotion.springGentle(),
        label = "ghostTextFadeIn"
    )
    
    // Measure text to position ghost text correctly
    androidx.compose.foundation.layout.BoxWithConstraints {
        val ghostTextModifier = if (suggestion.confidence > 0.85f) {
            Modifier.organicPulsingGlow(
                color = KlarityTheme.extendedColors.accentAI,
                intensity = 0.15f,
                pulseSpeed = 2000
            )
        } else {
            Modifier
        }
        
        // Render ghost text inline at cursor position
        Text(
            text = buildString {
                // Add spaces to position at cursor
                append(contentText.substring(0, cursorPosition))
                append(suggestion.text)
            },
            style = textStyle.copy(
                color = textStyle.color.copy(alpha = alpha * 0.4f)
            ),
            modifier = ghostTextModifier
        )
    }
}

@Composable
fun MarkdownPreviewContent(
    note: Note,
    onWikiLinkClick: (noteName: String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .padding(bottom = 128.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            note.title.ifBlank { "Untitled" },
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 40.sp
        )

        // Rendered markdown content with wiki link support
        com.example.klarity.presentation.components.MarkdownRenderer(
            content = note.content,
            onWikiLinkClick = onWikiLinkClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EditorFooter(wordCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Word Count: $wordCount",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Graph Mapping: On", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(KlarityColors.Success, androidx.compose.foundation.shape.CircleShape)
                    )
                    Text("Saved", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        }
    }
}

/**
 * Instant Editor State - Click anywhere to start writing
 * Auto-creates a note on first interaction
 */
@Composable
private fun InstantEditorState(
    onCreateNote: () -> Unit
) {
    val luminousTeal = KlarityColors.LuminousTeal
    var isHovered by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onCreateNote() }
            .hoverable(interactionSource)
            .semantics { contentDescription = "Click to start writing" },
        contentAlignment = Alignment.Center
    ) {
        // Track hover state
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is androidx.compose.foundation.interaction.HoverInteraction.Enter -> isHovered = true
                    is androidx.compose.foundation.interaction.HoverInteraction.Exit -> isHovered = false
                }
            }
        }
        
        // Subtle background glow on hover
        if (isHovered) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                luminousTeal.copy(alpha = 0.03f),
                                Color.Transparent
                            ),
                            radius = 800f
                        )
                    )
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(48.dp)
        ) {
            // Large, inviting icon
            Text(
                text = "📝",
                fontSize = 96.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (isHovered) 0.4f else 0.2f
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Primary message - Direct call to action
            Text(
                text = "Start typing your thought…",
                color = if (isHovered) 
                    luminousTeal.copy(alpha = 0.9f) 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            // Secondary message
            Text(
                text = "Click anywhere to begin",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (isHovered) 0.8f else 0.5f
                ),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Keyboard Shortcuts - Minimal and subtle
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShortcutHint("Ctrl+N", "New note")
                ShortcutHint("Ctrl+K", "Command palette")
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: String,
    label: String,
    shortcut: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    accentColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Surface(
        onClick = onClick,
        color = when {
            isPrimary && isHovered -> accentColor.copy(alpha = 0.2f)
            isPrimary -> accentColor.copy(alpha = 0.12f)
            isHovered -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(220.dp)
            .hoverable(interactionSource)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 18.sp)
            
            Text(
                text = label,
                color = if (isPrimary) accentColor else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = shortcut,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ShortcutHint(
    shortcut: String,
    action: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = shortcut,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
        Text(
            text = action,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 10.sp
        )
    }
}
