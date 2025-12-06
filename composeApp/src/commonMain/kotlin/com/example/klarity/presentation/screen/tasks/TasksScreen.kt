package com.example.klarity.presentation.screen.tasks

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityColors
import kotlinx.datetime.Clock

/**
 * TasksScreen - Main TaskFlow Interface
 * 
 * Desktop Design: "TaskFlow: Kanban + Timeline"
 * Combines Kanban board and Timeline views with
 * filtering, search, and view switching.
 */

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier
) {
    // State
    var viewMode by remember { mutableStateOf(TaskViewMode.KANBAN) }
    var timelineScale by remember { mutableStateOf(TimelineScale.WEEK) }
    var filter by remember { mutableStateOf(TaskFilter()) }
    var sortOption by remember { mutableStateOf(TaskSortOption.PRIORITY) }
    var showFilterPanel by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showTaskEditor by remember { mutableStateOf(false) }
    
    // Sample data - in real app, this comes from ViewModel
    var columns by remember { mutableStateOf(createSampleKanbanColumns()) }
    val allTasks = columns.flatMap { it.tasks }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KlarityColors.BgPrimary)
    ) {
        // Top Toolbar
        TasksToolbar(
            viewMode = viewMode,
            onViewModeChange = { viewMode = it },
            filter = filter,
            onFilterChange = { filter = it },
            sortOption = sortOption,
            onSortOptionChange = { sortOption = it },
            showFilterPanel = showFilterPanel,
            onToggleFilterPanel = { showFilterPanel = !showFilterPanel },
            onCreateTask = { 
                selectedTask = null
                showTaskEditor = true 
            }
        )
        
        // Filter Panel (Collapsible)
        AnimatedVisibility(
            visible = showFilterPanel,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FilterPanel(
                filter = filter,
                onFilterChange = { filter = it },
                allTags = allTasks.flatMap { it.tags }.distinct(),
                allAssignees = allTasks.mapNotNull { it.assignee }.distinct()
            )
        }
        
        // Main Content
        Box(modifier = Modifier.weight(1f)) {
            when (viewMode) {
                TaskViewMode.KANBAN -> {
                    KanbanBoard(
                        columns = columns,
                        onTaskMove = { taskId, fromStatus, toStatus, newIndex ->
                            columns = moveTask(columns, taskId, fromStatus, toStatus, newIndex)
                        },
                        onTaskClick = { task ->
                            selectedTask = task
                            showTaskEditor = true
                        },
                        onTaskCreate = { status ->
                            // Create new task in column
                            val newTask = Task(
                                id = "task-${System.currentTimeMillis()}",
                                title = "New Task",
                                status = status,
                                createdAt = Clock.System.now(),
                                updatedAt = Clock.System.now()
                            )
                            columns = addTaskToColumn(columns, newTask, status)
                            selectedTask = newTask
                            showTaskEditor = true
                        },
                        onTaskDelete = { task ->
                            columns = removeTaskFromColumn(columns, task)
                        },
                        onTaskToggleComplete = { task ->
                            columns = toggleTaskComplete(columns, task)
                        },
                        onColumnCollapse = { status, collapsed ->
                            columns = toggleColumnCollapse(columns, status, collapsed)
                        }
                    )
                }
                
                TaskViewMode.TIMELINE -> {
                    TaskTimeline(
                        tasks = allTasks,
                        scale = timelineScale,
                        onScaleChange = { timelineScale = it },
                        onTaskClick = { task ->
                            selectedTask = task
                            showTaskEditor = true
                        },
                        onTaskMove = { task, newStart, newDue ->
                            // Update task dates
                        }
                    )
                }
                
                TaskViewMode.LIST -> {
                    TaskListView(
                        tasks = allTasks,
                        sortOption = sortOption,
                        onTaskClick = { task ->
                            selectedTask = task
                            showTaskEditor = true
                        },
                        onTaskToggleComplete = { task ->
                            columns = toggleTaskComplete(columns, task)
                        }
                    )
                }
                
                TaskViewMode.CALENDAR -> {
                    // Calendar view placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🗓️ Calendar View Coming Soon",
                            style = MaterialTheme.typography.titleMedium,
                            color = KlarityColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
    
    // Task Editor Side Panel
    AnimatedVisibility(
        visible = showTaskEditor,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
    ) {
        TaskEditorPanel(
            task = selectedTask,
            onSave = { updatedTask ->
                columns = updateTaskInColumns(columns, updatedTask)
                showTaskEditor = false
            },
            onClose = { showTaskEditor = false },
            onDelete = { task ->
                columns = removeTaskFromColumn(columns, task)
                showTaskEditor = false
            }
        )
    }
}

@Composable
private fun TasksToolbar(
    viewMode: TaskViewMode,
    onViewModeChange: (TaskViewMode) -> Unit,
    filter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    sortOption: TaskSortOption,
    onSortOptionChange: (TaskSortOption) -> Unit,
    showFilterPanel: Boolean,
    onToggleFilterPanel: () -> Unit,
    onCreateTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(KlarityColors.BgSecondary)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🧩",
                fontSize = 18.sp
            )
            Text(
                text = "TaskFlow",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = KlarityColors.TextPrimary
            )
        }
        
        // View Mode Selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TaskViewMode.entries.forEach { mode ->
                FilterChip(
                    selected = viewMode == mode,
                    onClick = { onViewModeChange(mode) },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = mode.emoji)
                            Text(text = mode.label)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KlarityColors.AccentPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = KlarityColors.AccentPrimary
                    )
                )
            }
        }
        
        // Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter toggle
            IconButton(onClick = onToggleFilterPanel) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Filters",
                    tint = if (showFilterPanel) KlarityColors.AccentPrimary else KlarityColors.TextSecondary
                )
            }
            
            // Search
            IconButton(onClick = { /* Open search */ }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = KlarityColors.TextSecondary
                )
            }
            
            // Create task
            Button(
                onClick = onCreateTask,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KlarityColors.AccentPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Task")
            }
        }
    }
}

