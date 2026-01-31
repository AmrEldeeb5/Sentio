package com.example.klarity.presentation.screen.home.util

import com.example.klarity.domain.models.Note

/**
 * Returns a display title for the note.
 * Logic:
 * 1. If title is not blank, use it.
 * 2. If title is blank, try to extract from content (first non-empty line).
 * 3. Default to "Untitled Note".
 */
fun Note.displayTitle(): String {
    if (title.isNotBlank()) return title

    // Try to extract from content
    if (content.isNotBlank()) {
        val firstLine = content.lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?.replace(Regex("^#+\\s+"), "") // Remove markdown headers like "# "
            
        if (!firstLine.isNullOrBlank()) {
            return if (firstLine.length > 30) {
                firstLine.take(30) + "..."
            } else {
                firstLine
            }
        }
    }

    return "Untitled Note"
}
