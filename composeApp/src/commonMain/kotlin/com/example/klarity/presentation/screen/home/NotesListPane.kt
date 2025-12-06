package com.example.klarity.presentation.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Note
import com.example.klarity.presentation.theme.KlarityColors
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Sort options for the note list
 */
enum class NoteSortOption(val label: String, val icon: String) {
    UPDATED("Last Updated", "🕐"),
    CREATED("Created Date", "📅"),
    TITLE("Title A-Z", "🔤"),
    TITLE_DESC("Title Z-A", "🔠")
}

/**
 * Group options for the note list
 */
enum class NoteGroupOption(val label: String, val icon: String) {
    NONE("No Grouping", "📋"),
    FOLDER("By Folder", "📁"),
    TAG("By Tag", "🏷️"),
    DATE("By Date", "📆")
}

/**
 * Enhanced Notes List with Desktop Features
 * 
 * Features:
 * - Multi-select (Shift, Ctrl)
 * - Sorting & grouping
 * - Tag filtering
 * - Hover reveals quick actions
 */
@Composable
fun NotesListPane(
    notes: List<Note>,
    selectedNoteId: String?,
    selectedNoteIds: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onNoteSelect: (Note, Boolean, Boolean) -> Unit, // note, isCtrlPressed, isShiftPressed
    onCreateNote: () -> Unit,
    onTogglePin: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onAskAI: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val luminousTeal = Color(0xFF1FDBC8)
    val electricMint = Color(0xFF3DD68C)
    
    var sortOption by remember { mutableStateOf(NoteSortOption.UPDATED) }
    var groupOption by remember { mutableStateOf(NoteGroupOption.NONE) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showGroupMenu by remember { mutableStateOf(false) }
    var aiClusterEnabled by remember { mutableStateOf(false) }
    var filterTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Sorted and filtered notes
    val processedNotes = remember(notes, sortOption, filterTags) {
        notes
            .filter { note ->
                filterTags.isEmpty() || note.tags.any { it in filterTags }
            }
            .sortedWith(
                when (sortOption) {
                    NoteSortOption.UPDATED -> compareByDescending { it.updatedAt }
                    NoteSortOption.CREATED -> compareByDescending { it.createdAt }
                    NoteSortOption.TITLE -> compareBy { it.title.lowercase() }
                    NoteSortOption.TITLE_DESC -> compareByDescending { it.title.lowercase() }
                }
            )
    }
    
    // All available tags
    val allTags = remember(notes) {
        notes.flatMap { it.tags }.distinct().sorted()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KlarityColors.BgSecondary)
            .padding(start = 1.dp) // Subtle left border effect
    ) {
        // Header with search and actions
        NotesListHeader(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onCreateNote = onCreateNote,
            noteCount = processedNotes.size,
            selectedCount = selectedNoteIds.size,
            luminousTeal = luminousTeal
        )
        
        // Toolbar - Sort, Group, Filter
        NotesListToolbar(
            sortOption = sortOption,
            groupOption = groupOption,
            showSortMenu = showSortMenu,
            showGroupMenu = showGroupMenu,
            onSortClick = { showSortMenu = !showSortMenu },
            onGroupClick = { showGroupMenu = !showGroupMenu },
            onSortSelected = { 
                sortOption = it
                showSortMenu = false
            },
            onGroupSelected = {
                groupOption = it
                showGroupMenu = false
            },
            aiClusterEnabled = aiClusterEnabled,
            onAiClusterToggle = { aiClusterEnabled = !aiClusterEnabled },
            luminousTeal = luminousTeal,
            electricMint = electricMint
        )
        
        // Tag filter chips
        if (allTags.isNotEmpty()) {
            TagFilterRow(
                allTags = allTags,
                selectedTags = filterTags,
                onTagToggle = { tag ->
                    filterTags = if (tag in filterTags) {
                        filterTags - tag
                    } else {
                        filterTags + tag
                    }
                },
                luminousTeal = luminousTeal
            )
        }
        
        HorizontalDivider(
            color = KlarityColors.BorderPrimary.copy(alpha = 0.3f),
            thickness = 1.dp
        )
        
        // Notes list with improved spacing
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = processedNotes,
                key = { _, item -> item.id }
            ) { index, note ->
                NoteListItem(
                    note = note,
                    isSelected = note.id == selectedNoteId,
                    isMultiSelected = note.id in selectedNoteIds,
                    showMultiSelectMode = selectedNoteIds.size > 1,
                    onClick = { onNoteClick(note) },
                    onSelect = { ctrlPressed, shiftPressed -> 
                        onNoteSelect(note, ctrlPressed, shiftPressed) 
                    },
                    onTogglePin = { onTogglePin(note.id) },
                    onDelete = { onDeleteNote(note.id) },
                    onAskAI = { onAskAI(note) },
                    luminousTeal = luminousTeal,
                    electricMint = electricMint
                )
            }
        }
    }
}

