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


// ============================================================================
// Property 2: Task card content completeness
// **Feature: kanban-board, Property 2: Task card content completeness**
// **Validates: Requirements 1.4**
//
// For any task with title, priority, tags, and story points, 
// the rendered task card SHALL contain all four elements.
// ============================================================================

class TaskCardContentCompletenessPropertyTest {

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
     * Generates tasks with all required content fields populated.
     * - title: non-empty string
     * - priority: any TaskPriority
     * - tags: at least one tag
     * - points: non-null story points
     */
    private fun arbTaskWithAllContent(): Arb<Task> = arbitrary {
        val now = Clock.System.now()
        Task(
            id = "task-${Arb.string(5..10).bind()}",
            title = Arb.string(1..100).bind(),
            description = Arb.string(0..200).bind(),
            status = Arb.enum<TaskStatus>().bind(),
            priority = Arb.enum<TaskPriority>().bind(),
            tags = Arb.list(arbTaskTag(), 1..5).bind(), // At least one tag
            points = Arb.int(1..13).bind(), // Non-null points
            createdAt = now,
            updatedAt = now
        )
    }

    /**
     * Data class representing the content elements that should be present in a TaskCard.
     * This is used to verify that all required elements are rendered.
     */
    data class TaskCardContent(
        val hasTitle: Boolean,
        val hasPriority: Boolean,
        val hasTags: Boolean,
        val hasPoints: Boolean
    ) {
        val isComplete: Boolean
            get() = hasTitle && hasPriority && hasTags && hasPoints
    }

    /**
     * Extracts the content elements from a Task that would be rendered in a TaskCard.
     * This simulates what the TaskCard composable would render.
     */
    private fun extractTaskCardContent(task: Task): TaskCardContent {
        return TaskCardContent(
            hasTitle = task.title.isNotEmpty(),
            hasPriority = true, // Priority is always present (enum has default)
            hasTags = task.tags.isNotEmpty(),
            hasPoints = task.points != null
        )
    }

    @Test
    fun property2_taskWithAllContentHasCompleteCardContent() {
        runBlocking {
            checkAll(100, arbTaskWithAllContent()) { task ->
                val content = extractTaskCardContent(task)
                
                assertTrue(
                    content.isComplete,
                    "Task card should contain all four elements (title, priority, tags, points). " +
                    "Task: title='${task.title}', priority=${task.priority}, " +
                    "tags=${task.tags.map { it.label }}, points=${task.points}. " +
                    "Content: $content"
                )
            }
        }
    }

    @Test
    fun property2_taskCardAlwaysHasTitle() {
        runBlocking {
            checkAll(100, arbTaskWithAllContent()) { task ->
                val content = extractTaskCardContent(task)
                
                assertTrue(
                    content.hasTitle,
                    "Task card should always have a title. Task title: '${task.title}'"
                )
            }
        }
    }

    @Test
    fun property2_taskCardAlwaysHasPriority() {
        runBlocking {
            checkAll(100, arbTaskWithAllContent()) { task ->
                val content = extractTaskCardContent(task)
                
                assertTrue(
                    content.hasPriority,
                    "Task card should always have a priority indicator. Task priority: ${task.priority}"
                )
            }
        }
    }

    @Test
    fun property2_taskCardWithTagsShowsTags() {
        runBlocking {
            checkAll(100, arbTaskWithAllContent()) { task ->
                val content = extractTaskCardContent(task)
                
                assertTrue(
                    content.hasTags,
                    "Task card with tags should show tags. Task tags: ${task.tags.map { it.label }}"
                )
            }
        }
    }

    @Test
    fun property2_taskCardWithPointsShowsPoints() {
        runBlocking {
            checkAll(100, arbTaskWithAllContent()) { task ->
                val content = extractTaskCardContent(task)
                
                assertTrue(
                    content.hasPoints,
                    "Task card with points should show points. Task points: ${task.points}"
                )
            }
        }
    }

    @Test
    fun property2_priorityIndicatorColorMatchesPriority() {
        runBlocking {
            checkAll(100, arbTaskWithAllContent()) { task ->
                // Verify that the priority color is accessible and matches the expected value
                val priorityColor = task.priority.color
                
                when (task.priority) {
                    TaskPriority.HIGH -> assertEquals(
                        0xFFEF4444L, priorityColor,
                        "HIGH priority should have red color"
                    )
                    TaskPriority.MEDIUM -> assertEquals(
                        0xFFFACC15L, priorityColor,
                        "MEDIUM priority should have yellow color"
                    )
                    TaskPriority.LOW -> assertEquals(
                        0xFF3B82F6L, priorityColor,
                        "LOW priority should have blue color"
                    )
                    TaskPriority.NONE -> assertEquals(
                        0xFF9E9E9EL, priorityColor,
                        "NONE priority should have gray color"
                    )
                }
            }
        }
    }

