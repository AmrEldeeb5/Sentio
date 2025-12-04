package com.example.sentio.presentation.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentio.domain.models.Folder
import com.example.sentio.domain.models.Note
import com.example.sentio.presentation.theme.SentioColors

/**
 * File Explorer Panel - Shows folder tree structure
 */
@Composable
fun FileExplorerPanel(
    folders: List<Folder>,
    notes: List<Note>,
    expandedFolderIds: Set<String>,
    selectedNoteId: String?,
    onToggleFolder: (String) -> Unit,
    onNoteSelect: (String) -> Unit,
    onCreateFolder: (String) -> Unit
) {
    Surface(
        modifier = Modifier.width(220.dp).fillMaxHeight(),
        color = Color(0xFF11221F),
        border = BorderStroke(1.dp, SentioColors.BorderPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Project X",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SmallIconButton("➕") { onCreateFolder("New Folder") }
                    SmallIconButton("⋯") { }
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
                            onNoteSelect = onNoteSelect
                        )
                    }
                }

                // Root-level notes (no folder)
                val rootNotes = notes.filter { it.folderId == null }
                items(rootNotes, key = { "note_${it.id}" }) { note ->
                    FileTreeNoteItem(
                        note = note,
                        isSelected = selectedNoteId == note.id,
                        depth = 0,
                        onClick = { onNoteSelect(note.id) }
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
                        .background(SentioColors.AccentAI, CircleShape)
                )
                Text(
                    "AI Indexing active...",
                    fontSize = 11.sp,
                    color = SentioColors.TextTertiary
                )
            }
        }
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
    onNoteSelect: (String) -> Unit
) {
    val isExpanded = folder.id in expandedFolderIds
    val subFolders = folders.filter { it.parentId == folder.id }
    val folderNotes = notes.filter { it.folderId == folder.id }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Column {
        // Folder row
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(interactionSource = interactionSource, indication = null) { onToggleFolder(folder.id) }
                .hoverable(interactionSource),
            shape = RoundedCornerShape(6.dp),
            color = if (isHovered) SentioColors.BgElevated.copy(alpha = 0.6f) else Color.Transparent
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
                    color = SentioColors.TextTertiary
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
                    color = if (isHovered) Color.White else SentioColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                    onNoteSelect = onNoteSelect
                )
            }
            // Notes in this folder
            folderNotes.forEach { note ->
                FileTreeNoteItem(
                    note = note,
                    isSelected = selectedNoteId == note.id,
                    depth = depth + 1,
                    onClick = { onNoteSelect(note.id) }
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
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(6.dp),
        color = when {
            isSelected -> SentioColors.BgSelected.copy(alpha = 0.8f)
            isHovered -> SentioColors.BgElevated.copy(alpha = 0.6f)
            else -> Color.Transparent
        }
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
                color = if (isSelected) SentioColors.AccentAI else SentioColors.TextTertiary
            )
            Text(
                note.title.ifBlank { "Untitled" },
                fontSize = 13.sp,
                color = when {
                    isSelected -> Color.White
                    isHovered -> Color.White
                    else -> SentioColors.TextSecondary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

