package com.example.sentio.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the app.
 */
sealed interface Screen {
    @Serializable
    data object Home : Screen
    @Serializable
    data class Editor(val noteId: String) : Screen
    @Serializable
    data object Settings : Screen
}
