package com.example.klarity.presentation.screen.tasks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.example.klarity.presentation.theme.KlarityColors
import kotlinx.datetime.*

/**
 * TaskFlow Timeline View (Gantt-style)
 * 
 * Desktop Design: "TaskFlow: Kanban + Timeline"
 * - Horizontal timeline with task bars
 * - Week/Month/Quarter view options
 * - Task duration visualization
 * - Dependencies (future enhancement)
 */

enum class TimelineScale(val label: String, val daysPerUnit: Int) {
    DAY("Day", 1),
    WEEK("Week", 7),
    MONTH("Month", 30),
    QUARTER("Quarter", 90)
}

@Composable
fun TaskTimeline(
    tasks: List<Task>,
    scale: TimelineScale = TimelineScale.WEEK,
    onScaleChange: (TimelineScale) -> Unit,
    onTaskClick: (Task) -> Unit,
    onTaskMove: (Task, newStartDate: Instant, newDueDate: Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { Clock.System.now() }
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KlarityColors.BgPrimary)
    ) {
        // Timeline Header with scale selector
        TimelineHeader(
            scale = scale,
            onScaleChange = onScaleChange
        )
        
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Task list sidebar
            TaskListSidebar(
                tasks = tasks,
                onTaskClick = onTaskClick,
                modifier = Modifier.width(280.dp)
            )
            
            // Divider
            VerticalDivider(
                color = KlarityColors.TextTertiary.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxHeight()
            )
            
            // Timeline grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                TimelineGrid(
                    tasks = tasks,
                    scale = scale,
                    today = today,
                    onTaskClick = onTaskClick,
                    onTaskMove = onTaskMove
                )
            }
        }
    }
}

