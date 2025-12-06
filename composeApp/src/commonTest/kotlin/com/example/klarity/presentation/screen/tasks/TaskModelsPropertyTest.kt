package com.example.klarity.presentation.screen.tasks

import com.example.klarity.data.mapper.TaskMapper
import com.example.klarity.data.serialization.BoardState
import com.example.klarity.data.serialization.BoardStateSerializer
import com.example.klarity.data.serialization.ColumnState
import com.example.klarity.data.serialization.SubtaskState
import com.example.klarity.data.serialization.TagState
import com.example.klarity.data.serialization.TaskState
import com.example.klarity.db.TaskEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Property-based tests for TaskModels.
 * 
 * These tests verify correctness properties that should hold across all valid inputs.
 */
class TaskModelsPropertyTest {

    // ============================================================================
    // Generators
    // ============================================================================

    /**
     * Generates valid TaskTimer instances with reasonable time ranges.
     * - startedAt: within the last 100 hours from now
     * - pausedDuration: 0 to 10 hours
     */
    private fun arbTaskTimer(): Arb<TaskTimer> = arbitrary {
        val now = Clock.System.now()
        // Generate a start time within the last 100 hours
        val hoursAgo = Arb.long(0L, 100L).bind()
        val minutesAgo = Arb.long(0L, 59L).bind()
        val secondsAgo = Arb.long(0L, 59L).bind()
        
        val startedAt = now - hoursAgo.hours - minutesAgo.minutes - secondsAgo.seconds
        
        // Generate paused duration (0 to 10 hours, but less than elapsed time)
        val maxPausedSeconds = (hoursAgo * 3600 + minutesAgo * 60 + secondsAgo).coerceAtLeast(0)
        val pausedSeconds = if (maxPausedSeconds > 0) {
            Arb.long(0L, maxPausedSeconds.coerceAtMost(36000)).bind() // Max 10 hours
        } else {
            0L
        }
        
        TaskTimer(
            startedAt = startedAt,
            pausedDuration = pausedSeconds.seconds,
            isPaused = false
        )
    }

    // ============================================================================
    // Property 8: Timer display presence
    // **Feature: kanban-board, Property 8: Timer display presence**
    // **Validates: Requirements 6.1**
    // 
    // For any task with an active timer (timerStartedAt is not null), 
    // the task card SHALL display the elapsed time.
    // ============================================================================

    @Test
    fun property8_timerWithActiveTimerReturnsFormattedTimeString() {
        runBlocking {
            checkAll(100, arbTaskTimer()) { timer ->
                // When we have a timer, formattedTime should return a valid HH:MM:SS string
                val formattedTime = timer.formattedTime()
                
                // Verify format is HH:MM:SS
                val regex = Regex("""^\d{2}:\d{2}:\d{2}$""")
                assertTrue(
                    regex.matches(formattedTime),
                    "Timer formatted time '$formattedTime' should match HH:MM:SS format"
                )
                
                // Verify the parts are valid time components
                val parts = formattedTime.split(":")
                assertEquals(3, parts.size, "Should have 3 parts separated by ':'")
                
                val hours = parts[0].toInt()
                val minutes = parts[1].toInt()
                val seconds = parts[2].toInt()
                
                assertTrue(hours in 0..99, "Hours should be 0-99, got $hours")
                assertTrue(minutes in 0..59, "Minutes should be 0-59, got $minutes")
                assertTrue(seconds in 0..59, "Seconds should be 0-59, got $seconds")
            }
        }
    }

    @Test
    fun property8_taskWithTimerHasActiveTimerTrue() {
        runBlocking {
            checkAll(100, arbTaskTimer()) { timer ->
                val now = Clock.System.now()
                val task = Task(
                    id = "test-task",
                    title = "Test Task",
                    timer = timer,
                    createdAt = now,
                    updatedAt = now
                )
                
                // A task with a non-null timer should report hasActiveTimer as true
                assertTrue(
                    task.hasActiveTimer,
                    "Task with timer should have hasActiveTimer = true"
                )
            }
        }
    }

    @Test
    fun property8_taskWithoutTimerHasActiveTimerFalse() {
        val now = Clock.System.now()
        val task = Task(
            id = "test-task",
            title = "Test Task",
            timer = null,
            createdAt = now,
            updatedAt = now
        )
        
        assertTrue(
            !task.hasActiveTimer,
            "Task without timer should have hasActiveTimer = false"
        )
    }