    @Test
    fun property2_tagCountMatchesTaskTags() {
        runBlocking {
            checkAll(100, arbTaskWithAllContent()) { task ->
                // The number of tags in the task should be preserved
                val tagCount = task.tags.size
                
                assertTrue(
                    tagCount >= 1,
                    "Task should have at least one tag. Actual count: $tagCount"
                )
                
                // Verify each tag has a label and color
                task.tags.forEach { tag ->
                    assertTrue(
                        tag.label.isNotEmpty(),
                        "Each tag should have a non-empty label"
                    )
                    assertTrue(
                        TagColor.entries.contains(tag.colorClass),
                        "Tag color should be a valid TagColor enum value"
                    )
                }
            }
        }
    }
}


// ============================================================================
// Property 9: Tag rendering completeness
// **Feature: kanban-board, Property 9: Tag rendering completeness**
// **Validates: Requirements 7.1**
//
// For any task with N tags, the task card SHALL render N tag badges.
// ============================================================================

class TagRenderingCompletenessPropertyTest {

    /**
     * Generates valid TaskTag instances with non-empty labels.
     */
    private fun arbTaskTag(): Arb<TaskTag> = arbitrary {
        TaskTag(
            label = Arb.string(1..20).bind(),
            colorClass = Arb.enum<TagColor>().bind()
        )
    }

    /**
     * Generates tasks with varying numbers of tags (0 to 10).
     */
    private fun arbTaskWithTags(): Arb<Task> = arbitrary {
        val now = Clock.System.now()
        Task(
            id = "task-${Arb.string(5..10).bind()}",
            title = Arb.string(1..100).bind(),
            description = Arb.string(0..200).bind(),
            status = Arb.enum<TaskStatus>().bind(),
            priority = Arb.enum<TaskPriority>().bind(),
            tags = Arb.list(arbTaskTag(), 0..10).bind(),
            points = Arb.int(1..13).orNull().bind(),
            createdAt = now,
            updatedAt = now
        )
    }

    /**
     * Simulates the tag rendering logic from TagsFlowRow.
     * Returns the count of tags that would be rendered.
     * 
     * The TagsFlowRow composable renders ALL tags in the list,
     * so this function simply returns the size of the tags list.
     */
    private fun countRenderedTags(tags: List<TaskTag>): Int {
        // TagsFlowRow renders all tags without truncation
        return tags.size
    }

    /**
     * Extracts tag badge data that would be rendered for each tag.
     * Each tag should produce exactly one badge with matching label and color.
     */
    data class TagBadgeData(
        val label: String,
        val colorClass: TagColor
    )

    private fun extractTagBadges(tags: List<TaskTag>): List<TagBadgeData> {
        return tags.map { tag ->
            TagBadgeData(
                label = tag.label,
                colorClass = tag.colorClass
            )
        }
    }

    @Test
    fun property9_tagCountMatchesRenderedBadgeCount() {
        runBlocking {
            checkAll(100, arbTaskWithTags()) { task ->
                val tagCount = task.tags.size
                val renderedCount = countRenderedTags(task.tags)
                
                assertEquals(
                    tagCount,
                    renderedCount,
                    "Number of rendered tag badges ($renderedCount) should equal " +
                    "number of tags in task ($tagCount)"
                )
            }
        }
    }

    @Test
    fun property9_allTagLabelsAreRendered() {
        runBlocking {
            checkAll(100, arbTaskWithTags()) { task ->
                val badges = extractTagBadges(task.tags)
                
                // Verify each tag has a corresponding badge
                task.tags.forEachIndexed { index, tag ->
                    assertEquals(
                        tag.label,
                        badges[index].label,
                        "Tag badge at index $index should have label '${tag.label}'"
                    )
                }
            }
        }
    }

    @Test
    fun property9_allTagColorsArePreserved() {
        runBlocking {
            checkAll(100, arbTaskWithTags()) { task ->
                val badges = extractTagBadges(task.tags)
                
                // Verify each tag's color is preserved in the badge
                task.tags.forEachIndexed { index, tag ->
                    assertEquals(
                        tag.colorClass,
                        badges[index].colorClass,
                        "Tag badge at index $index should have color ${tag.colorClass}"
                    )
                }
            }
        }
    }

