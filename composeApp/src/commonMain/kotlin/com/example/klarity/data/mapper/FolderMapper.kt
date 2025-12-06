package com.example.klarity.data.mapper

import com.example.klarity.domain.models.Folder
import kotlinx.datetime.Instant
import com.example.klarity.db.Folder as FolderEntity

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