@Composable
private fun TimelineHeader(
    scale: TimelineScale,
    onScaleChange: (TimelineScale) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(KlarityColors.BgSecondary)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📅 Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = KlarityColors.TextPrimary
        )
        
        // Scale selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TimelineScale.entries.forEach { s ->
                FilterChip(
                    selected = scale == s,
                    onClick = { onScaleChange(s) },
                    label = {
                        Text(
                            text = s.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KlarityColors.AccentPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = KlarityColors.AccentPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun TaskListSidebar(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(KlarityColors.BgSecondary)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(KlarityColors.BgTertiary)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.labelLarge,
                color = KlarityColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        
        HorizontalDivider(color = KlarityColors.TextTertiary.copy(alpha = 0.2f))
        
        // Task list
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(tasks, key = { it.id }) { task ->
                TimelineTaskRow(
                    task = task,
                    onClick = { onTaskClick(task) }
                )
                HorizontalDivider(
                    color = KlarityColors.TextTertiary.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
private fun TimelineTaskRow(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Priority dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(task.priority.color))
        )
        
        // Task title
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyMedium,
            color = KlarityColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        // Status emoji
        Text(
            text = task.status.emoji,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TimelineGrid(
    tasks: List<Task>,
    scale: TimelineScale,
    today: Instant,
    onTaskClick: (Task) -> Unit,
    onTaskMove: (Task, Instant, Instant) -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate date range
    val dateRange = calculateDateRange(tasks, today, scale)
    val columnWidth = when (scale) {
        TimelineScale.DAY -> 40.dp
        TimelineScale.WEEK -> 80.dp
        TimelineScale.MONTH -> 120.dp
        TimelineScale.QUARTER -> 160.dp
    }
    
    Column(modifier = modifier) {
        // Date header
        Row(
            modifier = Modifier
                .height(48.dp)
                .background(KlarityColors.BgTertiary)
        ) {
            dateRange.forEach { date ->
                Box(
                    modifier = Modifier
                        .width(columnWidth)
                        .fillMaxHeight()
                        .border(
                            width = 0.5.dp,
                            color = KlarityColors.TextTertiary.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatDateHeader(date, scale),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday(date, today)) KlarityColors.AccentPrimary 
                               else KlarityColors.TextSecondary
                    )
                }
            }
        }
        
        HorizontalDivider(color = KlarityColors.TextTertiary.copy(alpha = 0.2f))
        
        // Task bars
        LazyColumn {
            items(tasks, key = { it.id }) { task ->
                TimelineTaskBar(
                    task = task,
                    dateRange = dateRange,
                    columnWidth = columnWidth,
                    today = today,
                    onClick = { onTaskClick(task) }
                )
                HorizontalDivider(
                    color = KlarityColors.TextTertiary.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
private fun TimelineTaskBar(
    task: Task,
    dateRange: List<Instant>,
    columnWidth: androidx.compose.ui.unit.Dp,
    today: Instant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startDate = task.startDate ?: task.createdAt
    val endDate = task.dueDate ?: task.startDate ?: task.createdAt
    
    // Calculate bar position and width
    val startIndex = dateRange.indexOfFirst { it >= startDate }.coerceAtLeast(0)
    val endIndex = dateRange.indexOfFirst { it >= endDate }.let { 
        if (it == -1) dateRange.size - 1 else it 
    }
    
    val barWidth = ((endIndex - startIndex + 1) * columnWidth.value).dp
    val barOffset = (startIndex * columnWidth.value).dp
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(vertical = 8.dp)
    ) {
        // Today indicator line
        val todayIndex = dateRange.indexOfFirst { isToday(it, today) }
        if (todayIndex >= 0) {
            Box(
                modifier = Modifier
                    .offset(x = (todayIndex * columnWidth.value + columnWidth.value / 2).dp)
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(KlarityColors.AccentPrimary.copy(alpha = 0.5f))
            )
        }
        
        // Task bar
        if (barWidth > 0.dp) {
            Box(
                modifier = Modifier
                    .offset(x = barOffset)
                    .width(barWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when (task.status) {
                            TaskStatus.DONE -> KlarityColors.AccentSecondary.copy(alpha = 0.3f)
                            TaskStatus.IN_PROGRESS -> KlarityColors.AccentPrimary.copy(alpha = 0.5f)
                            else -> Color(task.priority.color).copy(alpha = 0.3f)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = when (task.status) {
                            TaskStatus.DONE -> KlarityColors.AccentSecondary
                            TaskStatus.IN_PROGRESS -> KlarityColors.AccentPrimary
                            else -> Color(task.priority.color)
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable(onClick = onClick)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Progress bar for tasks with subtasks
                if (task.subtasks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(task.progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (task.status) {
                                    TaskStatus.DONE -> KlarityColors.AccentSecondary
                                    TaskStatus.IN_PROGRESS -> KlarityColors.AccentPrimary
                                    else -> Color(task.priority.color)
                                }.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }
}

// ============================================================================
// Utility Functions
// ============================================================================

private fun calculateDateRange(
    tasks: List<Task>,
    today: Instant,
    scale: TimelineScale
): List<Instant> {
    // Find the earliest and latest dates
    val allDates = tasks.flatMap { task ->
        listOfNotNull(task.startDate, task.dueDate, task.createdAt)
    }
    
    val earliest = allDates.minOrNull() ?: today
    val latest = allDates.maxOrNull() ?: today
    
    // Extend range to include some buffer
    val startDate = earliest - kotlin.time.Duration.parse("${scale.daysPerUnit * 2}d")
    val endDate = latest + kotlin.time.Duration.parse("${scale.daysPerUnit * 5}d")
    
    // Generate date list based on scale
    val dates = mutableListOf<Instant>()
    var current = startDate
    while (current <= endDate) {
        dates.add(current)
        current = current + kotlin.time.Duration.parse("${scale.daysPerUnit}d")
    }
    return dates
}

private fun formatDateHeader(instant: Instant, scale: TimelineScale): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return when (scale) {
        TimelineScale.DAY -> "${dateTime.dayOfMonth}"
        TimelineScale.WEEK -> "W${dateTime.dayOfYear / 7 + 1}"
        TimelineScale.MONTH -> dateTime.month.name.take(3)
        TimelineScale.QUARTER -> "Q${(dateTime.monthNumber - 1) / 3 + 1}"
    }
}

private fun isToday(instant: Instant, today: Instant): Boolean {
    val date1 = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val date2 = today.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return date1 == date2
}
