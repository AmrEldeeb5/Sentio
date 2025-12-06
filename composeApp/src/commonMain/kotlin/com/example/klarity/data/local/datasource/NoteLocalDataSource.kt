package com.example.klarity.data.local.datasource

import com.example.klarity.db.Note
import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for note database operations.
 * Separates database access from repository logic.
 */
interface NoteLocalDataSource {
    fun observeAll(): Flow<List<Note>>
    fun observeById(id: String): Flow<Note?>
    fun observeByFolder(folderId: String): Flow<List<Note>>
    fun observePinned(): Flow<List<Note>>
    fun observeFavorites(): Flow<List<Note>>
    fun search(query: String): Flow<List<Note>>
    
    suspend fun getById(id: String): Note?
    suspend fun insert(note: Note)
    suspend fun update(note: Note)
    suspend fun delete(id: String)
}
