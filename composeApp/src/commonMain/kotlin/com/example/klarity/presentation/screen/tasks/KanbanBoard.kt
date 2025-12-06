package com.example.klarity.presentation.screen.tasks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.klarity.presentation.theme.KlarityColors
import kotlinx.datetime.*
import kotlin.math.roundToInt

/**
 * TaskFlow Kanban Board
 * 
 * Desktop Design: "TaskFlow: Kanban + Timeline"
 * - Horizontally scrollable columns
 * - Draggable task cards between columns
 * - WIP limits per column
 * - Priority indicators
 * - Quick actions on hover
 */

@Composable
fun KanbanBoard(
    columns: List<KanbanColumn>,
    onTaskMove: (taskId: String, fromStatus: TaskStatus, toStatus: TaskStatus, newIndex: Int) -> Unit,
    onTaskClick: (Task) -> Unit,
    onTaskCreate: (TaskStatus) -> Unit,
    onTaskDelete: (Task) -> Unit,
    onTaskToggleComplete: (Task) -> Unit,
    onColumnCollapse: (TaskStatus, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragState by remember { mutableStateOf(DragState()) }
    
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(KlarityColors.BgPrimary)
            .horizontalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        columns.forEach { column ->
            KanbanColumnView(
                column = column,
                dragState = dragState,
                onDragStart = { task ->
                    dragState = DragState(
                        isDragging = true,
                        draggedTaskId = task.id,
                        sourceColumn = column.status
                    )
                },
                onDragEnd = {
                    if (dragState.targetColumn != null && dragState.targetColumn != dragState.sourceColumn) {
                        dragState.draggedTaskId?.let { taskId ->
                            onTaskMove(
                                taskId,
                                dragState.sourceColumn!!,
                                dragState.targetColumn!!,
                                dragState.targetIndex ?: 0
                            )
                        }
                    }
                    dragState = DragState()
                },
                onDragCancel = {
                    dragState = DragState()
                },
                onDropTargetEnter = { status, index ->
                    dragState = dragState.copy(targetColumn = status, targetIndex = index)
                },
                onTaskClick = onTaskClick,
                onTaskCreate = { onTaskCreate(column.status) },
                onTaskDelete = onTaskDelete,
                onTaskToggleComplete = onTaskToggleComplete,
                onColumnCollapse = { collapsed -> onColumnCollapse(column.status, collapsed) },
                modifier = Modifier.width(320.dp)
            )
        }
        
        // Add Column Button
        AddColumnButton(
            onClick = { /* TODO: Add custom column */ }
        )
    }
}

@Composable
private fun KanbanColumnView(
    column: KanbanColumn,
    dragState: DragState,
    onDragStart: (Task) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onDropTargetEnter: (TaskStatus, Int) -> Unit,
    onTaskClick: (Task) -> Unit,
    onTaskCreate: () -> Unit,
    onTaskDelete: (Task) -> Unit,
    onTaskToggleComplete: (Task) -> Unit,
    onColumnCollapse: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDropTarget = dragState.isDragging && dragState.targetColumn == column.status
    val borderColor by animateColorAsState(
        targetValue = if (isDropTarget) KlarityColors.AccentPrimary else Color.Transparent,
        animationSpec = tween(150)
    )
    
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(KlarityColors.BgSecondary)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        // Column Header
        ColumnHeader(
            column = column,
            isCollapsed = column.isCollapsed,
            onCollapseToggle = onColumnCollapse,
            onAddTask = onTaskCreate
        )
        
        // Column Content
        AnimatedVisibility(
            visible = !column.isCollapsed,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(column.tasks) { index, task ->
                    // Drop zone indicator before each task
                    if (dragState.isDragging && 
                        dragState.targetColumn == column.status &&
                        dragState.targetIndex == index) {
                        DropZoneIndicator()
                    }
                    
                    TaskCard(
                        task = task,
                        isDragging = dragState.draggedTaskId == task.id,
                        onDragStart = { onDragStart(task) },
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragCancel,
                        onClick = { onTaskClick(task) },
                        onDelete = { onTaskDelete(task) },
                        onToggleComplete = { onTaskToggleComplete(task) },
                        onHoverIndex = { onDropTargetEnter(column.status, index) }
                    )
                }
                
                // Drop zone at end of column
                item {
                    if (dragState.isDragging) {
                        DropZoneIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .pointerInput(Unit) {
                                    // Detect hover
                                }
                        )
                    }
                }
                
                // Add task button at bottom
                item {
                    AddTaskButton(onClick = onTaskCreate)
                }
            }
        }
    }
}

