package com.example.klarity.domain.usecase

import com.example.klarity.domain.repositories.NoteRepository

class DeleteNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: String): Result<Unit> {
        return noteRepository.deleteNote(noteId)
    }
}
