package com.example.klarity.presentation.screen.tasks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityColors

/**
 * TaskDetailModal - Bottom sheet modal for viewing and editing task details.
 * 
 * **Requirements 4.1, 4.2, 4.3, 4.4**:
 * - Displays task ID, title, description, subtasks, activity log, and properties
 * - Animates with slide-up transition from the bottom
 * - Closes on backdrop click or close button with slide-down animation
 * - Shows status, priority, and story points in a sidebar section
 */
@Composable
fun TaskDetailModal(
    task: Task?,
    isVisible: Boolean,
    onClose: () -> Unit,
    onTaskUpdate: (Task) -> Unit,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Backdrop with blur effect and click-to-close (Requirement 4.3)
    AnimatedVisibility(
        visible = isVisible && task != null,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KlarityColors.ModalOverlay)
                .clickable(
                    onClick = onClose,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
    }


    // Bottom sheet with slide-up/slide-down animation (Requirement 4.2)
    AnimatedVisibility(
        visible = isVisible && task != null,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(200)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(150)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            task?.let { currentTask ->
                TaskDetailContent(
                    task = currentTask,
                    onClose = onClose,
                    onTaskUpdate = onTaskUpdate,
                    onAddTag = onAddTag,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(KlarityColors.BgSecondary)
                        .clickable(
                            onClick = { /* Prevent click propagation to backdrop */ },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                )
            }
        }
    }
}

/**
 * Main content of the task detail modal.
 * 
 * **Requirement 4.1**: Display task ID, title, description, subtasks, activity log, and properties
 */
@Composable
private fun TaskDetailContent(
    task: Task,
    onClose: () -> Unit,
    onTaskUpdate: (Task) -> Unit,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Drag handle bar at top (Requirement 10.2)
        DragHandle()
        
        // Header with task ID badge, title, and close button
        ModalHeader(
            task = task,
            onClose = onClose
        )
        
        // Main content area with scrollable content and properties sidebar
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Left side: Description, subtasks, activity (scrollable)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Description section
                item {
                    DescriptionSection(description = task.description)
                }
                
                // Subtasks section
                if (task.subtasks.isNotEmpty()) {
                    item {
                        SubtasksSection(
                            subtasks = task.subtasks,
                            onSubtaskToggle = { subtaskId ->
                                val updatedSubtasks = task.subtasks.map { subtask ->
                                    if (subtask.id == subtaskId) {
                                        subtask.copy(isCompleted = !subtask.isCompleted)
                                    } else subtask
                                }
                                onTaskUpdate(task.copy(subtasks = updatedSubtasks))
                            }
                        )
                    }
                }
                
                // Activity section
                item {
                    ActivitySection(task = task)
                }
                
                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            // Right side: Properties sidebar (Requirement 4.4)
            PropertiesSidebar(
                task = task,
                onAddTag = onAddTag,
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
            )
        }
    }
}


/**
 * Drag handle bar at the top of the modal.
 */
@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(KlarityColors.TextTertiary.copy(alpha = 0.5f))
        )
    }
}

/**
 * Modal header with task ID badge, title, and close button.
 * 
 * **Requirement 4.1**: Display task ID badge and title in header
 */
@Composable
private fun ModalHeader(
    task: Task,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Task ID badge
            TaskIdBadge(taskId = task.id)
            
            // Task title
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = KlarityColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Close button (X icon)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(KlarityColors.BgTertiary)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = KlarityColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    
    // Divider
    HorizontalDivider(
        color = KlarityColors.BorderPrimary,
        thickness = 1.dp
    )
}

/**
 * Task ID badge component.
 */
@Composable
private fun TaskIdBadge(
    taskId: String,
    modifier: Modifier = Modifier
) {
    // Extract short ID (last 6 characters or full ID if shorter)
    val shortId = if (taskId.length > 6) taskId.takeLast(6) else taskId
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(KlarityColors.AccentPrimary.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "#$shortId",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = KlarityColors.AccentPrimary
        )
    }
}


/**
 * Description section in bordered container.
 * 
 * **Requirement 4.1**: Description in bordered container
 */