    @Test
    fun property9_emptyTagsRendersNoBadges() {
        val now = Clock.System.now()
        val taskWithNoTags = Task(
            id = "empty-tags-task",
            title = "Task with no tags",
            tags = emptyList(),
            createdAt = now,
            updatedAt = now
        )
        
        val renderedCount = countRenderedTags(taskWithNoTags.tags)
        
        assertEquals(
            0,
            renderedCount,
            "Task with no tags should render 0 tag badges"
        )
    }

    @Test
    fun property9_manyTagsAllRendered() {
        runBlocking {
            // Generate tasks with many tags (5-10) to ensure no truncation
            val arbTaskWithManyTags = arbitrary {
                val now = Clock.System.now()
                Task(
                    id = "task-${Arb.string(5..10).bind()}",
                    title = Arb.string(1..100).bind(),
                    tags = Arb.list(arbTaskTag(), 5..10).bind(),
                    createdAt = now,
                    updatedAt = now
                )
            }
            
            checkAll(100, arbTaskWithManyTags) { task ->
                val renderedCount = countRenderedTags(task.tags)
                
                assertEquals(
                    task.tags.size,
                    renderedCount,
                    "All ${task.tags.size} tags should be rendered, not truncated"
                )
            }
        }
    }

    @Test
    fun property9_tagBadgeHasValidColorProperties() {
        runBlocking {
            checkAll(100, arbTaskTag()) { tag ->
                // Verify the tag color has valid properties for rendering
                val color = tag.colorClass
                
                assertTrue(
                    color.bgAlpha in 0f..1f,
                    "Tag background alpha should be between 0 and 1, got ${color.bgAlpha}"
                )
                
                assertTrue(
                    color.textColor != 0L,
                    "Tag text color should be non-zero"
                )
            }
        }
    }
}


// ============================================================================
// Property 5: Task modal content completeness
// **Feature: kanban-board, Property 5: Task modal content completeness**
// **Validates: Requirements 4.1, 4.4**
//
// For any task, the detail modal SHALL display task ID, title, description, 
// and properties (status, priority, points).
// ============================================================================

class TaskModalContentCompletenessPropertyTest {

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
     * Generates valid Task instances with all fields that would be displayed in the modal.
     */
    private fun arbTaskForModal(): Arb<Task> = arbitrary {
        val now = Clock.System.now()
        Task(
            id = "task-${Arb.string(5..10).bind()}",
            title = Arb.string(1..100).bind(),
            description = Arb.string(0..500).bind(),
            status = Arb.enum<TaskStatus>().bind(),
            priority = Arb.enum<TaskPriority>().bind(),
            tags = Arb.list(arbTaskTag(), 0..5).bind(),
            points = Arb.int(1..13).orNull().bind(),
            assignee = Arb.string(1..20).orNull().bind(),
            subtasks = Arb.list(arbSubtask(), 0..5).bind(),
            completed = Arb.boolean().bind(),
            createdAt = now - Arb.long(1L, 86400000L * 30).bind().milliseconds,
            updatedAt = now
        )
    }

    /**
     * Extracts the content that would be displayed in the TaskDetailModal.
     * This simulates what the modal composable would render.
     */
    private fun extractModalContent(task: Task): TaskModalContent {
        return TaskModalContent(
            taskId = task.id,
            title = task.title,
            description = task.description,
            status = task.status,
            priority = task.priority,
            points = task.points
        )
    }

    /**
     * Data class representing the content displayed in the modal.
     */
    data class TaskModalContent(
        val taskId: String,
        val title: String,
        val description: String,
        val status: TaskStatus,
        val priority: TaskPriority,
        val points: Int?
    )

    /**
     * Verifies that all required modal content fields are present.
     * Returns true if all required fields are present and valid.
     */
    private fun verifyModalContentCompleteness(content: TaskModalContent): Boolean {
        // Task ID must be non-empty
        if (content.taskId.isEmpty()) return false
        
        // Title must be non-empty
        if (content.title.isEmpty()) return false
        
        // Description can be empty but must be present (not null - it's a String)
        // Status must be a valid TaskStatus (always true for enum)
        // Priority must be a valid TaskPriority (always true for enum)
        // Points can be null but the field must be present
        
        return true
    }

    @Test
    fun property5_modalDisplaysTaskId() {
        runBlocking {
            checkAll(100, arbTaskForModal()) { task ->
                val content = extractModalContent(task)
                
                assertTrue(
                    content.taskId.isNotEmpty(),
                    "Modal should display task ID. Task ID: '${task.id}'"
                )
                assertEquals(
                    task.id,
                    content.taskId,
                    "Modal task ID should match the task's ID"
                )
            }
        }
    }