@Composable
private fun ColumnHeader(
    column: KanbanColumn,
    isCollapsed: Boolean,
    onCollapseToggle: (Boolean) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val taskCount = column.tasks.size
    val wipLimitExceeded = column.isOverWipLimit
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(KlarityColors.BgTertiary)
            .clickable { onCollapseToggle(!isCollapsed) }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status emoji
            Text(
                text = column.status.emoji,
                fontSize = 16.sp
            )
            
            // Column title
            Text(
                text = column.status.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = KlarityColors.TextPrimary
            )
            
            // Task count badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (wipLimitExceeded) Color(0xFFFF5252).copy(alpha = 0.2f)
                        else KlarityColors.BgElevated
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (column.wipLimit != null) "$taskCount/${column.wipLimit}" else "$taskCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (wipLimitExceeded) Color(0xFFFF5252) else KlarityColors.TextSecondary
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add task button
            IconButton(
                onClick = onAddTask,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add task",
                    tint = KlarityColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Collapse/Expand icon
            Icon(
                imageVector = if (isCollapsed) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isCollapsed) "Expand" else "Collapse",
                tint = KlarityColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    onHoverIndex: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isHovered by remember { mutableStateOf(false) }
    
    val elevation by animateFloatAsState(
        targetValue = if (isDragging) 8f else if (isHovered) 4f else 0f,
        animationSpec = tween(150)
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.05f else 1f,
        animationSpec = tween(150)
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = elevation
            }
            .zIndex(if (isDragging) 10f else 0f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        onDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    },
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                        onDragEnd()
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                        onDragCancel()
                    }
                )
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) KlarityColors.BgElevated else KlarityColors.BgCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Priority & Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority indicator
                PriorityBadge(priority = task.priority)
                
                // Due date if exists
                task.dueDate?.let { dueDate ->
                    DueDateBadge(
                        dueDate = dueDate,
                        isOverdue = task.isOverdue
                    )
                }
            }
            
            // Task Title
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (task.status == TaskStatus.DONE) 
                    KlarityColors.TextTertiary else KlarityColors.TextPrimary,
                textDecoration = if (task.status == TaskStatus.DONE) 
                    TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Description preview (if exists)
            if (task.description.isNotEmpty()) {
                Text(
                    text = task.description.take(100),
                    style = MaterialTheme.typography.bodySmall,
                    color = KlarityColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Tags
            if (task.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    task.tags.take(3).forEach { tag ->
                        TagChip(tag = tag)
                    }
                    if (task.tags.size > 3) {
                        Text(
                            text = "+${task.tags.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = KlarityColors.TextTertiary
                        )
                    }
                }
            }
            
            // Subtasks progress
            if (task.subtasks.isNotEmpty()) {
                SubtaskProgress(
                    completed = task.subtasks.count { it.isCompleted },
                    total = task.subtasks.size,
                    progress = task.progress
                )
            }
            
            // Footer: Assignee, Links, Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Assignee avatar
                    task.assignee?.let { assignee ->
                        AssigneeAvatar(name = assignee)
                    }
                    
                    // Linked notes indicator
                    if (task.linkedNoteIds.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔗", fontSize = 12.sp)
                            Text(
                                text = "${task.linkedNoteIds.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = KlarityColors.TextTertiary
                            )
                        }
                    }
                }
                
                // Quick actions (visible on hover)
                AnimatedVisibility(visible = isHovered || isDragging) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onToggleComplete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (task.status == TaskStatus.DONE) 
                                    Icons.Default.Refresh else Icons.Default.Check,
                                contentDescription = "Toggle complete",
                                tint = KlarityColors.AccentSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityBadge(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(priority.color).copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = priority.emoji, fontSize = 10.sp)
        Text(
            text = priority.label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(priority.color),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DueDateBadge(
    dueDate: Instant,
    isOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isOverdue) Color(0xFFFF5252) else KlarityColors.TextSecondary
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isOverdue) "⚠️" else "📅",
            fontSize = 10.sp
        )
        Text(
            text = formatDueDate(dueDate),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun TagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(KlarityColors.AccentPrimary.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = KlarityColors.AccentPrimary
        )
    }
}

@Composable
private fun SubtaskProgress(
    completed: Int,
    total: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "✓",
            fontSize = 12.sp,
            color = KlarityColors.AccentSecondary
        )
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = KlarityColors.AccentSecondary,
            trackColor = KlarityColors.BgTertiary,
        )
        
        Text(
            text = "$completed/$total",
            style = MaterialTheme.typography.labelSmall,
            color = KlarityColors.TextTertiary
        )
    }
}

@Composable
private fun AssigneeAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(KlarityColors.AccentPrimary.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = KlarityColors.AccentPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DropZoneIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(KlarityColors.AccentPrimary)
    )
}

@Composable
private fun AddTaskButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add task",
            tint = KlarityColors.TextTertiary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Add task",
            style = MaterialTheme.typography.bodySmall,
            color = KlarityColors.TextTertiary
        )
    }
}

@Composable
private fun AddColumnButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = KlarityColors.TextTertiary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add column",
            tint = KlarityColors.TextTertiary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add Column",
            style = MaterialTheme.typography.bodyMedium,
            color = KlarityColors.TextTertiary
        )
    }
}

// ============================================================================
// Utility Functions
// ============================================================================

private fun formatDueDate(instant: Instant): String {
    val now = Clock.System.now()
    val duration = instant - now
    
    return when {
        duration.inWholeDays < -1 -> "${-duration.inWholeDays}d overdue"
        duration.inWholeDays == -1L -> "Yesterday"
        duration.inWholeDays == 0L -> "Today"
        duration.inWholeDays == 1L -> "Tomorrow"
        duration.inWholeDays < 7 -> "${duration.inWholeDays}d"
        duration.inWholeDays < 30 -> "${duration.inWholeDays / 7}w"
        else -> "${duration.inWholeDays / 30}mo"
    }
}
