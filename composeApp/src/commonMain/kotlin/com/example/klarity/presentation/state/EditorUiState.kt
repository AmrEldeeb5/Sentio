package com.example.klarity.presentation.state

import com.example.klarity.domain.models.Note

/**
 * Sealed class representing the UI state for the Editor screen.
 */
sealed class EditorUiState {
    /**
     * Initial/idle state.
     */
    data object Idle : EditorUiState()

    /**
     * Loading state - note is being loaded.
     */
    data object Loading : EditorUiState()

    /**
     * Success state - note has been loaded and ready for editing.
     */
    data class Success(
        val note: Note,
        val isSaving: Boolean = false,
        val hasUnsavedChanges: Boolean = false,
        val lastSavedAt: Long? = null
    ) : EditorUiState()

    /**
     * New note state - creating a new note.
     */
    data class NewNote(
        val title: String = "",
        val content: String = "",
        val isSaving: Boolean = false
    ) : EditorUiState()

    /**
     * Error state - an error occurred.
     */
    data class Error(
        val message: String,
        val retryAction: (() -> Unit)? = null
    ) : EditorUiState()
}

/**
 * View modes for the editor layout
 */
enum class EditorViewMode {
    TRI_PANE,    // Sidebar + Notes List + Editor
    DUAL_PANE,   // Notes List + Editor
    SINGLE_PANE, // Editor only
    ZEN          // Minimal distraction-free editor
}

/**
 * UI events that can be triggered from the Editor screen.
 */
sealed class EditorUiEvent {
    data class LoadNote(val noteId: String) : EditorUiEvent()
    data object CreateNewNote : EditorUiEvent()
    data class UpdateTitle(val title: String) : EditorUiEvent()
    data class UpdateContent(val content: String) : EditorUiEvent()
    data object TogglePin : EditorUiEvent()
    data object ToggleFavorite : EditorUiEvent()
    data object Save : EditorUiEvent()
    data object Delete : EditorUiEvent()
    data class AddTag(val tag: String) : EditorUiEvent()
    data class RemoveTag(val tag: String) : EditorUiEvent()
    
    // Text formatting events
    data class FormatBold(val selectionStart: Int, val selectionEnd: Int) : EditorUiEvent()
    data class FormatItalic(val selectionStart: Int, val selectionEnd: Int) : EditorUiEvent()
    data class FormatCode(val selectionStart: Int, val selectionEnd: Int) : EditorUiEvent()
    data class FormatLink(val selectionStart: Int, val selectionEnd: Int, val url: String) : EditorUiEvent()
    data class InsertCodeBlock(val cursorPosition: Int) : EditorUiEvent()
}

/**
 * Side effects that should be handled by the UI.
 */
sealed class EditorUiEffect {
    data object NavigateBack : EditorUiEffect()
    data class ShowSnackbar(val message: String) : EditorUiEffect()
    data class ShowError(val message: String) : EditorUiEffect()
    data object NoteSaved : EditorUiEffect()
    data object NoteDeleted : EditorUiEffect()
}
