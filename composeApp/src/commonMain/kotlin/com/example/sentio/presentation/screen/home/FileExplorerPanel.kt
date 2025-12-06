package com.example.sentio.presentation.screen.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sentio.domain.models.Folder
import com.example.sentio.domain.models.Note
import com.example.sentio.domain.models.NoteStatus
import com.example.sentio.presentation.theme.KlarityColors

/**
 * File Explorer Panel - Shows folder tree structure with full folder management
 */
@Composable
fun FileExplorerPanel(
    folders: List<Folder>,
    notes: List<Note>,
    expandedFolderIds: Set<String>,
    selectedNoteId: String?,
    projectName: String = "My Workspace",
    onProjectNameChange: (String) -> Unit = {},
    onToggleFolder: (String) -> Unit,
    onNoteSelect: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRenameFolder: (String, String) -> Unit = { _, _ -> },
    onDeleteFolder: (String) -> Unit = {},
    onMoveNoteToFolder: (String, String?) -> Unit = { _, _ -> }
) {
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<Folder?>(null) }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }
    var isEditingProjectName by remember { mutableStateOf(false) }
    var editedProjectName by remember(projectName) { mutableStateOf(projectName) }

    Surface(
        modifier = Modifier.width(220.dp).fillMaxHeight(),
        color = Color(0xFF11221F),
        border = BorderStroke(1.dp, KlarityColors.BorderPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header - Editable project name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditingProjectName) {
                    BasicTextField(
                        value = editedProjectName,
                        onValueChange = { editedProjectName = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(KlarityColors.AccentAI),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = KlarityColors.BgElevated,
                                border = BorderStroke(1.dp, KlarityColors.AccentAI)
                            ) {
                                Box(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    innerTextField()
                                }
                            }
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                    SmallIconButton("✓") {
                        onProjectNameChange(editedProjectName)
                        isEditingProjectName = false
                    }
                } else {
                    Text(
                        projectName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isEditingProjectName = true }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SmallIconButton("➕") { showCreateFolderDialog = true }
                        SmallIconButton("✏️") { isEditingProjectName = true }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // File Tree
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Root folders
                val rootFolders = folders.filter { it.parentId == null }

                rootFolders.forEach { folder ->
                    item(key = "folder_${folder.id}") {
                        FolderTreeItem(
                            folder = folder,
                            folders = folders,
                            notes = notes,
                            expandedFolderIds = expandedFolderIds,
                            selectedNoteId = selectedNoteId,
                            depth = 0,
                            onToggleFolder = onToggleFolder,
                            onNoteSelect = onNoteSelect,
                            onRenameFolder = { folderToRename = it },
                            onDeleteFolder = { folderToDelete = it },
                            onMoveNoteToFolder = onMoveNoteToFolder
                        )
                    }
                }

                // Root-level notes (no folder)
                val rootNotes = notes.filter { it.folderId == null }
                items(rootNotes, key = { "note_${it.id}" }) { note ->
                    DraggableNoteItem(
                        note = note,
                        isSelected = selectedNoteId == note.id,
                        depth = 0,
                        onClick = { onNoteSelect(note.id) },
                        folders = folders,
                        onMoveToFolder = { folderId -> onMoveNoteToFolder(note.id, folderId) }
                    )
                }
            }

            // Footer - AI Indexing status
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(KlarityColors.AccentAI, CircleShape)
                )
                Text(
                    "AI Indexing active...",
                    fontSize = 11.sp,
                    color = KlarityColors.TextTertiary
                )
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onCreate = { name ->
                onCreateFolder(name)
                showCreateFolderDialog = false
            }
        )
    }

    // Rename Folder Dialog
    folderToRename?.let { folder ->
        RenameFolderDialog(
            folder = folder,
            onDismiss = { folderToRename = null },
            onRename = { newName ->
                onRenameFolder(folder.id, newName)
                folderToRename = null
            }
        )
    }

    // Delete Folder Confirmation
    folderToDelete?.let { folder ->
        DeleteFolderDialog(
            folder = folder,
            onDismiss = { folderToDelete = null },
            onConfirm = {
                onDeleteFolder(folder.id)
                folderToDelete = null
            }
        )
    }
}