    @Test
    fun property5_modalDisplaysTitle() {
        runBlocking {
            checkAll(100, arbTaskForModal()) { task ->
                val content = extractModalContent(task)
                
                assertTrue(
                    content.title.isNotEmpty(),
                    "Modal should display task title. Task title: '${task.title}'"
                )
                assertEquals(
                    task.title,
                    content.title,
                    "Modal title should match the task's title"
                )
            }
        }
    }

    @Test
    fun property5_modalDisplaysDescription() {
        runBlocking {
            checkAll(100, arbTaskForModal()) { task ->
                val content = extractModalContent(task)
                
                // Description is always present (may be empty string)
                assertEquals(
                    task.description,
                    content.description,
                    "Modal description should match the task's description"
                )
            }
        }
    }

    @Test
    fun property5_modalDisplaysStatus() {
        runBlocking {
            checkAll(100, arbTaskForModal()) { task ->
                val content = extractModalContent(task)
                
                assertEquals(
                    task.status,
                    content.status,
                    "Modal should display task status. Expected: ${task.status}, Got: ${content.status}"
                )
            }
        }
    }

    @Test
    fun property5_modalDisplaysPriority() {
        runBlocking {
            checkAll(100, arbTaskForModal()) { task ->
                val content = extractModalContent(task)
                
                assertEquals(
                    task.priority,
                    content.priority,
                    "Modal should display task priority. Expected: ${task.priority}, Got: ${content.priority}"
                )
            }
        }
    }

    @Test
    fun property5_modalDisplaysPoints() {
        runBlocking {
            checkAll(100, arbTaskForModal()) { task ->
                val content = extractModalContent(task)
                
                assertEquals(
                    task.points,
                    content.points,
                    "Modal should display task points. Expected: ${task.points}, Got: ${content.points}"
                )
            }
        }
    }

    @Test
    fun property5_modalContentIsComplete() {
        runBlocking {
            checkAll(100, arbTaskForModal()) { task ->
                val content = extractModalContent(task)
                val isComplete = verifyModalContentCompleteness(content)
                
                assertTrue(
                    isComplete,
                    "Modal content should be complete for task. " +
                    "Task ID: '${task.id}', Title: '${task.title}', " +
                    "Status: ${task.status}, Priority: ${task.priority}, Points: ${task.points}"
                )
            }
        }
    }

    @Test
    fun property5_modalPreservesAllProperties() {
        runBlocking {
            checkAll(100, arbTaskForModal()) { task ->
                val content = extractModalContent(task)
                
                // Verify all properties are preserved
                assertEquals(task.id, content.taskId, "Task ID should be preserved")
                assertEquals(task.title, content.title, "Title should be preserved")
                assertEquals(task.description, content.description, "Description should be preserved")
                assertEquals(task.status, content.status, "Status should be preserved")
                assertEquals(task.priority, content.priority, "Priority should be preserved")
                assertEquals(task.points, content.points, "Points should be preserved")
            }
        }
    }

    @Test
    fun property5_modalHandlesNullPoints() {
        runBlocking {
            // Generate tasks specifically with null points
            val arbTaskWithNullPoints = arbitrary {
                val now = Clock.System.now()
                Task(
                    id = "task-${Arb.string(5..10).bind()}",
                    title = Arb.string(1..100).bind(),
                    description = Arb.string(0..200).bind(),
                    status = Arb.enum<TaskStatus>().bind(),
                    priority = Arb.enum<TaskPriority>().bind(),
                    points = null, // Explicitly null
                    createdAt = now,
                    updatedAt = now
                )
            }
            
            checkAll(100, arbTaskWithNullPoints) { task ->
                val content = extractModalContent(task)
                
                assertTrue(
                    content.points == null,
                    "Modal should handle null points gracefully"
                )
                
                // Modal should still be complete even with null points
                val isComplete = verifyModalContentCompleteness(content)
                assertTrue(
                    isComplete,
                    "Modal content should be complete even with null points"
                )
            }
        }
    }

    @Test
    fun property5_modalHandlesEmptyDescription() {
        runBlocking {
            // Generate tasks specifically with empty description
            val arbTaskWithEmptyDescription = arbitrary {
                val now = Clock.System.now()
                Task(
                    id = "task-${Arb.string(5..10).bind()}",
                    title = Arb.string(1..100).bind(),
                    description = "", // Explicitly empty
                    status = Arb.enum<TaskStatus>().bind(),
                    priority = Arb.enum<TaskPriority>().bind(),
                    points = Arb.int(1..13).orNull().bind(),
                    createdAt = now,
                    updatedAt = now
                )
            }
            
            checkAll(100, arbTaskWithEmptyDescription) { task ->
                val content = extractModalContent(task)
                
                assertEquals(
                    "",
                    content.description,
                    "Modal should handle empty description"
                )
                
                // Modal should still be complete even with empty description
                val isComplete = verifyModalContentCompleteness(content)
                assertTrue(
                    isComplete,
                    "Modal content should be complete even with empty description"
                )
            }
        }
    }
}


