package com.example.sentio.data.mapper

import com.example.sentio.domain.models.Folder
import kotlinx.datetime.Instant
import com.example.sentio.db.Folder as FolderEntity

/**
 * Mapper functions for converting between Folder domain model and database entity.
 */
object FolderMapper {

    fun toDomain(entity: FolderEntity): Folder {
        return Folder(
            id = entity.id,
            name = entity.name,
            parentId = entity.parentId,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
            icon = entity.icon
        )
    }

    fun toEntity(domain: Folder): FolderEntity {
        return FolderEntity(
            id = domain.id,
            name = domain.name,
            parentId = domain.parentId,
            createdAt = domain.createdAt.toEpochMilliseconds(),
            icon = domain.icon
        )
    }
}

/**
 * Extension functions for convenient mapping.
 */
fun FolderEntity.toDomain(): Folder = FolderMapper.toDomain(this)
fun Folder.toEntity(): FolderEntity = FolderMapper.toEntity(this)