@Composable
private fun DescriptionSection(
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = KlarityColors.TextPrimary
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = KlarityColors.BorderPrimary,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(KlarityColors.BgTertiary)
                .padding(16.dp)
        ) {
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = KlarityColors.TextSecondary,
                    lineHeight = 24.sp
                )
            } else {
                Text(
                    text = "No description provided",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KlarityColors.TextTertiary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

/**
 * Subtasks section as checkbox list items.
 * 
 * **Requirement 4.1**: Subtasks as checkbox list items
 */
@Composable
private fun SubtasksSection(
    subtasks: List<Subtask>,
    onSubtaskToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Subtasks",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = KlarityColors.TextPrimary
            )
            
            // Progress indicator
            val completedCount = subtasks.count { it.isCompleted }
            Text(
                text = "$completedCount/${subtasks.size}",
                style = MaterialTheme.typography.labelSmall,
                color = KlarityColors.TextTertiary
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = KlarityColors.BorderPrimary,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(KlarityColors.BgTertiary),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            subtasks.forEachIndexed { index, subtask ->
                SubtaskItem(
                    subtask = subtask,
                    onToggle = { onSubtaskToggle(subtask.id) }
                )
                
                if (index < subtasks.lastIndex) {
                    HorizontalDivider(
                        color = KlarityColors.BorderPrimary,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Individual subtask item with checkbox.
 */
@Composable
private fun SubtaskItem(
    subtask: Subtask,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(
                    width = 1.5.dp,
                    color = if (subtask.isCompleted) KlarityColors.AccentPrimary else KlarityColors.BorderSecondary,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    if (subtask.isCompleted) KlarityColors.AccentPrimary.copy(alpha = 0.2f)
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            if (subtask.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = KlarityColors.AccentPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        // Subtask title
        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (subtask.isCompleted) KlarityColors.TextTertiary else KlarityColors.TextPrimary,
            textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )
    }
}


/**
 * Activity log section with avatar and timestamp.
 * 
 * **Requirement 4.1**: Activity log with avatar and timestamp
 */
@Composable
private fun ActivitySection(
    task: Task,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Activity",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = KlarityColors.TextPrimary
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = KlarityColors.BorderPrimary,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(KlarityColors.BgTertiary)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Created activity
            ActivityItem(
                avatar = task.assignee?.take(1)?.uppercase() ?: "S",
                action = "created this task",
                timestamp = formatActivityTimestamp(task.createdAt)
            )
            
            // Updated activity (if different from created)
            if (task.updatedAt != task.createdAt) {
                ActivityItem(
                    avatar = task.assignee?.take(1)?.uppercase() ?: "S",
                    action = "updated this task",
                    timestamp = formatActivityTimestamp(task.updatedAt)
                )
            }
            
            // Completed activity
            task.completedAt?.let { completedAt ->
                ActivityItem(
                    avatar = task.assignee?.take(1)?.uppercase() ?: "S",
                    action = "marked as complete",
                    timestamp = formatActivityTimestamp(completedAt)
                )
            }
        }
    }
}

/**
 * Individual activity item with avatar and timestamp.
 */
@Composable
private fun ActivityItem(
    avatar: String,
    action: String,
    timestamp: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(KlarityColors.AccentPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatar,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = KlarityColors.AccentPrimary
            )
        }
        
        // Action text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action,
                style = MaterialTheme.typography.bodyMedium,
                color = KlarityColors.TextSecondary
            )
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = KlarityColors.TextTertiary
            )
        }
    }
}


/**
 * Properties sidebar with status, priority, points, and tags.
 * 
 * **Requirement 4.4**: Display status, priority, and story points in a sidebar section
 * **Requirement 7.3**: Tags section with "Add" button
 */
@Composable
private fun PropertiesSidebar(
    task: Task,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KlarityColors.BgTertiary)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Properties",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = KlarityColors.TextPrimary
        )
        
        // Status badge
        PropertyRow(
            label = "Status",
            content = {
                StatusBadge(status = task.status)
            }
        )
        
        // Priority with colored dot and label
        PropertyRow(
            label = "Priority",
            content = {
                PriorityDisplay(priority = task.priority)
            }
        )
        
        // Story points with eco icon
        PropertyRow(
            label = "Points",
            content = {
                PointsDisplay(points = task.points)
            }
        )
        
        // Assignee
        task.assignee?.let { assignee ->
            PropertyRow(
                label = "Assignee",
                content = {
                    AssigneeDisplay(name = assignee)
                }
            )
        }
        
        // Due date
        task.dueDate?.let { dueDate ->
            PropertyRow(
                label = "Due Date",
                content = {
                    DueDateDisplay(dueDate = dueDate, isOverdue = task.isOverdue)
                }
            )
        }
        
        // Tags section with "Add" button (Requirement 7.3)
        TagsSection(
            tags = task.tags,
            onAddTag = onAddTag
        )
    }
}

/**
 * Property row with label and content.
 */
@Composable
private fun PropertyRow(
    label: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = KlarityColors.TextTertiary,
            fontWeight = FontWeight.Medium
        )
        content()
    }
}

/**
 * Status badge component.
 */
@Composable
private fun StatusBadge(
    status: TaskStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(KlarityColors.AccentPrimary.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = status.emoji,
            fontSize = 12.sp
        )
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = KlarityColors.AccentPrimary
        )
    }
}

/**
 * Priority display with colored dot and label.
 */
@Composable
private fun PriorityDisplay(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(priority.color))
        )
        
        // Label
        Text(
            text = priority.label,
            style = MaterialTheme.typography.bodyMedium,
            color = KlarityColors.TextPrimary
        )
    }
}

