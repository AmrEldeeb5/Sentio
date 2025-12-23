package com.example.klarity.presentation.screen.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Folder
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.NoteStatus
import com.example.klarity.presentation.screen.home.util.formatRelativeTime
import com.example.klarity.presentation.theme.KlarityColors

/**
 * Notes Tree Sidebar - Right sidebar showing notes organized in tree structure
 */
@Composable
fun NotesTreeSidebar(
    notes: List<Note>,
    folders: List<Folder>,
    expandedFolderIds: Set<String>,
    pinnedSectionExpanded: Boolean,
    searchQuery: String,
    selectedNoteId: String?,
    onSearchQueryChange: (String) -> Unit,
    onNoteSelect: (String) -> Unit,
    onCreateNote: () -> Unit,
    onToggleFolder: (String) -> Unit,
    onTogglePinnedSection: () -> Unit,
    onTogglePin: (String) -> Unit,
    onDeleteNote: (String) -> Unit
) {
    Surface(
        modifier = Modifier.width(320.dp).fillMaxHeight(),
        color = KlarityColors.BgPrimary,
        border = BorderStroke(1.dp, KlarityColors.BorderPrimary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search + New Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input
                Surface(
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = KlarityColors.BgElevated,
                    border = BorderStroke(1.dp, Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔍", fontSize = 14.sp, color = KlarityColors.TextTertiary)
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(KlarityColors.AccentAI),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Search notes...", color = KlarityColors.TextTertiary, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                // New Button
                Button(
                    onClick = onCreateNote,
                    colors = ButtonDefaults.buttonColors(containerColor = KlarityColors.AccentAI),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KlarityColors.BgSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("New", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KlarityColors.BgSecondary)
                }
            }

            // Filter notes
            val filteredNotes = notes.filter { note ->
                searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
            }.sortedByDescending { it.updatedAt }

            val pinnedNotes = filteredNotes.filter { it.isPinned }

            // Notes Tree
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // PINNED Section
                if (pinnedNotes.isNotEmpty()) {
                    item {
                        TreeSectionHeader(
                            title = "PINNED",
                            icon = "📌",
                            iconColor = Color(0xFFF43F5E),
                            isExpanded = pinnedSectionExpanded,
                            onToggle = onTogglePinnedSection
                        )
                    }
                    if (pinnedSectionExpanded) {
                        items(pinnedNotes, key = { "pinned_${it.id}" }) { note ->
                            TreeNoteCard(
                                note = note,
                                isSelected = selectedNoteId == note.id,
                                onClick = { onNoteSelect(note.id) }
                            )
                        }
                    }
                    item { TreeDivider() }
                }

                // Folder sections with tree structure
                val rootFolders = folders.filter { it.parentId == null }
                rootFolders.forEach { folder ->
                    item(key = "tree_folder_${folder.id}") {
                        TreeFolderSection(
                            folder = folder,
                            folders = folders,
                            notes = filteredNotes,
                            expandedFolderIds = expandedFolderIds,
                            selectedNoteId = selectedNoteId,
                            onToggleFolder = onToggleFolder,
                            onNoteSelect = onNoteSelect
                        )
                    }
                }

                // Uncategorized notes
                val uncategorizedNotes = filteredNotes.filter { it.folderId == null && !it.isPinned }
                if (uncategorizedNotes.isNotEmpty()) {
                    item { TreeDivider() }
                    item {
                        TreeSectionHeader(
                            title = "UNCATEGORIZED",
                            icon = "📄",
                            isExpanded = true,
                            onToggle = { }
                        )
                    }
                    items(uncategorizedNotes, key = { "uncategorized_${it.id}" }) { note ->
                        TreeNoteCard(
                            note = note,
                            isSelected = selectedNoteId == note.id,
                            onClick = { onNoteSelect(note.id) }
                        )
                    }
                }

                // Empty state
                if (filteredNotes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No notes found", color = KlarityColors.TextTertiary, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TreeSectionHeader(
    title: String,
    icon: String,
    iconColor: Color = KlarityColors.TextTertiary,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onToggle)
            .hoverable(interactionSource)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            if (isExpanded) "▼" else "▶",
            fontSize = 8.sp,
            color = KlarityColors.TextTertiary
        )
        Text(icon, fontSize = 12.sp, color = iconColor)
        Text(
            title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHovered) Color.White else KlarityColors.TextTertiary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun TreeFolderSection(
    folder: Folder,
    folders: List<Folder>,
    notes: List<Note>,
    expandedFolderIds: Set<String>,
    selectedNoteId: String?,
    onToggleFolder: (String) -> Unit,
    onNoteSelect: (String) -> Unit
) {
    val isExpanded = folder.id in expandedFolderIds
    val folderNotes = notes.filter { it.folderId == folder.id }
    val subFolders = folders.filter { it.parentId == folder.id }

    Column {
        // Folder header
        TreeSectionHeader(
            title = folder.name.uppercase(),
            icon = folder.icon ?: "📁",
            isExpanded = isExpanded,
            onToggle = { onToggleFolder(folder.id) }
        )

        // Contents (if expanded)
        if (isExpanded) {
            // Sub-folders
            subFolders.forEach { subFolder ->
                Box(modifier = Modifier.padding(start = 16.dp)) {
                    TreeFolderSection(
                        folder = subFolder,
                        folders = folders,
                        notes = notes,
                        expandedFolderIds = expandedFolderIds,
                        selectedNoteId = selectedNoteId,
                        onToggleFolder = onToggleFolder,
                        onNoteSelect = onNoteSelect
                    )
                }
            }
            // Notes
            folderNotes.forEach { note ->
                TreeNoteCard(
                    note = note,
                    isSelected = selectedNoteId == note.id,
                    onClick = { onNoteSelect(note.id) }
                )
            }
        }

        TreeDivider()
    }
}

@Composable
fun TreeDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(1.dp)
            .background(KlarityColors.BorderPrimary.copy(alpha = 0.3f))
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TreeNoteCard(
    note: Note,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> KlarityColors.BgSelected.copy(alpha = 0.8f)
            isHovered -> KlarityColors.BgElevated.copy(alpha = 0.4f)
            else -> Color.Transparent
        },
        animationSpec = tween(150)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = if (isSelected) BorderStroke(1.dp, KlarityColors.BorderPrimary.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "📄",
                        fontSize = 14.sp,
                        color = if (isSelected) KlarityColors.AccentAI else KlarityColors.TextTertiary
                    )
                    Text(
                        note.title.ifBlank { "Untitled" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected || isHovered) Color.White else KlarityColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    formatRelativeTime(note.updatedAt),
                    fontSize = 10.sp,
                    color = KlarityColors.TextTertiary
                )
            }

            // Preview
            Text(
                note.preview(),
                fontSize = 12.sp,
                color = KlarityColors.TextTertiary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier.padding(start = 22.dp)
            )

            // Status badge and Tags
            Row(
                modifier = Modifier.padding(start = 22.dp, top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Status badge
                if (note.status != NoteStatus.NONE) {
                    TreeNoteStatusBadge(note.status)
                }
                // Tags
                note.tags.take(2).forEach { tag ->
                    TreeTagBadge(tag)
                }
            }
        }
    }
}

