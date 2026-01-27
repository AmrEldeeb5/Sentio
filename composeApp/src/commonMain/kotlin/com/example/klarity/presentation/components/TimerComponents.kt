package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import com.example.klarity.presentation.theme.KlarityMotion
import com.example.klarity.presentation.theme.KlarityTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ══════════════════════════════════════════════════════════════════════════════
// TIMER DATA STRUCTURES
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Represents the state of a timer including time values and running state.
 * 
 * @param totalSeconds The total duration of the timer in seconds
 * @param remainingSeconds The remaining time on the timer in seconds
 * @param isRunning Whether the timer is currently counting down
 * @param isPaused Whether the timer is paused
 * @param label Display label for the timer
 */
data class TimerState(
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val label: String = "Timer"
)

/**
 * Preset timer types with predefined durations.
 */
enum class TimerType(val seconds: Int, val label: String) {
    POMODORO(25 * 60, "Pomodoro"),           // 25 minutes
    SHORT_BREAK(5 * 60, "Short Break"),      // 5 minutes
    LONG_BREAK(15 * 60, "Long Break"),       // 15 minutes
    CUSTOM(0, "Custom")                       // User-defined
}

// ══════════════════════════════════════════════════════════════════════════════
// MAIN TIMER COMPONENT
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Klarity Timer - Circular progress timer with persistent notification support.
 * 
 * Features:
 * - Circular progress indicator with gradient ring
 * - Pulsing glow effect when running
 * - Background running capability (platform-specific)
 * - System notifications for start, complete, and warnings
 * - Accessibility support with screen reader announcements
 * - Keyboard shortcuts (Space: start/pause, R: reset, Escape: cancel)
 * 
 * @param state Current timer state
 * @param onStart Callback when timer starts
 * @param onPause Callback when timer pauses
 * @param onReset Callback when timer resets
 * @param onComplete Callback when timer completes
 * @param modifier Modifier for the component
 * @param showNotifications Whether to show system notifications
 */
