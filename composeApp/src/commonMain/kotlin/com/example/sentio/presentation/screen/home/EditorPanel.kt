package com.example.sentio.presentation.screen.home

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
import com.example.sentio.domain.models.Folder
import com.example.sentio.domain.models.Note
import com.example.sentio.presentation.theme.SentioColors

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
    modifier: Modifier = Modifier
) {
    // Track formatting state for toolbar callbacks
    var formatBoldTrigger by remember { mutableStateOf(0) }
    var formatItalicTrigger by remember { mutableStateOf(0) }
    var formatCodeTrigger by remember { mutableStateOf(0) }
    var formatLinkTrigger by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(SentioColors.BgPrimary)
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
                onTogglePin = onTogglePin,
                onDelete = onDelete,
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
                        projectName = "Project X",
                        folderName = folder?.name ?: "Design Docs",
                        noteName = selectedNote.title.ifBlank { "Untitled" }
                    )

                    // Editor Content (editable)
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
                } else {
                    // Empty State
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📝", fontSize = 64.sp, color = SentioColors.TextTertiary.copy(alpha = 0.3f))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Select a note to start editing",
                                color = SentioColors.TextTertiary,
                                fontSize = 16.sp
                            )
                        }
                    }
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
    onTogglePin: () -> Unit = {},
    onDelete: () -> Unit = {},
    hasNote: Boolean = false,
    onBold: () -> Unit = {},
    onItalic: () -> Unit = {},
    onCode: () -> Unit = {},
    onLink: () -> Unit = {},
    onSlashMenu: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(8.dp),
        color = SentioColors.BgPrimary.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, SentioColors.BorderPrimary)
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
                        .background(SentioColors.BorderPrimary)
                )

                ToolbarButton("✨", isActive = true, isPrimary = true, onClick = onSlashMenu, tooltip = "Commands (Ctrl+/)")
                ToolbarButton("⋯")
            }

            // Right: Pin, Delete, Settings
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (hasNote) {
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

    Box {
        Surface(
            modifier = Modifier
                .size(32.dp)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .hoverable(interactionSource),
            shape = RoundedCornerShape(6.dp),
            color = if (isActive || isHovered) SentioColors.BgElevated.copy(alpha = 0.5f) else Color.Transparent
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text,
                    fontSize = 14.sp,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isPrimary -> SentioColors.AccentAI
                        isActive -> Color.White
                        else -> SentioColors.TextTertiary
                    }
                )
            }
        }
        
        // Tooltip on hover
        if (isHovered && tooltip.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 36.dp),
                shape = RoundedCornerShape(4.dp),
                color = SentioColors.BgElevated,
                shadowElevation = 4.dp
            ) {
                Text(
                    tooltip,
                    fontSize = 11.sp,
                    color = SentioColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
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
        Text("/", color = SentioColors.BorderPrimary, fontSize = 14.sp)
        BreadcrumbItem(folderName, isClickable = true)
        Text("/", color = SentioColors.BorderPrimary, fontSize = 14.sp)
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
            isHovered -> SentioColors.AccentAI
            else -> SentioColors.TextTertiary
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
            cursorBrush = SolidColor(SentioColors.AccentAI),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box {
                    if (titleValue.text.isEmpty()) {
                        Text(
                            "Untitled",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = SentioColors.TextTertiary.copy(alpha = 0.5f),
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
                color = SentioColors.TextSecondary,
                lineHeight = 28.sp
            ),
            cursorBrush = SolidColor(SentioColors.AccentAI),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 300.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (contentValue.text.isEmpty()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                "Start typing your note, or press ",
                                color = SentioColors.TextTertiary,
                                fontSize = 16.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SentioColors.AccentAI.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "Ctrl+/",
                                    color = SentioColors.AccentAI,
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(" for commands...", color = SentioColors.TextTertiary, fontSize = 16.sp)
                        }
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun EditorFooter(wordCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SentioColors.BgPrimary,
        border = BorderStroke(1.dp, SentioColors.BorderPrimary)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Word Count: $wordCount",
                fontSize = 12.sp,
                color = SentioColors.TextTertiary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Graph Mapping: On", fontSize = 12.sp, color = SentioColors.TextTertiary)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), androidx.compose.foundation.shape.CircleShape)
                    )
                    Text("Saved", fontSize = 12.sp, color = SentioColors.TextTertiary)
                }
            }
        }
    }
}

