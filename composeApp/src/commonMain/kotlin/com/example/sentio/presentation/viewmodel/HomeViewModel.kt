package com.example.sentio.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sentio.domain.models.Note
import com.example.sentio.domain.repositories.NoteRepository
import com.example.sentio.domain.usecase.NoteUseCases
import com.example.sentio.presentation.state.HomeUiEffect
import com.example.sentio.presentation.state.HomeUiEvent
import com.example.sentio.presentation.state.HomeUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen following MVVM pattern.
 * Uses sealed classes for state management.
 */
class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedNoteId = MutableStateFlow<String?>(null)
    val selectedNoteId: StateFlow<String?> = _selectedNoteId.asStateFlow()

    private val _effects = Channel<HomeUiEffect>(Channel.BUFFERED)
    val effects: Flow<HomeUiEffect> = _effects.receiveAsFlow()

    val notes: StateFlow<List<Note>> = searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                noteRepository.getAllNotes()
            } else {
                noteUseCases.search(query)
            }
        }
        .onEach { notes ->
            _uiState.value = HomeUiState.Success(
                notes = notes,
                searchQuery = _searchQuery.value,
                isSearching = _searchQuery.value.isNotBlank()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pinnedNotes: StateFlow<List<Note>> = noteRepository.getPinnedNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Handle UI events from the screen.
     */
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is HomeUiEvent.ClearSearch -> clearSearch()
            is HomeUiEvent.DeleteNote -> deleteNote(event.noteId)
            is HomeUiEvent.CreateNote -> createNote()
            is HomeUiEvent.Refresh -> refresh()
            is HomeUiEvent.OpenNote -> openNote(event.noteId)
            is HomeUiEvent.SelectNote -> selectNote(event.noteId)
            HomeUiEvent.CloseNote -> closeNote()
        }
    }

    private fun createNote(title: String = "Untitled Note") {
        viewModelScope.launch {
            noteUseCases.create(title = title)
                .onSuccess { note ->
                    _effects.send(HomeUiEffect.NavigateToEditor(note.id))
                }
                .onFailure { error ->
                    _effects.send(HomeUiEffect.ShowError(error.message ?: "Failed to create note"))
                }
        }
    }

    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteUseCases.delete(noteId)
                .onSuccess {
                    _effects.send(HomeUiEffect.ShowSnackbar("Note deleted"))
                }
                .onFailure { error ->
                    _effects.send(HomeUiEffect.ShowError(error.message ?: "Failed to delete note"))
                }
        }
    }

    private fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun clearSearch() {
        _searchQuery.value = ""
    }

    private fun navigateToNote(noteId: String) {
        viewModelScope.launch {
            _selectedNoteId.value = noteId
            _effects.send(HomeUiEffect.NavigateToEditor(noteId))
        }
    }

    private fun refresh() {
        _uiState.value = HomeUiState.Loading
        // Notes will auto-refresh via Flow
    }

    private fun openNote(noteId: String) {
        _selectedNoteId.value = noteId
    }

    private fun selectNote(noteId: String) {
        _selectedNoteId.value = noteId
    }

    private fun closeNote() {
        _selectedNoteId.value = null
    }
}
