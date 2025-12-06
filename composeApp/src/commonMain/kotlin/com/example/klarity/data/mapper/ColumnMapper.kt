package com.example.klarity.data.mapper

import com.example.klarity.db.ColumnEntity
import com.example.klarity.presentation.screen.tasks.KanbanColumn
import com.example.klarity.presentation.screen.tasks.Task
import com.example.klarity.presentation.screen.tasks.TaskStatus

/**
 * Mapper for converting between KanbanColumn domain model and ColumnEntity database entity.
 * 
 * Requirements: 9.1, 9.2 - Board state persistence
 */
object ColumnMapper {
    
    /**
     * Converts a ColumnEntity database row to a KanbanColumn domain model.
     * Note: Tasks must be provided separately as they are stored in a different table.
     */
    fun toDomain(entity: ColumnEntity, tasks: List<Task> = emptyList()): KanbanColumn {
        return KanbanColumn(
            status = TaskStatus.entries.find { it.name == entity.status } ?: TaskStatus.TODO,
            tasks = tasks,
            isCollapsed = entity.isCollapsed == 1L,
            wipLimit = entity.wipLimit?.toInt()
        )
    }
    
    /**
     * Converts a KanbanColumn domain model to values suitable for database insertion/update.
     */
    fun toEntityValues(column: KanbanColumn, order: Int): ColumnEntityValues {
        return ColumnEntityValues(
            id = column.status.name,
            title = column.status.label,
            status = column.status.name,
            columnOrder = order.toLong(),
            isCollapsed = if (column.isCollapsed) 1L else 0L,
            wipLimit = column.wipLimit?.toLong()
        )
    }
}

/**
 * Data class holding all values needed for database operations.
 */
data class ColumnEntityValues(
    val id: String,
    val title: String,
    val status: String,
    val columnOrder: Long,
    val isCollapsed: Long,
    val wipLimit: Long?
)
