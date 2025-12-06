package com.example.klarity.domain.models

import kotlinx.serialization.Serializable

/**
 * Represents a tag for categorizing notes
 */
@Serializable
data class Tag(
    val id: String,
    val name: String,
    val color: String? = null // Hex color code (e.g., "#FF5733")
)
