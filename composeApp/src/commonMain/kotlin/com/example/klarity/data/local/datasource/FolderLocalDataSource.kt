package com.example.klarity.data.local.datasource

import com.example.klarity.db.Folder
import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for folder database operations.
 */
interface FolderLocalDataSource {
    fun observeAll(): Flow<List<Folder>>
    fun observeRoots(): Flow<List<Folder>>
    fun observeChildren(parentId: String): Flow<List<Folder>>
    
    suspend fun getById(id: String): Folder?
    suspend fun insert(folder: Folder)
    suspend fun update(folder: Folder)
    suspend fun delete(id: String)
}
