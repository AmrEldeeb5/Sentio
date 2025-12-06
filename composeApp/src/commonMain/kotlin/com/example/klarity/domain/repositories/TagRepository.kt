package com.example.klarity.domain.repositories

import com.example.klarity.domain.models.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun getTagById(id: String): Tag?
    suspend fun createTag(tag: Tag): Result<Tag>
    suspend fun updateTag(tag: Tag): Result<Tag>
    suspend fun deleteTag(id: String): Result<Unit>
}