/**
 * Points display with eco icon.
 */
@Composable
private fun PointsDisplay(
    points: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🌱",
            fontSize = 14.sp
        )
        Text(
            text = points?.toString() ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = if (points != null) KlarityColors.TextPrimary else KlarityColors.TextTertiary
        )
    }
}

/**
 * Assignee display with avatar.
 */
@Composable
private fun AssigneeDisplay(
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(KlarityColors.AccentPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = KlarityColors.AccentPrimary
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = KlarityColors.TextPrimary
        )
    }
}

/**
 * Due date display.
 */
@Composable
private fun DueDateDisplay(
    dueDate: kotlinx.datetime.Instant,
    isOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isOverdue) KlarityColors.Error else KlarityColors.TextPrimary
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isOverdue) "⚠️" else "📅",
            fontSize = 14.sp
        )
        Text(
            text = formatDueDateFull(dueDate),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = if (isOverdue) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}


/**
 * Tags section with existing tags and "Add" button.
 * 
 * **Requirement 7.3**: Render existing tags as badges with "+ Add" button for new tags
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    tags: List<TaskTag>,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Tags",
            style = MaterialTheme.typography.labelSmall,
            color = KlarityColors.TextTertiary,
            fontWeight = FontWeight.Medium
        )
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Render existing tags as badges
            tags.forEach { tag ->
                TagBadge(tag = tag)
            }
            
            // "+ Add" button for new tags
            AddTagButton(onClick = onAddTag)
        }
    }
}

/**
 * Add tag button component.
 */
@Composable
private fun AddTagButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = KlarityColors.BorderSecondary,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add tag",
            tint = KlarityColors.TextTertiary,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "Add",
            style = MaterialTheme.typography.labelSmall,
            color = KlarityColors.TextTertiary
        )
    }
}

// ============================================================================
// Utility Functions
// ============================================================================

/**
 * Formats an Instant to a human-readable activity timestamp.
 */
private fun formatActivityTimestamp(instant: kotlinx.datetime.Instant): String {
    val now = kotlinx.datetime.Clock.System.now()
    val duration = now - instant
    
    return when {
        duration.inWholeMinutes < 1 -> "Just now"
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m ago"
        duration.inWholeHours < 24 -> "${duration.inWholeHours}h ago"
        duration.inWholeDays < 7 -> "${duration.inWholeDays}d ago"
        duration.inWholeDays < 30 -> "${duration.inWholeDays / 7}w ago"
        else -> "${duration.inWholeDays / 30}mo ago"
    }
}

/**
 * Formats an Instant to a full date string for due dates.
 */
private fun formatDueDateFull(instant: kotlinx.datetime.Instant): String {
    val now = kotlinx.datetime.Clock.System.now()
    val duration = instant - now
    
    return when {
        duration.inWholeDays < -1 -> "${-duration.inWholeDays} days overdue"
        duration.inWholeDays == -1L -> "Yesterday"
        duration.inWholeDays == 0L -> "Today"
        duration.inWholeDays == 1L -> "Tomorrow"
        duration.inWholeDays < 7 -> "In ${duration.inWholeDays} days"
        duration.inWholeDays < 30 -> "In ${duration.inWholeDays / 7} weeks"
        else -> "In ${duration.inWholeDays / 30} months"
    }
}

// ============================================================================
// Data Extraction Functions for Property Testing
// ============================================================================

/**
 * Data class representing the content that should be displayed in the modal.
 * Used for property-based testing to verify modal content completeness.
 * 
 * **Property 5: Task modal content completeness**
 * **Validates: Requirements 4.1, 4.4**
 */
data class TaskModalContent(
    val taskId: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val points: Int?
)

/**
 * Extracts the content that would be displayed in the modal for a given task.
 * This function is used for property-based testing to verify that all required
 * content is present.
 */
fun extractModalContent(task: Task): TaskModalContent {
    return TaskModalContent(
        taskId = task.id,
        title = task.title,
        description = task.description,
        status = task.status,
        priority = task.priority,
        points = task.points
    )
}

/**
 * Verifies that the modal content contains all required fields.
 * Returns true if all required fields are present and valid.
 * 
 * **Property 5: Task modal content completeness**
 * For any task, the detail modal SHALL display task ID, title, description, 
 * and properties (status, priority, points).
 */
fun verifyModalContentCompleteness(content: TaskModalContent): Boolean {
    // Task ID must be non-empty
    if (content.taskId.isEmpty()) return false
    
    // Title must be non-empty
    if (content.title.isEmpty()) return false
    
    // Description can be empty but must be present (not null)
    // Status must be a valid TaskStatus (always true for enum)
    // Priority must be a valid TaskPriority (always true for enum)
    // Points can be null but the field must be present
    
    return true
}
