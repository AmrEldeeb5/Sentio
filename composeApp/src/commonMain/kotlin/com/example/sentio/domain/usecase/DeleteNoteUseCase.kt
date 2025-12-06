package com.example.sentio.domain.usecase

import com.example.sentio.domain.repositories.NoteRepository

class DeleteNoteUseCase(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(noteId: String): Result<Unit> {
        return noteRepository.deleteNote(noteId)
    }
}