@Composable
fun TreeNoteStatusBadge(status: NoteStatus) {
    val (bgColor, textColor, label) = when (status) {
        NoteStatus.IN_PROGRESS -> Triple(
            Color(0xFF0EA5E9).copy(alpha = 0.15f),
            Color(0xFF38BDF8),
            "In Progress"
        )
        NoteStatus.COMPLETED -> Triple(
            Color(0xFF10B981).copy(alpha = 0.15f),
            Color(0xFF34D399),
            "Completed"
        )
        NoteStatus.ON_HOLD -> Triple(
            Color(0xFFF59E0B).copy(alpha = 0.15f),
            Color(0xFFFBBF24),
            "On Hold"
        )
        NoteStatus.ARCHIVED -> Triple(
            Color(0xFF6B7280).copy(alpha = 0.15f),
            Color(0xFF9CA3AF),
            "Archived"
        )
        else -> return
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            label,
            fontSize = 9.sp,
            color = textColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun TreeTagBadge(tag: String) {
    val (bgColor, textColor, borderColor) = when {
        tag.contains("progress", ignoreCase = true) ->
            Triple(
                Color(0xFF0EA5E9).copy(alpha = 0.1f),
                Color(0xFF38BDF8),
                Color(0xFF0EA5E9).copy(alpha = 0.2f)
            )
        tag.contains("complete", ignoreCase = true) ->
            Triple(
                Color(0xFF10B981).copy(alpha = 0.1f),
                Color(0xFF34D399),
                Color(0xFF10B981).copy(alpha = 0.2f)
            )
        else ->
            Triple(
                KlarityColors.BgSelected.copy(alpha = 0.5f),
                KlarityColors.TextSecondary,
                KlarityColors.BorderPrimary.copy(alpha = 0.5f)
            )
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            tag,
            fontSize = 10.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

