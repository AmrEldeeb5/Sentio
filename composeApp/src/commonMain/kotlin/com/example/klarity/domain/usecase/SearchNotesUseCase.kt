package com.example.klarity.domain.usecase

import com.example.klarity.domain.models.Note
import com.example.klarity.domain.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow

class SearchNotesUseCase(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(query: String): Flow<List<Note>> {
        return noteRepository.searchNotes(query)
    }
}
