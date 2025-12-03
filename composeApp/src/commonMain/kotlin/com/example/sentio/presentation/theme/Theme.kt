package com.example.sentio.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Sentio Dark Color Scheme
 * Beautiful dark teal/green theme matching the UI mockups.
 */
private val DarkColorScheme = darkColorScheme(
    // Primary: Bright Teal - buttons, links, active states
    primary = SentioColors.AccentPrimary,
    onPrimary = SentioColors.BgPrimary,
    primaryContainer = SentioColors.AccentSecondary,
    onPrimaryContainer = SentioColors.TextPrimary,

    // Secondary: AI Accent - AI features, suggestions
    secondary = SentioColors.AccentAI,
    onSecondary = SentioColors.BgPrimary,
    secondaryContainer = SentioColors.AccentAI.copy(alpha = 0.2f),
    onSecondaryContainer = SentioColors.TextPrimary,

    // Tertiary: Info color
    tertiary = SentioColors.Info,
    onTertiary = SentioColors.BgPrimary,

    // Error
    error = SentioColors.Error,
    onError = SentioColors.TextPrimary,

    // Background: Dark teal - main app background
    background = SentioColors.BgPrimary,
    onBackground = SentioColors.TextPrimary,

    // Surface: Card backgrounds
    surface = SentioColors.BgSecondary,
    onSurface = SentioColors.TextPrimary,
    surfaceVariant = SentioColors.BgTertiary,
    onSurfaceVariant = SentioColors.TextSecondary,

    // Surface containers for different elevations
    surfaceContainerLowest = SentioColors.BgPrimary,
    surfaceContainerLow = SentioColors.BgSecondary,
    surfaceContainer = SentioColors.BgTertiary,
    surfaceContainerHigh = SentioColors.BgElevated,
    surfaceContainerHighest = SentioColors.BgSelected,

    // Outline: Borders
    outline = SentioColors.BorderPrimary,
    outlineVariant = SentioColors.BorderSecondary,

    // Inverse colors
    inverseSurface = SentioColors.TextPrimary,
    inverseOnSurface = SentioColors.BgPrimary,
    inversePrimary = SentioColors.AccentTertiary
)

/**
 * Sentio Shapes - Rounded corners for modern look
 */
private val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Sentio app theme - Dark teal theme for developers.
 */
@Composable
fun SentioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = SentioTypography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Extension properties to access custom Sentio colors from MaterialTheme
 */
val ColorScheme.bgSelected: androidx.compose.ui.graphics.Color
    get() = SentioColors.BgSelected

val ColorScheme.borderSelected: androidx.compose.ui.graphics.Color
    get() = SentioColors.BorderSelected

val ColorScheme.cardBg: androidx.compose.ui.graphics.Color
    get() = SentioColors.BgCard

val ColorScheme.borderPrimary: androidx.compose.ui.graphics.Color
    get() = SentioColors.BorderPrimary

val ColorScheme.textTertiary: androidx.compose.ui.graphics.Color
    get() = SentioColors.TextTertiary

val ColorScheme.accentAI: androidx.compose.ui.graphics.Color
    get() = SentioColors.AccentAI

val ColorScheme.success: androidx.compose.ui.graphics.Color
    get() = SentioColors.Success

val ColorScheme.warning: androidx.compose.ui.graphics.Color
    get() = SentioColors.Warning

