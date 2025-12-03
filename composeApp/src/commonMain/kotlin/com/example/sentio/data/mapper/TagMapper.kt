package com.example.sentio.data.mapper

import com.example.sentio.domain.models.Tag
import com.example.sentio.db.Tag as TagEntity

/**
 * Mapper functions for converting between Tag domain model and database entity.
 */
object TagMapper {

    fun toDomain(entity: TagEntity): Tag {
        return Tag(
            id = entity.id,
            name = entity.name,
            color = entity.color
        )
    }

    fun toEntity(domain: Tag): TagEntity {
        return TagEntity(
            id = domain.id,
            name = domain.name,
            color = domain.color
        )
    }
}

/**
 * Extension functions for convenient mapping.
 */
fun TagEntity.toDomain(): Tag = TagMapper.toDomain(this)
fun Tag.toEntity(): TagEntity = TagMapper.toEntity(this)