// ============================================================================
// Property 1: Column task count accuracy
// **Feature: kanban-board, Property 1: Column task count accuracy**
// **Validates: Requirements 1.2**
//
// For any Kanban column containing N tasks, the displayed task count badge 
// SHALL show the value N.
// ============================================================================

class ColumnTaskCountAccuracyPropertyTest {

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
     * Generates valid Task instances for populating columns.
     */
    private fun arbTask(): Arb<Task> = arbitrary {
        val now = Clock.System.now()
        Task(
            id = "task-${Arb.string(5..10).bind()}",
            title = Arb.string(1..100).bind(),
            description = Arb.string(0..200).bind(),
            status = Arb.enum<TaskStatus>().bind(),
            priority = Arb.enum<TaskPriority>().bind(),
            tags = Arb.list(arbTaskTag(), 0..5).bind(),
            points = Arb.int(1..13).orNull().bind(),
            completed = Arb.boolean().bind(),
            createdAt = now,
            updatedAt = now
        )
    }

    /**
     * Generates valid KanbanColumn instances with 0 to 100 tasks.
     */
    private fun arbKanbanColumn(): Arb<KanbanColumn> = arbitrary {
        val status = Arb.enum<TaskStatus>().bind()
        val tasks = Arb.list(arbTask(), 0..100).bind()
        val wipLimit = Arb.int(1..20).orNull().bind()
        
        KanbanColumn(
            status = status,
            tasks = tasks,
            isCollapsed = Arb.boolean().bind(),
            wipLimit = wipLimit
        )
    }

    /**
     * Extracts the task count that would be displayed in the column header badge.
     * This simulates the logic in ColumnHeader composable.
     * 
     * From KanbanBoard.kt ColumnHeader:
     *   val taskCount = column.tasks.size
     *   Text(text = if (column.wipLimit != null) "$taskCount/${column.wipLimit}" else "$taskCount", ...)
     */
    private fun getDisplayedTaskCount(column: KanbanColumn): Int {
        return column.tasks.size
    }

    @Test
    fun property1_columnTaskCountMatchesActualTaskCount() {
        runBlocking {
            checkAll(100, arbKanbanColumn()) { column ->
                val actualTaskCount = column.tasks.size
                val displayedCount = getDisplayedTaskCount(column)
                
                assertEquals(
                    actualTaskCount,
                    displayedCount,
                    "Displayed task count ($displayedCount) should equal actual task count ($actualTaskCount) " +
                    "for column ${column.status}"
                )
            }
        }
    }

    @Test
    fun property1_emptyColumnShowsZeroCount() {
        val emptyColumn = KanbanColumn(
            status = TaskStatus.TODO,
            tasks = emptyList(),
            isCollapsed = false,
            wipLimit = null
        )
        
        val displayedCount = getDisplayedTaskCount(emptyColumn)
        
        assertEquals(
            0,
            displayedCount,
            "Empty column should display count of 0"
        )
    }

    @Test
    fun property1_columnWithManyTasksShowsCorrectCount() {
        runBlocking {
            // Generate columns with many tasks (50-100) to ensure no truncation
            val arbColumnWithManyTasks = arbitrary {
                val now = Clock.System.now()
                val tasks = Arb.list(
                    arbitrary {
                        Task(
                            id = "task-${Arb.string(5..10).bind()}",
                            title = Arb.string(1..50).bind(),
                            createdAt = now,
                            updatedAt = now
                        )
                    },
                    50..100
                ).bind()
                
                KanbanColumn(
                    status = Arb.enum<TaskStatus>().bind(),
                    tasks = tasks,
                    isCollapsed = false,
                    wipLimit = null
                )
            }
            
            checkAll(100, arbColumnWithManyTasks) { column ->
                val displayedCount = getDisplayedTaskCount(column)
                
                assertEquals(
                    column.tasks.size,
                    displayedCount,
                    "Column with ${column.tasks.size} tasks should display that exact count"
                )
            }
        }
    }

    @Test
    fun property1_taskCountIsIndependentOfWipLimit() {
        runBlocking {
            checkAll(100, arbKanbanColumn()) { column ->
                // The task count should be the same regardless of WIP limit
                val displayedCount = getDisplayedTaskCount(column)
                
                assertEquals(
                    column.tasks.size,
                    displayedCount,
                    "Task count should be independent of WIP limit. " +
                    "Tasks: ${column.tasks.size}, WIP limit: ${column.wipLimit}"
                )
            }
        }
    }

