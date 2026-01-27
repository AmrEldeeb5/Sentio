package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.theme.KlarityMotion
import com.example.klarity.presentation.theme.KlarityTheme
import com.example.klarity.presentation.utils.shouldReduceMotion
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ══════════════════════════════════════════════════════════════════════════════
// AI TASK SUGGESTION BANNER
// ══════════════════════════════════════════════════════════════════════════════

/**
 * AI Task Suggestion Banner - Displays AI-detected tasks from note content.
 * 
 * Shows a prominent banner with AI-detected task suggestions, featuring:
 * - AI sparkle icon with confidence-based glow
 * - Task count and preview text
 * - Accept/Dismiss actions
 * - Auto-dismiss with optional countdown
 * - Confidence indicator for transparency
 * 
 * Visual Design:
 * - Container: KlarityNeumorphicCard
 * - Glow effect: Pulsing glow scaled by confidence (0.0-1.0)
 * - Entrance: Slide from top + fade in with bouncy spring
 * - Exit: Slide to top + fade out
 * 
 * Accessibility:
 * - Semantic role: AlertDialog (temporary banner)
 * - Content descriptions for all icons
 * - Respects reduced motion preferences
 * - Clear button labels
 * 
 * @param taskCount Number of tasks detected by AI
 * @param taskPreview Preview text of the first task
 * @param confidence AI confidence level (0.0 to 1.0)
 * @param onAccept Callback when user accepts the suggestions
 * @param onDismiss Callback when user dismisses the banner
 * @param modifier Modifier for the banner container
 * @param autoHideDelay Auto-dismiss delay in milliseconds (default: 10 seconds)
 * 
 * @example
 * ```kotlin
 * AITaskSuggestionBanner(
 *     taskCount = 3,
 *     taskPreview = "Review design mockups",
 *     confidence = 0.87f,
 *     onAccept = { /* Create tasks */ },
 *     onDismiss = { /* Hide banner */ }
 * )
 * ```
 */
@Composable
fun AITaskSuggestionBanner(
    taskCount: Int,
    taskPreview: String,
    confidence: Float,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    autoHideDelay: Long = 10_000L
) {
    // State for visibility and auto-dismiss
    var isVisible by remember { mutableStateOf(true) }
    
    // Hover state for lift animation
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Respect reduced motion preferences
    val shouldReduce = shouldReduceMotion()
    
    // Auto-dismiss after delay
    LaunchedEffect(Unit) {
        delay(autoHideDelay)
        isVisible = false
        delay(300) // Wait for exit animation
        onDismiss()
    }
    
    // Confidence-based glow intensity
    val glowIntensity = (confidence * 0.4f).coerceIn(0f, 0.4f)
    
    // Organic lift animations
    val elevation by animateDpAsState(
        targetValue = if (isHovered && !shouldReduce) 6.dp else 2.dp,
        animationSpec = KlarityMotion.springBouncy(),
        label = "bannerElevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered && !shouldReduce) 1.01f else 1.0f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "bannerScale"
    )
    
    val rotationX by animateFloatAsState(
        targetValue = if (isHovered && !shouldReduce) -1.5f else 0f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "bannerRotationX"
    )
    
    // Animation for entrance/exit
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = KlarityMotion.springBouncy()
        ) + fadeIn(
            animationSpec = KlarityMotion.emphasizedEnter()
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = KlarityMotion.emphasizedExit()
        ) + fadeOut(
            animationSpec = KlarityMotion.emphasizedExit()
        )
    ) {
        KlarityNeumorphicCard(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                    this.rotationX = rotationX
                    this.shadowElevation = elevation.toPx()
                }
                .hoverable(interactionSource)
                .pulsingGlow(
                    color = KlarityTheme.extendedColors.sentioPurple,
                    intensity = glowIntensity
                )
                .semantics {
                    role = androidx.compose.ui.semantics.Role.Button
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: AI Sparkle Icon
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "AI detected tasks",
                    modifier = Modifier.size(24.dp),
                    tint = KlarityTheme.extendedColors.sentioPurple
                )
                
                // Center: Text Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.extraSmall)
                ) {
                    // Title with task count
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "I found $taskCount potential task${if (taskCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Confidence indicator
                        when {
                            confidence >= 0.85f -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "High confidence",
                                    modifier = Modifier.size(16.dp),
                                    tint = KlarityTheme.extendedColors.success
                                )
                            }
                            confidence < 0.6f -> {
                                Text(
                                    text = "Low confidence",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    // Subtitle with task preview
                    Text(
                        text = "$taskPreview...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                // Right: Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dismiss Button (text only)
                    TextButton(
                        onClick = {
                            isVisible = false
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    
                    // Accept Button (primary)
                    Button(
                        onClick = {
                            isVisible = false
                            onAccept()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KlarityTheme.extendedColors.sentioPurple,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Accept",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            
            // Optional: Countdown progress bar at bottom
            if (autoHideDelay > 0L) {
                CountdownProgressBar(
                    duration = autoHideDelay,
                    color = KlarityTheme.extendedColors.sentioPurple
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COUNTDOWN PROGRESS BAR (INTERNAL HELPER)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Internal countdown progress bar for auto-dismiss feedback.
 * Shows a thin progress bar at the bottom of the banner that depletes over time.
 */
@Composable
private fun CountdownProgressBar(
    duration: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Animate progress from 1.0 to 0.0 over the duration
    var progress by remember { mutableStateOf(1f) }
    
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (progress > 0f) {
            val elapsed = System.currentTimeMillis() - startTime
            progress = 1f - (elapsed.toFloat() / duration.toFloat())
            delay(16L) // ~60fps
        }
    }
    
    // Respect reduced motion - show static bar if motion is reduced
    val shouldReduce = shouldReduceMotion()
    val effectiveProgress = if (shouldReduce) 0.5f else progress
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(color.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(effectiveProgress.coerceIn(0f, 1f))
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.7f)
                        )
                    )
                )
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// AI MEMORY TRAIL DATA STRUCTURE
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Data class representing a single AI memory/interaction item in the breadcrumb trail.
 * 
 * @param id Unique identifier for the memory item
 * @param query User's question or command that initiated this interaction
 * @param timestamp Unix timestamp (milliseconds) when this interaction occurred
 * @param context Brief description of what was discussed or accomplished
 * @param isLatest True if this is the most recent interaction (for visual highlighting)
 */
data class AIMemoryItem(
    val id: String,
    val query: String,
    val timestamp: Long,
    val context: String,
    val isLatest: Boolean = false
)

// ══════════════════════════════════════════════════════════════════════════════
// AI MEMORY TRAIL BREADCRUMB COMPONENT
// ══════════════════════════════════════════════════════════════════════════════

/**
 * AI Memory Trail - Breadcrumb-style navigation for recent AI interactions.
 * 
 * Displays the last 5 AI interactions as a horizontal scrollable breadcrumb trail,
 * allowing users to navigate back to previous contexts. The most recent interaction
 * is visually highlighted with a pulsing glow effect.
 * 
 * Visual Design:
 * - Horizontal scrollable row with breadcrumb items
 * - Each item: Small neumorphic card with truncated query text
 * - Latest item: Pulsing glow with luminous teal color, slightly larger (1.1x scale)
 * - Arrow separators between items
 * - Optional timestamps below query text
 * 
 * Animations:
 * - Entrance: Slide in from left with bouncy spring
 * - New item added: Items slide right, oldest fades out
 * - Hover: Lift by 2dp with bouncy spring
 * - Pulsing glow on latest item only
 * 
 * Accessibility:
 * - Each item has Button role and content description
 * - Screen reader announces "Latest" for newest item
 * - Keyboard navigation with Tab and Arrow keys
 * - Long press to show full query text in tooltip
 * - Respects reduced motion preferences
 * 
 * @param memoryItems List of AI memory items (max 5 items, newest first)
 * @param onItemClick Callback when a memory item is clicked
 * @param modifier Modifier for the trail container
 * @param showTimestamps Whether to display timestamps below query text (default: true)
 * 
 * @example
 * ```kotlin
 * AIMemoryTrail(
 *     memoryItems = listOf(
 *         AIMemoryItem(
 *             id = "1",
 *             query = "What are the Q1 goals?",
 *             timestamp = System.currentTimeMillis(),
 *             context = "Discussed project roadmap and milestones",
 *             isLatest = true
 *         ),
 *         AIMemoryItem(
 *             id = "2",
 *             query = "Find related notes",
 *             timestamp = System.currentTimeMillis() - 60000,
 *             context = "Found 5 related notes",
 *             isLatest = false
 *         )
 *     ),
 *     onItemClick = { item -> /* Navigate to context */ },
 *     showTimestamps = true
 * )
 * ```
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AIMemoryTrail(
    memoryItems: List<AIMemoryItem>,
    onItemClick: (AIMemoryItem) -> Unit,
    modifier: Modifier = Modifier,
    showTimestamps: Boolean = true
) {
    // Take only the first 5 items (most recent)
    val displayItems = remember(memoryItems) { memoryItems.take(5) }
    
    // State for keyboard navigation
    var focusedIndex by remember { mutableStateOf(-1) }
    
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .onKeyEvent { event ->
                when {
                    event.key == Key.DirectionRight && event.type == KeyEventType.KeyDown -> {
                        if (focusedIndex < displayItems.size - 1) {
                            focusedIndex++
                            true
                        } else false
                    }
                    event.key == Key.DirectionLeft && event.type == KeyEventType.KeyDown -> {
                        if (focusedIndex > 0) {
                            focusedIndex--
                            true
                        } else false
                    }
                    event.key == Key.Enter && event.type == KeyEventType.KeyDown -> {
                        if (focusedIndex >= 0 && focusedIndex < displayItems.size) {
                            onItemClick(displayItems[focusedIndex])
                            true
                        } else false
                    }
                    else -> false
                }
            },
        horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.extraSmall),
        contentPadding = PaddingValues(horizontal = KlarityTheme.spacing.small)
    ) {
        items(
            items = displayItems,
            key = { it.id }
        ) { item ->
            val isLatest = item.isLatest
            val itemIndex = displayItems.indexOf(item)
            
            // Entrance animation for each item
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = KlarityMotion.springBouncy()
                ) + fadeIn(
                    animationSpec = KlarityMotion.emphasizedEnter()
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Memory breadcrumb item
                    MemoryBreadcrumbItem(
                        item = item,
                        onClick = { onItemClick(item) },
                        showTimestamp = showTimestamps,
                        isFocused = focusedIndex == itemIndex,
                        onFocusChanged = { focused ->
                            if (focused) focusedIndex = itemIndex
                        }
                    )
                    
                    // Arrow separator (except after last item)
                    if (itemIndex < displayItems.size - 1) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual memory breadcrumb item card.
 * Internal composable for rendering each memory item in the trail.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MemoryBreadcrumbItem(
    item: AIMemoryItem,
    onClick: () -> Unit,
    showTimestamp: Boolean,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Respect reduced motion preferences
    val shouldReduce = shouldReduceMotion()
    
    // Hover elevation animation - enhanced with rotation and scale
    val elevationOffset by animateDpAsState(
        targetValue = if (isHovered && !shouldReduce) 4.dp else 0.dp,
        animationSpec = KlarityMotion.springBouncy(),
        label = "breadcrumbElevation"
    )
    
    // Scale for latest item (1.1x larger)
    val scale by animateFloatAsState(
        targetValue = if (item.isLatest) 1.1f else 1f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "breadcrumbScale"
    )
    
    val rotationX by animateFloatAsState(
        targetValue = if (isHovered && !shouldReduce) -1.5f else 0f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "breadcrumbRotationX"
    )
    
    // Format timestamp
    val timestampText = remember(item.timestamp) {
        formatTimestamp(item.timestamp)
    }
    
    // Tooltip state for long press
    var showTooltip by remember { mutableStateOf(false) }
    
    Box {
        KlarityNeumorphicCard(
            onClick = onClick,
            modifier = modifier
                .graphicsLayer {
                    this.scaleX = scale
                    this.scaleY = scale
                    this.rotationX = rotationX
                    this.translationY = -elevationOffset.toPx()
                    this.shadowElevation = elevationOffset.toPx()
                }
                .then(
                    if (item.isLatest) {
                        Modifier.pulsingGlow(
                            color = KlarityTheme.extendedColors.luminousTeal,
                            intensity = 0.35f
                        )
                    } else Modifier
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showTooltip = true },
                    interactionSource = interactionSource,
                    indication = null
                )
                .onFocusChanged { focusState ->
                    onFocusChanged(focusState.isFocused)
                }
                .semantics {
                    role = Role.Button
                    contentDescription = "${item.query} from $timestampText${if (item.isLatest) " - Latest" else ""}"
                },
            glowIntensity = 0.2f
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 180.dp)
                    .padding(KlarityTheme.spacing.small),
                verticalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.extraSmall)
            ) {
                // Query text (truncated to 30 chars)
                Text(
                    text = item.query.take(30).let { if (item.query.length > 30) "$it..." else it },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.isLatest) 
                        KlarityTheme.extendedColors.luminousTeal 
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Optional timestamp
                if (showTimestamp) {
                    Text(
                        text = timestampText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                // Latest badge
                if (item.isLatest) {
                    Text(
                        text = "Latest",
                        style = MaterialTheme.typography.labelSmall,
                        color = KlarityTheme.extendedColors.luminousTeal,
                        modifier = Modifier
                            .background(
                                color = KlarityTheme.extendedColors.luminousTeal.copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        
        // Tooltip for full query text (on long press)
        if (showTooltip) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(
                            text = item.query,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                state = rememberTooltipState(isPersistent = true),
                focusable = false
            ) {}
            
            // Auto-hide tooltip after 3 seconds
            LaunchedEffect(showTooltip) {
                delay(3000)
                showTooltip = false
            }
        }
    }
}

/**
 * Helper function to format timestamp as relative time.
 * Examples: "Just now", "5m ago", "2h ago", "Yesterday", "3 days ago"
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 172800_000 -> "Yesterday"
        diff < 604800_000 -> "${diff / 86400_000} days ago"
        else -> {
            // Format as date for older items
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${dateTime.monthNumber}/${dateTime.dayOfMonth}"
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// AI LINK SUGGESTION DATA STRUCTURE
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Represents an AI-suggested related note with relevance scoring.
 * 
 * @param noteId Unique identifier for the note
 * @param noteTitle Display title of the note
 * @param relevance Relevance score from 0.0 to 1.0
 * @param snippet 50-character preview of note content
 * @param commonKeywords List of shared keywords between notes
 */
data class AILinkSuggestion(
    val noteId: String,
    val noteTitle: String,
    val relevance: Float, // 0.0 to 1.0
    val snippet: String, // 50-char preview
    val commonKeywords: List<String>
)

// ══════════════════════════════════════════════════════════════════════════════
// AI LINK SUGGESTION PANEL
// ══════════════════════════════════════════════════════════════════════════════

/**
 * AI Link Suggestion Panel - Displays AI-suggested related notes with relevance scores.
 * 
 * Features an organic pulsing glow effect and staggered entrance animations for suggestions.
 * Each suggestion includes a relevance indicator, note preview, and common keywords.
 * 
 * Visual Design:
 * - Container: KlarityNeumorphicCard with pulsingGlow (sentioIndigo, 0.25f intensity)
 * - Header: "Related Notes" title with dismiss button
 * - Suggestions: Cards with hover lift animation (2dp elevation)
 * - Relevance indicator: Circular progress ring with color-coded scoring
 * - Common keywords: Chips that fade in after 300ms delay
 * 
 * Accessibility:
 * - Each suggestion has Role.Button semantics
 * - Relevance percentage announced to screen readers
 * - Keyboard navigation: Tab to focus, Enter to activate
 * 
 * @param suggestions List of AI-suggested related notes (max 5 displayed)
 * @param onLinkClick Callback when a suggestion is clicked (receives noteId)
 * @param onDismiss Callback when dismiss button is clicked
 * @param modifier Modifier for the panel container
 * @param maxSuggestions Maximum number of suggestions to display (default: 5)
 * 
 * @example
 * ```kotlin
 * AILinkSuggestionPanel(
 *     suggestions = listOf(
 *         AILinkSuggestion(
 *             noteId = "123",
 *             noteTitle = "Project Roadmap 2024",
 *             relevance = 0.87f,
 *             snippet = "Q1 goals include improving user onboarding...",
 *             commonKeywords = listOf("roadmap", "goals", "Q1")
 *         )
 *     ),
 *     onLinkClick = { noteId -> /* Navigate */ },
 *     onDismiss = { /* Hide panel */ }
 * )
 * ```
 */
@Composable
fun AILinkSuggestionPanel(
    suggestions: List<AILinkSuggestion>,
    onLinkClick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    maxSuggestions: Int = 5
) {
    val displayedSuggestions = suggestions.take(maxSuggestions)
    
    KlarityNeumorphicCard(
        modifier = modifier
            .pulsingGlow(
                color = KlarityTheme.extendedColors.sentioIndigo,
                intensity = 0.25f
            )
            .fillMaxWidth()
    ) {
        // Header with title and dismiss button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Related Notes",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss suggestions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(KlarityTheme.spacing.small))
        
        // List of suggestions with staggered animation
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            itemsIndexed(displayedSuggestions) { index, suggestion ->
                var isVisible by remember { mutableStateOf(false) }
                
                // Staggered entrance animation (50ms delay per item)
                LaunchedEffect(Unit) {
                    delay(index * 50L)
                    isVisible = true
                }
                
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = KlarityMotion.springBouncy()) +
                            expandVertically(animationSpec = KlarityMotion.springBouncy())
                ) {
                    AILinkSuggestionItem(
                        suggestion = suggestion,
                        onClick = { onLinkClick(suggestion.noteId) }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// AI LINK SUGGESTION ITEM
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Individual suggestion item within the AI Link Suggestion Panel.
 * 
 * Features:
 * - Hover animation: Lifts by 2dp with bouncy spring
 * - Relevance indicator: Circular progress ring
 * - Note title and snippet preview
 * - Common keywords as chips (fade in after 300ms)
 * - Keyboard navigation support
 * 
 * @param suggestion The AI link suggestion data
 * @param onClick Callback when item is clicked
 */
@Composable
private fun AILinkSuggestionItem(
    suggestion: AILinkSuggestion,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Respect reduced motion preferences
    val shouldReduce = shouldReduceMotion()
    
    // Hover lift animation (2dp elevation) - enhanced with rotation and scale
    val elevation by animateDpAsState(
        targetValue = if (isHovered && !shouldReduce) 6.dp else 2.dp,
        animationSpec = KlarityMotion.springBouncy(),
        label = "suggestionHoverLift"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered && !shouldReduce) 1.02f else 1.0f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "suggestionScale"
    )
    
    val rotationX by animateFloatAsState(
        targetValue = if (isHovered && !shouldReduce) -2f else 0f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "suggestionRotationX"
    )
    
    val relevancePercentage = (suggestion.relevance * 100).toInt()
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.rotationX = rotationX
                this.shadowElevation = elevation.toPx()
            }
            .semantics {
                role = Role.Button
                contentDescription = "${suggestion.noteTitle}, $relevancePercentage% relevant"
            }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = if (shouldReduce) elevation else 0.dp,
        shadowElevation = if (shouldReduce) elevation else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KlarityTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Relevance indicator (circular progress)
            RelevanceIndicator(
                relevance = suggestion.relevance,
                modifier = Modifier.size(40.dp)
            )
            
            // Note title, snippet, and keywords
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.extraSmall)
            ) {
                Text(
                    text = suggestion.noteTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = suggestion.snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Common keywords (fade in after 300ms)
                var showKeywords by remember { mutableStateOf(false) }
                
                LaunchedEffect(Unit) {
                    delay(300L)
                    showKeywords = true
                }
                
                AnimatedVisibility(
                    visible = showKeywords && suggestion.commonKeywords.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(300))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.extraSmall)
                    ) {
                        suggestion.commonKeywords.take(3).forEach { keyword ->
                            KeywordChip(text = keyword)
                        }
                    }
                }
            }
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// RELEVANCE INDICATOR
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Circular progress ring showing relevance percentage with color-coded scoring.
 * 
 * Color Mapping:
 * - < 0.5 (< 50%): error (red)
 * - 0.5-0.75 (50-75%): tertiary (info blue)
 * - > 0.75 (> 75%): sentioIndigo (purple)
 * 
 * @param relevance Relevance score from 0.0 to 1.0
 * @param modifier Modifier for the indicator
 */
@Composable
private fun RelevanceIndicator(
    relevance: Float,
    modifier: Modifier = Modifier
) {
    val percentage = (relevance * 100).toInt()
    
    // Determine color based on relevance threshold
    val color = when {
        relevance < 0.5f -> MaterialTheme.colorScheme.error
        relevance < 0.75f -> MaterialTheme.colorScheme.tertiary
        else -> KlarityTheme.extendedColors.sentioIndigo
    }
    
    // Animate progress from 0 to target relevance
    val animatedProgress by animateFloatAsState(
        targetValue = relevance,
        animationSpec = KlarityMotion.springBouncy(),
        label = "relevanceProgress"
    )
    
    Box(
        modifier = modifier
            .drawBehind {
                val strokeWidth = 4.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)
                
                // Background circle (gray)
                drawCircle(
                    color = color.copy(alpha = 0.2f),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )
                
                // Progress arc
                val sweepAngle = 360f * animatedProgress
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        x = centerOffset.x - radius,
                        y = centerOffset.y - radius
                    ),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// KEYWORD CHIP
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Small chip displaying a common keyword between notes.
 * 
 * @param text The keyword text to display
 */
@Composable
private fun KeywordChip(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.height(20.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
