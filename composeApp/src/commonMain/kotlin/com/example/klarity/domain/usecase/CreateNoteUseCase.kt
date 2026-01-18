package com.example.klarity.domain.usecase

import com.benasher44.uuid.uuid4
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.repositories.NoteRepository
import kotlinx.datetime.Clock

/**
 * Use case for creating a new note.
 * Includes validation for required fields.
 */
class CreateNoteUseCase(
    private val noteRepository: NoteRepository
) {
    /**
     * Creates a new note with the given parameters.
     *
     * @param title The note title (will be trimmed, defaults to "Untitled" if blank)
     * @param content The note content
     * @param folderId Optional folder ID for organization
     * @return Result containing the created note or an error
     */
    suspend operator fun invoke(
        title: String,
        content: String = "",
        folderId: String? = null
    ): Result<Note> {
        // Validate and sanitize input
        val sanitizedTitle = title.trim().ifBlank { "Untitled" }
        val sanitizedContent = content.trim()

        // Validate title length (reasonable limit)
        if (sanitizedTitle.length > 500) {
            return Result.failure(IllegalArgumentException("Title exceeds maximum length of 500 characters"))
        }

        val now = Clock.System.now()
        val note = Note(
            id = uuid4().toString(),
            title = sanitizedTitle,
            content = sanitizedContent,
            folderId = folderId,
            tags = emptyList(),
            createdAt = now,
            updatedAt = now
        )
        return noteRepository.createNote(note)
    }
}
