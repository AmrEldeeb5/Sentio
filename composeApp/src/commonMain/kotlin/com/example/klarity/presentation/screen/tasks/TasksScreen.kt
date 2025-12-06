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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.klarity.presentation.state.TasksUiEffect
import com.example.klarity.presentation.state.TasksUiEvent
import com.example.klarity.presentation.state.TasksUiState
import com.example.klarity.presentation.theme.KlarityColors
import com.example.klarity.presentation.viewmodel.TasksViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

/**
 * TasksScreen - Main TaskFlow Interface
 * 
 * Desktop Design: "TaskFlow: Kanban + Timeline"
 * Combines Kanban board and Timeline views with
 * filtering, search, and view switching.
 * 
 * **Requirements: 1.1, 3.1** - Wired up with TasksViewModel
 */
@Composable
fun TasksScreen(
    viewModel: TasksViewModel = viewModel { TasksViewModel() },
    modifier: Modifier = Modifier
) {
    // Collect UI state from ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    // Local UI state for filter panel visibility
    var showFilterPanel by remember { mutableStateOf(false) }
    
    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TasksUiEffect.ShowSnackbar -> {
                    // TODO: Show snackbar
                }
                is TasksUiEffect.ShowError -> {
                    // TODO: Show error
                }
                is TasksUiEffect.NavigateToTask -> {
                    // TODO: Navigate to task
                }
                is TasksUiEffect.TimerTick -> {
                    // Timer tick handled separately
                }
            }
        }
    }
    
    // Timer tick effect - updates every second when a task has active timer
    // **Requirement 6.1**: Timer display updates
    val successState = uiState as? TasksUiState.Success
    val hasActiveTimer = successState?.columns?.any { column ->
        column.tasks.any { task -> task.isActive && task.timer != null }
    } ?: false
    
    // Force recomposition every second when timer is active
    var timerTick by remember { mutableStateOf(0L) }
    LaunchedEffect(hasActiveTimer) {
        if (hasActiveTimer) {
            while (true) {
                delay(1000L)
                timerTick = Clock.System.now().toEpochMilliseconds()
            }
        }
    }
    
    // Render based on UI state
    when (val state = uiState) {
        is TasksUiState.Loading -> {
            LoadingScreen()
        }
        is TasksUiState.Error -> {
            ErrorScreen(
                message = state.message,
                onRetry = { viewModel.onEvent(TasksUiEvent.Refresh) }
            )
        }
        is TasksUiState.Success -> {
            TasksScreenContent(
                state = state,
                showFilterPanel = showFilterPanel,
                timerTick = timerTick,
                onEvent = viewModel::onEvent,
                onToggleFilterPanel = { showFilterPanel = !showFilterPanel },
                modifier = modifier
            )
        }
    }
}

/**
 * Main content of TasksScreen when in Success state.
 * 
 * **Requirements: 1.1, 3.1** - Displays columns and handles task interactions
 */
@Composable
private fun TasksScreenContent(
    state: TasksUiState.Success,
    showFilterPanel: Boolean,
    timerTick: Long,
    onEvent: (TasksUiEvent) -> Unit,
    onToggleFilterPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTasks = state.columns.flatMap { it.tasks }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KlarityColors.BgPrimary)
        ) {
            // Header with view mode tabs (Requirement 10.1, 10.2)
            TasksHeader(
                currentViewMode = state.viewMode,
                onViewModeChange = { mode -> onEvent(TasksUiEvent.ViewModeChanged(mode)) },
                onDeepWorkModeClick = { /* TODO: Deep work mode */ },
                onNotificationsClick = { /* TODO: Notifications */ },
                onUserAvatarClick = { /* TODO: User profile */ }
            )
            
            // Board controls with filter chips (Requirements 2.1, 5.1-5.4)
            BoardControls(
                onAddColumn = { onEvent(TasksUiEvent.ColumnAdded("New Column")) },
                onFilterClick = onToggleFilterPanel,
                onSortByClick = { /* TODO: Sort dropdown */ },
                onAssigneeClick = { /* TODO: Assignee filter */ },
                onTagsClick = { /* TODO: Tags filter */ }
            )
            
            // Filter Panel (Collapsible)
            AnimatedVisibility(
                visible = showFilterPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                FilterPanel(
                    filter = state.filter,
                    onFilterChange = { filter -> onEvent(TasksUiEvent.FilterChanged(filter)) },
                    allTags = allTasks.flatMap { it.tags }.map { it.label }.distinct(),
                    allAssignees = allTasks.mapNotNull { it.assignee }.distinct()
                )
            }
            
            // Main Content
            Box(modifier = Modifier.weight(1f)) {
                when (state.viewMode) {
                    TaskViewMode.KANBAN -> {
                        KanbanBoard(
                            columns = state.columns,
                            onTaskMove = { taskId, fromStatus, toStatus, newIndex ->
                                onEvent(TasksUiEvent.TaskMoved(taskId, toStatus, newIndex))
                            },
                            onTaskClick = { task ->
                                onEvent(TasksUiEvent.TaskClicked(task))
                            },
                            onTaskCreate = { status ->
                                onEvent(TasksUiEvent.TaskCreated(status))
                            },
                            onTaskDelete = { task ->
                                onEvent(TasksUiEvent.TaskDeleted(task))
                            },
                            onTaskToggleComplete = { task ->
                                onEvent(TasksUiEvent.TaskToggleComplete(task.id))
                            },
                            onColumnCollapse = { status, collapsed ->
                                onEvent(TasksUiEvent.ColumnCollapsed(status, collapsed))
                            }
                        )
                    }
                    
                    TaskViewMode.TIMELINE -> {
                        TaskTimeline(
                            tasks = allTasks,
                            scale = TimelineScale.WEEK,
                            onScaleChange = { /* TODO */ },
                            onTaskClick = { task ->
                                onEvent(TasksUiEvent.TaskClicked(task))
                            },
                            onTaskMove = { task, newStart, newDue ->
                                // TODO: Update task dates
                            }
                        )
                    }
                    
                    TaskViewMode.LIST -> {
                        TaskListView(
                            tasks = allTasks,
                            sortOption = state.sortBy,
                            onTaskClick = { task ->
                                onEvent(TasksUiEvent.TaskClicked(task))
                            },
                            onTaskToggleComplete = { task ->
                                onEvent(TasksUiEvent.TaskToggleComplete(task.id))
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
        
        // Task Detail Modal (Requirements 4.1, 4.3)
        TaskDetailModal(
            task = state.selectedTask,
            isVisible = state.isModalOpen,
            onClose = { onEvent(TasksUiEvent.ModalClosed) },
            onTaskUpdate = { task -> onEvent(TasksUiEvent.TaskUpdated(task)) },
            onAddTag = { /* TODO: Add tag dialog */ }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KlarityColors.BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = KlarityColors.AccentPrimary
        )
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KlarityColors.BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 48.sp
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = KlarityColors.TextSecondary
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KlarityColors.AccentPrimary
                )
            ) {
                Text("Retry")
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
