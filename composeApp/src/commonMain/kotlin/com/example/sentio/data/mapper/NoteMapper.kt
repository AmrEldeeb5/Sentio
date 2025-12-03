package com.example.sentio.data.mapper

import com.example.sentio.domain.models.Note
import kotlinx.datetime.Instant
import com.example.sentio.db.Note as NoteEntity

/**
 * Mapper functions for converting between Note domain model and database entity.
 */
object NoteMapper {

    /**
     * Converts a database entity to domain model.
     * Note: Tags are loaded separately and passed in.
     */
    fun toDomain(entity: NoteEntity, tags: List<String> = emptyList()): Note {
        return Note(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            folderId = entity.folderId,
            tags = tags,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
            updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt),
            isPinned = entity.isPinned == 1L,
            isFavorite = entity.isFavorite == 1L
        )
    }

    /**
     * Converts a domain model to database entity.
     */
    fun toEntity(domain: Note): NoteEntity {
        return NoteEntity(
            id = domain.id,
            title = domain.title,
            content = domain.content,
            folderId = domain.folderId,
            createdAt = domain.createdAt.toEpochMilliseconds(),
            updatedAt = domain.updatedAt.toEpochMilliseconds(),
            isPinned = if (domain.isPinned) 1L else 0L,
            isFavorite = if (domain.isFavorite) 1L else 0L
        )
    }
}

/**
 * Extension functions for convenient mapping.
 */
fun NoteEntity.toDomain(tags: List<String> = emptyList()): Note = NoteMapper.toDomain(this, tags)
fun Note.toEntity(): NoteEntity = NoteMapper.toEntity(this)