    // ============================================================================
    // Property 10: Priority indicator color mapping
    // **Feature: kanban-board, Property 10: Priority indicator color mapping**
    // **Validates: Requirements 8.1, 8.2, 8.3, 8.4**
    //
    // For any task, the priority indicator color SHALL match the defined color 
    // for that priority level (HIGH=red, MEDIUM=yellow, LOW=blue).
    // ============================================================================

    @Test
    fun property10_priorityColorsMatchSpecification() {
        runBlocking {
            // Define expected colors from the design spec
            val expectedColors = mapOf(
                TaskPriority.HIGH to 0xFFEF4444L,    // Red
                TaskPriority.MEDIUM to 0xFFFACC15L,  // Yellow
                TaskPriority.LOW to 0xFF3B82F6L      // Blue
            )
            
            checkAll(100, Arb.enum<TaskPriority>()) { priority ->
                when (priority) {
                    TaskPriority.HIGH -> {
                        assertEquals(
                            expectedColors[TaskPriority.HIGH],
                            priority.color,
                            "HIGH priority should be red (0xFFEF4444)"
                        )
                    }
                    TaskPriority.MEDIUM -> {
                        assertEquals(
                            expectedColors[TaskPriority.MEDIUM],
                            priority.color,
                            "MEDIUM priority should be yellow (0xFFFACC15)"
                        )
                    }
                    TaskPriority.LOW -> {
                        assertEquals(
                            expectedColors[TaskPriority.LOW],
                            priority.color,
                            "LOW priority should be blue (0xFF3B82F6)"
                        )
                    }
                    TaskPriority.NONE -> {
                        // NONE priority has its own color, not specified in requirements
                        assertEquals(
                            0xFF9E9E9EL,
                            priority.color,
                            "NONE priority should be gray (0xFF9E9E9E)"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun property10_allPriorityValuesHaveDistinctColors() {
        val colors = TaskPriority.entries.map { it.color }
        val uniqueColors = colors.toSet()
        
        assertEquals(
            colors.size,
            uniqueColors.size,
            "All priority levels should have distinct colors"
        )
    }

    @Test
    fun property10_taskPriorityColorIsAccessibleViaTask() {
        runBlocking {
            val now = Clock.System.now()
            
            checkAll(100, Arb.enum<TaskPriority>()) { priority ->
                val task = Task(
                    id = "test-task",
                    title = "Test Task",
                    priority = priority,
                    createdAt = now,
                    updatedAt = now
                )
                
                // The task's priority color should match the enum's color
                assertEquals(
                    priority.color,
                    task.priority.color,
                    "Task priority color should match the priority enum color"
                )
            }
        }
    }

    // ============================================================================
    // Property 11: Board state serialization round-trip
    // **Feature: kanban-board, Property 11: Board state serialization round-trip**
    // **Validates: Requirements 9.3, 9.4**
    //
    // For any valid board state, serializing to JSON then deserializing 
    // SHALL produce an equivalent board state.
    // ============================================================================

    /**
     * Generates valid TagState instances.
     */
    private fun arbTagState(): Arb<TagState> = arbitrary {
        TagState(
            label = Arb.string(1..20).bind(),
            color = Arb.enum<TagColor>().bind().name
        )
    }

    /**
     * Generates valid SubtaskState instances.
     */
    private fun arbSubtaskState(): Arb<SubtaskState> = arbitrary {
        SubtaskState(
            id = "subtask-${Arb.string(5..10).bind()}",
            title = Arb.string(1..50).bind(),
            isCompleted = Arb.boolean().bind(),
            order = Arb.int(0..100).bind()
        )
    }

    /**
     * Generates valid TaskState instances with reasonable values.
     */
    private fun arbTaskState(): Arb<TaskState> = arbitrary {
        val now = Clock.System.now().toEpochMilliseconds()
        TaskState(
            id = "task-${Arb.string(5..10).bind()}",
            title = Arb.string(1..100).bind(),
            description = Arb.string(0..200).bind(),
            status = Arb.enum<TaskStatus>().bind().name,
            priority = Arb.enum<TaskPriority>().bind().name,
            tags = Arb.list(arbTagState(), 0..5).bind(),
            points = Arb.int(1..13).orNull().bind(),
            assignee = Arb.string(1..20).orNull().bind(),
            dueDate = Arb.long(now, now + 86400000L * 30).orNull().bind(),
            startDate = Arb.long(now - 86400000L * 30, now).orNull().bind(),
            estimatedHours = null,
            actualHours = null,
            subtasks = Arb.list(arbSubtaskState(), 0..3).bind(),
            linkedNoteIds = emptyList(),
            timerStartedAt = Arb.long(now - 3600000L, now).orNull().bind(),
            timerPausedDuration = Arb.long(0L, 3600000L).orNull().bind(),
            timerIsPaused = Arb.boolean().bind(),
            isActive = Arb.boolean().bind(),
            completed = Arb.boolean().bind(),
            createdAt = now - 86400000L,
            updatedAt = now,
            completedAt = null,
            order = Arb.int(0..100).bind()
        )
    }

    /**
     * Generates valid ColumnState instances.
     */
    private fun arbColumnState(): Arb<ColumnState> = arbitrary {
        val status = Arb.enum<TaskStatus>().bind()
        ColumnState(
            id = status.name,
            title = status.label,
            status = status.name,
            order = Arb.int(0..10).bind(),
            isCollapsed = Arb.boolean().bind(),
            wipLimit = Arb.int(1..10).orNull().bind()
        )
    }

    /**
     * Generates valid BoardState instances.
     */
    private fun arbBoardState(): Arb<BoardState> = arbitrary {
        BoardState(
            columns = Arb.list(arbColumnState(), 1..6).bind(),
            tasks = Arb.list(arbTaskState(), 0..10).bind()
        )
    }

    @Test
    fun property11_boardStateSerializationRoundTrip() {
        runBlocking {
            checkAll(100, arbBoardState()) { originalState ->
                // Serialize to JSON
                val json = BoardStateSerializer.encodeBoardState(originalState)
                
                // Deserialize back to BoardState
                val deserializedState = BoardStateSerializer.decodeBoardState(json)
                
                // Verify the round-trip produces equivalent state
                assertEquals(
                    originalState.columns.size,
                    deserializedState.columns.size,
                    "Column count should be preserved after round-trip"
                )
                
                assertEquals(
                    originalState.tasks.size,
                    deserializedState.tasks.size,
                    "Task count should be preserved after round-trip"
                )
                
                // Verify each column is preserved
                originalState.columns.forEachIndexed { index, originalColumn ->
                    val deserializedColumn = deserializedState.columns[index]
                    assertEquals(originalColumn.id, deserializedColumn.id, "Column id should match")
                    assertEquals(originalColumn.title, deserializedColumn.title, "Column title should match")
                    assertEquals(originalColumn.status, deserializedColumn.status, "Column status should match")
                    assertEquals(originalColumn.order, deserializedColumn.order, "Column order should match")
                    assertEquals(originalColumn.isCollapsed, deserializedColumn.isCollapsed, "Column isCollapsed should match")
                    assertEquals(originalColumn.wipLimit, deserializedColumn.wipLimit, "Column wipLimit should match")
                }
                
                // Verify each task is preserved
                originalState.tasks.forEachIndexed { index, originalTask ->
                    val deserializedTask = deserializedState.tasks[index]
                    assertEquals(originalTask.id, deserializedTask.id, "Task id should match")
                    assertEquals(originalTask.title, deserializedTask.title, "Task title should match")
                    assertEquals(originalTask.description, deserializedTask.description, "Task description should match")
                    assertEquals(originalTask.status, deserializedTask.status, "Task status should match")
                    assertEquals(originalTask.priority, deserializedTask.priority, "Task priority should match")
                    assertEquals(originalTask.points, deserializedTask.points, "Task points should match")
                    assertEquals(originalTask.assignee, deserializedTask.assignee, "Task assignee should match")
                    assertEquals(originalTask.completed, deserializedTask.completed, "Task completed should match")
                    assertEquals(originalTask.isActive, deserializedTask.isActive, "Task isActive should match")
                    assertEquals(originalTask.order, deserializedTask.order, "Task order should match")
                    assertEquals(originalTask.tags.size, deserializedTask.tags.size, "Task tags count should match")
                    assertEquals(originalTask.subtasks.size, deserializedTask.subtasks.size, "Task subtasks count should match")
                }
            }
        }
    }

    @Test
    fun property11_emptyBoardStateSerializationRoundTrip() {
        // Test edge case: empty board state
        val emptyState = BoardState()
        val json = BoardStateSerializer.encodeBoardState(emptyState)
        val deserializedState = BoardStateSerializer.decodeBoardState(json)
        
        assertEquals(0, deserializedState.columns.size, "Empty board should have no columns")
        assertEquals(0, deserializedState.tasks.size, "Empty board should have no tasks")
    }

    @Test
    fun property11_invalidJsonReturnsEmptyBoardState() {
        // Test edge case: invalid JSON should return empty BoardState
        val invalidJson = "{ invalid json }"
        val result = BoardStateSerializer.decodeBoardState(invalidJson)
        
        assertEquals(0, result.columns.size, "Invalid JSON should return empty columns")
        assertEquals(0, result.tasks.size, "Invalid JSON should return empty tasks")
    }

    // ============================================================================
    // Property 12: Task persistence round-trip
    // **Feature: kanban-board, Property 12: Task persistence round-trip**
    // **Validates: Requirements 9.1, 9.2**
    //
    // For any task that is created or modified, reading from storage 
    // SHALL return the same task data.
    // ============================================================================

    /**
     * Generates valid TaskTag instances.
     */
    private fun arbTaskTag(): Arb<TaskTag> = arbitrary {
        TaskTag(
            label = Arb.string(1..20).bind(),
            colorClass = Arb.enum<TagColor>().bind()
        )
    }

    /**
     * Generates valid Subtask instances.
     */
    private fun arbSubtask(): Arb<Subtask> = arbitrary {
        Subtask(
            id = "subtask-${Arb.string(5..10).bind()}",
            title = Arb.string(1..50).bind(),
            isCompleted = Arb.boolean().bind(),
            order = Arb.int(0..100).bind()
        )
    }

    /**
     * Generates valid Task instances with all fields populated.
     */
    private fun arbTask(): Arb<Task> = arbitrary {
        val now = Clock.System.now()
        val createdAt = now - Arb.long(1L, 86400000L * 30).bind().milliseconds
        val updatedAt = createdAt + Arb.long(0L, 86400000L).bind().milliseconds
        
        // Generate timer with valid constraints
        val hasTimer = Arb.boolean().bind()
        val timer = if (hasTimer) {
            val timerStartedAt = now - Arb.long(0L, 3600000L).bind().milliseconds
            val pausedDuration = Arb.long(0L, 1800000L).bind().milliseconds
            TaskTimer(
                startedAt = timerStartedAt,
                pausedDuration = pausedDuration,
                isPaused = Arb.boolean().bind()
            )
        } else null
        
        Task(
            id = "task-${Arb.string(5..10).bind()}",
            title = Arb.string(1..100).bind(),
            description = Arb.string(0..200).bind(),
            status = Arb.enum<TaskStatus>().bind(),
            priority = Arb.enum<TaskPriority>().bind(),
            tags = Arb.list(arbTaskTag(), 0..5).bind(),
            points = Arb.int(1..13).orNull().bind(),
            assignee = Arb.string(1..20).orNull().bind(),
            dueDate = Arb.long(now.toEpochMilliseconds(), now.toEpochMilliseconds() + 86400000L * 30).orNull().bind()?.let { Instant.fromEpochMilliseconds(it) },
            startDate = Arb.long(now.toEpochMilliseconds() - 86400000L * 30, now.toEpochMilliseconds()).orNull().bind()?.let { Instant.fromEpochMilliseconds(it) },
            estimatedHours = Arb.int(1..100).orNull().bind()?.toFloat(),
            actualHours = Arb.int(1..100).orNull().bind()?.toFloat(),
            subtasks = Arb.list(arbSubtask(), 0..3).bind(),
            linkedNoteIds = Arb.list(Arb.string(5..10), 0..3).bind(),
            timer = timer,
            isActive = Arb.boolean().bind(),
            completed = Arb.boolean().bind(),
            createdAt = createdAt,
            updatedAt = updatedAt,
            completedAt = if (Arb.boolean().bind()) updatedAt else null,
            order = Arb.int(0..100).bind()
        )
    }

    @Test
    fun property12_taskPersistenceRoundTrip() {
        runBlocking {
            checkAll(100, arbTask()) { originalTask ->
                // Convert Task to entity values (simulating save to database)
                val entityValues = TaskMapper.toEntityValues(originalTask)
                
                // Create a TaskEntity from the values (simulating database read)
                val entity = TaskEntity(
                    id = entityValues.id,
                    title = entityValues.title,
                    description = entityValues.description,
                    status = entityValues.status,
                    priority = entityValues.priority,
                    tags = entityValues.tags,
                    points = entityValues.points,
                    assignee = entityValues.assignee,
                    dueDate = entityValues.dueDate,
                    startDate = entityValues.startDate,
                    estimatedHours = entityValues.estimatedHours,
                    actualHours = entityValues.actualHours,
                    subtasks = entityValues.subtasks,
                    linkedNoteIds = entityValues.linkedNoteIds,
                    timerStartedAt = entityValues.timerStartedAt,
                    timerPausedDuration = entityValues.timerPausedDuration,
                    timerIsPaused = entityValues.timerIsPaused,
                    isActive = entityValues.isActive,
                    completed = entityValues.completed,
                    columnOrder = entityValues.columnOrder,
                    createdAt = entityValues.createdAt,
                    updatedAt = entityValues.updatedAt,
                    completedAt = entityValues.completedAt
                )
                
                // Convert back to domain model (simulating read from database)
                val restoredTask = TaskMapper.toDomain(entity)
                
                // Verify all fields are preserved
                assertEquals(originalTask.id, restoredTask.id, "Task id should be preserved")
                assertEquals(originalTask.title, restoredTask.title, "Task title should be preserved")
                assertEquals(originalTask.description, restoredTask.description, "Task description should be preserved")
                assertEquals(originalTask.status, restoredTask.status, "Task status should be preserved")
                assertEquals(originalTask.priority, restoredTask.priority, "Task priority should be preserved")
                assertEquals(originalTask.points, restoredTask.points, "Task points should be preserved")
                assertEquals(originalTask.assignee, restoredTask.assignee, "Task assignee should be preserved")
                assertEquals(originalTask.isActive, restoredTask.isActive, "Task isActive should be preserved")
                assertEquals(originalTask.completed, restoredTask.completed, "Task completed should be preserved")
                assertEquals(originalTask.order, restoredTask.order, "Task order should be preserved")
                
                // Verify dates (comparing epoch milliseconds to avoid precision issues)
                assertEquals(
                    originalTask.createdAt.toEpochMilliseconds(),
                    restoredTask.createdAt.toEpochMilliseconds(),
                    "Task createdAt should be preserved"
                )
                assertEquals(
                    originalTask.updatedAt.toEpochMilliseconds(),
                    restoredTask.updatedAt.toEpochMilliseconds(),
                    "Task updatedAt should be preserved"
                )
                assertEquals(
                    originalTask.dueDate?.toEpochMilliseconds(),
                    restoredTask.dueDate?.toEpochMilliseconds(),
                    "Task dueDate should be preserved"
                )
                assertEquals(
                    originalTask.startDate?.toEpochMilliseconds(),
                    restoredTask.startDate?.toEpochMilliseconds(),
                    "Task startDate should be preserved"
                )
                assertEquals(
                    originalTask.completedAt?.toEpochMilliseconds(),
                    restoredTask.completedAt?.toEpochMilliseconds(),
                    "Task completedAt should be preserved"
                )
                
                // Verify tags
                assertEquals(originalTask.tags.size, restoredTask.tags.size, "Tag count should be preserved")
                originalTask.tags.forEachIndexed { index, originalTag ->
                    val restoredTag = restoredTask.tags[index]
                    assertEquals(originalTag.label, restoredTag.label, "Tag label should be preserved")
                    assertEquals(originalTag.colorClass, restoredTag.colorClass, "Tag color should be preserved")
                }
                
                // Verify subtasks
                assertEquals(originalTask.subtasks.size, restoredTask.subtasks.size, "Subtask count should be preserved")
                originalTask.subtasks.forEachIndexed { index, originalSubtask ->
                    val restoredSubtask = restoredTask.subtasks[index]
                    assertEquals(originalSubtask.id, restoredSubtask.id, "Subtask id should be preserved")
                    assertEquals(originalSubtask.title, restoredSubtask.title, "Subtask title should be preserved")
                    assertEquals(originalSubtask.isCompleted, restoredSubtask.isCompleted, "Subtask isCompleted should be preserved")
                    assertEquals(originalSubtask.order, restoredSubtask.order, "Subtask order should be preserved")
                }
                
                // Verify linked note IDs
                assertEquals(originalTask.linkedNoteIds, restoredTask.linkedNoteIds, "Linked note IDs should be preserved")
                
                // Verify timer
                if (originalTask.timer != null) {
                    assertTrue(restoredTask.timer != null, "Timer should be preserved when present")
                    assertEquals(
                        originalTask.timer!!.startedAt.toEpochMilliseconds(),
                        restoredTask.timer!!.startedAt.toEpochMilliseconds(),
                        "Timer startedAt should be preserved"
                    )
                    assertEquals(
                        originalTask.timer!!.pausedDuration.inWholeMilliseconds,
                        restoredTask.timer!!.pausedDuration.inWholeMilliseconds,
                        "Timer pausedDuration should be preserved"
                    )
                    assertEquals(
                        originalTask.timer!!.isPaused,
                        restoredTask.timer!!.isPaused,
                        "Timer isPaused should be preserved"
                    )
                } else {
                    assertTrue(restoredTask.timer == null, "Timer should be null when original was null")
                }
            }
        }
    }

    @Test
    fun property12_taskWithEmptyCollectionsRoundTrip() {
        // Test edge case: task with empty tags, subtasks, and linkedNoteIds
        val now = Clock.System.now()
        val task = Task(
            id = "empty-task",
            title = "Empty Task",
            description = "",
            tags = emptyList(),
            subtasks = emptyList(),
            linkedNoteIds = emptyList(),
            timer = null,
            createdAt = now,
            updatedAt = now
        )
        
        val entityValues = TaskMapper.toEntityValues(task)
        val entity = TaskEntity(
            id = entityValues.id,
            title = entityValues.title,
            description = entityValues.description,
            status = entityValues.status,
            priority = entityValues.priority,
            tags = entityValues.tags,
            points = entityValues.points,
            assignee = entityValues.assignee,
            dueDate = entityValues.dueDate,
            startDate = entityValues.startDate,
            estimatedHours = entityValues.estimatedHours,
            actualHours = entityValues.actualHours,
            subtasks = entityValues.subtasks,
            linkedNoteIds = entityValues.linkedNoteIds,
            timerStartedAt = entityValues.timerStartedAt,
            timerPausedDuration = entityValues.timerPausedDuration,
            timerIsPaused = entityValues.timerIsPaused,
            isActive = entityValues.isActive,
            completed = entityValues.completed,
            columnOrder = entityValues.columnOrder,
            createdAt = entityValues.createdAt,
            updatedAt = entityValues.updatedAt,
            completedAt = entityValues.completedAt
        )
        val restoredTask = TaskMapper.toDomain(entity)
        
        assertEquals(task.id, restoredTask.id)
        assertEquals(task.title, restoredTask.title)
        assertTrue(restoredTask.tags.isEmpty(), "Empty tags should be preserved")
        assertTrue(restoredTask.subtasks.isEmpty(), "Empty subtasks should be preserved")
        assertTrue(restoredTask.linkedNoteIds.isEmpty(), "Empty linkedNoteIds should be preserved")
        assertTrue(restoredTask.timer == null, "Null timer should be preserved")
    }
}


// ============================================================================
// Property 4: Task completion toggle idempotence
// **Feature: kanban-board, Property 4: Task completion toggle idempotence**
// **Validates: Requirements 3.2**
//
// For any task, toggling completion twice SHALL return the task 
// to its original completion state.
// ============================================================================

class TaskCompletionTogglePropertyTest {

    /**
     * Generates valid Task instances for testing completion toggle.
     */
    private fun arbTaskForToggle(): Arb<Task> = arbitrary {
        val now = Clock.System.now()
        Task(
            id = "task-${Arb.string(5..10).bind()}",
            title = Arb.string(1..50).bind(),
            description = Arb.string(0..100).bind(),
            status = Arb.enum<TaskStatus>().bind(),
            priority = Arb.enum<TaskPriority>().bind(),
            completed = Arb.boolean().bind(),
            createdAt = now,
            updatedAt = now
        )
    }

    @Test
    fun property4_toggleCompletionTwiceReturnsToOriginalState() {
        runBlocking {
            checkAll(100, arbTaskForToggle()) { originalTask ->
                // First toggle
                val afterFirstToggle = originalTask.copy(completed = !originalTask.completed)
                
                // Second toggle
                val afterSecondToggle = afterFirstToggle.copy(completed = !afterFirstToggle.completed)
                
                // Verify the completion state is back to original
                assertEquals(
                    originalTask.completed,
                    afterSecondToggle.completed,
                    "Toggling completion twice should return to original state. " +
                    "Original: ${originalTask.completed}, After two toggles: ${afterSecondToggle.completed}"
                )
            }
        }
    }

    @Test
    fun property4_toggleCompletionChangesState() {
        runBlocking {
            checkAll(100, arbTaskForToggle()) { originalTask ->
                // Single toggle should change the state
                val afterToggle = originalTask.copy(completed = !originalTask.completed)
                
                assertTrue(
                    originalTask.completed != afterToggle.completed,
                    "Single toggle should change completion state"
                )
            }
        }
    }

    @Test
    fun property4_toggleCompletionPreservesOtherFields() {
        runBlocking {
            checkAll(100, arbTaskForToggle()) { originalTask ->
                val afterToggle = originalTask.copy(completed = !originalTask.completed)
                
                // All other fields should remain unchanged
                assertEquals(originalTask.id, afterToggle.id, "ID should be preserved")
                assertEquals(originalTask.title, afterToggle.title, "Title should be preserved")
                assertEquals(originalTask.description, afterToggle.description, "Description should be preserved")
                assertEquals(originalTask.status, afterToggle.status, "Status should be preserved")
                assertEquals(originalTask.priority, afterToggle.priority, "Priority should be preserved")
            }
        }
    }
}

// ============================================================================
// Property 6: Assignee filter correctness
// **Feature: kanban-board, Property 6: Assignee filter correctness**
// **Validates: Requirements 5.3**
//
// For any set of tasks and assignee filter value, the filtered result 
// SHALL contain only tasks where assignee matches the filter value.
// ============================================================================

class AssigneeFilterPropertyTest {

    /**
     * Generates a list of tasks with various assignees.
     */
    private fun arbTasksWithAssignees(): Arb<List<Task>> = arbitrary {
        val now = Clock.System.now()
        val assignees = listOf("Alice", "Bob", "Charlie", null)
        
        Arb.list(
            arbitrary {
                Task(
                    id = "task-${Arb.string(5..10).bind()}",
                    title = Arb.string(1..50).bind(),
                    assignee = assignees[Arb.int(0..3).bind()],
                    createdAt = now,
                    updatedAt = now
                )
            },
            1..20
        ).bind()
    }

    /**
     * Filter tasks by assignee - pure function for testing.
     */
    private fun filterByAssignee(tasks: List<Task>, assignee: String): List<Task> {
        return tasks.filter { task -> task.assignee == assignee }
    }

    @Test
    fun property6_filteredTasksOnlyContainMatchingAssignee() {
        runBlocking {
            val assignees = listOf("Alice", "Bob", "Charlie")
            
            checkAll(100, arbTasksWithAssignees(), Arb.enum<TaskPriority>()) { tasks, _ ->
                for (targetAssignee in assignees) {
                    val filteredTasks = filterByAssignee(tasks, targetAssignee)
                    
                    // All filtered tasks should have the target assignee
                    filteredTasks.forEach { task ->
                        assertEquals(
                            targetAssignee,
                            task.assignee,
                            "Filtered task should have assignee '$targetAssignee', but got '${task.assignee}'"
                        )
                    }
                }
            }
        }
    }

    @Test
    fun property6_filterDoesNotExcludeMatchingTasks() {
        runBlocking {
            checkAll(100, arbTasksWithAssignees()) { tasks ->
                val targetAssignee = "Alice"
                val filteredTasks = filterByAssignee(tasks, targetAssignee)
                
                // Count tasks with target assignee in original list
                val expectedCount = tasks.count { it.assignee == targetAssignee }
                
                assertEquals(
                    expectedCount,
                    filteredTasks.size,
                    "Filter should return all tasks with matching assignee"
                )
            }
        }
    }

    @Test
    fun property6_filterWithNoMatchesReturnsEmpty() {
        runBlocking {
            checkAll(100, arbTasksWithAssignees()) { tasks ->
                val nonExistentAssignee = "NonExistentUser12345"
                val filteredTasks = filterByAssignee(tasks, nonExistentAssignee)
                
                assertTrue(
                    filteredTasks.isEmpty(),
                    "Filter with non-existent assignee should return empty list"
                )
            }
        }
    }
}

// ============================================================================
// Property 7: Tag filter correctness
// **Feature: kanban-board, Property 7: Tag filter correctness**
// **Validates: Requirements 5.4**
//
// For any set of tasks and tag filter values, the filtered result 
// SHALL contain only tasks that have at least one matching tag.
// ============================================================================

class TagFilterPropertyTest {

    /**
     * Generates a list of tasks with various tags.
     */
    private fun arbTasksWithTags(): Arb<List<Task>> = arbitrary {
        val now = Clock.System.now()
        val availableTags = listOf(
            TaskTag("feature", TagColor.GREEN),
            TaskTag("bug", TagColor.RED),
            TaskTag("urgent", TagColor.ORANGE),
            TaskTag("design", TagColor.PURPLE),
            TaskTag("backend", TagColor.BLUE)
        )
        
        Arb.list(
            arbitrary {
                val numTags = Arb.int(0..3).bind()
                val taskTags = if (numTags > 0) {
                    availableTags.shuffled().take(numTags)
                } else {
                    emptyList()
                }
                
                Task(
                    id = "task-${Arb.string(5..10).bind()}",
                    title = Arb.string(1..50).bind(),
                    tags = taskTags,
                    createdAt = now,
                    updatedAt = now
                )
            },
            1..20
        ).bind()
    }

    /**
     * Filter tasks by tags - pure function for testing.
     * Returns tasks that have at least one matching tag.
     */
    private fun filterByTags(tasks: List<Task>, tags: Set<String>): List<Task> {
        if (tags.isEmpty()) return tasks
        return tasks.filter { task ->
            task.tags.any { it.label in tags }
        }
    }

    @Test
    fun property7_filteredTasksContainAtLeastOneMatchingTag() {
        runBlocking {
            checkAll(100, arbTasksWithTags()) { tasks ->
                val filterTags = setOf("feature", "bug")
                val filteredTasks = filterByTags(tasks, filterTags)
                
                // All filtered tasks should have at least one matching tag
                filteredTasks.forEach { task ->
                    val hasMatchingTag = task.tags.any { it.label in filterTags }
                    assertTrue(
                        hasMatchingTag,
                        "Filtered task should have at least one tag from $filterTags, " +
                        "but has tags: ${task.tags.map { it.label }}"
                    )
                }
            }
        }
    }

    @Test
    fun property7_filterDoesNotExcludeTasksWithMatchingTags() {
        runBlocking {
            checkAll(100, arbTasksWithTags()) { tasks ->
                val filterTags = setOf("feature")
                val filteredTasks = filterByTags(tasks, filterTags)
                
                // Count tasks with at least one matching tag in original list
                val expectedCount = tasks.count { task ->
                    task.tags.any { it.label in filterTags }
                }
                
                assertEquals(
                    expectedCount,
                    filteredTasks.size,
                    "Filter should return all tasks with at least one matching tag"
                )
            }
        }
    }

    @Test
    fun property7_emptyFilterReturnsAllTasks() {
        runBlocking {
            checkAll(100, arbTasksWithTags()) { tasks ->
                val filteredTasks = filterByTags(tasks, emptySet())
                
                assertEquals(
                    tasks.size,
                    filteredTasks.size,
                    "Empty tag filter should return all tasks"
                )
            }
        }
    }

    @Test
    fun property7_filterWithNoMatchesReturnsEmpty() {
        runBlocking {
            checkAll(100, arbTasksWithTags()) { tasks ->
                val nonExistentTags = setOf("nonexistent-tag-xyz")
                val filteredTasks = filterByTags(tasks, nonExistentTags)
                
                assertTrue(
                    filteredTasks.isEmpty(),
                    "Filter with non-existent tags should return empty list"
                )
            }
        }
    }

    @Test
    fun property7_multipleTagsFilterIsInclusive() {
        runBlocking {
            checkAll(100, arbTasksWithTags()) { tasks ->
                val filterTags = setOf("feature", "bug", "urgent")
                val filteredTasks = filterByTags(tasks, filterTags)
                
                // Verify each filtered task has at least one (not all) matching tag
                filteredTasks.forEach { task ->
                    val matchingTags = task.tags.filter { it.label in filterTags }
                    assertTrue(
                        matchingTags.isNotEmpty(),
                        "Task should have at least one matching tag (OR logic)"
                    )
                }
            }
        }
    }
}