@Composable
private fun FilterPanel(
    filter: TaskFilter,
    onFilterChange: (TaskFilter) -> Unit,
    allTags: List<String>,
    allAssignees: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(KlarityColors.BgTertiary)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Status filter
        Column {
            Text(
                text = "Status",
                style = MaterialTheme.typography.labelMedium,
                color = KlarityColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TaskStatus.entries.filter { it != TaskStatus.ARCHIVED }.forEach { status ->
                    FilterChip(
                        selected = status in filter.statuses,
                        onClick = {
                            val newStatuses = if (status in filter.statuses) {
                                filter.statuses - status
                            } else {
                                filter.statuses + status
                            }
                            onFilterChange(filter.copy(statuses = newStatuses))
                        },
                        label = { Text(status.emoji) }
                    )
                }
            }
        }
        
        // Priority filter
        Column {
            Text(
                text = "Priority",
                style = MaterialTheme.typography.labelMedium,
                color = KlarityColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TaskPriority.entries.forEach { priority ->
                    FilterChip(
                        selected = priority in filter.priorities,
                        onClick = {
                            val newPriorities = if (priority in filter.priorities) {
                                filter.priorities - priority
                            } else {
                                filter.priorities + priority
                            }
                            onFilterChange(filter.copy(priorities = newPriorities))
                        },
                        label = { Text(priority.emoji) }
                    )
                }
            }
        }
        
        // Overdue toggle
        Column {
            Text(
                text = "Show",
                style = MaterialTheme.typography.labelMedium,
                color = KlarityColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            FilterChip(
                selected = filter.showOverdueOnly,
                onClick = { onFilterChange(filter.copy(showOverdueOnly = !filter.showOverdueOnly)) },
                label = { Text("⚠️ Overdue only") }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Clear filters
        TextButton(
            onClick = { onFilterChange(TaskFilter()) }
        ) {
            Text("Clear Filters", color = KlarityColors.AccentPrimary)
        }
    }
}

@Composable
private fun TaskListView(
    tasks: List<Task>,
    sortOption: TaskSortOption,
    onTaskClick: (Task) -> Unit,
    onTaskToggleComplete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sort tasks
    val sortedTasks = remember(tasks, sortOption) {
        when (sortOption) {
            TaskSortOption.PRIORITY -> tasks.sortedBy { it.priority.ordinal }
            TaskSortOption.DUE_DATE -> tasks.sortedBy { it.dueDate?.toEpochMilliseconds() ?: Long.MAX_VALUE }
            TaskSortOption.CREATED -> tasks.sortedByDescending { it.createdAt }
            TaskSortOption.UPDATED -> tasks.sortedByDescending { it.updatedAt }
            TaskSortOption.TITLE -> tasks.sortedBy { it.title.lowercase() }
            TaskSortOption.MANUAL -> tasks.sortedBy { it.order }
        }
    }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = sortedTasks,
            key = { task -> task.id }
        ) { task ->
            TaskListItem(
                task = task,
                onClick = { onTaskClick(task) },
                onToggleComplete = { onTaskToggleComplete(task) }
            )
        }
    }
}

