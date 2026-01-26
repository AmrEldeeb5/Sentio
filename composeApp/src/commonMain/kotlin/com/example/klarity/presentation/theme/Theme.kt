package com.example.klarity.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ══════════════════════════════════════════════════════════════════════════════
// MATERIAL 3 MOTION TOKENS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Motion Easing curves for consistent animations
 */
object KlarityMotion {
    // Standard easing - used for most animations
    val EasingStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    // Emphasized easing - used for hero/focal animations
    val EasingEmphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EasingEmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EasingEmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    // Duration tokens (in milliseconds)
    object Duration {
        const val Short1 = 50
        const val Short2 = 100
        const val Short3 = 150
        const val Short4 = 200
        const val Medium1 = 250
        const val Medium2 = 300
        const val Medium3 = 350
        const val Medium4 = 400
        const val Long1 = 450
        const val Long2 = 500
        const val Long3 = 550
        const val Long4 = 600
        const val ExtraLong1 = 700
        const val ExtraLong2 = 800
        const val ExtraLong3 = 900
        const val ExtraLong4 = 1000
    }

    // Pre-built animation specs
    fun <T> emphasizedEnter() = tween<T>(
        durationMillis = Duration.Medium3,
        easing = EasingEmphasizedDecelerate
    )

    fun <T> emphasizedExit() = tween<T>(
        durationMillis = Duration.Medium1,
        easing = EasingEmphasizedAccelerate
    )

    fun <T> standardEnter() = tween<T>(
        durationMillis = Duration.Medium2,
        easing = EasingStandard
    )

    fun <T> standardExit() = tween<T>(
        durationMillis = Duration.Short4,
        easing = EasingStandard
    )

    fun <T> quickExit() = tween<T>(
        durationMillis = Duration.Short2,
        easing = EasingStandard
    )

    // ══════════════════════════════════════════════════════════════════════════════
    // SPRING PHYSICS SPECS
    // ══════════════════════════════════════════════════════════════════════════════

    /**
     * Bouncy spring animation with organic, playful motion.
     *
     * Characteristics:
     * - Medium bounce (damping ratio: 0.5)
     * - Medium stiffness (1500f)
     * - Natural, organic feel with visible overshoot
     *
     * Recommended use cases:
     * - Card hover lifts and elevation changes
     * - Button press feedback
     * - Interactive UI element responses
     * - Organic transitions that need personality
     * - Focus indicators and selection animations
     *
     * Example:
     * ```kotlin
     * val elevation by animateDpAsState(
     *     targetValue = if (hovered) 4.dp else 2.dp,
     *     animationSpec = KlarityMotion.springBouncy()
     * )
     * ```
     *
     * @param dampingRatio Controls the bounce amount (default: 0.5 = medium bouncy)
     * @param stiffness Controls animation speed (default: 1500f = medium speed)
     * @return SpringSpec for use with Compose animation APIs
     */
    fun <T> springBouncy(
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium
    ): SpringSpec<T> = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness
    )

    /**
     * Gentle spring animation with smooth, subtle motion.
     *
     * Characteristics:
     * - No bounce (damping ratio: 1.0)
     * - Low stiffness (200f)
     * - Smooth, fluid transitions without overshoot
     *
     * Recommended use cases:
     * - Panel slides and drawer animations
     * - Opacity fade transitions
     * - Smooth offset animations
     * - Gentle scale changes
     * - Background color transitions
     *
     * Example:
     * ```kotlin
     * val offset by animateFloatAsState(
     *     targetValue = if (visible) 0f else 1000f,
     *     animationSpec = KlarityMotion.springGentle()
     * )
     * ```
     *
     * @param dampingRatio Controls the bounce amount (default: 1.0 = no bounce)
     * @param stiffness Controls animation speed (default: 200f = gentle speed)
     * @return SpringSpec for use with Compose animation APIs
     */
    fun <T> springGentle(
        dampingRatio: Float = Spring.DampingRatioNoBouncy,
        stiffness: Float = Spring.StiffnessLow
    ): SpringSpec<T> = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness
    )

    /**
     * Snappy spring animation with quick, responsive motion.
     *
     * Characteristics:
     * - Low bounce (damping ratio: 0.25)
     * - High stiffness (10000f)
     * - Quick, energetic response with minimal overshoot
     *
     * Recommended use cases:
     * - Toggle switches and checkboxes
     * - Quick action feedback
     * - Snappy UI responses
     * - Interactive controls
     * - Rapid state changes
     *
     * Example:
     * ```kotlin
     * val color by animateColorAsState(
     *     targetValue = if (checked) primary else outline,
     *     animationSpec = KlarityMotion.springSnappy()
     * )
     * ```
     *
     * @param dampingRatio Controls the bounce amount (default: 0.25 = low bouncy)
     * @param stiffness Controls animation speed (default: 10000f = very fast)
     * @return SpringSpec for use with Compose animation APIs
     */
    fun <T> springSnappy(
        dampingRatio: Float = Spring.DampingRatioLowBouncy,
        stiffness: Float = Spring.StiffnessHigh
    ): SpringSpec<T> = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// EXTENDED COLOR SYSTEM
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Extended colors beyond Material 3's standard ColorScheme.
 * Provides semantic colors specific to Klarity's design system.
 */
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val accentAI: Color,
    val onAccentAI: Color,
    val accentAIContainer: Color,
    val surfaceGlow: Color,
    val borderSelected: Color,
    val textTertiary: Color,
    val textMuted: Color,
    // Accent colors
    val luminousTeal: Color,
    val electricMint: Color,
    // Sentio colors
    val sentioPurple: Color,
    val sentioIndigo: Color,
    val sentioRose: Color,
    val sentioGradientAI: Pair<Color, Color>,
    val sentioGradientEnergy: Pair<Color, Color>
)

