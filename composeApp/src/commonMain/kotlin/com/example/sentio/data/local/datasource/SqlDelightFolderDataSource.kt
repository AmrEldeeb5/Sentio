package com.example.sentio.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.sentio.data.util.DispatcherProvider
import com.example.sentio.db.Folder
import com.example.sentio.db.SentioDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * SQLDelight implementation of [FolderLocalDataSource].
 */
class SqlDelightFolderDataSource(
    private val database: SentioDatabase,
    private val dispatchers: DispatcherProvider
) : FolderLocalDataSource {

    private val queries get() = database.folderQueries

    override fun observeAll(): Flow<List<Folder>> =
        queries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)

    override fun observeRoots(): Flow<List<Folder>> =
        queries.selectRoots()
            .asFlow()
            .mapToList(dispatchers.io)

    override fun observeChildren(parentId: String): Flow<List<Folder>> =
        queries.selectChildren(parentId)
            .asFlow()
            .mapToList(dispatchers.io)

    override suspend fun getById(id: String): Folder? = withContext(dispatchers.io) {
        queries.selectById(id).executeAsOneOrNull()
    }

    override suspend fun insert(folder: Folder): Unit = withContext(dispatchers.io) {
        queries.insert(
            id = folder.id,
            name = folder.name,
            parentId = folder.parentId,
            createdAt = folder.createdAt,
            icon = folder.icon
        )
    }

    override suspend fun update(folder: Folder): Unit = withContext(dispatchers.io) {
        queries.update(
            name = folder.name,
            parentId = folder.parentId,
            icon = folder.icon,
            id = folder.id
        )
    }

    override suspend fun delete(id: String): Unit = withContext(dispatchers.io) {
        queries.delete(id)
    }
}
