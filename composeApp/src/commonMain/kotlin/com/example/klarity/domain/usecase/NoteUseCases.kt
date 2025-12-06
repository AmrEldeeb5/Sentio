package com.example.klarity.domain.usecase

/**
 * Container for all note-related use cases.
 * Provides a clean API for ViewModels without injecting many dependencies.
 *
 * As the app grows, you can add more use cases here without changing ViewModel constructors.
 */
data class NoteUseCases(
    val create: CreateNoteUseCase,
    val update: UpdateNoteUseCase,
    val delete: DeleteNoteUseCase,
    val search: SearchNotesUseCase
)
