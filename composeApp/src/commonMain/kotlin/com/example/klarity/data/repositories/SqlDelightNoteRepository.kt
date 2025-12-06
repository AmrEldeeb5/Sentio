package com.example.klarity.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.klarity.data.mapper.toDomain
import com.example.klarity.data.mapper.toEntity
import com.example.klarity.data.util.DispatcherProvider
import com.example.klarity.db.KlarityDatabase
import com.example.klarity.domain.models.Note
import com.example.klarity.domain.repositories.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository implementation for Note operations.
 * Directly uses SQLDelight database - no DataSource layer needed for simple cases.
 */
class SqlDelightNoteRepository(
    private val database: KlarityDatabase,
    private val dispatchers: DispatcherProvider
) : NoteRepository {

    private val noteQueries get() = database.noteQueries
    private val tagQueries get() = database.tagQueries

    override fun getAllNotes(): Flow<List<Note>> =
        noteQueries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities ->
                entities.map { entity ->
                    val tags = getTagsForNote(entity.id)
                    entity.toDomain(tags)
                }
            }

    override suspend fun getNoteById(id: String): Note? = withContext(dispatchers.io) {
        val entity = noteQueries.selectById(id).executeAsOneOrNull() ?: return@withContext null
        val tags = getTagsForNote(id)
        entity.toDomain(tags)
    }

    override fun getNotesByFolder(folderId: String): Flow<List<Note>> =
        noteQueries.selectByFolder(folderId)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities ->
                entities.map { entity ->
                    val tags = getTagsForNote(entity.id)
                    entity.toDomain(tags)
                }
            }

    override fun getNotesByTag(tagId: String): Flow<List<Note>> =
        getAllNotes().map { notes ->
            notes.filter { note -> note.tags.contains(tagId) }
        }

    override fun getPinnedNotes(): Flow<List<Note>> =
        noteQueries.selectPinned()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities ->
                entities.map { entity ->
                    val tags = getTagsForNote(entity.id)
                    entity.toDomain(tags)
                }
            }

    override fun getFavoriteNotes(): Flow<List<Note>> =
        noteQueries.selectFavorites()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities ->
                entities.map { entity ->
                    val tags = getTagsForNote(entity.id)
                    entity.toDomain(tags)
                }
            }

    override suspend fun createNote(note: Note): Result<Note> = runCatching {
        withContext(dispatchers.io) {
            val entity = note.toEntity()
            noteQueries.insert(
                id = entity.id,
                title = entity.title,
                content = entity.content,
                folderId = entity.folderId,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                isPinned = entity.isPinned,
                isFavorite = entity.isFavorite
            )
            
            // Link tags
            note.tags.forEach { tagName ->
                tagQueries.linkNoteTag(note.id, tagName)
            }
        }
        note
    }

    override suspend fun updateNote(note: Note): Result<Note> = runCatching {
        withContext(dispatchers.io) {
            val entity = note.toEntity()
            noteQueries.update(
                title = entity.title,
                content = entity.content,
                folderId = entity.folderId,
                updatedAt = entity.updatedAt,
                isPinned = entity.isPinned,
                isFavorite = entity.isFavorite,
                id = entity.id
            )
            
            // Update tags: remove old, add new
            tagQueries.unlinkAllNoteTags(note.id)
            note.tags.forEach { tagName ->
                tagQueries.linkNoteTag(note.id, tagName)
            }
        }
        note
    }

    override suspend fun deleteNote(id: String): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            noteQueries.delete(id)
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> =
        noteQueries.search(query, query)
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities ->
                entities.map { entity ->
                    val tags = getTagsForNote(entity.id)
                    entity.toDomain(tags)
                }
            }

    private suspend fun getTagsForNote(noteId: String): List<String> =
        withContext(dispatchers.io) {
            tagQueries.selectByNote(noteId)
                .executeAsList()
                .map { it.name }
        }
}