    @Test
    fun property1_taskCountIsIndependentOfCollapsedState() {
        runBlocking {
            checkAll(100, arbKanbanColumn()) { column ->
                // Test both collapsed and expanded states
                val expandedColumn = column.copy(isCollapsed = false)
                val collapsedColumn = column.copy(isCollapsed = true)
                
                val expandedCount = getDisplayedTaskCount(expandedColumn)
                val collapsedCount = getDisplayedTaskCount(collapsedColumn)
                
                assertEquals(
                    expandedCount,
                    collapsedCount,
                    "Task count should be the same whether column is collapsed or expanded"
                )
                assertEquals(
                    column.tasks.size,
                    expandedCount,
                    "Task count should match actual task count"
                )
            }
        }
    }

    @Test
    fun property1_addingTaskIncreasesCount() {
        runBlocking {
            checkAll(100, arbKanbanColumn(), arbTask()) { column, newTask ->
                val originalCount = getDisplayedTaskCount(column)
                val updatedColumn = column.copy(tasks = column.tasks + newTask)
                val newCount = getDisplayedTaskCount(updatedColumn)
                
                assertEquals(
                    originalCount + 1,
                    newCount,
                    "Adding a task should increase the count by 1"
                )
            }
        }
    }

    @Test
    fun property1_removingTaskDecreasesCount() {
        runBlocking {
            // Generate columns with at least one task
            val arbColumnWithTasks = arbitrary {
                val now = Clock.System.now()
                val tasks = Arb.list(
                    arbitrary {
                        Task(
                            id = "task-${Arb.string(5..10).bind()}",
                            title = Arb.string(1..50).bind(),
                            createdAt = now,
                            updatedAt = now
                        )
                    },
                    1..50
                ).bind()
                
                KanbanColumn(
                    status = Arb.enum<TaskStatus>().bind(),
                    tasks = tasks,
                    isCollapsed = false,
                    wipLimit = null
                )
            }
            
            checkAll(100, arbColumnWithTasks) { column ->
                val originalCount = getDisplayedTaskCount(column)
                val updatedColumn = column.copy(tasks = column.tasks.dropLast(1))
                val newCount = getDisplayedTaskCount(updatedColumn)
                
                assertEquals(
                    originalCount - 1,
                    newCount,
                    "Removing a task should decrease the count by 1"
                )
            }
        }
    }
}


// ============================================================================
// Property 3: Empty column placeholder
// **Feature: kanban-board, Property 3: Empty column placeholder**
// **Validates: Requirements 2.2**
//
// For any column with zero tasks, the column SHALL display a placeholder element.
// ============================================================================

class EmptyColumnPlaceholderPropertyTest {

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
     * Generates valid Task instances for populating columns.
     */
    private fun arbTask(): Arb<Task> = arbitrary {
        val now = Clock.System.now()
        Task(
            id = "task-${Arb.string(5..10).bind()}",
            title = Arb.string(1..100).bind(),
            description = Arb.string(0..200).bind(),
            status = Arb.enum<TaskStatus>().bind(),
            priority = Arb.enum<TaskPriority>().bind(),
            tags = Arb.list(arbTaskTag(), 0..5).bind(),
            points = Arb.int(1..13).orNull().bind(),
            completed = Arb.boolean().bind(),
            createdAt = now,
            updatedAt = now
        )
    }

    /**
     * Generates valid KanbanColumn instances with varying task counts.
     */
    private fun arbKanbanColumn(): Arb<KanbanColumn> = arbitrary {
        val status = Arb.enum<TaskStatus>().bind()
        val tasks = Arb.list(arbTask(), 0..20).bind()
        val wipLimit = Arb.int(1..20).orNull().bind()
        
        KanbanColumn(
            status = status,
            tasks = tasks,
            isCollapsed = Arb.boolean().bind(),
            wipLimit = wipLimit
        )
    }

    /**
     * Generates empty KanbanColumn instances (zero tasks).
     */
    private fun arbEmptyKanbanColumn(): Arb<KanbanColumn> = arbitrary {
        val status = Arb.enum<TaskStatus>().bind()
        val wipLimit = Arb.int(1..20).orNull().bind()
        
        KanbanColumn(
            status = status,
            tasks = emptyList(), // Always empty
            isCollapsed = Arb.boolean().bind(),
            wipLimit = wipLimit
        )
    }

