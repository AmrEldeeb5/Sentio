package com.example.klarity.domain.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents a folder for organizing notes hierarchically
 */
@Serializable
data class Folder(
    val id: String,
    val name: String,
    val parentId: String?, // null for root folders
    val createdAt: Instant,
    val icon: String? = null // Emoji or icon identifier
)
