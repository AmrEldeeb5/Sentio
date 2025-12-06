package com.example.klarity.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Klarity Dark Color Scheme
 * Beautiful dark teal/green theme matching the UI mockups.
 */
private val DarkColorScheme = darkColorScheme(
    // Primary: Bright Teal - buttons, links, active states
    primary = KlarityColors.AccentPrimary,
    onPrimary = KlarityColors.BgPrimary,
    primaryContainer = KlarityColors.AccentSecondary,
    onPrimaryContainer = KlarityColors.TextPrimary,

    // Secondary: AI Accent - AI features, suggestions
    secondary = KlarityColors.AccentAI,
    onSecondary = KlarityColors.BgPrimary,
    secondaryContainer = KlarityColors.AccentAI.copy(alpha = 0.2f),
    onSecondaryContainer = KlarityColors.TextPrimary,

    // Tertiary: Info color
    tertiary = KlarityColors.Info,
    onTertiary = KlarityColors.BgPrimary,

    // Error
    error = KlarityColors.Error,
    onError = KlarityColors.TextPrimary,

    // Background: Dark teal - main app background
    background = KlarityColors.BgPrimary,
    onBackground = KlarityColors.TextPrimary,

    // Surface: Card backgrounds
    surface = KlarityColors.BgSecondary,
    onSurface = KlarityColors.TextPrimary,
    surfaceVariant = KlarityColors.BgTertiary,
    onSurfaceVariant = KlarityColors.TextSecondary,

    // Surface containers for different elevations
    surfaceContainerLowest = KlarityColors.BgPrimary,
    surfaceContainerLow = KlarityColors.BgSecondary,
    surfaceContainer = KlarityColors.BgTertiary,
    surfaceContainerHigh = KlarityColors.BgElevated,
    surfaceContainerHighest = KlarityColors.BgSelected,

    // Outline: Borders
    outline = KlarityColors.BorderPrimary,
    outlineVariant = KlarityColors.BorderSecondary,

    // Inverse colors
    inverseSurface = KlarityColors.TextPrimary,
    inverseOnSurface = KlarityColors.BgPrimary,
    inversePrimary = KlarityColors.AccentTertiary
)

/**
 * Klarity Shapes - Rounded corners for modern look
 */
private val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/**
 * Klarity app theme - Dark teal theme for developers.
 */
@Composable
fun KlarityTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = KlarityTypography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Extension properties to access custom Klarity colors from MaterialTheme
 */
val ColorScheme.bgSelected: androidx.compose.ui.graphics.Color
    get() = KlarityColors.BgSelected

val ColorScheme.borderSelected: androidx.compose.ui.graphics.Color
    get() = KlarityColors.BorderSelected

val ColorScheme.cardBg: androidx.compose.ui.graphics.Color
    get() = KlarityColors.BgCard

val ColorScheme.borderPrimary: androidx.compose.ui.graphics.Color
    get() = KlarityColors.BorderPrimary

val ColorScheme.textTertiary: androidx.compose.ui.graphics.Color
    get() = KlarityColors.TextTertiary

val ColorScheme.accentAI: androidx.compose.ui.graphics.Color
    get() = KlarityColors.AccentAI

val ColorScheme.success: androidx.compose.ui.graphics.Color
    get() = KlarityColors.Success

val ColorScheme.warning: androidx.compose.ui.graphics.Color
    get() = KlarityColors.Warning

