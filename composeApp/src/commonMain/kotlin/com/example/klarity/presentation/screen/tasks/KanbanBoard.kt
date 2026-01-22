package com.example.klarity.presentation.screen.tasks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
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
    highlightedColumns: Set<TaskStatus> = emptySet(),
    modifier: Modifier = Modifier
) {
    var dragState by remember { mutableStateOf(DragState()) }
    
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                isHighlighted = column.status in highlightedColumns,
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
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isDropTarget = dragState.isDragging && dragState.targetColumn == column.status
    val borderColor by animateColorAsState(
        targetValue = when {
            isDropTarget -> MaterialTheme.colorScheme.primary
            isHighlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else -> Color.Transparent
        },
        animationSpec = tween(150)
    )
    
    // Apply dashed border for highlighted columns (like AI suggestion grouping)
    val borderModifier = if (isHighlighted && !isDropTarget) {
        Modifier.dashedBorder(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            shape = RoundedCornerShape(12.dp),
            dashLength = 8.dp,
            gapLength = 4.dp
        )
    } else {
        Modifier.border(
            width = 2.dp,
            color = borderColor,
            shape = RoundedCornerShape(12.dp)
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(borderModifier)
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
                // Empty column placeholder (Requirement 2.2)
                if (column.tasks.isEmpty()) {
                    item {
                        EmptyColumnPlaceholder(onClick = onTaskCreate)
                    }
                }
                
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
                
                // Add task button at bottom (only show if column has tasks)
                if (column.tasks.isNotEmpty()) {
                    item {
                        AddTaskButton(onClick = onTaskCreate)
                    }
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
            .background(MaterialTheme.colorScheme.surface)
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
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Task count badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (wipLimitExceeded) Color(0xFFFF5252).copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.secondary
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (column.wipLimit != null) "$taskCount/${column.wipLimit}" else "$taskCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (wipLimitExceeded) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface
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
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Collapse/Expand icon
            Icon(
                imageVector = if (isCollapsed) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isCollapsed) "Expand" else "Collapse",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
    
    // Pulsing glow animation for active timer (Property 6.3)
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    // Apply opacity when task is completed (Requirement 3.3)
    val cardAlpha = if (task.completed) 0.6f else 1f
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = elevation
                alpha = cardAlpha
            }
            .zIndex(if (isDragging) 10f else 0f)
            // Apply pulsing glow shadow when task has active timer
            .then(
                if (task.isActive && task.timer != null) {
                    Modifier.shadow(
                        elevation = (8 * glowAlpha).dp,
                        shape = RoundedCornerShape(10.dp),
                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
                    )
                } else Modifier
            )
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
            containerColor = if (isDragging) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Priority & Labels Row with priority dot at top-right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority badge (left side)
                    PriorityBadge(priority = task.priority)
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Story points display with eco icon (Requirement 1.4)
                        task.points?.let { points ->
                            StoryPointsBadge(points = points)
                        }
                        
                        // Priority color dot indicator (top-right position, Requirement 8.1)
                        PriorityDot(priority = task.priority)
                    }
                }
                
                // Timer display when task has active timer (Requirement 6.1)
                task.timer?.let { timer ->
                    TimerDisplay(timer = timer, isActive = task.isActive)
                }
                
                // Task Title with strikethrough when completed (Requirement 3.3)
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (task.completed || task.status == TaskStatus.DONE) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.completed || task.status == TaskStatus.DONE)
                        TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Description preview (if exists)
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description.take(100),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Due date if exists
                task.dueDate?.let { dueDate ->
                    DueDateBadge(
                        dueDate = dueDate,
                        isOverdue = task.isOverdue
                    )
                }
                
                // Tags - Render all tags in wrapping FlowRow layout (Requirement 7.2)
                if (task.tags.isNotEmpty()) {
                    TagsFlowRow(tags = task.tags)
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
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
                                    imageVector = if (task.completed || task.status == TaskStatus.DONE) 
                                        Icons.Default.Refresh else Icons.Default.Check,
                                    contentDescription = "Toggle complete",
                                    tint = MaterialTheme.colorScheme.primary,
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
            
            // Checkbox overlay for completion toggle (positioned at top-right corner)
            // Requirement 3.2: Toggle completion without opening modal
            CompletionCheckbox(
                isCompleted = task.completed || task.status == TaskStatus.DONE,
                onToggle = onToggleComplete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
    }
}