@Composable
fun FolderTreeItem(
    folder: Folder,
    folders: List<Folder>,
    notes: List<Note>,
    expandedFolderIds: Set<String>,
    selectedNoteId: String?,
    depth: Int,
    onToggleFolder: (String) -> Unit,
    onNoteSelect: (String) -> Unit,
    onRenameFolder: (Folder) -> Unit = {},
    onDeleteFolder: (Folder) -> Unit = {},
    onMoveNoteToFolder: (String, String?) -> Unit = { _, _ -> },
    isDropTarget: Boolean = false,
    onDropTargetChange: (Boolean) -> Unit = {},
    onNoteDrop: (String) -> Unit = {}
) {
    val isExpanded = folder.id in expandedFolderIds
    val subFolders = folders.filter { it.parentId == folder.id }
    val folderNotes = notes.filter { it.folderId == folder.id }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showContextMenu by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = when {
            isDropTarget -> KlarityColors.AccentAI.copy(alpha = 0.2f)
            isHovered -> KlarityColors.BgElevated.copy(alpha = 0.6f)
            else -> Color.Transparent
        },
        animationSpec = tween(150)
    )

    Column {
        // Folder row
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(interactionSource = interactionSource, indication = null) { onToggleFolder(folder.id) }
                    .hoverable(interactionSource),
                shape = RoundedCornerShape(6.dp),
                color = bgColor,
                border = if (isDropTarget) BorderStroke(1.dp, KlarityColors.AccentAI) else null
            ) {
                Row(
                    modifier = Modifier
                        .padding(start = (depth * 12).dp, end = 8.dp)
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Chevron
                    Text(
                        if (isExpanded) "▼" else "▶",
                        fontSize = 8.sp,
                        color = KlarityColors.TextTertiary
                    )
                    // Folder icon
                    Text(
                        folder.icon ?: "📁",
                        fontSize = 14.sp
                    )
                    // Folder name
                    Text(
                        folder.name,
                        fontSize = 13.sp,
                        color = if (isHovered || isDropTarget) Color.White else KlarityColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // Context menu button (visible on hover)
                    if (isHovered) {
                        Text(
                            "⋯",
                            fontSize = 12.sp,
                            color = KlarityColors.TextTertiary,
                            modifier = Modifier.clickable { showContextMenu = true }
                        )
                    }
                }
            }

            // Context menu dropdown
            androidx.compose.material3.DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("✏️ Rename", fontSize = 13.sp) },
                    onClick = {
                        showContextMenu = false
                        onRenameFolder(folder)
                    }
                )
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("🗑️ Delete", fontSize = 13.sp, color = Color(0xFFF43F5E)) },
                    onClick = {
                        showContextMenu = false
                        onDeleteFolder(folder)
                    }
                )
            }
        }

        // Children (if expanded)
        if (isExpanded) {
            // Sub-folders
            subFolders.forEach { subFolder ->
                FolderTreeItem(
                    folder = subFolder,
                    folders = folders,
                    notes = notes,
                    expandedFolderIds = expandedFolderIds,
                    selectedNoteId = selectedNoteId,
                    depth = depth + 1,
                    onToggleFolder = onToggleFolder,
                    onNoteSelect = onNoteSelect,
                    onRenameFolder = onRenameFolder,
                    onDeleteFolder = onDeleteFolder,
                    onMoveNoteToFolder = onMoveNoteToFolder
                )
            }
            // Notes in this folder
            folderNotes.forEach { note ->
                DraggableNoteItem(
                    note = note,
                    isSelected = selectedNoteId == note.id,
                    depth = depth + 1,
                    onClick = { onNoteSelect(note.id) },
                    folders = folders,
                    onMoveToFolder = { folderId -> onMoveNoteToFolder(note.id, folderId) }
                )
            }
        }
    }
}

@Composable
fun FileTreeNoteItem(
    note: Note,
    isSelected: Boolean,
    depth: Int,
    onClick: () -> Unit
) {
    DraggableNoteItem(note, isSelected, depth, onClick)
}

