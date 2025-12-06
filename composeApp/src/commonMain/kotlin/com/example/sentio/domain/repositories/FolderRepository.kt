package com.example.sentio.domain.repositories

import com.example.sentio.domain.models.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    fun getRootFolders(): Flow<List<Folder>>
    fun getSubFolders(parentId: String): Flow<List<Folder>>
    suspend fun getFolderById(id: String): Folder?
    suspend fun createFolder(folder: Folder): Result<Folder>
    suspend fun updateFolder(folder: Folder): Result<Folder>
    suspend fun deleteFolder(id: String): Result<Unit>
}
