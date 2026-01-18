package com.example.klarity.domain.usecase

import com.example.klarity.domain.models.Note
import com.example.klarity.domain.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for CreateNoteUseCase.
 * Tests input validation and note creation logic.
 */
class CreateNoteUseCaseTest {

    private class FakeNoteRepository : NoteRepository {
        var lastCreatedNote: Note? = null

        override fun getAllNotes(): Flow<List<Note>> = flowOf(emptyList())
        override suspend fun getNoteById(id: String): Note? = null
        override fun getNotesByFolder(folderId: String): Flow<List<Note>> = flowOf(emptyList())
        override fun getNotesByTag(tagId: String): Flow<List<Note>> = flowOf(emptyList())
        override fun getPinnedNotes(): Flow<List<Note>> = flowOf(emptyList())
        override fun getFavoriteNotes(): Flow<List<Note>> = flowOf(emptyList())
        override suspend fun createNote(note: Note): Result<Note> {
            lastCreatedNote = note
            return Result.success(note)
        }
        override suspend fun updateNote(note: Note): Result<Note> = Result.success(note)
        override suspend fun deleteNote(id: String): Result<Unit> = Result.success(Unit)
        override fun searchNotes(query: String): Flow<List<Note>> = flowOf(emptyList())
    }

    @Test
    fun `createNote with valid title creates note successfully`() = runTest {
        val repository = FakeNoteRepository()
        val useCase = CreateNoteUseCase(repository)

        val result = useCase(title = "Test Note")

        assertTrue(result.isSuccess)
        assertEquals("Test Note", repository.lastCreatedNote?.title)
    }

    @Test
    fun `createNote with blank title defaults to Untitled`() = runTest {
        val repository = FakeNoteRepository()
        val useCase = CreateNoteUseCase(repository)

        val result = useCase(title = "   ")

        assertTrue(result.isSuccess)
        assertEquals("Untitled", repository.lastCreatedNote?.title)
    }

    @Test
    fun `createNote trims whitespace from title`() = runTest {
        val repository = FakeNoteRepository()
        val useCase = CreateNoteUseCase(repository)

        val result = useCase(title = "  My Note  ")

        assertTrue(result.isSuccess)
        assertEquals("My Note", repository.lastCreatedNote?.title)
    }

    @Test
    fun `createNote with very long title returns failure`() = runTest {
        val repository = FakeNoteRepository()
        val useCase = CreateNoteUseCase(repository)

        val longTitle = "A".repeat(501)
        val result = useCase(title = longTitle)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `createNote with folderId assigns folder correctly`() = runTest {
        val repository = FakeNoteRepository()
        val useCase = CreateNoteUseCase(repository)

        val result = useCase(title = "Test", folderId = "folder-123")

        assertTrue(result.isSuccess)
        assertEquals("folder-123", repository.lastCreatedNote?.folderId)
    }

    @Test
    fun `createNote generates unique id`() = runTest {
        val repository = FakeNoteRepository()
        val useCase = CreateNoteUseCase(repository)

        useCase(title = "Note 1")
        val firstId = repository.lastCreatedNote?.id

        useCase(title = "Note 2")
        val secondId = repository.lastCreatedNote?.id

        assertTrue(firstId != secondId, "Each note should have a unique ID")
    }
}
