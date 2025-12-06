package com.example.klarity.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.klarity.data.util.DispatcherProvider
import com.example.klarity.data.mapper.toDomain
import com.example.klarity.db.KlarityDatabase
import com.example.klarity.domain.models.Folder
import com.example.klarity.domain.repositories.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository implementation for Folder operations.
 * Directly uses SQLDelight queries for database access.
 */
class SqlDelightFolderRepository(
    private val database: KlarityDatabase,
    private val dispatchers: DispatcherProvider
) : FolderRepository {

    private val queries get() = database.folderQueries

    override fun getAllFolders(): Flow<List<Folder>> =
        queries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities -> entities.map { it.toDomain() } }

    override fun getRootFolders(): Flow<List<Folder>> =
        queries.selectRoots()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities -> entities.map { it.toDomain() } }

    override fun getSubFolders(parentId: String): Flow<List<Folder>> =
        queries.selectChildren(parentId)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getFolderById(id: String): Folder? = withContext(dispatchers.io) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun createFolder(folder: Folder): Result<Folder> = runCatching {
        withContext(dispatchers.io) {
            queries.insert(
                id = folder.id,
                name = folder.name,
                parentId = folder.parentId,
                createdAt = folder.createdAt.toEpochMilliseconds(),
                icon = folder.icon
            )
        }
        folder
    }

    override suspend fun updateFolder(folder: Folder): Result<Folder> = runCatching {
        withContext(dispatchers.io) {
            queries.update(
                name = folder.name,
                parentId = folder.parentId,
                icon = folder.icon,
                id = folder.id
            )
        }
        folder
    }

    override suspend fun deleteFolder(id: String): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            queries.delete(id)
        }
    }
}