@Composable
fun DraggableNoteItem(
    note: Note,
    isSelected: Boolean,
    depth: Int,
    onClick: () -> Unit,
    folders: List<Folder> = emptyList(),
    onMoveToFolder: (String?) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    var showMoveMenu by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> KlarityColors.BgSelected.copy(alpha = 0.8f)
            isHovered -> KlarityColors.BgElevated.copy(alpha = 0.6f)
            else -> Color.Transparent
        },
        animationSpec = tween(150)
    )

    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .hoverable(interactionSource),
            shape = RoundedCornerShape(6.dp),
            color = bgColor
        ) {
            Row(
                modifier = Modifier
                    .padding(start = (depth * 12 + 14).dp, end = 8.dp)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "📄",
                    fontSize = 14.sp,
                    color = if (isSelected) KlarityColors.AccentAI else KlarityColors.TextTertiary
                )
                Text(
                    note.title.ifBlank { "Untitled" },
                    fontSize = 13.sp,
                    color = when {
                        isSelected -> Color.White
                        isHovered -> Color.White
                        else -> KlarityColors.TextSecondary
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Move button (visible on hover)
                if (isHovered && folders.isNotEmpty()) {
                    Text(
                        "📁",
                        fontSize = 12.sp,
                        color = KlarityColors.TextTertiary,
                        modifier = Modifier.clickable { showMoveMenu = true }
                    )
                }
                // Status badge
                NoteStatusBadge(note.status)
            }
        }

        // Move to folder dropdown
        androidx.compose.material3.DropdownMenu(
            expanded = showMoveMenu,
            onDismissRequest = { showMoveMenu = false }
        ) {
            androidx.compose.material3.DropdownMenuItem(
                text = { 
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📂", fontSize = 14.sp)
                        Text("Uncategorized", fontSize = 13.sp)
                    }
                },
                onClick = {
                    onMoveToFolder(null)
                    showMoveMenu = false
                }
            )
            folders.forEach { folder ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { 
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(folder.icon ?: "📁", fontSize = 14.sp)
                            Text(folder.name, fontSize = 13.sp)
                        }
                    },
                    onClick = {
                        onMoveToFolder(folder.id)
                        showMoveMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun NoteStatusBadge(status: NoteStatus) {
    if (status == NoteStatus.NONE) return
    
    val (bgColor, textColor, label) = when (status) {
        NoteStatus.IN_PROGRESS -> Triple(
            Color(0xFF0EA5E9).copy(alpha = 0.2f),
            Color(0xFF38BDF8),
            "⏳"
        )
        NoteStatus.COMPLETED -> Triple(
            Color(0xFF10B981).copy(alpha = 0.2f),
            Color(0xFF34D399),
            "✓"
        )
        NoteStatus.ON_HOLD -> Triple(
            Color(0xFFF59E0B).copy(alpha = 0.2f),
            Color(0xFFFBBF24),
            "⏸"
        )
        NoteStatus.ARCHIVED -> Triple(
            Color(0xFF6B7280).copy(alpha = 0.2f),
            Color(0xFF9CA3AF),
            "📦"
        )
        else -> return
    }
    
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            label,
            fontSize = 10.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

@Composable
fun DropZone(
    label: String,
    isActive: Boolean,
    onDropTargetChange: (Boolean) -> Unit,
    onNoteDrop: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    LaunchedEffect(isHovered) {
        onDropTargetChange(isHovered)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(6.dp),
        color = if (isActive) KlarityColors.AccentAI.copy(alpha = 0.1f) else Color.Transparent,
        border = if (isActive) BorderStroke(1.dp, KlarityColors.AccentAI.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("📂", fontSize = 12.sp, color = KlarityColors.TextTertiary)
            Text(
                label,
                fontSize = 11.sp,
                color = if (isActive) KlarityColors.AccentAI else KlarityColors.TextTertiary
            )
        }
    }
}

// Dialogs for folder management
@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = KlarityColors.BgSecondary,
            border = BorderStroke(1.dp, KlarityColors.BorderPrimary)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).width(300.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Create New Folder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Surface(
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = KlarityColors.BgElevated,
                    border = BorderStroke(1.dp, KlarityColors.BorderPrimary)
                ) {
                    BasicTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = SolidColor(KlarityColors.AccentAI),
                        decorationBox = { innerTextField ->
                            if (folderName.isEmpty()) {
                                Text("Folder name...", color = KlarityColors.TextTertiary, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = KlarityColors.BgElevated),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = KlarityColors.TextSecondary)
                    }
                    Button(
                        onClick = { if (folderName.isNotBlank()) onCreate(folderName) },
                        colors = ButtonDefaults.buttonColors(containerColor = KlarityColors.AccentAI),
                        shape = RoundedCornerShape(8.dp),
                        enabled = folderName.isNotBlank()
                    ) {
                        Text("Create", color = KlarityColors.BgSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun RenameFolderDialog(
    folder: Folder,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var folderName by remember { mutableStateOf(folder.name) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = KlarityColors.BgSecondary,
            border = BorderStroke(1.dp, KlarityColors.BorderPrimary)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).width(300.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Rename Folder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Surface(
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = KlarityColors.BgElevated,
                    border = BorderStroke(1.dp, KlarityColors.BorderPrimary)
                ) {
                    BasicTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = SolidColor(KlarityColors.AccentAI)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = KlarityColors.BgElevated),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = KlarityColors.TextSecondary)
                    }
                    Button(
                        onClick = { if (folderName.isNotBlank()) onRename(folderName) },
                        colors = ButtonDefaults.buttonColors(containerColor = KlarityColors.AccentAI),
                        shape = RoundedCornerShape(8.dp),
                        enabled = folderName.isNotBlank()
                    ) {
                        Text("Rename", color = KlarityColors.BgSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteFolderDialog(
    folder: Folder,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = KlarityColors.BgSecondary,
            border = BorderStroke(1.dp, KlarityColors.BorderPrimary)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).width(300.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Delete Folder?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Text(
                    "Are you sure you want to delete \"${folder.name}\"? Notes inside will be moved to Uncategorized.",
                    fontSize = 14.sp,
                    color = KlarityColors.TextSecondary,
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = KlarityColors.BgElevated),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = KlarityColors.TextSecondary)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        }
    }
}