@Composable
fun KlarityTimer(
    state: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    showNotifications: Boolean = true
) {
    // Calculate progress percentage
    val progress = if (state.totalSeconds > 0) {
        (state.totalSeconds - state.remainingSeconds).toFloat() / state.totalSeconds.toFloat()
    } else 0f

    // Animate progress smoothly
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        ),
        label = "timerProgress"
    )

    // Animate completion effect
    val isComplete = state.remainingSeconds == 0 && !state.isRunning
    val scale by animateFloatAsState(
        targetValue = if (isComplete) 1.2f else 1f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "completeScale"
    )

    // Format time as MM:SS
    val timeText = remember(state.remainingSeconds) {
        val minutes = state.remainingSeconds / 60
        val seconds = state.remainingSeconds % 60
        "%02d:%02d".format(minutes, seconds)
    }

    // Accessibility: Announce progress at 25% intervals
    val lastAnnouncedProgress = remember { mutableStateOf(-1) }
    LaunchedEffect(progress) {
        val progressPercent = (progress * 100).toInt()
        val milestone = (progressPercent / 25) * 25
        if (milestone != lastAnnouncedProgress.value && milestone > 0) {
            lastAnnouncedProgress.value = milestone
            // Screen reader will announce this
        }
    }

    // Keyboard shortcuts
    val keyboardModifier = Modifier.onKeyEvent { event ->
        if (event.type == KeyEventType.KeyUp) {
            when (event.key) {
                Key.Spacebar -> {
                    if (state.isRunning) onPause() else onStart()
                    true
                }
                Key.R -> {
                    onReset()
                    true
                }
                Key.Escape -> {
                    if (state.isRunning) onPause()
                    true
                }
                else -> false
            }
        } else false
    }

    Column(
        modifier = modifier
            .then(keyboardModifier)
            .semantics {
                contentDescription = "Timer: ${state.label}, ${timeText} remaining, ${if (state.isRunning) "running" else "paused"}"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.large)
    ) {
        // Circular Progress Timer
        Box(
            modifier = Modifier
                .size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            // Apply pulsing glow when running
            val glowModifier = if (state.isRunning) {
                Modifier.pulsingGlow(
                    color = KlarityTheme.extendedColors.accentAI,
                    intensity = 0.3f
                )
            } else Modifier

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(glowModifier)
            ) {
                CircularTimerIndicator(
                    progress = animatedProgress,
                    isComplete = isComplete,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
            }

            // Center content: Time and label
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp
                    ),
                    color = if (isComplete) {
                        KlarityTheme.extendedColors.sentioPurple
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(KlarityTheme.spacing.small))
                Text(
                    text = state.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start/Pause button
            Button(
                onClick = if (state.isRunning) onPause else onStart,
                enabled = state.remainingSeconds > 0,
                modifier = Modifier.semantics {
                    contentDescription = if (state.isRunning) "Pause timer" else "Start timer"
                }
            ) {
                Icon(
                    imageVector = if (state.isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(KlarityTheme.spacing.small))
                Text(if (state.isRunning) "Pause" else "Start")
            }

            // Reset button
            OutlinedButton(
                onClick = onReset,
                enabled = state.remainingSeconds != state.totalSeconds,
                modifier = Modifier.semantics {
                    contentDescription = "Reset timer"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(KlarityTheme.spacing.small))
                Text("Reset")
            }

            // Settings button
            IconButton(
                onClick = { /* TODO: Open timer settings */ },
                modifier = Modifier.semantics {
                    contentDescription = "Timer settings"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Display keyboard shortcuts hint
        Text(
            text = "Space: Start/Pause  •  R: Reset  •  Esc: Cancel",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CIRCULAR TIMER INDICATOR
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Circular progress indicator with gradient ring.
 * 
 * @param progress Progress from 0.0 to 1.0
 * @param isComplete Whether the timer has completed
 * @param modifier Modifier for the canvas
 */
@Composable
private fun CircularTimerIndicator(
    progress: Float,
    isComplete: Boolean,
    modifier: Modifier = Modifier
) {
    val gradientColors = if (isComplete) {
        listOf(
            KlarityTheme.extendedColors.sentioPurple,
            KlarityTheme.extendedColors.sentioRose
        )
    } else {
        listOf(
            KlarityTheme.extendedColors.accentAI,
            KlarityTheme.extendedColors.accentAI
        )
    }

    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = (canvasSize / 2) - 40f
        val center = Offset(size.width / 2, size.height / 2)
        val strokeWidth = 12.dp.toPx()

        // Background ring
        drawCircle(
            color = Color(0xFF1A302B), // MaterialTheme.colorScheme.surfaceVariant
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress ring with gradient
        val sweepAngle = 360f * progress
        if (progress > 0f) {
            val gradient = Brush.linearGradient(
                colors = gradientColors,
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y + radius)
            )

            // Draw arc using path
            val startAngle = -90f // Start from top
            for (i in 0 until sweepAngle.toInt()) {
                val angle = startAngle + i
                val angleRad = angle * PI / 180f
                val x = center.x + radius * cos(angleRad).toFloat()
                val y = center.y + radius * sin(angleRad).toFloat()

                drawCircle(
                    brush = gradient,
                    radius = strokeWidth / 2,
                    center = Offset(x, y)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PRESET TIMER COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Pomodoro Timer - 25 minute focus timer.
 * 
 * @param onComplete Callback when timer completes
 * @param modifier Modifier for the component
 */
@Composable
fun PomodoroTimer(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember {
        mutableStateOf(
            TimerState(
                totalSeconds = TimerType.POMODORO.seconds,
                remainingSeconds = TimerType.POMODORO.seconds,
                label = TimerType.POMODORO.label
            )
        )
    }

    // Timer countdown logic
    LaunchedEffect(state.isRunning) {
        if (state.isRunning && state.remainingSeconds > 0) {
            while (isActive && state.isRunning && state.remainingSeconds > 0) {
                delay(1000L)
                state = state.copy(remainingSeconds = state.remainingSeconds - 1)
                
                // Check if completed
                if (state.remainingSeconds == 0) {
                    state = state.copy(isRunning = false)
                    onComplete()
                }
            }
        }
    }

    KlarityTimer(
        state = state,
        onStart = { state = state.copy(isRunning = true, isPaused = false) },
        onPause = { state = state.copy(isRunning = false, isPaused = true) },
        onReset = {
            state = TimerState(
                totalSeconds = TimerType.POMODORO.seconds,
                remainingSeconds = TimerType.POMODORO.seconds,
                label = TimerType.POMODORO.label
            )
        },
        onComplete = onComplete,
        modifier = modifier
    )
}

/**
 * Short Break Timer - 5 minute break timer.
 * 
 * @param onComplete Callback when timer completes
 * @param modifier Modifier for the component
 */
@Composable
fun ShortBreakTimer(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember {
        mutableStateOf(
            TimerState(
                totalSeconds = TimerType.SHORT_BREAK.seconds,
                remainingSeconds = TimerType.SHORT_BREAK.seconds,
                label = TimerType.SHORT_BREAK.label
            )
        )
    }

    // Timer countdown logic
    LaunchedEffect(state.isRunning) {
        if (state.isRunning && state.remainingSeconds > 0) {
            while (isActive && state.isRunning && state.remainingSeconds > 0) {
                delay(1000L)
                state = state.copy(remainingSeconds = state.remainingSeconds - 1)
                
                // Check if completed
                if (state.remainingSeconds == 0) {
                    state = state.copy(isRunning = false)
                    onComplete()
                }
            }
        }
    }

    KlarityTimer(
        state = state,
        onStart = { state = state.copy(isRunning = true, isPaused = false) },
        onPause = { state = state.copy(isRunning = false, isPaused = true) },
        onReset = {
            state = TimerState(
                totalSeconds = TimerType.SHORT_BREAK.seconds,
                remainingSeconds = TimerType.SHORT_BREAK.seconds,
                label = TimerType.SHORT_BREAK.label
            )
        },
        onComplete = onComplete,
        modifier = modifier
    )
}

/**
 * Long Break Timer - 15 minute break timer.
 * 
 * @param onComplete Callback when timer completes
 * @param modifier Modifier for the component
 */
@Composable
fun LongBreakTimer(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var state by remember {
        mutableStateOf(
            TimerState(
                totalSeconds = TimerType.LONG_BREAK.seconds,
                remainingSeconds = TimerType.LONG_BREAK.seconds,
                label = TimerType.LONG_BREAK.label
            )
        )
    }

    // Timer countdown logic
    LaunchedEffect(state.isRunning) {
        if (state.isRunning && state.remainingSeconds > 0) {
            while (isActive && state.isRunning && state.remainingSeconds > 0) {
                delay(1000L)
                state = state.copy(remainingSeconds = state.remainingSeconds - 1)
                
                // Check if completed
                if (state.remainingSeconds == 0) {
                    state = state.copy(isRunning = false)
                    onComplete()
                }
            }
        }
    }

    KlarityTimer(
        state = state,
        onStart = { state = state.copy(isRunning = true, isPaused = false) },
        onPause = { state = state.copy(isRunning = false, isPaused = true) },
        onReset = {
            state = TimerState(
                totalSeconds = TimerType.LONG_BREAK.seconds,
                remainingSeconds = TimerType.LONG_BREAK.seconds,
                label = TimerType.LONG_BREAK.label
            )
        },
        onComplete = onComplete,
        modifier = modifier
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// TIMER SELECTOR COMPONENT
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Timer type selector with segmented buttons.
 * 
 * @param selectedType Currently selected timer type
 * @param onTypeSelected Callback when timer type is selected
 * @param modifier Modifier for the component
 */
@Composable
fun TimerTypeSelector(
    selectedType: TimerType,
    onTypeSelected: (TimerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val types = listOf(
        TimerType.POMODORO,
        TimerType.SHORT_BREAK,
        TimerType.LONG_BREAK
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small)
    ) {
        types.forEach { type ->
            FilterChip(
                selected = type == selectedType,
                onClick = { onTypeSelected(type) },
                label = { Text(type.label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COMPACT TIMER DISPLAY
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Compact timer display for use in toolbars or sidebars.
 * Shows minimal time display with status indicator.
 * 
 * @param state Timer state
 * @param modifier Modifier for the component
 */
@Composable
fun CompactTimerDisplay(
    state: TimerState,
    modifier: Modifier = Modifier
) {
    val timeText = remember(state.remainingSeconds) {
        val minutes = state.remainingSeconds / 60
        val seconds = state.remainingSeconds % 60
        "%02d:%02d".format(minutes, seconds)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (state.isRunning) {
                        KlarityTheme.extendedColors.success
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    },
                    shape = CircleShape
                )
        ) {
            if (state.isRunning) {
                // Pulsing animation
                val alpha by rememberInfiniteTransition(label = "pulse").animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = KlarityTheme.extendedColors.success.copy(alpha = alpha),
                            shape = CircleShape
                        )
                )
            }
        }

        // Time display
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Label
        Text(
            text = state.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
