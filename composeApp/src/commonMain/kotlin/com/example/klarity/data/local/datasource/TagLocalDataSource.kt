package com.example.klarity.data.local.datasource

import com.example.klarity.db.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for tag database operations.
 */
interface TagLocalDataSource {
    fun observeAll(): Flow<List<Tag>>
    fun observeByNote(noteId: String): Flow<List<Tag>>
    
    suspend fun getById(id: String): Tag?
    suspend fun insert(tag: Tag)
    suspend fun update(tag: Tag)
    suspend fun delete(id: String)
    suspend fun linkToNote(noteId: String, tagId: String)
    suspend fun unlinkFromNote(noteId: String, tagId: String)
    suspend fun unlinkAllFromNote(noteId: String)
}