@Composable
private fun TaskListItem(
    task: Task,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = KlarityColors.BgSecondary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = task.status == TaskStatus.DONE,
                onCheckedChange = { onToggleComplete() },
                colors = CheckboxDefaults.colors(
                    checkedColor = KlarityColors.AccentSecondary,
                    uncheckedColor = KlarityColors.TextTertiary
                )
            )
            
            // Priority indicator
            Text(text = task.priority.emoji)
            
            // Title and description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = KlarityColors.TextPrimary
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = KlarityColors.TextSecondary,
                        maxLines = 1
                    )
                }
            }
            
            // Status
            Text(text = task.status.emoji)
            
            // Due date
            task.dueDate?.let {
                Text(
                    text = "📅",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (task.isOverdue) androidx.compose.ui.graphics.Color(0xFFFF5252) 
                           else KlarityColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun TaskEditorPanel(
    task: Task?,
    onSave: (Task) -> Unit,
    onClose: () -> Unit,
    onDelete: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember(task) { mutableStateOf(task?.title ?: "") }
    var description by remember(task) { mutableStateOf(task?.description ?: "") }
    var priority by remember(task) { mutableStateOf(task?.priority ?: TaskPriority.NORMAL) }
    var status by remember(task) { mutableStateOf(task?.status ?: TaskStatus.TODO) }
    
    Column(
        modifier = modifier
            .width(400.dp)
            .fillMaxHeight()
            .background(KlarityColors.BgSecondary)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (task == null) "New Task" else "Edit Task",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KlarityColors.TextPrimary
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = KlarityColors.TextSecondary
                )
            }
        }
        
        HorizontalDivider(color = KlarityColors.TextTertiary.copy(alpha = 0.2f))
        
        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KlarityColors.AccentPrimary,
                unfocusedBorderColor = KlarityColors.TextTertiary.copy(alpha = 0.3f),
                focusedLabelColor = KlarityColors.AccentPrimary
            )
        )
        
        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KlarityColors.AccentPrimary,
                unfocusedBorderColor = KlarityColors.TextTertiary.copy(alpha = 0.3f),
                focusedLabelColor = KlarityColors.AccentPrimary
            )
        )
        
        // Priority
        Column {
            Text(
                text = "Priority",
                style = MaterialTheme.typography.labelMedium,
                color = KlarityColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TaskPriority.entries.forEach { p ->
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text("${p.emoji} ${p.label}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = androidx.compose.ui.graphics.Color(p.color).copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
        
        // Status
        Column {
            Text(
                text = "Status",
                style = MaterialTheme.typography.labelMedium,
                color = KlarityColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                TaskStatus.entries.filter { it != TaskStatus.ARCHIVED }.forEach { s ->
                    FilterChip(
                        selected = status == s,
                        onClick = { status = s },
                        label = { Text("${s.emoji} ${s.label}") }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (task != null) {
                OutlinedButton(
                    onClick = { onDelete(task) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = androidx.compose.ui.graphics.Color(0xFFFF5252)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
            
            Button(
                onClick = {
                    val updatedTask = (task ?: Task(
                        id = "task-${System.currentTimeMillis()}",
                        title = title,
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now()
                    )).copy(
                        title = title,
                        description = description,
                        priority = priority,
                        status = status,
                        updatedAt = Clock.System.now()
                    )
                    onSave(updatedTask)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KlarityColors.AccentPrimary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}

// ============================================================================
// Helper Functions
// ============================================================================

private fun createSampleKanbanColumns(): List<KanbanColumn> {
    val now = Clock.System.now()
    return listOf(
        KanbanColumn(
            status = TaskStatus.BACKLOG,
            tasks = listOf(
                Task(
                    id = "1",
                    title = "Research new AI models",
                    description = "Explore latest LLM options for the assistant",
                    priority = TaskPriority.LOW,
                    tags = listOf("research", "ai"),
                    createdAt = now,
                    updatedAt = now
                )
            )
        ),
        KanbanColumn(
            status = TaskStatus.TODO,
            tasks = listOf(
                Task(
                    id = "2",
                    title = "Implement Knowledge Graph",
                    description = "Build the spatial canvas for note connections",
                    priority = TaskPriority.HIGH,
                    tags = listOf("feature", "graph"),
                    createdAt = now,
                    updatedAt = now
                ),
                Task(
                    id = "3",
                    title = "Design settings screen",
                    priority = TaskPriority.NORMAL,
                    tags = listOf("design", "ui"),
                    createdAt = now,
                    updatedAt = now
                )
            ),
            wipLimit = 5
        ),
        KanbanColumn(
            status = TaskStatus.IN_PROGRESS,
            tasks = listOf(
                Task(
                    id = "4",
                    title = "TaskFlow Kanban Board",
                    description = "Implement drag-and-drop kanban view",
                    priority = TaskPriority.URGENT,
                    tags = listOf("feature", "tasks"),
                    subtasks = listOf(
                        Subtask("s1", "Create models", true),
                        Subtask("s2", "Build UI components", true),
                        Subtask("s3", "Add drag-and-drop", false),
                        Subtask("s4", "Test interactions", false)
                    ),
                    createdAt = now,
                    updatedAt = now
                )
            ),
            wipLimit = 3
        ),
        KanbanColumn(
            status = TaskStatus.IN_REVIEW,
            tasks = emptyList()
        ),
        KanbanColumn(
            status = TaskStatus.DONE,
            tasks = listOf(
                Task(
                    id = "5",
                    title = "Setup project structure",
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.DONE,
                    createdAt = now,
                    updatedAt = now,
                    completedAt = now
                )
            )
        )
    )
}

private fun moveTask(
    columns: List<KanbanColumn>,
    taskId: String,
    fromStatus: TaskStatus,
    toStatus: TaskStatus,
    newIndex: Int
): List<KanbanColumn> {
    val task = columns.find { it.status == fromStatus }?.tasks?.find { it.id == taskId } 
        ?: return columns
    
    return columns.map { column ->
        when (column.status) {
            fromStatus -> column.copy(tasks = column.tasks.filter { it.id != taskId })
            toStatus -> {
                val newTasks = column.tasks.toMutableList()
                val updatedTask = task.copy(status = toStatus, updatedAt = Clock.System.now())
                val insertIndex = newIndex.coerceIn(0, newTasks.size)
                newTasks.add(insertIndex, updatedTask)
                column.copy(tasks = newTasks)
            }
            else -> column
        }
    }
}

private fun addTaskToColumn(
    columns: List<KanbanColumn>,
    task: Task,
    status: TaskStatus
): List<KanbanColumn> {
    return columns.map { column ->
        if (column.status == status) {
            column.copy(tasks = column.tasks + task)
        } else column
    }
}

private fun removeTaskFromColumn(
    columns: List<KanbanColumn>,
    task: Task
): List<KanbanColumn> {
    return columns.map { column ->
        column.copy(tasks = column.tasks.filter { it.id != task.id })
    }
}

private fun toggleTaskComplete(
    columns: List<KanbanColumn>,
    task: Task
): List<KanbanColumn> {
    val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.TODO else TaskStatus.DONE
    return moveTask(columns, task.id, task.status, newStatus, 0)
}

private fun toggleColumnCollapse(
    columns: List<KanbanColumn>,
    status: TaskStatus,
    collapsed: Boolean
): List<KanbanColumn> {
    return columns.map { column ->
        if (column.status == status) {
            column.copy(isCollapsed = collapsed)
        } else column
    }
}

private fun updateTaskInColumns(
    columns: List<KanbanColumn>,
    task: Task
): List<KanbanColumn> {
    return columns.map { column ->
        column.copy(
            tasks = column.tasks.map { 
                if (it.id == task.id) task else it 
            }
        )
    }
}
