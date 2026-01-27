package com.example.klarity.presentation.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.klarity.domain.models.Folder
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.NoteStatus
import androidx.compose.foundation.BorderStroke

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

    // Redesigned to match reference - simpler, flatter, darker
    Box(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(Color(0xFF0A1612)) // Darker teal-gray background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Project header with chevron
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chevron (always expanded in this design)
                Text(
                    "▼",
                    fontSize = 10.sp,
                    color = Color(0xFF7D8590)
                )
                Text(
                    projectName,
                    color = Color(0xFFE6EDF3),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                // Add folder button
                Text(
                    "+",
                    fontSize = 16.sp,
                    color = Color(0xFF7D8590),
                    modifier = Modifier.clickable { showCreateFolderDialog = true }
                )
            }

            // File Tree
            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
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

    Column {
        // Folder row - flat design
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(
                        if (isHovered) Color(0xFF1A3B34) else Color.Transparent,
                        RoundedCornerShape(4.dp)
                    )
                    .clickable(interactionSource = interactionSource, indication = null) { 
                        onToggleFolder(folder.id) 
                    }
                    .hoverable(interactionSource)
                    .padding(start = (depth * 16 + 8).dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Chevron
                Text(
                    if (isExpanded) "▼" else "▶",
                    fontSize = 9.sp,
                    color = Color(0xFF7D8590)
                )
                // Folder icon (using emoji)
                Text(
                    folder.icon ?: "📁",
                    fontSize = 16.sp
                )
                // Folder name
                Text(
                    folder.name,
                    fontSize = 13.sp,
                    color = if (isHovered) Color(0xFFE6EDF3) else Color(0xFFADBAC7),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Context menu button (visible on hover)
                if (isHovered) {
                    Text(
                        "⋯",
                        fontSize = 14.sp,
                        color = Color(0xFF7D8590),
                        modifier = Modifier.clickable { showContextMenu = true }
                    )
                }
            }

            // Context menu dropdown
            androidx.compose.material3.DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("Rename", fontSize = 13.sp) },
                    onClick = {
                        showContextMenu = false
                        onRenameFolder(folder)
                    }
                )
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("Delete", fontSize = 13.sp, color = Color(0xFFF43F5E)) },
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

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(
                    when {
                        isSelected -> Color(0xFF1A3B34) // Teal selection
                        isHovered -> Color(0xFF1A3B34).copy(alpha = 0.6f)
                        else -> Color.Transparent
                    },
                    RoundedCornerShape(4.dp)
                )
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .hoverable(interactionSource)
                .padding(start = (depth * 16 + 24).dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Document icon (using emoji)
            Text(
                "📄",
                fontSize = 14.sp,
                color = if (isSelected) Color(0xFF2DD4BF) else Color(0xFF7D8590)
            )
            // Note title
            Text(
                note.title.ifBlank { "Untitled" },
                fontSize = 13.sp,
                color = when {
                    isSelected -> Color(0xFFE6EDF3)
                    isHovered -> Color(0xFFE6EDF3)
                    else -> Color(0xFFADBAC7)
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
                    color = Color(0xFF7D8590),
                    modifier = Modifier.clickable { showMoveMenu = true }
                )
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
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF161B22),
            border = BorderStroke(1.dp, Color(0xFF30363D))
        ) {
            Column(
                modifier = Modifier.padding(24.dp).width(300.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Create New Folder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE6EDF3)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0D1117),
                    border = BorderStroke(1.dp, Color(0xFF30363D))
                ) {
                    BasicTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
                        singleLine = true,
                        textStyle = TextStyle(color = Color(0xFFE6EDF3), fontSize = 14.sp),
                        cursorBrush = SolidColor(Color(0xFF2DD4BF)),
                        decorationBox = { innerTextField ->
                            if (folderName.isEmpty()) {
                                Text("Folder name...", color = Color(0xFF7D8590), fontSize = 14.sp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Cancel", color = Color(0xFFE6EDF3))
                    }
                    Button(
                        onClick = { if (folderName.isNotBlank()) onCreate(folderName) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636)),
                        shape = RoundedCornerShape(6.dp),
                        enabled = folderName.isNotBlank()
                    ) {
                        Text("Create", color = Color.White)
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
            color = Color(0xFF161B22),
            border = BorderStroke(1.dp, Color(0xFF30363D))
        ) {
            Column(
                modifier = Modifier.padding(24.dp).width(300.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Rename Folder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE6EDF3)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0D1117),
                    border = BorderStroke(1.dp, Color(0xFF30363D))
                ) {
                    BasicTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
                        singleLine = true,
                        textStyle = TextStyle(color = Color(0xFFE6EDF3), fontSize = 14.sp),
                        cursorBrush = SolidColor(Color(0xFF2DD4BF))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Cancel", color = Color(0xFFE6EDF3))
                    }
                    Button(
                        onClick = { if (folderName.isNotBlank()) onRename(folderName) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636)),
                        shape = RoundedCornerShape(6.dp),
                        enabled = folderName.isNotBlank()
                    ) {
                        Text("Rename", color = Color.White)
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
            color = Color(0xFF161B22),
            border = BorderStroke(1.dp, Color(0xFF30363D))
        ) {
            Column(
                modifier = Modifier.padding(24.dp).width(300.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Delete Folder?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE6EDF3)
                )

                Text(
                    "Are you sure you want to delete \"${folder.name}\"? Notes inside will be moved to Uncategorized.",
                    fontSize = 14.sp,
                    color = Color(0xFF7D8590),
                    lineHeight = 20.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21262D)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Cancel", color = Color(0xFFE6EDF3))
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDA3633)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                }
            }
        }
    }
}
