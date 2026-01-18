package com.example.klarity.data.mapper

import com.example.klarity.domain.models.Note
import com.example.klarity.domain.models.NoteStatus
import kotlinx.datetime.Instant
import com.example.klarity.db.Note as NoteEntity

/**
 * Extension functions for converting between Note domain model and database entity.
 */

fun NoteEntity.toDomain(tags: List<String> = emptyList()): Note = Note(
    id = id,
    title = title,
    content = content,
    folderId = folderId,
    tags = tags,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt),
    isPinned = isPinned == 1L,
    isFavorite = isFavorite == 1L,
    status = try { NoteStatus.valueOf(status) } catch (e: Exception) { NoteStatus.NONE }
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    folderId = folderId,
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds(),
    isPinned = if (isPinned) 1L else 0L,
    isFavorite = if (isFavorite) 1L else 0L,
    status = status.name
)
