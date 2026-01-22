package com.example.klarity.presentation.screen.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.klarity.presentation.screen.home.util.formatRelativeTime
import kotlinx.datetime.*

/**
 * HomeDashboard - 2-Column Dashboard
 * 
 * Simplified Dashboard focused on Notes + Tasks:
 * - Column 1: Recent Notes (notes edited recently)
 * - Column 2: Today's Tasks (pinned tasks, daily goals)
 */

// Data classes for dashboard items
data class RecentItem(
    val id: String,
    val title: String,
    val type: RecentItemType,
    val lastModified: Instant,
    val preview: String? = null,
    val tags: List<String> = emptyList()
)

enum class RecentItemType {
    NOTE, TASK, FOLDER
}

data class FocusItem(
    val id: String,
    val title: String,
    val type: FocusItemType,
    val priority: Priority = Priority.NORMAL,
    val dueTime: String? = null,
    val isCompleted: Boolean = false,
    val progress: Float? = null
)

enum class FocusItemType {
    PINNED_TASK, DAILY_GOAL, SCHEDULED_EVENT, HABIT
}

enum class Priority {
    LOW, NORMAL, HIGH, URGENT
}

@Composable
fun HomeDashboard(
    recentNotes: List<Note>,
    recentItems: List<RecentItem> = emptyList(),
    focusItems: List<FocusItem> = emptyList(),
    onNoteClick: (Note) -> Unit,
    onRecentItemClick: (RecentItem) -> Unit = {},
    onFocusItemClick: (FocusItem) -> Unit = {},
    onFocusItemToggle: (FocusItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Column 1: Recent Notes
        RecentWorkColumn(
            recentNotes = recentNotes,
            recentItems = recentItems,
            onNoteClick = onNoteClick,
            onRecentItemClick = onRecentItemClick,
            modifier = Modifier.weight(1f)
        )
        
        // Column 2: Today's Tasks
        TodaysFocusColumn(
            focusItems = focusItems,
            onFocusItemClick = onFocusItemClick,
            onFocusItemToggle = onFocusItemToggle,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DashboardColumn(
    title: String,
    emoji: String,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Column Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
        
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 1.dp
        )
        
        // Column Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
private fun DashboardSubsection(
    title: String,
    count: Int? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            if (count != null) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        content()
    }
}

// ============================================================================
// Column 1: Recent Work
// ============================================================================

@Composable
private fun RecentWorkColumn(
    recentNotes: List<Note>,
    recentItems: List<RecentItem>,
    onNoteClick: (Note) -> Unit,
    onRecentItemClick: (RecentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardColumn(
        title = "Recent Work",
        emoji = "🕒",
        accentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier,
        actions = {
            IconButton(
                onClick = { /* Refresh */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Recent Notes Section
            if (recentNotes.isNotEmpty()) {
                item {
                    DashboardSubsection(
                        title = "Notes",
                        count = recentNotes.size
                    ) {
                        Column {
                            recentNotes.take(5).forEach { note ->
                                RecentNoteItem(
                                    note = note,
                                    onClick = { onNoteClick(note) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Other Recent Items Section
            if (recentItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    DashboardSubsection(
                        title = "Other Items",
                        count = recentItems.size
                    ) {
                        Column {
                            recentItems.take(5).forEach { item ->
                                RecentItemCard(
                                    item = item,
                                    onClick = { onRecentItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Empty state
            if (recentNotes.isEmpty() && recentItems.isEmpty()) {
                item {
                    EmptyStateCard(
                        emoji = "🕒",
                        title = "No recent activity",
                        description = "Your recently edited notes and tasks will appear here"
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentNoteItem(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (note.isPinned) "📌" else "📝",
                fontSize = 14.sp
            )
        }
        
        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = note.title.ifEmpty { "Untitled" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (note.content.isNotEmpty()) {
                Text(
                    text = note.content.take(80),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatRelativeTime(note.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        // Tags indicator
        if (note.tags.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${note.tags.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun RecentItemCard(
    item: RecentItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (emoji, tint) = when (item.type) {
        RecentItemType.NOTE -> "📝" to MaterialTheme.colorScheme.primary
        RecentItemType.TASK -> "✅" to MaterialTheme.colorScheme.secondary
        RecentItemType.FOLDER -> "📁" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 14.sp)
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatRelativeTime(item.lastModified),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ============================================================================
// Column 2: Today's Focus
// ============================================================================

@Composable
private fun TodaysFocusColumn(
    focusItems: List<FocusItem>,
    onFocusItemClick: (FocusItem) -> Unit,
    onFocusItemToggle: (FocusItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val pinnedTasks = focusItems.filter { it.type == FocusItemType.PINNED_TASK }
    val dailyGoals = focusItems.filter { it.type == FocusItemType.DAILY_GOAL }
    val scheduledEvents = focusItems.filter { it.type == FocusItemType.SCHEDULED_EVENT }
    val habits = focusItems.filter { it.type == FocusItemType.HABIT }
    
    DashboardColumn(
        title = "Today's Focus",
        emoji = "🎯",
        accentColor = MaterialTheme.colorScheme.secondary,
        modifier = modifier,
        actions = {
            IconButton(
                onClick = { /* Add focus item */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Progress Overview
            item {
                FocusProgressCard(focusItems)
            }
            
            // Pinned Tasks
            if (pinnedTasks.isNotEmpty()) {
                item {
                    DashboardSubsection(
                        title = "Pinned Tasks",
                        count = pinnedTasks.size
                    ) {
                        Column {
                            pinnedTasks.forEach { task ->
                                FocusTaskItem(
                                    item = task,
                                    onClick = { onFocusItemClick(task) },
                                    onToggle = { onFocusItemToggle(task) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Daily Goals
            if (dailyGoals.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    DashboardSubsection(
                        title = "Daily Goals",
                        count = dailyGoals.size
                    ) {
                        Column {
                            dailyGoals.forEach { goal ->
                                FocusGoalItem(
                                    item = goal,
                                    onClick = { onFocusItemClick(goal) },
                                    onToggle = { onFocusItemToggle(goal) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Scheduled Events
            if (scheduledEvents.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    DashboardSubsection(
                        title = "Schedule",
                        count = scheduledEvents.size
                    ) {
                        Column {
                            scheduledEvents.forEach { event ->
                                FocusScheduleItem(
                                    item = event,
                                    onClick = { onFocusItemClick(event) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Habits
            if (habits.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    DashboardSubsection(
                        title = "Habits",
                        count = habits.size
                    ) {
                        Column {
                            habits.forEach { habit ->
                                FocusHabitItem(
                                    item = habit,
                                    onClick = { onFocusItemClick(habit) },
                                    onToggle = { onFocusItemToggle(habit) }
                                )
                            }
                        }
                    }
                }
            }
            
            // Empty state
            if (focusItems.isEmpty()) {
                item {
                    EmptyStateCard(
                        emoji = "🎯",
                        title = "No focus items today",
                        description = "Pin tasks or set daily goals to see them here"
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusProgressCard(
    focusItems: List<FocusItem>,
    modifier: Modifier = Modifier
) {
    val totalItems = focusItems.size
    val completedItems = focusItems.count { it.isCompleted }
    val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "$completedItems / $totalItems",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun FocusTaskItem(
    item: FocusItem,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (item.priority) {
        Priority.URGENT -> MaterialTheme.colorScheme.error
        Priority.HIGH -> Color(0xFFFFAB40)
        Priority.NORMAL -> MaterialTheme.colorScheme.secondary
        Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isCompleted,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.secondary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                checkmarkColor = MaterialTheme.colorScheme.onSecondary
            )
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.dueTime != null) {
                Text(
                    text = item.dueTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        // Priority indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(priorityColor)
        )
    }
}

@Composable
private fun FocusGoalItem(
    item: FocusItem,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (item.isCompleted) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (item.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (item.progress != null) {
                LinearProgressIndicator(
                    progress = { item.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FocusScheduleItem(
    item: FocusItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            Text(
                text = item.dueTime ?: "--:--",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FocusHabitItem(
    item: FocusItem,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (item.isCompleted)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (item.isCompleted) "✓" else "○",
                fontSize = 14.sp,
                color = if (item.isCompleted)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (item.isCompleted)
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

// ============================================================================
// Shared Components
// ============================================================================

@Composable
private fun EmptyStateCard(
    emoji: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 40.sp
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// formatRelativeTime is imported from HomeUtils.kt
