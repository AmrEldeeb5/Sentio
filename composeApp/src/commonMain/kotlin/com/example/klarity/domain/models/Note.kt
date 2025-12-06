package com.example.klarity.domain.models

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Note status enum for tracking progress
 */
@Serializable
enum class NoteStatus {
    NONE,
    IN_PROGRESS,
    COMPLETED,
    ON_HOLD,
    ARCHIVED
}

/**
 * Core domain model for a note in Klarity.
 * Represents a single document with markdown content, metadata, and relationships.
 */
@Serializable
data class Note(
    val id: String = uuid4().toString(),
    val title: String,
    val content: String,
    val folderId: String?,
    val tags: List<String> = emptyList(),
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val status: NoteStatus = NoteStatus.NONE
) {
    /**
     * Returns the word count of the content
     */
    fun wordCount(): Int {
        return content.split(Regex("\\s+"))
            .count { it.isNotBlank() }
    }

    /**
     * Returns a preview of the content (first 200 characters)
     */
    fun preview(): String {
        return content.take(200).trim() + if (content.length > 200) "..." else ""
    }

    /**
     * Checks if note matches search query
     */
    fun matchesQuery(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return title.lowercase().contains(lowerQuery) ||
                content.lowercase().contains(lowerQuery) ||
                tags.any { it.lowercase().contains(lowerQuery) }
    }
}

