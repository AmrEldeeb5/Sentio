package com.example.klarity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klarity.domain.models.Folder
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.NoteStatus
import com.example.klarity.domain.repositories.FolderRepository
import com.example.klarity.domain.repositories.NoteRepository
import com.example.klarity.domain.usecase.NoteUseCases
import com.example.klarity.presentation.state.HomeUiEffect
import com.example.klarity.presentation.state.HomeUiEvent
import com.example.klarity.presentation.state.HomeUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen following MVVM pattern.
 *
 * State Management:
 * - Single source of truth via [uiState] StateFlow
 * - UI emits events → ViewModel processes → State updates → UI recomposes
 * - One-time effects via [effects] Channel (snackbars, navigation)
 */
class HomeViewModel(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    // ══════════════════════════════════════════════════════════════
    // INTERNAL STATE HOLDERS (private - not exposed to UI)
    // ══════════════════════════════════════════════════════════════

    private val _searchQuery = MutableStateFlow("")
    private val _selectedNoteId = MutableStateFlow<String?>(null)
    private val _expandedFolderIds = MutableStateFlow<Set<String>>(emptySet())
    private val _pinnedSectionExpanded = MutableStateFlow(true)

    // ══════════════════════════════════════════════════════════════
    // DATA FLOWS FROM REPOSITORY
    // ══════════════════════════════════════════════════════════════

    private val _folders: Flow<List<Folder>> = folderRepository.getAllFolders()

    private val _notes: Flow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                noteRepository.getAllNotes()
            } else {
                noteUseCases.search(query)
            }
        }

    private val _pinnedNotes: Flow<List<Note>> = noteRepository.getPinnedNotes()

    // ══════════════════════════════════════════════════════════════
    // PUBLIC STATE (Single Source of Truth)
    // ══════════════════════════════════════════════════════════════

    /**
     * Combined UI state - the single source of truth for the Home screen.
     * Combines all data flows and UI state into one reactive stream.
     *
     * OPTIMIZED: Uses more efficient nested combine structure.
     * Benefits:
     * - Groups related data (notes/folders vs UI state)
     * - More efficient than deeply nested structures
     * - Clear separation of data vs UI state
     */
    val uiState: StateFlow<HomeUiState> = combine(
        combine(_notes, _pinnedNotes, _folders) { notes, pinnedNotes, folders ->
            DataState(notes, pinnedNotes, folders)
        },
        combine(_searchQuery, _selectedNoteId, _expandedFolderIds) { searchQuery, selectedNoteId, expandedFolderIds ->
            UiComponentState(searchQuery, selectedNoteId, expandedFolderIds)
        }
    ) { dataState, uiComponentState ->
        HomeUiState.Success(
            notes = dataState.notes,
            pinnedNotes = dataState.pinnedNotes,
            folders = dataState.folders,
            expandedFolderIds = uiComponentState.expandedFolderIds,
            searchQuery = uiComponentState.searchQuery,
            isSearching = uiComponentState.searchQuery.isNotBlank(),
            selectedNoteId = uiComponentState.selectedNoteId
        ) as HomeUiState
    }.catch { e ->
        emit(HomeUiState.Error(
            message = e.message ?: "An unexpected error occurred",
            retryAction = { refresh() }
        ))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    // Helper data classes for combine (internal to ViewModel)
    private data class DataState(
        val notes: List<Note>,
        val pinnedNotes: List<Note>,
        val folders: List<Folder>
    )

    private data class UiComponentState(
        val searchQuery: String,
        val selectedNoteId: String?,
        val expandedFolderIds: Set<String>
    )

    /**
     * Expose search query for UI binding (read-only)
     */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Expose selected note ID for UI binding (read-only)
     */
    val selectedNoteId: StateFlow<String?> = _selectedNoteId.asStateFlow()

    /**
     * Expose expanded folder IDs for UI binding (read-only)
     */
    val expandedFolderIds: StateFlow<Set<String>> = _expandedFolderIds.asStateFlow()

    /**
     * Expose pinned section state for UI binding (read-only)
     */
    val pinnedSectionExpanded: StateFlow<Boolean> = _pinnedSectionExpanded.asStateFlow()

    // ══════════════════════════════════════════════════════════════
    // DERIVED STATE (Computed from uiState for convenience)
    // ══════════════════════════════════════════════════════════════

    /**
     * Current notes list - derived from uiState for backward compatibility
     */
    val notes: StateFlow<List<Note>> = uiState
        .map { state ->
            when (state) {
                is HomeUiState.Success -> state.notes
                else -> emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Current pinned notes - derived from uiState for backward compatibility
     */
    val pinnedNotes: StateFlow<List<Note>> = uiState
        .map { state ->
            when (state) {
                is HomeUiState.Success -> state.pinnedNotes
                else -> emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Current folders - derived from uiState for backward compatibility
     */
    val folders: StateFlow<List<Folder>> = uiState
        .map { state ->
            when (state) {
                is HomeUiState.Success -> state.folders
                else -> emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ══════════════════════════════════════════════════════════════
    // EFFECTS CHANNEL (One-time events)
    // ══════════════════════════════════════════════════════════════

    private val _effects = Channel<HomeUiEffect>(Channel.BUFFERED)
    val effects: Flow<HomeUiEffect> = _effects.receiveAsFlow()

    // ══════════════════════════════════════════════════════════════
    // EVENT HANDLING
    // ══════════════════════════════════════════════════════════════

    /**
     * Handle UI events from the screen.
     * Single entry point for all user interactions.
     */
    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is HomeUiEvent.ClearSearch -> clearSearch()
            is HomeUiEvent.DeleteNote -> deleteNote(event.noteId)
            is HomeUiEvent.CreateNote -> createNote()
            is HomeUiEvent.CreateNoteWithTitle -> createNoteWithTitle(event.title)
            is HomeUiEvent.CreateFolder -> createFolder(event.name)
            is HomeUiEvent.ToggleFolder -> toggleFolder(event.folderId)
            is HomeUiEvent.TogglePinnedSection -> togglePinnedSection()
            is HomeUiEvent.Refresh -> refresh()
            is HomeUiEvent.OpenNote -> openNote(event.noteId)
            is HomeUiEvent.SelectNote -> selectNote(event.noteId)
            HomeUiEvent.CloseNote -> closeNote()
            is HomeUiEvent.UpdateNoteTitle -> updateNoteTitle(event.noteId, event.title)
            is HomeUiEvent.UpdateNoteContent -> updateNoteContent(event.noteId, event.content)
            is HomeUiEvent.SaveNote -> saveNote(event.noteId)
            is HomeUiEvent.ToggleNotePin -> toggleNotePin(event.noteId)
            is HomeUiEvent.RenameFolder -> renameFolder(event.folderId, event.newName)
            is HomeUiEvent.DeleteFolder -> deleteFolder(event.folderId)
            is HomeUiEvent.UpdateNoteStatus -> updateNoteStatus(event.noteId, event.status)
            is HomeUiEvent.MoveNoteToFolder -> moveNoteToFolder(event.noteId, event.folderId)
        }
    }

    // ══════════════════════════════════════════════════════════════
    // PRIVATE HANDLERS
    // ══════════════════════════════════════════════════════════════

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
                    _selectedNoteId.value = note.id
                    _effects.send(HomeUiEffect.NavigateToEditor(note.id))
                }
                .onFailure { error ->
                    _effects.send(HomeUiEffect.ShowError(error.message ?: "Failed to create note"))
                }
        }
    }

    private fun createNoteWithTitle(title: String) {
        viewModelScope.launch {
            noteUseCases.create(title = title)
                .onSuccess { note ->
                    _selectedNoteId.value = note.id
                    _effects.send(HomeUiEffect.ShowSnackbar("Created note: $title"))
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
                    if (_selectedNoteId.value == noteId) {
                        _selectedNoteId.value = null
                    }
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

    private fun refresh() {
        // State will automatically refresh via Flow collection
        // Force a resubscription by updating search query
        val currentQuery = _searchQuery.value
        _searchQuery.value = ""
        _searchQuery.value = currentQuery
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

    private fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState !is HomeUiState.Success) return@launch
            val folder = currentState.folders.find { it.id == folderId } ?: return@launch
            folderRepository.updateFolder(folder.copy(name = newName))
        }
    }

    private fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            folderRepository.deleteFolder(folderId)
            _effects.send(HomeUiEffect.ShowSnackbar("Folder deleted"))
        }
    }

    private fun updateNoteStatus(noteId: String, status: NoteStatus) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId) ?: return@launch
            noteRepository.updateNote(note.copy(status = status, updatedAt = kotlinx.datetime.Clock.System.now()))
        }
    }

    private fun moveNoteToFolder(noteId: String, folderId: String?) {
        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId) ?: return@launch
            noteRepository.updateNote(note.copy(folderId = folderId, updatedAt = kotlinx.datetime.Clock.System.now()))
            _effects.send(HomeUiEffect.ShowSnackbar("Note moved"))
        }
    }
}
