package com.example.klarity.presentation.screen.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.NoteStatus
import com.example.klarity.presentation.components.*
import com.example.klarity.presentation.theme.KlarityColors
import klarity.composeapp.generated.resources.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import org.jetbrains.compose.resources.painterResource

/**
 * Notes List Panel - RIGHT SIDE (Sidebar)
 * 
 * Replicated from image mockup:
 * - Search + New button at top
 * - Card-based note list categorized by sections
 */
@Composable
fun NotesListPane(
    notes: List<Note>,
    folders: List<com.example.klarity.domain.models.Folder> = emptyList(),
    selectedNoteId: String?,
    selectedNoteIds: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onNoteSelect: (Note, Boolean, Boolean) -> Unit,
    onCreateNote: () -> Unit,
    onTogglePin: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onAskAI: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    // Group notes by folder names dynamically
    val groupedNotes = remember(notes, folders) {
        val groups = mutableMapOf<String, List<Note>>()
        
        // Always show pinned notes first
        val pinned = notes.filter { it.isPinned }
        if (pinned.isNotEmpty()) groups["📌 PINNED"] = pinned
        
        // Group by folder
        val folderMap = folders.associateBy { it.id }
        val notesByFolder = notes.filter { !it.isPinned }.groupBy { it.folderId }
        
        // Add notes with folders
        notesByFolder.forEach { (folderId, notesInFolder) ->
            if (folderId != null) {
                val folder = folderMap[folderId]
                val folderName = folder?.name?.uppercase() ?: "UNCATEGORIZED"
                groups["FOLDER:$folderName"] = notesInFolder
            }
        }
        
        // Add notes without folder (root level)
        notesByFolder[null]?.let { rootNotes ->
            if (rootNotes.isNotEmpty()) {
                groups["FOLDER:UNCATEGORIZED"] = rootNotes
            }
        }
        
        groups
    }
    
    Row(modifier = modifier.fillMaxSize()) {
        // Pixel-perfect vertical divider on the left
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color(0xFF1F3D35).copy(alpha = 0.5f))
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(KlarityColors.BgPrimary)
                .padding(16.dp)
        ) {
            // Search + New button
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search
                Surface(
                    color = Color(0xFF161B22).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF484F58).copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFFE6EDF3),
                                fontSize = 13.sp
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search notes...", color = Color(0xFF484F58), fontSize = 13.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
                
                // + New
                Surface(
                    onClick = onCreateNote,
                    color = Color(0xFF34D399),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("+", color = Color(0xFF08100E), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("New", color = Color(0xFF08100E), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Notes list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                groupedNotes.forEach { (section, sectionNotes) ->
                    // Section header
                    item(key = "header_$section") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (section.startsWith("FOLDER:")) {
                                Icon(
                                    painter = painterResource(Res.drawable.solar__folder_with_files_bold),
                                    contentDescription = null,
                                    tint = Color(0xFF5C7C75),
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text(
                                    text = if (section == "PINNED") "📌" else "",
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = section.removePrefix("FOLDER:"),
                                color = Color(0xFF5C7C75),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    
                    // Notes as Cards
                    items(items = sectionNotes, key = { it.id }) { note ->
                        KlarityNoteCard(
                            headline = note.title.ifEmpty { "Untitled Note" },
                            supporting = if (note.content.isNotEmpty()) note.content.take(80) else "No content...",
                            timestamp = formatTimeAgo(note.updatedAt),
                            icon = when (section) {
                                "DESIGN DOCS" -> painterResource(Res.drawable.solar__file_bold)
                                "MEETING NOTES" -> painterResource(Res.drawable.solar__file_bold)
                                else -> painterResource(Res.drawable.solar__file_bold)
                            },
                            showAvatar = section == "PINNED" || note.title.contains("holy"),
                            status = when (note.status) {
                                NoteStatus.IN_PROGRESS -> "In Progress"
                                NoteStatus.COMPLETED -> "Completed"
                                NoteStatus.ON_HOLD -> "On Hold"
                                else -> null
                            },
                            statusColor = when (note.status) {
                                NoteStatus.IN_PROGRESS -> Color(0xFF38BDF8)
                                NoteStatus.COMPLETED -> Color(0xFF34D399)
                                NoteStatus.ON_HOLD -> Color(0xFFFBBF24)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            tag = if (section == "DESIGN DOCS") "API" else null,
                            onClick = { onNoteClick(note) }
                        )
                    }
                    
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFF1F3D35).copy(alpha = 0.3f)
                        )
                    }
                }
                
                // Placeholder/Empty state logic if needed
                if (notes.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("📌 PINNED", color = Color(0xFF5C7C75), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            KlarityNoteCard(
                                headline = "Editor UI Spec",
                                supporting = "This document outlines the core features and desig...",
                                timestamp = "2h ago",
                                status = "In Progress",
                                statusColor = Color(0xFF38BDF8),
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimeAgo(instant: kotlinx.datetime.Instant): String {
    val now = Clock.System.now()
    val duration = instant.periodUntil(now, TimeZone.currentSystemDefault())
    return when {
        duration.years > 0 -> "${duration.years}y ago"
        duration.months > 0 -> "${duration.months}mo ago"
        duration.days > 0 -> "${duration.days}d ago"
        duration.hours > 0 -> "${duration.hours}h ago"
        duration.minutes > 0 -> "${duration.minutes}m ago"
        else -> "now"
    }
}