/**
 * Checkbox overlay for task completion toggle (Requirement 3.2)
 * Positioned at top-right corner with click handling that doesn't propagate to card
 */
@Composable
private fun CompletionCheckbox(
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.5.dp,
                color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .clickable(
                onClick = onToggle,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
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

/**
 * Priority color dot indicator (Requirement 8.1, 8.2, 8.3, 8.4)
 * Displays a colored dot based on priority level:
 * - HIGH = red
 * - MEDIUM = yellow
 * - LOW = blue
 */
@Composable
private fun PriorityDot(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color(priority.color))
    )
}

/**
 * Story points display with eco icon (Requirement 1.4)
 */
@Composable
private fun StoryPointsBadge(
    points: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🌱",
            fontSize = 10.sp
        )
        Text(
            text = "$points",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Timer display showing elapsed time (Requirement 6.1)
 */
@Composable
private fun TimerDisplay(
    timer: TaskTimer,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isActive) "⏱️" else "⏸️",
                fontSize = 12.sp
            )
            Text(
                text = timer.formattedTime(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DueDateBadge(
    dueDate: Instant,
    isOverdue: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isOverdue) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurface

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
    color: TagColor = TagColor.GRAY,
    modifier: Modifier = Modifier
) {
    val tagColor = Color(color.textColor)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(tagColor.copy(alpha = color.bgAlpha))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = tagColor
        )
    }
}

/**
 * TagBadge composable for rendering task tags with colored styling.
 * 
 * Accepts a TaskTag with label and color, applies background alpha and text color
 * from TagColor enum, uses rounded corners and padding.
 * 
 * **Requirement 7.1**: Display each tag as a colored badge with label text
 */
@Composable
fun TagBadge(
    tag: TaskTag,
    modifier: Modifier = Modifier
) {
    val tagColor = Color(tag.colorClass.textColor)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(tagColor.copy(alpha = tag.colorClass.bgAlpha))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = tag.label,
            style = MaterialTheme.typography.labelSmall,
            color = tagColor
        )
    }
}

/**
 * Renders all tags in a wrapping FlowRow layout.
 * 
 * **Requirement 7.2**: Display all tags in a wrapping layout, handle overflow gracefully
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsFlowRow(
    tags: List<TaskTag>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tags.forEach { tag ->
            TagBadge(tag = tag)
        }
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
            color = MaterialTheme.colorScheme.primary
        )
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        
        Text(
            text = "$completed/$total",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun AssigneeAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(24.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
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
            .background(MaterialTheme.colorScheme.primary)
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
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "Add task",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

/**
 * Empty column placeholder (Requirement 2.2)
 * 
 * Shows "Add task" placeholder when column has no tasks.
 * Styled with dashed border and muted text to indicate tasks can be added.
 */
@Composable
fun EmptyColumnPlaceholder(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .dashedBorder(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                dashLength = 8.dp,
                gapLength = 4.dp
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add task",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Add task",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Extension function to draw a dashed border around a composable.
 */
private fun Modifier.dashedBorder(
    width: androidx.compose.ui.unit.Dp,
    color: Color,
    shape: RoundedCornerShape,
    dashLength: androidx.compose.ui.unit.Dp,
    gapLength: androidx.compose.ui.unit.Dp
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        val strokeWidth = width.toPx()
        val dash = dashLength.toPx()
        val gap = gapLength.toPx()
        
        drawRoundRect(
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(dash, gap),
                    0f
                )
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                shape.topStart.toPx(size, this),
                shape.topStart.toPx(size, this)
            )
        )
    }
)

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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
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
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add Column",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
