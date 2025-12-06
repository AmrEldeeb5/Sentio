package com.example.klarity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klarity.domain.repositories.NoteRepository
import com.example.klarity.domain.usecase.NoteUseCases
import com.example.klarity.presentation.state.EditorUiEffect
import com.example.klarity.presentation.state.EditorUiEvent
import com.example.klarity.presentation.state.EditorUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Editor screen following MVVM pattern.
 * Uses sealed classes for state management.
 */
class EditorViewModel(
    private val noteRepository: NoteRepository,
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditorUiState>(EditorUiState.Idle)
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _effects = Channel<EditorUiEffect>(Channel.BUFFERED)
    val effects: Flow<EditorUiEffect> = _effects.receiveAsFlow()

    // All notes for sidebar
    val allNotes = noteRepository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Pinned notes for sidebar
    val pinnedNotes = noteRepository.getPinnedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Handle UI events from the screen.
     */
    fun onEvent(event: EditorUiEvent) {
        when (event) {
            is EditorUiEvent.LoadNote -> loadNote(event.noteId)
            is EditorUiEvent.CreateNewNote -> createNewNote()
            is EditorUiEvent.UpdateTitle -> updateTitle(event.title)
            is EditorUiEvent.UpdateContent -> updateContent(event.content)
            is EditorUiEvent.TogglePin -> togglePin()
            is EditorUiEvent.ToggleFavorite -> toggleFavorite()
            is EditorUiEvent.Save -> saveNote()
            is EditorUiEvent.Delete -> deleteNote()
            is EditorUiEvent.AddTag -> addTag(event.tag)
            is EditorUiEvent.RemoveTag -> removeTag(event.tag)
            is EditorUiEvent.FormatBold -> formatBold(event.selectionStart, event.selectionEnd)
            is EditorUiEvent.FormatItalic -> formatItalic(event.selectionStart, event.selectionEnd)
            is EditorUiEvent.FormatCode -> formatCode(event.selectionStart, event.selectionEnd)
            is EditorUiEvent.FormatLink -> formatLink(event.selectionStart, event.selectionEnd, event.url)
            is EditorUiEvent.InsertCodeBlock -> insertCodeBlock(event.cursorPosition)
        }
    }

    private fun loadNote(noteId: String) {
        viewModelScope.launch {
            _uiState.value = EditorUiState.Loading
            try {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    _uiState.value = EditorUiState.Success(note = note)
                } else {
                    _uiState.value = EditorUiState.Error(
                        message = "Note not found",
                        retryAction = { loadNote(noteId) }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = EditorUiState.Error(
                    message = e.message ?: "Failed to load note",
                    retryAction = { loadNote(noteId) }
                )
            }
        }
    }

    private fun createNewNote() {
        _uiState.value = EditorUiState.NewNote()
    }

    private fun updateTitle(title: String) {
        when (val state = _uiState.value) {
            is EditorUiState.Success -> {
                _uiState.value = state.copy(
                    note = state.note.copy(title = title),
                    hasUnsavedChanges = true
                )
            }
            is EditorUiState.NewNote -> {
                _uiState.value = state.copy(title = title)
            }
            else -> {}
        }
    }

    private fun updateContent(content: String) {
        when (val state = _uiState.value) {
            is EditorUiState.Success -> {
                _uiState.value = state.copy(
                    note = state.note.copy(content = content),
                    hasUnsavedChanges = true
                )
            }
            is EditorUiState.NewNote -> {
                _uiState.value = state.copy(content = content)
            }
            else -> {}
        }
    }

    private fun saveNote() {
        val state = _uiState.value
        if (state !is EditorUiState.Success) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            noteUseCases.update(state.note)
                .onSuccess {
                    _uiState.value = state.copy(
                        isSaving = false,
                        hasUnsavedChanges = false,
                        lastSavedAt = System.currentTimeMillis()
                    )
                    _effects.send(EditorUiEffect.NoteSaved)
                }
                .onFailure { error ->
                    _uiState.value = state.copy(isSaving = false)
                    _effects.send(EditorUiEffect.ShowError(error.message ?: "Failed to save"))
                }
        }
    }

    private fun togglePin() {
        val state = _uiState.value
        if (state !is EditorUiState.Success) return

        val updatedNote = state.note.copy(isPinned = !state.note.isPinned)
        _uiState.value = state.copy(note = updatedNote, hasUnsavedChanges = true)

        viewModelScope.launch {
            noteUseCases.update(updatedNote)
        }
    }

    private fun toggleFavorite() {
        val state = _uiState.value
        if (state !is EditorUiState.Success) return

        val updatedNote = state.note.copy(isFavorite = !state.note.isFavorite)
        _uiState.value = state.copy(note = updatedNote, hasUnsavedChanges = true)

        viewModelScope.launch {
            noteUseCases.update(updatedNote)
        }
    }

    private fun deleteNote() {
        viewModelScope.launch {
            _effects.send(EditorUiEffect.NoteDeleted)
            _effects.send(EditorUiEffect.NavigateBack)
        }
    }

    private fun addTag(tag: String) {
        val state = _uiState.value
        if (state !is EditorUiState.Success) return

        val updatedTags = (state.note.tags + tag).distinct()
        _uiState.value = state.copy(
            note = state.note.copy(tags = updatedTags),
            hasUnsavedChanges = true
        )
    }

    private fun removeTag(tag: String) {
        val state = _uiState.value
        if (state !is EditorUiState.Success) return

        val updatedTags = state.note.tags.filter { it != tag }
        _uiState.value = state.copy(
            note = state.note.copy(tags = updatedTags),
            hasUnsavedChanges = true
        )
    }

    // ══════════════════════════════════════════════════════════════
    // TEXT FORMATTING
    // ══════════════════════════════════════════════════════════════

    private fun formatBold(selectionStart: Int, selectionEnd: Int) {
        wrapSelection(selectionStart, selectionEnd, "**", "**")
    }

    private fun formatItalic(selectionStart: Int, selectionEnd: Int) {
        wrapSelection(selectionStart, selectionEnd, "_", "_")
    }

    private fun formatCode(selectionStart: Int, selectionEnd: Int) {
        wrapSelection(selectionStart, selectionEnd, "`", "`")
    }

    private fun formatLink(selectionStart: Int, selectionEnd: Int, url: String) {
        val state = _uiState.value
        val content = when (state) {
            is EditorUiState.Success -> state.note.content
            is EditorUiState.NewNote -> state.content
            else -> return
        }

        val selectedText = if (selectionStart < selectionEnd) {
            content.substring(selectionStart.coerceIn(0, content.length), selectionEnd.coerceIn(0, content.length))
        } else {
            "link"
        }

        val linkMarkdown = "[$selectedText]($url)"
        val newContent = content.replaceRange(
            selectionStart.coerceIn(0, content.length),
            selectionEnd.coerceIn(0, content.length),
            linkMarkdown
        )
        updateContent(newContent)
    }

    private fun insertCodeBlock(cursorPosition: Int) {
        val state = _uiState.value
        val content = when (state) {
            is EditorUiState.Success -> state.note.content
            is EditorUiState.NewNote -> state.content
            else -> return
        }

        val codeBlock = "\n```\n\n```\n"
        val newContent = StringBuilder(content)
            .insert(cursorPosition.coerceIn(0, content.length), codeBlock)
            .toString()
        updateContent(newContent)
    }

    private fun wrapSelection(selectionStart: Int, selectionEnd: Int, prefix: String, suffix: String) {
        val state = _uiState.value
        val content = when (state) {
            is EditorUiState.Success -> state.note.content
            is EditorUiState.NewNote -> state.content
            else -> return
        }

        val start = selectionStart.coerceIn(0, content.length)
        val end = selectionEnd.coerceIn(0, content.length)

        val selectedText = if (start < end) {
            content.substring(start, end)
        } else {
            ""
        }

        // Check if already wrapped - toggle off
        val isAlreadyWrapped = selectedText.startsWith(prefix) && selectedText.endsWith(suffix)
        
        val newContent = if (isAlreadyWrapped && selectedText.length >= prefix.length + suffix.length) {
            // Remove formatting
            val unwrapped = selectedText.drop(prefix.length).dropLast(suffix.length)
            content.replaceRange(start, end, unwrapped)
        } else {
            // Add formatting
            content.replaceRange(start, end, "$prefix$selectedText$suffix")
        }
        
        updateContent(newContent)
    }

    /**
     * Toggle pin status for a note by ID (used in sidebar)
     */
    fun toggleNotePinById(noteId: String) {
        viewModelScope.launch {
            try {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    noteRepository.updateNote(note.copy(isPinned = !note.isPinned))
                }
            } catch (e: Exception) {
                _effects.send(EditorUiEffect.ShowError("Failed to toggle pin: ${e.message}"))
            }
        }
    }
}
