package com.example.klarity.data.mapper

import com.example.klarity.domain.models.Tag
import com.example.klarity.db.Tag as TagEntity

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
