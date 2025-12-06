package com.example.sentio.domain.repositories

import com.example.sentio.domain.models.Note
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for note operations
 */
interface NoteRepository {
    /**
     * Get all notes as a reactive stream
     */
    fun getAllNotes(): Flow<List<Note>>

    /**
     * Get a single note by ID
     */
    suspend fun getNoteById(id: String): Note?

    /**
     * Get notes in a specific folder
     */
    fun getNotesByFolder(folderId: String): Flow<List<Note>>

    /**
     * Get notes with a specific tag
     */
    fun getNotesByTag(tagId: String): Flow<List<Note>>

    /**
     * Get pinned notes
     */
    fun getPinnedNotes(): Flow<List<Note>>

    /**
     * Get favorite notes
     */
    fun getFavoriteNotes(): Flow<List<Note>>

    /**
     * Create a new note
     */
    suspend fun createNote(note: Note): Result<Note>

    /**
     * Update an existing note
     */
    suspend fun updateNote(note: Note): Result<Note>

    /**
     * Delete a note
     */
    suspend fun deleteNote(id: String): Result<Unit>

    /**
     * Search notes by query
     */
    fun searchNotes(query: String): Flow<List<Note>>
}
