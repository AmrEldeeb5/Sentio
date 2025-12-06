package com.example.klarity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klarity.presentation.screen.tasks.KanbanColumn
import com.example.klarity.presentation.screen.tasks.Task
import com.example.klarity.presentation.screen.tasks.TaskFilter
import com.example.klarity.presentation.screen.tasks.TaskPriority
import com.example.klarity.presentation.screen.tasks.TaskSortOption
import com.example.klarity.presentation.screen.tasks.TaskStatus
import com.example.klarity.presentation.screen.tasks.TaskTimer
import com.example.klarity.presentation.screen.tasks.TaskViewMode
import com.example.klarity.presentation.state.TasksUiEffect
import com.example.klarity.presentation.state.TasksUiEvent
import com.example.klarity.presentation.state.TasksUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel for the Tasks/Kanban screen.
 * 
 * Manages task state, filtering, sorting, and timer functionality
 * using an event-based architecture.
 */
class TasksViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TasksUiState>(TasksUiState.Loading)
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _effects = Channel<TasksUiEffect>(Channel.BUFFERED)
    val effects: Flow<TasksUiEffect> = _effects.receiveAsFlow()

    init {
        loadInitialData()
    }

    /**
     * Handle UI events from the Tasks screen.
     */
    fun onEvent(event: TasksUiEvent) {
        when (event) {
            // Task interaction events
            is TasksUiEvent.TaskClicked -> handleTaskClicked(event.task)
            is TasksUiEvent.TaskToggleComplete -> handleTaskToggleComplete(event.taskId)
            is TasksUiEvent.TaskMoved -> handleTaskMoved(event.taskId, event.toColumn, event.index)
            is TasksUiEvent.TaskCreated -> handleTaskCreated(event.status)
            is TasksUiEvent.TaskDeleted -> handleTaskDeleted(event.task)
            is TasksUiEvent.TaskUpdated -> handleTaskUpdated(event.task)
            
            // Timer events
            is TasksUiEvent.TimerStarted -> handleTimerStarted(event.taskId)
            is TasksUiEvent.TimerStopped -> handleTimerStopped(event.taskId)
            is TasksUiEvent.TimerPaused -> handleTimerPaused(event.taskId)
            is TasksUiEvent.TimerResumed -> handleTimerResumed(event.taskId)
            
            // Filter and sort events
            is TasksUiEvent.FilterChanged -> handleFilterChanged(event.filter)
            is TasksUiEvent.SortChanged -> handleSortChanged(event.sortBy)
            is TasksUiEvent.AssigneeFilterChanged -> handleAssigneeFilterChanged(event.assignee)
            is TasksUiEvent.TagFilterChanged -> handleTagFilterChanged(event.tags)
            is TasksUiEvent.PriorityFilterChanged -> handlePriorityFilterChanged(event.priority)
            
            // View mode events
            is TasksUiEvent.ViewModeChanged -> handleViewModeChanged(event.mode)
            
            // Column events
            is TasksUiEvent.ColumnAdded -> handleColumnAdded(event.title)
            is TasksUiEvent.ColumnCollapsed -> handleColumnCollapsed(event.status, event.collapsed)
            
            // Modal events
            TasksUiEvent.ModalClosed -> handleModalClosed()
            
            // Refresh
            TasksUiEvent.Refresh -> loadInitialData()
        }
    }


    // ============================================================================
    // Task Interaction Handlers
    // ============================================================================

    private fun handleTaskClicked(task: Task) {
        updateSuccessState { state ->
            state.copy(
                selectedTask = task,
                isModalOpen = true
            )
        }
    }

    private fun handleTaskToggleComplete(taskId: String) {
        updateSuccessState { state ->
            val updatedColumns = state.columns.map { column ->
                val updatedTasks = column.tasks.map { task ->
                    if (task.id == taskId) {
                        task.copy(
                            completed = !task.completed,
                            updatedAt = Clock.System.now()
                        )
                    } else task
                }
                column.copy(tasks = updatedTasks)
            }
            state.copy(columns = updatedColumns)
        }
    }

    private fun handleTaskMoved(taskId: String, toColumn: TaskStatus, index: Int) {
        updateSuccessState { state ->
            // Find the task and its source column
            var movedTask: Task? = null
            var sourceColumn: TaskStatus? = null
            
            for (column in state.columns) {
                val task = column.tasks.find { it.id == taskId }
                if (task != null) {
                    movedTask = task
                    sourceColumn = column.status
                    break
                }
            }
            
            if (movedTask == null || sourceColumn == null) return@updateSuccessState state
            
            val updatedTask = movedTask.copy(
                status = toColumn,
                updatedAt = Clock.System.now()
            )
            
            val updatedColumns = state.columns.map { column ->
                when (column.status) {
                    sourceColumn -> column.copy(tasks = column.tasks.filter { it.id != taskId })
                    toColumn -> {
                        val newTasks = column.tasks.toMutableList()
                        val insertIndex = index.coerceIn(0, newTasks.size)
                        newTasks.add(insertIndex, updatedTask)
                        column.copy(tasks = newTasks)
                    }
                    else -> column
                }
            }
            
            state.copy(columns = updatedColumns)
        }
    }

    private fun handleTaskCreated(status: TaskStatus) {
        val newTask = Task(
            id = "task-${Clock.System.now().toEpochMilliseconds()}",
            title = "New Task",
            status = status,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
        
        updateSuccessState { state ->
            val updatedColumns = state.columns.map { column ->
                if (column.status == status) {
                    column.copy(tasks = column.tasks + newTask)
                } else column
            }
            state.copy(
                columns = updatedColumns,
                selectedTask = newTask,
                isModalOpen = true
            )
        }
        
        viewModelScope.launch {
            _effects.send(TasksUiEffect.ShowSnackbar("Task created"))
        }
    }

    private fun handleTaskDeleted(task: Task) {
        updateSuccessState { state ->
            val updatedColumns = state.columns.map { column ->
                column.copy(tasks = column.tasks.filter { it.id != task.id })
            }
            state.copy(
                columns = updatedColumns,
                selectedTask = if (state.selectedTask?.id == task.id) null else state.selectedTask,
                isModalOpen = if (state.selectedTask?.id == task.id) false else state.isModalOpen
            )
        }
        
        viewModelScope.launch {
            _effects.send(TasksUiEffect.ShowSnackbar("Task deleted"))
        }
    }

    private fun handleTaskUpdated(task: Task) {
        updateSuccessState { state ->
            val updatedColumns = state.columns.map { column ->
                column.copy(
                    tasks = column.tasks.map { 
                        if (it.id == task.id) task.copy(updatedAt = Clock.System.now()) else it 
                    }
                )
            }
            state.copy(
                columns = updatedColumns,
                selectedTask = if (state.selectedTask?.id == task.id) task else state.selectedTask
            )
        }
    }

    // ============================================================================
    // Timer Handlers
    // ============================================================================

    private fun handleTimerStarted(taskId: String) {
        updateSuccessState { state ->
            val updatedColumns = state.columns.map { column ->
                column.copy(
                    tasks = column.tasks.map { task ->
                        if (task.id == taskId) {
                            task.copy(
                                timer = TaskTimer(
                                    startedAt = Clock.System.now(),
                                    isPaused = false
                                ),
                                isActive = true,
                                updatedAt = Clock.System.now()
                            )
                        } else {
                            // Deactivate other tasks
                            task.copy(isActive = false)
                        }
                    }
                )
            }
            state.copy(columns = updatedColumns)
        }
    }

    private fun handleTimerStopped(taskId: String) {
        updateSuccessState { state ->
            val updatedColumns = state.columns.map { column ->
                column.copy(
                    tasks = column.tasks.map { task ->
                        if (task.id == taskId) {
                            task.copy(
                                timer = null,
                                isActive = false,
                                updatedAt = Clock.System.now()
                            )
                        } else task
                    }
                )
            }
            state.copy(columns = updatedColumns)
        }
    }

    private fun handleTimerPaused(taskId: String) {
        updateSuccessState { state ->
            val updatedColumns = state.columns.map { column ->
                column.copy(
                    tasks = column.tasks.map { task ->
                        if (task.id == taskId && task.timer != null) {
                            task.copy(
                                timer = task.timer.copy(isPaused = true),
                                updatedAt = Clock.System.now()
                            )
                        } else task
                    }
                )
            }
            state.copy(columns = updatedColumns)
        }
    }

    private fun handleTimerResumed(taskId: String) {
        updateSuccessState { state ->
            val updatedColumns = state.columns.map { column ->
                column.copy(
                    tasks = column.tasks.map { task ->
                        if (task.id == taskId && task.timer != null) {
                            task.copy(
                                timer = task.timer.copy(isPaused = false),
                                updatedAt = Clock.System.now()
                            )
                        } else task
                    }
                )
            }
            state.copy(columns = updatedColumns)
        }
    }


    // ============================================================================
    // Filter and Sort Handlers
    // ============================================================================

    private fun handleFilterChanged(filter: TaskFilter) {
        updateSuccessState { state ->
            state.copy(filter = filter)
        }
    }

    private fun handleSortChanged(sortBy: TaskSortOption) {
        updateSuccessState { state ->
            state.copy(sortBy = sortBy)
        }
    }

    private fun handleAssigneeFilterChanged(assignee: String?) {
        updateSuccessState { state ->
            val newAssignees = if (assignee != null) {
                if (assignee in state.filter.assignees) {
                    state.filter.assignees - assignee
                } else {
                    state.filter.assignees + assignee
                }
            } else {
                emptySet()
            }
            state.copy(filter = state.filter.copy(assignees = newAssignees))
        }
    }

    private fun handleTagFilterChanged(tags: Set<String>) {
        updateSuccessState { state ->
            state.copy(filter = state.filter.copy(tags = tags))
        }
    }

    private fun handlePriorityFilterChanged(priority: TaskPriority?) {
        updateSuccessState { state ->
            val newPriorities = if (priority != null) {
                if (priority in state.filter.priorities) {
                    state.filter.priorities - priority
                } else {
                    state.filter.priorities + priority
                }
            } else {
                TaskPriority.entries.toSet()
            }
            state.copy(filter = state.filter.copy(priorities = newPriorities))
        }
    }

    // ============================================================================
    // View Mode Handlers
    // ============================================================================

    private fun handleViewModeChanged(mode: TaskViewMode) {
        updateSuccessState { state ->
            state.copy(viewMode = mode)
        }
    }

    // ============================================================================
    // Column Handlers
    // ============================================================================

    private fun handleColumnAdded(title: String) {
        // For now, we don't support custom columns beyond the predefined statuses
        viewModelScope.launch {
            _effects.send(TasksUiEffect.ShowSnackbar("Custom columns coming soon"))
        }
    }

    private fun handleColumnCollapsed(status: TaskStatus, collapsed: Boolean) {
        updateSuccessState { state ->
            val updatedColumns = state.columns.map { column ->
                if (column.status == status) {
                    column.copy(isCollapsed = collapsed)
                } else column
            }
            state.copy(columns = updatedColumns)
        }
    }

    // ============================================================================
    // Modal Handlers
    // ============================================================================

    private fun handleModalClosed() {
        updateSuccessState { state ->
            state.copy(
                selectedTask = null,
                isModalOpen = false
            )
        }
    }

    // ============================================================================
    // Helper Functions
    // ============================================================================

    private fun loadInitialData() {
        _uiState.value = TasksUiState.Loading
        
        // For now, load sample data. In a real app, this would come from a repository.
        val columns = createInitialColumns()
        
        _uiState.value = TasksUiState.Success(
            columns = columns,
            viewMode = TaskViewMode.KANBAN,
            filter = TaskFilter(),
            sortBy = TaskSortOption.PRIORITY
        )
    }

    private fun updateSuccessState(update: (TasksUiState.Success) -> TasksUiState.Success) {
        _uiState.update { currentState ->
            when (currentState) {
                is TasksUiState.Success -> update(currentState)
                else -> currentState
            }
        }
    }

    /**
     * Get filtered tasks based on current filter settings.
     * This is a pure function that can be used for property testing.
     */
    fun getFilteredTasks(tasks: List<Task>, filter: TaskFilter): List<Task> {
        return tasks.filter { task ->
            // Filter by assignee
            val matchesAssignee = filter.assignees.isEmpty() || 
                (task.assignee != null && task.assignee in filter.assignees)
            
            // Filter by tags
            val matchesTags = filter.tags.isEmpty() || 
                task.tags.any { it.label in filter.tags }
            
            // Filter by priority
            val matchesPriority = filter.priorities.isEmpty() || 
                task.priority in filter.priorities
            
            // Filter by status
            val matchesStatus = filter.statuses.isEmpty() || 
                task.status in filter.statuses
            
            // Filter by search query
            val matchesSearch = filter.searchQuery.isBlank() ||
                task.title.contains(filter.searchQuery, ignoreCase = true) ||
                task.description.contains(filter.searchQuery, ignoreCase = true)
            
            // Filter by overdue
            val matchesOverdue = !filter.showOverdueOnly || task.isOverdue
            
            matchesAssignee && matchesTags && matchesPriority && 
                matchesStatus && matchesSearch && matchesOverdue
        }
    }

    /**
     * Filter tasks by assignee.
     * Returns only tasks where the assignee matches the filter value.
     */
    fun filterByAssignee(tasks: List<Task>, assignee: String): List<Task> {
        return tasks.filter { task -> task.assignee == assignee }
    }

    /**
     * Filter tasks by tags.
     * Returns only tasks that have at least one matching tag.
     */
    fun filterByTags(tasks: List<Task>, tags: Set<String>): List<Task> {
        if (tags.isEmpty()) return tasks
        return tasks.filter { task ->
            task.tags.any { it.label in tags }
        }
    }

    private fun createInitialColumns(): List<KanbanColumn> {
        val now = Clock.System.now()
        return listOf(
            KanbanColumn(status = TaskStatus.BACKLOG, tasks = emptyList()),
            KanbanColumn(status = TaskStatus.TODO, tasks = emptyList(), wipLimit = 5),
            KanbanColumn(status = TaskStatus.IN_PROGRESS, tasks = emptyList(), wipLimit = 3),
            KanbanColumn(status = TaskStatus.IN_REVIEW, tasks = emptyList()),
            KanbanColumn(status = TaskStatus.DONE, tasks = emptyList())
        )
    }
}
