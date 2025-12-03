package com.example.sentio.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.sentio.data.util.DispatcherProvider
import com.example.sentio.db.SentioDatabase
import com.example.sentio.db.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * SQLDelight implementation of [TagLocalDataSource].
 */
class SqlDelightTagDataSource(
    private val database: SentioDatabase,
    private val dispatchers: DispatcherProvider
) : TagLocalDataSource {

    private val queries get() = database.tagQueries

    override fun observeAll(): Flow<List<Tag>> =
        queries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)

    override fun observeByNote(noteId: String): Flow<List<Tag>> =
        queries.selectByNote(noteId)
            .asFlow()
            .mapToList(dispatchers.io)

    override suspend fun getById(id: String): Tag? = withContext(dispatchers.io) {
        queries.selectById(id).executeAsOneOrNull()
    }

    override suspend fun insert(tag: Tag): Unit = withContext(dispatchers.io) {
        queries.insert(tag.id, tag.name, tag.color)
    }

    override suspend fun update(tag: Tag): Unit = withContext(dispatchers.io) {
        queries.update(tag.name, tag.color, tag.id)
    }

    override suspend fun delete(id: String): Unit = withContext(dispatchers.io) {
        queries.delete(id)
    }

    override suspend fun linkToNote(noteId: String, tagId: String): Unit = withContext(dispatchers.io) {
        queries.linkNoteTag(noteId, tagId)
    }

    override suspend fun unlinkFromNote(noteId: String, tagId: String): Unit = withContext(dispatchers.io) {
        queries.unlinkNoteTag(noteId, tagId)
    }

    override suspend fun unlinkAllFromNote(noteId: String): Unit = withContext(dispatchers.io) {
        queries.unlinkAllNoteTags(noteId)
    }
}