private val KlarityExtendedColors = ExtendedColors(
    success = KlarityColors.Success,
    onSuccess = KlarityColors.BgPrimary,
    successContainer = KlarityColors.Success.copy(alpha = 0.2f),
    onSuccessContainer = KlarityColors.Success,
    warning = KlarityColors.Warning,
    onWarning = KlarityColors.BgPrimary,
    warningContainer = KlarityColors.Warning.copy(alpha = 0.2f),
    onWarningContainer = KlarityColors.Warning,
    info = KlarityColors.Info,
    onInfo = KlarityColors.BgPrimary,
    accentAI = KlarityColors.AccentAI,
    onAccentAI = KlarityColors.BgPrimary,
    accentAIContainer = KlarityColors.AccentAI.copy(alpha = 0.2f),
    surfaceGlow = KlarityColors.GlowColor,
    borderSelected = KlarityColors.BorderSelected,
    textTertiary = KlarityColors.TextTertiary,
    textMuted = KlarityColors.TextMuted,
    // Accent colors
    luminousTeal = KlarityColors.LuminousTeal,
    electricMint = KlarityColors.ElectricMint,
    // Sentio colors
    sentioPurple = KlarityColors.SentioPurple,
    sentioIndigo = KlarityColors.SentioIndigo,
    sentioRose = KlarityColors.SentioRose,
    sentioGradientAI = SentioGradientAI.start to SentioGradientAI.end,
    sentioGradientEnergy = SentioGradientEnergy.start to SentioGradientEnergy.end
)

val LocalExtendedColors = staticCompositionLocalOf { KlarityExtendedColors }

// ══════════════════════════════════════════════════════════════════════════════
// SPACING SYSTEM
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 aligned spacing tokens (4dp base unit)
 */
@Immutable
data class Spacing(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val massive: Dp = 64.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

// ══════════════════════════════════════════════════════════════════════════════
// DARK COLOR SCHEME
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Klarity Dark Color Scheme - Enhanced with M3 surface tint and dim/bright tokens
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
    tertiaryContainer = KlarityColors.Info.copy(alpha = 0.2f),
    onTertiaryContainer = KlarityColors.TextPrimary,

    // Error
    error = KlarityColors.Error,
    onError = KlarityColors.TextPrimary,
    errorContainer = KlarityColors.Error.copy(alpha = 0.2f),
    onErrorContainer = KlarityColors.Error,

    // Background: Dark teal - main app background
    background = KlarityColors.BgPrimary,
    onBackground = KlarityColors.TextPrimary,

    // Surface: Card backgrounds with tint support
    surface = KlarityColors.BgSecondary,
    onSurface = KlarityColors.TextPrimary,
    surfaceVariant = KlarityColors.BgTertiary,
    onSurfaceVariant = KlarityColors.TextSecondary,

    // NEW M3: Surface tint for elevation overlay
    surfaceTint = KlarityColors.AccentPrimary,

    // NEW M3: Surface brightness variants
    surfaceBright = KlarityColors.BgElevated,
    surfaceDim = KlarityColors.BgPrimary,

    // Surface containers for different elevations (M3 tonal system)
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
    inversePrimary = KlarityColors.AccentTertiary,

    // Scrim for modal overlays
    scrim = KlarityColors.ModalOverlay
)

// ══════════════════════════════════════════════════════════════════════════════
// SHAPES
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Klarity Shapes - Rounded corners following M3 shape scale
 */
private val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp) // M3 uses 28dp for extra large
)

// ══════════════════════════════════════════════════════════════════════════════
// THEME COMPOSABLE
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Klarity app theme - Dark teal theme with M3 features.
 * Provides extended colors, spacing, and motion tokens.
 */
@Composable
fun KlarityTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalExtendedColors provides KlarityExtendedColors,
        LocalSpacing provides Spacing()
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = KlarityTypography,
            shapes = Shapes,
            content = content
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// THEME ACCESSORS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Access extended colors from composition
 */
object KlarityTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current

    val spacing: Spacing
        @Composable
        get() = LocalSpacing.current
}

// ══════════════════════════════════════════════════════════════════════════════
// COLOR SCHEME EXTENSIONS (Backward Compatibility)
// ══════════════════════════════════════════════════════════════════════════════

val ColorScheme.bgSelected: Color
    get() = KlarityColors.BgSelected

val ColorScheme.borderSelected: Color
    get() = KlarityColors.BorderSelected

val ColorScheme.cardBg: Color
    get() = KlarityColors.BgCard

val ColorScheme.borderPrimary: Color
    get() = KlarityColors.BorderPrimary

val ColorScheme.textTertiary: Color
    get() = KlarityColors.TextTertiary

val ColorScheme.accentAI: Color
    get() = KlarityColors.AccentAI

val ColorScheme.success: Color
    get() = KlarityColors.Success

val ColorScheme.warning: Color
    get() = KlarityColors.Warning

// ══════════════════════════════════════════════════════════════════════════════
// SENTIO COLOR SCHEME EXTENSIONS
// ══════════════════════════════════════════════════════════════════════════════

val ColorScheme.sentioPurple: Color
    @Composable
    get() = KlarityTheme.extendedColors.sentioPurple

val ColorScheme.sentioIndigo: Color
    @Composable
    get() = KlarityTheme.extendedColors.sentioIndigo

val ColorScheme.sentioRose: Color
    @Composable
    get() = KlarityTheme.extendedColors.sentioRose

val ColorScheme.sentioGradientAI: Pair<Color, Color>
    @Composable
    get() = KlarityTheme.extendedColors.sentioGradientAI

val ColorScheme.sentioGradientEnergy: Pair<Color, Color>
    @Composable
    get() = KlarityTheme.extendedColors.sentioGradientEnergy

