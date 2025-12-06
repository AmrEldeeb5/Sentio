package com.example.sentio.presentation.screen.tasks

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Instant

/**
 * TaskFlow Data Models
 * 
 * Desktop Design: "TaskFlow: Kanban + Timeline"
 * - Kanban columns with draggable cards
 * - Timeline/Gantt view for scheduling
 * - Priority indicators and due dates
 */

// ============================================================================
// Task Status & Priority
// ============================================================================

enum class TaskStatus(val label: String, val emoji: String) {
    BACKLOG("Backlog", "📋"),
    TODO("To Do", "📝"),
    IN_PROGRESS("In Progress", "🔄"),
    IN_REVIEW("In Review", "👀"),
    DONE("Done", "✅"),
    ARCHIVED("Archived", "📦")
}

enum class TaskPriority(val label: String, val emoji: String, val color: Long) {
    URGENT("Urgent", "🔴", 0xFFFF5252),
    HIGH("High", "🟠", 0xFFFFAB40),
    NORMAL("Normal", "🟡", 0xFFFFEB3B),
    LOW("Low", "🟢", 0xFF4CAF50),
    NONE("None", "⚪", 0xFF9E9E9E)
}

// ============================================================================
// Task Model
// ============================================================================

data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val tags: List<String> = emptyList(),
    val assignee: String? = null,
    val dueDate: Instant? = null,
    val startDate: Instant? = null,
    val estimatedHours: Float? = null,
    val actualHours: Float? = null,
    val subtasks: List<Subtask> = emptyList(),
    val linkedNoteIds: List<String> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant,
    val completedAt: Instant? = null,
    val order: Int = 0
) {
    val progress: Float
        get() = if (subtasks.isEmpty()) {
            if (status == TaskStatus.DONE) 1f else 0f
        } else {
            subtasks.count { it.isCompleted }.toFloat() / subtasks.size
        }
    
    val isOverdue: Boolean
        get() = dueDate != null && status != TaskStatus.DONE && 
                dueDate < kotlinx.datetime.Clock.System.now()
}

data class Subtask(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val order: Int = 0
)

// ============================================================================
// Kanban Column Model
// ============================================================================

data class KanbanColumn(
    val status: TaskStatus,
    val tasks: List<Task>,
    val isCollapsed: Boolean = false,
    val wipLimit: Int? = null // Work-in-progress limit
) {
    val isOverWipLimit: Boolean
        get() = wipLimit != null && tasks.size > wipLimit
}

// ============================================================================
// View Mode
// ============================================================================

enum class TaskViewMode(val label: String, val emoji: String) {
    KANBAN("Kanban", "📊"),
    LIST("List", "📋"),
    TIMELINE("Timeline", "📅"),
    CALENDAR("Calendar", "🗓️")
}

// ============================================================================
// Filter & Sort
// ============================================================================

data class TaskFilter(
    val statuses: Set<TaskStatus> = TaskStatus.entries.toSet(),
    val priorities: Set<TaskPriority> = TaskPriority.entries.toSet(),
    val tags: Set<String> = emptySet(),
    val assignees: Set<String> = emptySet(),
    val showOverdueOnly: Boolean = false,
    val searchQuery: String = ""
)

enum class TaskSortOption(val label: String) {
    PRIORITY("Priority"),
    DUE_DATE("Due Date"),
    CREATED("Created"),
    UPDATED("Updated"),
    TITLE("Title"),
    MANUAL("Manual")
}

// ============================================================================
// Drag & Drop State
// ============================================================================

data class DragState(
    val isDragging: Boolean = false,
    val draggedTaskId: String? = null,
    val sourceColumn: TaskStatus? = null,
    val targetColumn: TaskStatus? = null,
    val targetIndex: Int? = null
)
