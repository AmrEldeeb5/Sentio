package com.example.sentio.data.mapper

import com.example.sentio.domain.models.Tag
import com.example.sentio.db.Tag as TagEntity

/**
 * Extension functions for converting between Tag domain model and database entity.
 */

fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    name = name,
    color = color
)

fun Tag.toEntity(): TagEntity = TagEntity(
    id = id,
    name = name,
    color = color
)
