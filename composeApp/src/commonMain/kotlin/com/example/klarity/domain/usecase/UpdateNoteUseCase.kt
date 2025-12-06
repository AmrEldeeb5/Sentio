package com.example.klarity.domain.usecase

import com.example.klarity.domain.models.Note
import com.example.klarity.domain.repositories.NoteRepository
import kotlinx.datetime.Clock

class UpdateNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Result<Note> {
        val updatedNote = note.copy(updatedAt = Clock.System.now())
        return noteRepository.updateNote(updatedNote)
    }
}
