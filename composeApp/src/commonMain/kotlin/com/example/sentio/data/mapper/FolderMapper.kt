package com.example.sentio.data.mapper

import com.example.sentio.domain.models.Folder
import kotlinx.datetime.Instant
import com.example.sentio.db.Folder as FolderEntity

/**
 * Extension functions for converting between Folder domain model and database entity.
 */

fun FolderEntity.toDomain(): Folder = Folder(
    id = id,
    name = name,
    parentId = parentId,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    icon = icon
)

fun Folder.toEntity(): FolderEntity = FolderEntity(
    id = id,
    name = name,
    parentId = parentId,
    createdAt = createdAt.toEpochMilliseconds(),
    icon = icon
)
