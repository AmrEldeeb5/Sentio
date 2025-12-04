package com.example.sentio.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sentio.domain.models.Folder
import com.example.sentio.domain.models.Note
import com.example.sentio.domain.repositories.FolderRepository
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
    private val folderRepository: FolderRepository,
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedNoteId = MutableStateFlow<String?>(null)
    val selectedNoteId: StateFlow<String?> = _selectedNoteId.asStateFlow()

    private val _expandedFolderIds = MutableStateFlow<Set<String>>(emptySet())
    val expandedFolderIds: StateFlow<Set<String>> = _expandedFolderIds.asStateFlow()

    private val _pinnedSectionExpanded = MutableStateFlow(true)
    val pinnedSectionExpanded: StateFlow<Boolean> = _pinnedSectionExpanded.asStateFlow()

    private val _effects = Channel<HomeUiEffect>(Channel.BUFFERED)
    val effects: Flow<HomeUiEffect> = _effects.receiveAsFlow()

    val folders: StateFlow<List<Folder>> = folderRepository.getAllFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
                folders = folders.value,
                expandedFolderIds = _expandedFolderIds.value,
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
            is HomeUiEvent.CreateFolder -> createFolder(event.name)
            is HomeUiEvent.ToggleFolder -> toggleFolder(event.folderId)
            is HomeUiEvent.TogglePinnedSection -> togglePinnedSection()
            is HomeUiEvent.Refresh -> refresh()
            is HomeUiEvent.OpenNote -> openNote(event.noteId)
            is HomeUiEvent.SelectNote -> selectNote(event.noteId)
            HomeUiEvent.CloseNote -> closeNote()
            // Editor events
            is HomeUiEvent.UpdateNoteTitle -> updateNoteTitle(event.noteId, event.title)
            is HomeUiEvent.UpdateNoteContent -> updateNoteContent(event.noteId, event.content)
            is HomeUiEvent.SaveNote -> saveNote(event.noteId)
            is HomeUiEvent.ToggleNotePin -> toggleNotePin(event.noteId)
        }
    }

    private fun updateNoteTitle(noteId: String, title: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId) ?: return@launch
            noteRepository.updateNote(note.copy(title = title, updatedAt = kotlinx.datetime.Clock.System.now()))
        }
    }

    private fun updateNoteContent(noteId: String, content: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId) ?: return@launch
            noteRepository.updateNote(note.copy(content = content, updatedAt = kotlinx.datetime.Clock.System.now()))
        }
    }

    private fun saveNote(noteId: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId) ?: return@launch
            noteUseCases.update(note)
                .onSuccess {
                    _effects.send(HomeUiEffect.ShowSnackbar("Note saved"))
                }
                .onFailure { error ->
                    _effects.send(HomeUiEffect.ShowError(error.message ?: "Failed to save note"))
                }
        }
    }

    private fun toggleNotePin(noteId: String) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId) ?: return@launch
            noteRepository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    private fun toggleFolder(folderId: String) {
        _expandedFolderIds.value = if (folderId in _expandedFolderIds.value) {
            _expandedFolderIds.value - folderId
        } else {
            _expandedFolderIds.value + folderId
        }
    }

    private fun togglePinnedSection() {
        _pinnedSectionExpanded.value = !_pinnedSectionExpanded.value
    }

    private fun createFolder(name: String) {
        viewModelScope.launch {
            val folder = Folder(
                id = com.benasher44.uuid.uuid4().toString(),
                name = name,
                parentId = null,
                createdAt = kotlinx.datetime.Clock.System.now()
            )
            folderRepository.createFolder(folder)
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