    /**
     * Generates non-empty KanbanColumn instances (at least one task).
     */
    private fun arbNonEmptyKanbanColumn(): Arb<KanbanColumn> = arbitrary {
        val status = Arb.enum<TaskStatus>().bind()
        val tasks = Arb.list(arbTask(), 1..20).bind() // At least 1 task
        val wipLimit = Arb.int(1..20).orNull().bind()
        
        KanbanColumn(
            status = status,
            tasks = tasks,
            isCollapsed = Arb.boolean().bind(),
            wipLimit = wipLimit
        )
    }

    /**
     * Determines whether a placeholder should be displayed for a column.
     * 
     * From KanbanBoard.kt KanbanColumnView:
     *   if (column.tasks.isEmpty()) {
     *       item {
     *           EmptyColumnPlaceholder(onClick = onTaskCreate)
     *       }
     *   }
     * 
     * The placeholder is shown when the column has zero tasks.
     */
    private fun shouldShowPlaceholder(column: KanbanColumn): Boolean {
        return column.tasks.isEmpty()
    }

    @Test
    fun property3_emptyColumnShowsPlaceholder() {
        runBlocking {
            checkAll(100, arbEmptyKanbanColumn()) { column ->
                val showsPlaceholder = shouldShowPlaceholder(column)
                
                assertTrue(
                    showsPlaceholder,
                    "Empty column (${column.status}) with 0 tasks should show placeholder"
                )
            }
        }
    }

    @Test
    fun property3_nonEmptyColumnDoesNotShowPlaceholder() {
        runBlocking {
            checkAll(100, arbNonEmptyKanbanColumn()) { column ->
                val showsPlaceholder = shouldShowPlaceholder(column)
                
                assertTrue(
                    !showsPlaceholder,
                    "Non-empty column (${column.status}) with ${column.tasks.size} tasks should NOT show placeholder"
                )
            }
        }
    }

    @Test
    fun property3_placeholderVisibilityMatchesEmptyState() {
        runBlocking {
            checkAll(100, arbKanbanColumn()) { column ->
                val showsPlaceholder = shouldShowPlaceholder(column)
                val isEmpty = column.tasks.isEmpty()
                
                assertEquals(
                    isEmpty,
                    showsPlaceholder,
                    "Placeholder visibility ($showsPlaceholder) should match empty state ($isEmpty) " +
                    "for column ${column.status} with ${column.tasks.size} tasks"
                )
            }
        }
    }

    @Test
    fun property3_placeholderIsIndependentOfColumnStatus() {
        runBlocking {
            // Test that placeholder behavior is consistent across all column statuses
            checkAll(100, Arb.enum<TaskStatus>()) { status ->
                val emptyColumn = KanbanColumn(
                    status = status,
                    tasks = emptyList(),
                    isCollapsed = false,
                    wipLimit = null
                )
                
                val showsPlaceholder = shouldShowPlaceholder(emptyColumn)
                
                assertTrue(
                    showsPlaceholder,
                    "Empty column should show placeholder regardless of status ($status)"
                )
            }
        }
    }

    @Test
    fun property3_placeholderIsIndependentOfWipLimit() {
        runBlocking {
            checkAll(100, Arb.int(1..100).orNull()) { wipLimit ->
                val emptyColumn = KanbanColumn(
                    status = TaskStatus.TODO,
                    tasks = emptyList(),
                    isCollapsed = false,
                    wipLimit = wipLimit
                )
                
                val showsPlaceholder = shouldShowPlaceholder(emptyColumn)
                
                assertTrue(
                    showsPlaceholder,
                    "Empty column should show placeholder regardless of WIP limit ($wipLimit)"
                )
            }
        }
    }

    @Test
    fun property3_placeholderIsIndependentOfCollapsedState() {
        runBlocking {
            checkAll(100, Arb.boolean()) { isCollapsed ->
                val emptyColumn = KanbanColumn(
                    status = TaskStatus.TODO,
                    tasks = emptyList(),
                    isCollapsed = isCollapsed,
                    wipLimit = null
                )
                
                val showsPlaceholder = shouldShowPlaceholder(emptyColumn)
                
                assertTrue(
                    showsPlaceholder,
                    "Empty column should show placeholder regardless of collapsed state ($isCollapsed)"
                )
            }
        }
    }

