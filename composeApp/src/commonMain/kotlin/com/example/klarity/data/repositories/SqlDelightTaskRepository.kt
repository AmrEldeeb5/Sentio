package com.example.klarity.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.klarity.data.mapper.TaskMapper
import com.example.klarity.data.util.DispatcherProvider
import com.example.klarity.db.KlarityDatabase
import com.example.klarity.domain.repositories.TaskRepository
import com.example.klarity.presentation.screen.tasks.Task
import com.example.klarity.presentation.screen.tasks.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * SQLDelight implementation of TaskRepository.
 * Handles all task persistence operations.
 */
class SqlDelightTaskRepository(
    private val database: KlarityDatabase,
    private val dispatchers: DispatcherProvider
) : TaskRepository {

    private val taskQueries get() = database.taskQueries

    override fun getAllTasks(): Flow<List<Task>> =
        taskQueries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities -> entities.map { TaskMapper.toDomain(it) } }

    override fun getTasksByStatus(status: TaskStatus): Flow<List<Task>> =
        taskQueries.selectByStatus(status.name)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities -> entities.map { TaskMapper.toDomain(it) } }

    override suspend fun getTaskById(id: String): Task? = withContext(dispatchers.io) {
        taskQueries.selectById(id).executeAsOneOrNull()?.let { TaskMapper.toDomain(it) }
    }

    override suspend fun getActiveTask(): Task? = withContext(dispatchers.io) {
        taskQueries.selectActive().executeAsOneOrNull()?.let { TaskMapper.toDomain(it) }
    }

    override suspend fun createTask(task: Task): Result<Task> = runCatching {
        withContext(dispatchers.io) {
            val values = TaskMapper.toEntityValues(task)
            taskQueries.insert(
                id = values.id,
                title = values.title,
                description = values.description,
                status = values.status,
                priority = values.priority,
                tags = values.tags,
                points = values.points,
                assignee = values.assignee,
                dueDate = values.dueDate,
                startDate = values.startDate,
                estimatedHours = values.estimatedHours,
                actualHours = values.actualHours,
                subtasks = values.subtasks,
                linkedNoteIds = values.linkedNoteIds,
                timerStartedAt = values.timerStartedAt,
                timerPausedDuration = values.timerPausedDuration,
                timerIsPaused = values.timerIsPaused,
                isActive = values.isActive,
                completed = values.completed,
                columnOrder = values.columnOrder,
                createdAt = values.createdAt,
                updatedAt = values.updatedAt,
                completedAt = values.completedAt
            )
        }
        task
    }

    override suspend fun updateTask(task: Task): Result<Task> = runCatching {
        withContext(dispatchers.io) {
            val values = TaskMapper.toEntityValues(task)
            taskQueries.update(
                title = values.title,
                description = values.description,
                status = values.status,
                priority = values.priority,
                tags = values.tags,
                points = values.points,
                assignee = values.assignee,
                dueDate = values.dueDate,
                startDate = values.startDate,
                estimatedHours = values.estimatedHours,
                actualHours = values.actualHours,
                subtasks = values.subtasks,
                linkedNoteIds = values.linkedNoteIds,
                timerStartedAt = values.timerStartedAt,
                timerPausedDuration = values.timerPausedDuration,
                timerIsPaused = values.timerIsPaused,
                isActive = values.isActive,
                completed = values.completed,
                columnOrder = values.columnOrder,
                updatedAt = values.updatedAt,
                completedAt = values.completedAt,
                id = values.id
            )
        }
        task
    }

    override suspend fun deleteTask(id: String): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            taskQueries.delete(id)
        }
    }

    override suspend fun updateTaskStatus(
        taskId: String,
        status: TaskStatus,
        order: Int
    ): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            val now = Clock.System.now().toEpochMilliseconds()
            taskQueries.updateStatus(
                status = status.name,
                columnOrder = order.toLong(),
                updatedAt = now,
                id = taskId
            )
        }
    }

    override suspend fun updateTaskCompletion(
        taskId: String,
        completed: Boolean
    ): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            val now = Clock.System.now().toEpochMilliseconds()
            taskQueries.updateCompleted(
                completed = if (completed) 1L else 0L,
                completedAt = if (completed) now else null,
                updatedAt = now,
                id = taskId
            )
        }
    }

    override suspend fun startTimer(taskId: String): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            val now = Clock.System.now().toEpochMilliseconds()
            // First, clear any existing active timers
            taskQueries.selectActive().executeAsOneOrNull()?.let { activeTask ->
                if (activeTask.id != taskId) {
                    taskQueries.clearTimer(updatedAt = now, id = activeTask.id)
                }
            }
            // Start timer for this task
            taskQueries.updateTimer(
                timerStartedAt = now,
                timerPausedDuration = 0L,
                timerIsPaused = 0L,
                isActive = 1L,
                updatedAt = now,
                id = taskId
            )
        }
    }

    override suspend fun stopTimer(taskId: String): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            val now = Clock.System.now().toEpochMilliseconds()
            taskQueries.clearTimer(updatedAt = now, id = taskId)
        }
    }

    override fun searchTasks(query: String): Flow<List<Task>> =
        taskQueries.search(query, query)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities -> entities.map { TaskMapper.toDomain(it) } }

    override suspend fun getTaskCountsByStatus(): Map<TaskStatus, Int> = 
        withContext(dispatchers.io) {
            taskQueries.countByStatus().executeAsList().associate { row ->
                val status = TaskStatus.entries.find { it.name == row.status } ?: TaskStatus.TODO
                status to row.count_.toInt()
            }
        }
}