@Composable
private fun NotesListHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCreateNote: () -> Unit,
    noteCount: Int,
    selectedCount: Int,
    luminousTeal: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title
            Text(
                text = "Notes",
                color = KlarityColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            // Create button
            Surface(
                onClick = onCreateNote,
                color = luminousTeal.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = luminousTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "New",
                        color = luminousTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Search field
        Surface(
            color = KlarityColors.BgTertiary,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = KlarityColors.TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
                
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = KlarityColors.TextPrimary,
                        fontSize = 13.sp
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search notes...",
                                    color = KlarityColors.TextTertiary,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = KlarityColors.TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        
        // Selection indicator
        AnimatedVisibility(
            visible = selectedCount > 1,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$selectedCount notes selected",
                    color = luminousTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = { /* Bulk delete */ }) {
                        Text("Delete", color = Color(0xFFFF6B6B), fontSize = 12.sp)
                    }
                    TextButton(onClick = { /* Bulk tag */ }) {
                        Text("Tag", color = KlarityColors.TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
        
        // Note count
        Text(
            text = "$noteCount notes",
            color = KlarityColors.TextTertiary,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun NotesListToolbar(
    sortOption: NoteSortOption,
    groupOption: NoteGroupOption,
    showSortMenu: Boolean,
    showGroupMenu: Boolean,
    onSortClick: () -> Unit,
    onGroupClick: () -> Unit,
    onSortSelected: (NoteSortOption) -> Unit,
    onGroupSelected: (NoteGroupOption) -> Unit,
    aiClusterEnabled: Boolean,
    onAiClusterToggle: () -> Unit,
    luminousTeal: Color,
    electricMint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sort dropdown
        Box {
            ToolbarButton(
                icon = sortOption.icon,
                label = "Sort",
                onClick = onSortClick
            )
            
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { onSortSelected(sortOption) },
                containerColor = KlarityColors.BgElevated
            ) {
                NoteSortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = option.icon, fontSize = 14.sp)
                                Text(
                                    text = option.label,
                                    color = if (option == sortOption) luminousTeal else KlarityColors.TextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        onClick = { onSortSelected(option) }
                    )
                }
            }
        }
        
        // Group dropdown
        Box {
            ToolbarButton(
                icon = groupOption.icon,
                label = "Group",
                onClick = onGroupClick
            )
            
            DropdownMenu(
                expanded = showGroupMenu,
                onDismissRequest = { onGroupSelected(groupOption) },
                containerColor = KlarityColors.BgElevated
            ) {
                NoteGroupOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = option.icon, fontSize = 14.sp)
                                Text(
                                    text = option.label,
                                    color = if (option == groupOption) luminousTeal else KlarityColors.TextPrimary,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        onClick = { onGroupSelected(option) }
                    )
                }
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        // AI Cluster toggle
        AIClusterToggle(
            enabled = aiClusterEnabled,
            onToggle = onAiClusterToggle,
            electricMint = electricMint
        )
    }
}

@Composable
private fun ToolbarButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val bgColor by animateColorAsState(
        targetValue = if (isHovered) KlarityColors.BgElevated else KlarityColors.BgTertiary.copy(alpha = 0.6f),
        animationSpec = tween(100),
        label = "toolbarBtnBg"
    )
    
    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(16.dp), // Pill shape
        modifier = Modifier
            .hoverable(interactionSource)
            .border(
                width = 1.dp,
                color = if (isHovered) KlarityColors.BorderPrimary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 12.sp)
            Text(
                text = label,
                color = if (isHovered) KlarityColors.TextPrimary else KlarityColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = KlarityColors.TextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AIClusterToggle(
    enabled: Boolean,
    onToggle: () -> Unit,
    electricMint: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Surface(
        onClick = onToggle,
        color = when {
            enabled -> electricMint.copy(alpha = 0.15f)
            isHovered -> KlarityColors.BgElevated
            else -> Color.Transparent
        },
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.hoverable(interactionSource)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🧠", fontSize = 12.sp)
            Text(
                text = "AI Topics",
                color = if (enabled) electricMint else KlarityColors.TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (enabled) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun TagFilterRow(
    allTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    luminousTeal: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        allTags.take(10).forEach { tag ->
            val isSelected = tag in selectedTags
            
            Surface(
                onClick = { onTagToggle(tag) },
                color = if (isSelected) luminousTeal.copy(alpha = 0.15f) else KlarityColors.BgTertiary,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "#$tag",
                    color = if (isSelected) luminousTeal else KlarityColors.TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        if (allTags.size > 10) {
            Text(
                text = "+${allTags.size - 10} more",
                color = KlarityColors.TextTertiary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun NoteListItem(
    note: Note,
    isSelected: Boolean,
    isMultiSelected: Boolean,
    showMultiSelectMode: Boolean,
    onClick: () -> Unit,
    onSelect: (Boolean, Boolean) -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onAskAI: () -> Unit,
    luminousTeal: Color,
    electricMint: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> luminousTeal.copy(alpha = 0.15f)
            isMultiSelected -> luminousTeal.copy(alpha = 0.08f)
            isHovered -> KlarityColors.BgElevated
            else -> Color.Transparent
        },
        animationSpec = tween(100),
        label = "bgColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> luminousTeal.copy(alpha = 0.4f)
            else -> Color.Transparent
        },
        animationSpec = tween(100),
        label = "borderColor"
    )
    
    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp), // Increased padding for breathing room
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Multi-select checkbox (visible in multi-select mode or on hover with Ctrl hint)
            AnimatedVisibility(
                visible = showMultiSelectMode || isHovered,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Checkbox(
                    checked = isMultiSelected,
                    onCheckedChange = { onSelect(true, false) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = luminousTeal,
                        uncheckedColor = KlarityColors.TextTertiary
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Note content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (note.isPinned) {
                        Text(text = "📌", fontSize = 12.sp)
                    }
                    
                    // Auto-generate title from content if empty
                    val displayTitle = remember(note.title, note.content, note.createdAt) {
                        when {
                            note.title.isNotBlank() -> note.title
                            note.content.isNotBlank() -> note.content.lines().firstOrNull()?.take(40)?.trim() ?: "New Note"
                            else -> "New Note \u2022 ${formatTime(note.createdAt)}"
                        }
                    }
                    
                    Text(
                        text = displayTitle,
                        color = if (isSelected) luminousTeal else KlarityColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Preview
                if (note.content.isNotEmpty()) {
                    Text(
                        text = note.content.take(80).replace("\n", " "),
                        color = KlarityColors.TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
                
                // Tags and time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        note.tags.take(2).forEach { tag ->
                            Text(
                                text = "#$tag",
                                color = KlarityColors.TextTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                    
                    // Time
                    Text(
                        text = formatTime(note.updatedAt),
                        color = KlarityColors.TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
            
            // Quick actions (visible on hover)
            AnimatedVisibility(
                visible = isHovered,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuickActionIcon(
                        icon = if (note.isPinned) "📍" else "📌",
                        onClick = onTogglePin,
                        tint = if (note.isPinned) luminousTeal else KlarityColors.TextTertiary
                    )
                    QuickActionIcon(
                        icon = "🤖",
                        onClick = onAskAI,
                        tint = electricMint
                    )
                    QuickActionIcon(
                        icon = "🗑️",
                        onClick = onDelete,
                        tint = Color(0xFFFF6B6B)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionIcon(
    icon: String,
    onClick: () -> Unit,
    tint: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Surface(
        onClick = onClick,
        color = if (isHovered) tint.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.hoverable(interactionSource)
    ) {
        Box(
            modifier = Modifier.padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 14.sp)
        }
    }
}

private fun formatTime(instant: Instant): String {
    val now = Clock.System.now()
    val diffMs = now.toEpochMilliseconds() - instant.toEpochMilliseconds()
    
    val seconds = diffMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        seconds < 60 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> "${days / 7}w"
    }
}
