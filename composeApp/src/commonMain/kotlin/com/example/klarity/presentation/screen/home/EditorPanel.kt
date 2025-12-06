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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Folder
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.NoteStatus
import com.example.klarity.presentation.theme.KlarityColors

/**
 * Editor Panel - Main content area for editing notes
 * Supports keyboard shortcuts:
 * - Ctrl+/ : Open slash command menu
 * - Ctrl+B : Bold
 * - Ctrl+I : Italic
 * - Ctrl+E : Code
 * - Ctrl+K : Link
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
            .background(KlarityColors.BgPrimary)
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
                    // Enhanced Empty State with Quick Actions
                    EmptyEditorState(
                        onCreateNote = { /* Trigger create note */ },
                        onOpenSearch = { onToggleSlashMenu() }
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(8.dp),
        color = KlarityColors.BgPrimary.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, KlarityColors.BorderPrimary)
    ) {
        Row(
            modifier = Modifier.padding(6.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Formatting buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ToolbarButton("B", isBold = true, onClick = onBold, tooltip = "Bold (Ctrl+B)")
                ToolbarButton("I", isItalic = true, onClick = onItalic, tooltip = "Italic (Ctrl+I)")
                ToolbarButton("</>", onClick = onCode, tooltip = "Code (Ctrl+E)")
                ToolbarButton("🔗", onClick = onLink, tooltip = "Link (Ctrl+K)")

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(KlarityColors.BorderPrimary)
                )

                ToolbarButton("✨", isActive = true, isPrimary = true, onClick = onSlashMenu, tooltip = "Commands (Ctrl+/)")
                
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(KlarityColors.BorderPrimary)
                )
                
                // Preview toggle
                ToolbarButton(
                    text = if (isPreviewMode) "✏️" else "👁",
                    isActive = isPreviewMode,
                    onClick = onTogglePreview,
                    tooltip = if (isPreviewMode) "Edit Mode" else "Preview Mode"
                )
                ToolbarButton("⋯")
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

                    ToolbarButton(
                        text = if (isPinned) "📌" else "📍",
                        isActive = isPinned,
                        onClick = onTogglePin,
                        tooltip = "Pin note"
                    )
                    ToolbarButton(
                        text = "🗑",
                        onClick = onDelete,
                        tooltip = "Delete note"
                    )
                }
                ToolbarButton("⚙", tooltip = "Settings")
            }
        }
    }
}

