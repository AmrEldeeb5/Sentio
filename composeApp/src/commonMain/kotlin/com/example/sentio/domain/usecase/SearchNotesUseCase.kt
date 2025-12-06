package com.example.sentio.domain.usecase

import com.example.sentio.domain.models.Note
import com.example.sentio.domain.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow

class SearchNotesUseCase(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(query: String): Flow<List<Note>> {
        return noteRepository.searchNotes(query)
    }
}
