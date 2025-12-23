package com.example.klarity.domain.repositories

import com.example.klarity.presentation.screen.tasks.Task
import com.example.klarity.presentation.screen.tasks.TaskStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Task operations.
 * Provides CRUD operations and queries for task management.
 */
interface TaskRepository {
    
    /**
     * Get all tasks as a Flow for reactive updates.
     */
    fun getAllTasks(): Flow<List<Task>>
    
    /**
     * Get tasks by status (for Kanban columns).
     */
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>
    
    /**
     * Get a single task by ID.
     */
    suspend fun getTaskById(id: String): Task?
    
    /**
     * Get the currently active task (with running timer).
     */
    suspend fun getActiveTask(): Task?
    
    /**
     * Create a new task.
     */
    suspend fun createTask(task: Task): Result<Task>
    
    /**
     * Update an existing task.
     */
    suspend fun updateTask(task: Task): Result<Task>
    
    /**
     * Delete a task by ID.
     */
    suspend fun deleteTask(id: String): Result<Unit>
    
    /**
     * Update task status (for drag-and-drop).
     */
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus, order: Int): Result<Unit>
    
    /**
     * Update task completion status.
     */
    suspend fun updateTaskCompletion(taskId: String, completed: Boolean): Result<Unit>
    
    /**
     * Start timer for a task.
     */
    suspend fun startTimer(taskId: String): Result<Unit>
    
    /**
     * Stop timer for a task.
     */
    suspend fun stopTimer(taskId: String): Result<Unit>
    
    /**
     * Search tasks by title or description.
     */
    fun searchTasks(query: String): Flow<List<Task>>
    
    /**
     * Get task counts by status.
     */
    suspend fun getTaskCountsByStatus(): Map<TaskStatus, Int>
}