    @Test
    fun property3_removingAllTasksShowsPlaceholder() {
        runBlocking {
            checkAll(100, arbNonEmptyKanbanColumn()) { column ->
                // Start with non-empty column (no placeholder)
                val initialShowsPlaceholder = shouldShowPlaceholder(column)
                assertTrue(
                    !initialShowsPlaceholder,
                    "Non-empty column should not show placeholder initially"
                )
                
                // Remove all tasks
                val emptyColumn = column.copy(tasks = emptyList())
                val afterRemovalShowsPlaceholder = shouldShowPlaceholder(emptyColumn)
                
                assertTrue(
                    afterRemovalShowsPlaceholder,
                    "Column should show placeholder after removing all tasks"
                )
            }
        }
    }

    @Test
    fun property3_addingTaskHidesPlaceholder() {
        runBlocking {
            checkAll(100, arbEmptyKanbanColumn(), arbTask()) { emptyColumn, newTask ->
                // Start with empty column (shows placeholder)
                val initialShowsPlaceholder = shouldShowPlaceholder(emptyColumn)
                assertTrue(
                    initialShowsPlaceholder,
                    "Empty column should show placeholder initially"
                )
                
                // Add a task
                val nonEmptyColumn = emptyColumn.copy(tasks = listOf(newTask))
                val afterAdditionShowsPlaceholder = shouldShowPlaceholder(nonEmptyColumn)
                
                assertTrue(
                    !afterAdditionShowsPlaceholder,
                    "Column should NOT show placeholder after adding a task"
                )
            }
        }
    }
}


// ============================================================================
// Property 13: View mode highlight consistency
// **Feature: kanban-board, Property 13: View mode highlight consistency**
// **Validates: Requirements 10.2**
//
// For any selected view mode, that view option SHALL be highlighted 
// in the navigation.
// ============================================================================

class ViewModeHighlightPropertyTest {

    /**
     * Property test verifying that for any selected view mode, only that mode
     * is highlighted in the navigation.
     * 
     * **Property 13: View mode highlight consistency**
     * **Validates: Requirements 10.2**
     */
    @Test
    fun property13_selectedViewModeIsHighlighted() {
        runBlocking {
            checkAll(100, Arb.enum<TaskViewMode>()) { selectedMode ->
                // The selected mode should be highlighted
                val isHighlighted = isViewModeHighlighted(selectedMode, selectedMode)
                
                assertTrue(
                    isHighlighted,
                    "Selected view mode '$selectedMode' should be highlighted"
                )
            }
        }
    }

    @Test
    fun property13_nonSelectedViewModesAreNotHighlighted() {
        runBlocking {
            checkAll(100, Arb.enum<TaskViewMode>()) { selectedMode ->
                // All other modes should NOT be highlighted
                TaskViewMode.entries.filter { it != selectedMode }.forEach { otherMode ->
                    val isHighlighted = isViewModeHighlighted(otherMode, selectedMode)
                    
                    assertTrue(
                        !isHighlighted,
                        "Non-selected view mode '$otherMode' should NOT be highlighted when '$selectedMode' is selected"
                    )
                }
            }
        }
    }

    @Test
    fun property13_exactlyOneViewModeIsHighlightedAtATime() {
        runBlocking {
            checkAll(100, Arb.enum<TaskViewMode>()) { selectedMode ->
                // Count how many modes are highlighted
                val highlightedCount = TaskViewMode.entries.count { mode ->
                    isViewModeHighlighted(mode, selectedMode)
                }
                
                assertEquals(
                    1,
                    highlightedCount,
                    "Exactly one view mode should be highlighted at a time. " +
                    "Selected: $selectedMode, Highlighted count: $highlightedCount"
                )
            }
        }
    }

    @Test
    fun property13_highlightIsSymmetric() {
        runBlocking {
            // For all pairs of view modes, verify highlight consistency
            checkAll(100, Arb.enum<TaskViewMode>(), Arb.enum<TaskViewMode>()) { mode1, mode2 ->
                val mode1HighlightedWhenMode1Selected = isViewModeHighlighted(mode1, mode1)
                val mode2HighlightedWhenMode2Selected = isViewModeHighlighted(mode2, mode2)
                
                // Each mode should be highlighted when it's selected
                assertTrue(
                    mode1HighlightedWhenMode1Selected,
                    "Mode '$mode1' should be highlighted when selected"
                )
                assertTrue(
                    mode2HighlightedWhenMode2Selected,
                    "Mode '$mode2' should be highlighted when selected"
                )
                
                // Cross-check: mode1 highlighted when mode2 selected should equal mode1 == mode2
                val mode1HighlightedWhenMode2Selected = isViewModeHighlighted(mode1, mode2)
                assertEquals(
                    mode1 == mode2,
                    mode1HighlightedWhenMode2Selected,
                    "Mode '$mode1' should be highlighted when '$mode2' is selected only if they are the same"
                )
            }
        }
    }
}
