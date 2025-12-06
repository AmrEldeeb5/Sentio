package com.example.klarity.presentation.state

import com.example.klarity.presentation.screen.tasks.KanbanColumn
import com.example.klarity.presentation.screen.tasks.Task
import com.example.klarity.presentation.screen.tasks.TaskFilter
import com.example.klarity.presentation.screen.tasks.TaskPriority
import com.example.klarity.presentation.screen.tasks.TaskSortOption
import com.example.klarity.presentation.screen.tasks.TaskStatus
import com.example.klarity.presentation.screen.tasks.TaskViewMode

/**
 * UI State for the Tasks/Kanban screen.
 * 
 * Follows the sealed interface pattern for type-safe state management.
 */
sealed interface TasksUiState {
    /**
     * Initial loading state while fetching tasks from storage.
     */
    data object Loading : TasksUiState
    
    /**
     * Success state containing all board data.
     * 
     * @property columns List of Kanban columns with their tasks
     * @property selectedTask Currently selected task for detail view (null if none)
     * @property isModalOpen Whether the task detail modal is open
     * @property viewMode Current view mode (Kanban, List, Calendar)
     * @property filter Current filter settings
     * @property sortBy Current sort option
     */
    data class Success(
        val columns: List<KanbanColumn>,
        val selectedTask: Task? = null,
        val isModalOpen: Boolean = false,
        val viewMode: TaskViewMode = TaskViewMode.KANBAN,
        val filter: TaskFilter = TaskFilter(),
        val sortBy: TaskSortOption = TaskSortOption.PRIORITY
    ) : TasksUiState
    
    /**
     * Error state when something goes wrong.
     */
    data class Error(val message: String) : TasksUiState
}

/**
 * UI Events that can be triggered from the Tasks screen.
 * 
 * These events are sent from the UI to the ViewModel for processing.
 */
sealed interface TasksUiEvent {
    // Task interaction events
    data class TaskClicked(val task: Task) : TasksUiEvent
    data class TaskToggleComplete(val taskId: String) : TasksUiEvent
    data class TaskMoved(val taskId: String, val toColumn: TaskStatus, val index: Int) : TasksUiEvent
    data class TaskCreated(val status: TaskStatus) : TasksUiEvent
    data class TaskDeleted(val task: Task) : TasksUiEvent
    data class TaskUpdated(val task: Task) : TasksUiEvent
    
    // Timer control events
    data class TimerStarted(val taskId: String) : TasksUiEvent
    data class TimerStopped(val taskId: String) : TasksUiEvent
    data class TimerPaused(val taskId: String) : TasksUiEvent
    data class TimerResumed(val taskId: String) : TasksUiEvent
    
    // Filter and sort events
    data class FilterChanged(val filter: TaskFilter) : TasksUiEvent
    data class SortChanged(val sortBy: TaskSortOption) : TasksUiEvent
    data class AssigneeFilterChanged(val assignee: String?) : TasksUiEvent
    data class TagFilterChanged(val tags: Set<String>) : TasksUiEvent
    data class PriorityFilterChanged(val priority: TaskPriority?) : TasksUiEvent
    
    // View mode events
    data class ViewModeChanged(val mode: TaskViewMode) : TasksUiEvent
    
    // Column events
    data class ColumnAdded(val title: String) : TasksUiEvent
    data class ColumnCollapsed(val status: TaskStatus, val collapsed: Boolean) : TasksUiEvent
    
    // Modal events
    data object ModalClosed : TasksUiEvent
    
    // Refresh event
    data object Refresh : TasksUiEvent
}

/**
 * Side effects that should be handled by the UI layer.
 * 
 * These are one-time events like showing a snackbar or navigating.
 */
sealed interface TasksUiEffect {
    data class ShowSnackbar(val message: String) : TasksUiEffect
    data class ShowError(val message: String) : TasksUiEffect
    data class NavigateToTask(val taskId: String) : TasksUiEffect
    data object TimerTick : TasksUiEffect
}
