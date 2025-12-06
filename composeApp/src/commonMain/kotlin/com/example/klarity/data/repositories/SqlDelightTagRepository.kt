package com.example.klarity.data.repositories

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.klarity.data.util.DispatcherProvider
import com.example.klarity.data.mapper.toDomain
import com.example.klarity.db.KlarityDatabase
import com.example.klarity.domain.models.Tag
import com.example.klarity.domain.repositories.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository implementation for Tag operations.
 * Directly uses SQLDelight queries for database access.
 */
class SqlDelightTagRepository(
    private val database: KlarityDatabase,
    private val dispatchers: DispatcherProvider
) : TagRepository {

    private val queries get() = database.tagQueries

    override fun getAllTags(): Flow<List<Tag>> =
        queries.selectAll()
            .asFlow()
            .mapToList(dispatchers.io)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTagById(id: String): Tag? = withContext(dispatchers.io) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun createTag(tag: Tag): Result<Tag> = runCatching {
        withContext(dispatchers.io) {
            queries.insert(
                id = tag.id,
                name = tag.name,
                color = tag.color
            )
        }
        tag
    }

    override suspend fun updateTag(tag: Tag): Result<Tag> = runCatching {
        withContext(dispatchers.io) {
            queries.update(
                name = tag.name,
                color = tag.color,
                id = tag.id
            )
        }
        tag
    }

    override suspend fun deleteTag(id: String): Result<Unit> = runCatching {
        withContext(dispatchers.io) {
            queries.delete(id)
        }
    }
}