@Composable
fun ToolbarButton(
    text: String,
    isBold: Boolean = false,
    isItalic: Boolean = false,
    isActive: Boolean = false,
    isPrimary: Boolean = false,
    onClick: () -> Unit = {},
    tooltip: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Fixed size box - tooltip rendered in popup layer
    Surface(
        modifier = Modifier
            .size(32.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(6.dp),
        color = if (isActive || isHovered) KlarityColors.BgElevated.copy(alpha = 0.5f) else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                fontSize = 14.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isPrimary -> KlarityColors.AccentAI
                    isActive -> Color.White
                    else -> KlarityColors.TextTertiary
                }
            )
        }
    }
    
    // Tooltip in popup layer - doesn't affect layout
    if (isHovered && tooltip.isNotEmpty()) {
        androidx.compose.ui.window.Popup(
            alignment = Alignment.TopCenter,
            offset = androidx.compose.ui.unit.IntOffset(0, 36)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF1A1A1A),
                shadowElevation = 8.dp
            ) {
                Text(
                    tooltip,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
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

fun getStatusColor(status: NoteStatus): Color = when (status) {
    NoteStatus.NONE -> KlarityColors.TextTertiary
    NoteStatus.IN_PROGRESS -> Color(0xFF38BDF8)
    NoteStatus.COMPLETED -> Color(0xFF34D399)
    NoteStatus.ON_HOLD -> Color(0xFFFBBF24)
    NoteStatus.ARCHIVED -> Color(0xFF9CA3AF)
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

@Composable
fun Breadcrumbs(
    projectName: String,
    folderName: String,
    noteName: String
) {
    Row(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BreadcrumbItem(projectName, isClickable = true)
        Text("/", color = KlarityColors.BorderPrimary, fontSize = 14.sp)
        BreadcrumbItem(folderName, isClickable = true)
        Text("/", color = KlarityColors.BorderPrimary, fontSize = 14.sp)
        BreadcrumbItem(noteName, isActive = true)
    }
}

@Composable
fun BreadcrumbItem(
    text: String,
    isClickable: Boolean = false,
    isActive: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Text(
        text,
        fontSize = 14.sp,
        fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
        color = when {
            isActive -> Color.White
            isHovered -> KlarityColors.AccentAI
            else -> KlarityColors.TextTertiary
        },
        modifier = if (isClickable) Modifier
            .clickable(interactionSource = interactionSource, indication = null) { }
            .hoverable(interactionSource)
        else Modifier
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
                color = Color.White,
                lineHeight = 40.sp
            ),
            cursorBrush = SolidColor(KlarityColors.AccentAI),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box {
                    if (titleValue.text.isEmpty()) {
                        Text(
                            "Untitled",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = KlarityColors.TextTertiary.copy(alpha = 0.5f),
                            lineHeight = 40.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        // Editable Content with keyboard shortcuts
        BasicTextField(
            value = contentValue,
            onValueChange = { newValue ->
                contentValue = newValue
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = KlarityColors.TextSecondary,
                lineHeight = 28.sp
            ),
            cursorBrush = SolidColor(KlarityColors.AccentAI),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 300.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (contentValue.text.isEmpty()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                "Start typing your note, or press ",
                                color = KlarityColors.TextTertiary,
                                fontSize = 16.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = KlarityColors.AccentAI.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "Ctrl+/",
                                    color = KlarityColors.AccentAI,
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(" for commands...", color = KlarityColors.TextTertiary, fontSize = 16.sp)
                        }
                    }
                    innerTextField()
                }
            }
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
        color = KlarityColors.BgPrimary,
        border = BorderStroke(1.dp, KlarityColors.BorderPrimary)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Word Count: $wordCount",
                fontSize = 12.sp,
                color = KlarityColors.TextTertiary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Graph Mapping: On", fontSize = 12.sp, color = KlarityColors.TextTertiary)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), androidx.compose.foundation.shape.CircleShape)
                    )
                    Text("Saved", fontSize = 12.sp, color = KlarityColors.TextTertiary)
                }
            }
        }
    }
}

/**
 * Enhanced Empty State with Quick Actions and Keyboard Shortcuts
 */
@Composable
private fun EmptyEditorState(
    onCreateNote: () -> Unit,
    onOpenSearch: () -> Unit
) {
    val luminousTeal = Color(0xFF1FDBC8)
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Icon
            Text(
                text = "📝",
                fontSize = 72.sp,
                color = KlarityColors.TextTertiary.copy(alpha = 0.2f)
            )
            
            // Main message
            Text(
                text = "Ready to capture your thoughts",
                color = KlarityColors.TextSecondary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Select a note from the list or create a new one",
                color = KlarityColors.TextTertiary,
                fontSize = 14.sp
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Quick Actions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    icon = "✨",
                    label = "Create New Note",
                    shortcut = "Ctrl+N",
                    onClick = onCreateNote,
                    isPrimary = true,
                    accentColor = luminousTeal
                )
                
                QuickActionButton(
                    icon = "🔍",
                    label = "Search Notes",
                    shortcut = "Ctrl+K",
                    onClick = onOpenSearch,
                    accentColor = luminousTeal
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Keyboard Shortcuts Hint
            Surface(
                color = KlarityColors.BgTertiary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Quick Shortcuts",
                        color = KlarityColors.TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ShortcutHint("Ctrl+/", "Commands")
                        ShortcutHint("Ctrl+B", "Bold")
                        ShortcutHint("Ctrl+I", "Italic")
                    }
                }
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
            isHovered -> KlarityColors.BgElevated
            else -> KlarityColors.BgTertiary.copy(alpha = 0.7f)
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
                color = if (isPrimary) accentColor else KlarityColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            
            Surface(
                color = KlarityColors.BgElevated,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = shortcut,
                    color = KlarityColors.TextTertiary,
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
            color = KlarityColors.BgElevated,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = shortcut,
                color = KlarityColors.TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
        Text(
            text = action,
            color = KlarityColors.TextTertiary,
            fontSize = 10.sp
        )
    }
}
