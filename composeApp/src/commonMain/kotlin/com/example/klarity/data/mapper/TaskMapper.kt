package com.example.klarity.data.mapper

import com.example.klarity.data.serialization.SubtaskState
import com.example.klarity.data.serialization.TagState
import com.example.klarity.db.TaskEntity
import com.example.klarity.presentation.screen.tasks.*
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

/**
 * Mapper for converting between Task domain model and TaskEntity database entity.
 * Handles JSON serialization for tags and subtasks fields.
 * 
 * Requirements: 9.1, 9.2 - Task persistence to local storage
 */
object TaskMapper {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    /**
     * Converts a TaskEntity database row to a Task domain model.
     */
    fun toDomain(entity: TaskEntity): Task {
        // Parse tags from JSON
        val tags = parseTagsJson(entity.tags)
        
        // Parse subtasks from JSON
        val subtasks = parseSubtasksJson(entity.subtasks)
        
        // Parse linked note IDs from JSON
        val linkedNoteIds = parseLinkedNoteIdsJson(entity.linkedNoteIds)
        
        // Build timer if present
        val timer = if (entity.timerStartedAt != null) {
            TaskTimer(
                startedAt = Instant.fromEpochMilliseconds(entity.timerStartedAt),
                pausedDuration = (entity.timerPausedDuration ?: 0L).milliseconds,
                isPaused = entity.timerIsPaused == 1L
            )
        } else null
        
        return Task(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            status = TaskStatus.entries.find { it.name == entity.status } ?: TaskStatus.TODO,
            priority = TaskPriority.entries.find { it.name == entity.priority } ?: TaskPriority.MEDIUM,
            tags = tags,
            points = entity.points?.toInt(),
            assignee = entity.assignee,
            dueDate = entity.dueDate?.let { Instant.fromEpochMilliseconds(it) },
            startDate = entity.startDate?.let { Instant.fromEpochMilliseconds(it) },
            estimatedHours = entity.estimatedHours?.toFloat(),
            actualHours = entity.actualHours?.toFloat(),
            subtasks = subtasks,
            linkedNoteIds = linkedNoteIds,
            timer = timer,
            isActive = entity.isActive == 1L,
            completed = entity.completed == 1L,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
            updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt),
            completedAt = entity.completedAt?.let { Instant.fromEpochMilliseconds(it) },
            order = entity.columnOrder.toInt()
        )
    }

    
    /**
     * Converts a Task domain model to values suitable for database insertion/update.
     * Returns a TaskEntityValues data class with all fields ready for SQL operations.
     */
    fun toEntityValues(task: Task): TaskEntityValues {
        return TaskEntityValues(
            id = task.id,
            title = task.title,
            description = task.description,
            status = task.status.name,
            priority = task.priority.name,
            tags = serializeTagsJson(task.tags),
            points = task.points?.toLong(),
            assignee = task.assignee,
            dueDate = task.dueDate?.toEpochMilliseconds(),
            startDate = task.startDate?.toEpochMilliseconds(),
            estimatedHours = task.estimatedHours?.toDouble(),
            actualHours = task.actualHours?.toDouble(),
            subtasks = serializeSubtasksJson(task.subtasks),
            linkedNoteIds = serializeLinkedNoteIdsJson(task.linkedNoteIds),
            timerStartedAt = task.timer?.startedAt?.toEpochMilliseconds(),
            timerPausedDuration = task.timer?.pausedDuration?.inWholeMilliseconds,
            timerIsPaused = if (task.timer?.isPaused == true) 1L else 0L,
            isActive = if (task.isActive) 1L else 0L,
            completed = if (task.completed) 1L else 0L,
            columnOrder = task.order.toLong(),
            createdAt = task.createdAt.toEpochMilliseconds(),
            updatedAt = task.updatedAt.toEpochMilliseconds(),
            completedAt = task.completedAt?.toEpochMilliseconds()
        )
    }
    
    // ============================================================================
    // JSON Parsing Helpers
    // ============================================================================
    
    private fun parseTagsJson(tagsJson: String): List<TaskTag> {
        return try {
            val tagStates = json.decodeFromString<List<TagState>>(tagsJson)
            tagStates.map { tagState ->
                TaskTag(
                    label = tagState.label,
                    colorClass = TagColor.entries.find { it.name == tagState.color } ?: TagColor.GRAY
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseSubtasksJson(subtasksJson: String): List<Subtask> {
        return try {
            val subtaskStates = json.decodeFromString<List<SubtaskState>>(subtasksJson)
            subtaskStates.map { state ->
                Subtask(
                    id = state.id,
                    title = state.title,
                    isCompleted = state.isCompleted,
                    order = state.order
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseLinkedNoteIdsJson(linkedNoteIdsJson: String): List<String> {
        return try {
            json.decodeFromString<List<String>>(linkedNoteIdsJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ============================================================================
    // JSON Serialization Helpers
    // ============================================================================
    
    private fun serializeTagsJson(tags: List<TaskTag>): String {
        val tagStates = tags.map { tag ->
            TagState(label = tag.label, color = tag.colorClass.name)
        }
        return json.encodeToString(tagStates)
    }
    
    private fun serializeSubtasksJson(subtasks: List<Subtask>): String {
        val subtaskStates = subtasks.map { subtask ->
            SubtaskState(
                id = subtask.id,
                title = subtask.title,
                isCompleted = subtask.isCompleted,
                order = subtask.order
            )
        }
        return json.encodeToString(subtaskStates)
    }
    
    private fun serializeLinkedNoteIdsJson(linkedNoteIds: List<String>): String {
        return json.encodeToString(linkedNoteIds)
    }
}

/**
 * Data class holding all values needed for database operations.
 * This avoids direct dependency on SQLDelight generated classes in the mapper.
 */
data class TaskEntityValues(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    val tags: String,
    val points: Long?,
    val assignee: String?,
    val dueDate: Long?,
    val startDate: Long?,
    val estimatedHours: Double?,
    val actualHours: Double?,
    val subtasks: String,
    val linkedNoteIds: String,
    val timerStartedAt: Long?,
    val timerPausedDuration: Long?,
    val timerIsPaused: Long,
    val isActive: Long,
    val completed: Long,
    val columnOrder: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val completedAt: Long?
)
