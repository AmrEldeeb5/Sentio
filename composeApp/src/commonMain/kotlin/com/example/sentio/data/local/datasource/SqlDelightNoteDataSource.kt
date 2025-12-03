package com.example.sentio.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.sentio.data.util.DispatcherProvider
import com.example.sentio.db.Note
import com.example.sentio.db.SentioDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * SQLDelight implementation of [NoteLocalDataSource].
 */
class SqlDelightNoteDataSource(
    private val database: SentioDatabase,
    private val dispatchers: DispatcherProvider
) : NoteLocalDataSource {

    private val queries get() = database.noteQueries

    override fun observeAll(): Flow<List<Note>> =
        queries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)

    override fun observeById(id: String): Flow<Note?> =
        queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)

    override fun observeByFolder(folderId: String): Flow<List<Note>> =
        queries.selectByFolder(folderId)
            .asFlow()
            .mapToList(dispatchers.io)

    override fun observePinned(): Flow<List<Note>> =
        queries.selectPinned()
            .asFlow()
            .mapToList(dispatchers.io)

    override fun observeFavorites(): Flow<List<Note>> =
        queries.selectFavorites()
            .asFlow()
            .mapToList(dispatchers.io)

    override fun search(query: String): Flow<List<Note>> =
        queries.search(query, query)
            .asFlow()
            .mapToList(dispatchers.io)

    override suspend fun getById(id: String): Note? = withContext(dispatchers.io) {
        queries.selectById(id).executeAsOneOrNull()
    }

    override suspend fun insert(note: Note): Unit = withContext(dispatchers.io) {
        queries.insert(
            id = note.id,
            title = note.title,
            content = note.content,
            folderId = note.folderId,
            createdAt = note.createdAt,
            updatedAt = note.updatedAt,
            isPinned = note.isPinned,
            isFavorite = note.isFavorite
        )
    }

    override suspend fun update(note: Note): Unit = withContext(dispatchers.io) {
        queries.update(
            title = note.title,
            content = note.content,
            folderId = note.folderId,
            updatedAt = note.updatedAt,
            isPinned = note.isPinned,
            isFavorite = note.isFavorite,
            id = note.id
        )
    }

    override suspend fun delete(id: String): Unit = withContext(dispatchers.io) {
        queries.delete(id)
    }
}
