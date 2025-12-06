package com.example.klarity.domain.usecase

import com.benasher44.uuid.uuid4
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.repositories.NoteRepository
import kotlinx.datetime.Clock

/**
 * Use case for creating a new note
 */
class CreateNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(
        title: String,
        content: String = "",
        folderId: String? = null
    ): Result<Note> {
        val now = Clock.System.now()
        val note = Note(
            id = uuid4().toString(),
            title = title,
            content = content,
            folderId = folderId,
            tags = emptyList(),
            createdAt = now,
            updatedAt = now
        )
        return noteRepository.createNote(note)
    }
}
