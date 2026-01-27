package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.theme.KlarityTheme
import com.example.klarity.presentation.components.KlarityNeumorphicCard
import com.example.klarity.presentation.components.pulsingGlow
import com.example.klarity.presentation.theme.KlarityMotion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Data class representing an AI-curated context item
 */
data class AIContextItem(
    val id: String,
    val title: String,
    val type: AIContextItemType,
    val relevance: Float, // 0.0 to 1.0
    val preview: String,
    val timestamp: Long,
    val isBookmarked: Boolean = false
)

/**
 * Data class for drag state during drag-and-drop operations
 */
data class DragState(
    val isDragging: Boolean = false,
    val dragOffset: Offset = Offset.Zero
)

/**
 * Data class representing a dragged context item during drag-and-drop
 */
data class DraggedContextItem(
    val item: AIContextItem,
    val sourcePosition: Offset,
    val currentPosition: Offset
)

/**
 * Enum representing the type of context item
 */
enum class AIContextItemType {
    NOTE, TASK, LINK, FILE
}

/**
 * State for the AI Context Panel
 */
data class AIContextState(
    val items: List<AIContextItem>,
    val isExpanded: Boolean = true,
    val maxItems: Int = 10,
    val bookmarkedItems: List<AIContextItem> = emptyList()
)

/**
 * AI Context Panel - Right-side drawer displaying AI-curated context
 *
 * @param state The current state of the panel
 * @param onItemClick Callback when an item is clicked
 * @param onToggleExpanded Callback to toggle expanded/collapsed state
 * @param onReorder Callback when items are reordered
 * @param onBookmarkToggle Callback when bookmark is toggled
 * @param onDragStart Callback when drag operation starts on an item
 * @param onDragEnd Callback when drag operation ends with final position
 * @param modifier Modifier for the panel
 * @param width Width of the panel when expanded
 */
@Composable
fun AIContextPanel(
    state: AIContextState,
    onItemClick: (AIContextItem) -> Unit,
    onToggleExpanded: () -> Unit,
    onReorder: (List<AIContextItem>) -> Unit,
    onBookmarkToggle: (AIContextItem) -> Unit,
    onDragStart: (AIContextItem) -> Unit = {},
    onDragEnd: (AIContextItem, Offset) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    width: Dp = 320.dp
) {
    var resizableWidth by remember { mutableStateOf(width) }
    var autoRefreshTrigger by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Recent, 1 = Bookmarked
    
    // Auto-refresh every 30 seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000)
            autoRefreshTrigger++
        }
    }
    
    val targetWidth by animateDpAsState(
        targetValue = if (state.isExpanded) resizableWidth else 40.dp,
        animationSpec = KlarityMotion.springBouncy(),
        label = "panel_width"
    )
    
    Box(
        modifier = modifier
            .width(targetWidth)
            .fillMaxHeight()
            .pulsingGlow(
                color = KlarityTheme.extendedColors.sentioPurple,
                intensity = 0.2f
            )
            .semantics { 
                contentDescription = "AI Context Panel"
            }
    ) {
        KlarityNeumorphicCard(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AIContextPanelHeader(
                    isExpanded = state.isExpanded,
                    onToggleExpanded = onToggleExpanded,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (state.isExpanded) {
                    // Tab Row for switching between Recent and Bookmarked
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = KlarityTheme.extendedColors.sentioPurple,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = "Recent",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "Bookmarked",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        )
                    }
                    
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInHorizontally(
                            animationSpec = KlarityMotion.springBouncy(),
                            initialOffsetX = { it }
                        ) + fadeIn(),
                        exit = slideOutHorizontally(
                            animationSpec = KlarityMotion.springBouncy(),
                            targetOffsetX = { it }
                        ) + fadeOut()
                    ) {
                        when (selectedTab) {
                            0 -> AIContextItemList(
                                items = state.items.take(state.maxItems),
                                onItemClick = onItemClick,
                                onReorder = onReorder,
                                onBookmarkToggle = onBookmarkToggle,
                                onDragStart = onDragStart,
                                onDragEnd = onDragEnd,
                                hasMore = state.items.size > state.maxItems,
                                modifier = Modifier.fillMaxSize()
                            )
                            1 -> BookmarkedItemList(
                                items = state.bookmarkedItems,
                                onItemClick = onItemClick,
                                onBookmarkToggle = onBookmarkToggle,
                                onDragStart = onDragStart,
                                onDragEnd = onDragEnd,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
        
        // Resize handle for desktop (left edge)
        if (state.isExpanded) {
            ResizeHandle(
                onDrag = { delta ->
                    val newWidth = (resizableWidth - delta.x.dp).coerceIn(200.dp, 600.dp)
                    resizableWidth = newWidth
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * Header for the AI Context Panel
 */
@Composable
private fun AIContextPanelHeader(
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KlarityTheme.spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isExpanded) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = KlarityTheme.extendedColors.sentioPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "AI Context",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            IconButton(
                onClick = onToggleExpanded,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowLeft,
                    contentDescription = if (isExpanded) "Collapse panel" else "Expand panel",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * List of AI context items with drag-to-reorder support
 */
@Composable
private fun AIContextItemList(
    items: List<AIContextItem>,
    onItemClick: (AIContextItem) -> Unit,
    onReorder: (List<AIContextItem>) -> Unit,
    onBookmarkToggle: (AIContextItem) -> Unit,
    onDragStart: (AIContextItem) -> Unit,
    onDragEnd: (AIContextItem, Offset) -> Unit,
    hasMore: Boolean,
    modifier: Modifier = Modifier
) {
    var draggedItem by remember { mutableStateOf<AIContextItem?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    
    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(KlarityTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.medium)
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.id }
            ) { index, item ->
                AIContextItemCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onBookmarkToggle = { onBookmarkToggle(item) },
                    onLongPress = { draggedItem = item },
                    onDragStart = onDragStart,
                    onDragEnd = onDragEnd,
                    dragOffset = if (draggedItem?.id == item.id) dragOffset.y else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        if (hasMore) {
            TextButton(
                onClick = { /* TODO: Show all items */ },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(KlarityTheme.spacing.medium)
            ) {
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.labelMedium,
                    color = KlarityTheme.extendedColors.sentioPurple
                )
            }
        }
    }
}

/**
 * List of bookmarked items (no limit, sorted by bookmark timestamp)
 */
@Composable
private fun BookmarkedItemList(
    items: List<AIContextItem>,
    onItemClick: (AIContextItem) -> Unit,
    onBookmarkToggle: (AIContextItem) -> Unit,
    onDragStart: (AIContextItem) -> Unit,
    onDragEnd: (AIContextItem, Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        // Empty state
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.medium)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No bookmarked conversations",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap the bookmark icon to save important AI interactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = KlarityTheme.spacing.large)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(KlarityTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.medium)
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.id }
            ) { index, item ->
                AIContextItemCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onBookmarkToggle = { onBookmarkToggle(item) },
                    onLongPress = { },
                    onDragStart = onDragStart,
                    onDragEnd = onDragEnd,
                    dragOffset = 0f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Individual AI context item card
 */
@Composable
private fun AIContextItemCard(
    item: AIContextItem,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onLongPress: () -> Unit,
    onDragStart: (AIContextItem) -> Unit,
    onDragEnd: (AIContextItem, Offset) -> Unit,
    dragOffset: Float,
    modifier: Modifier = Modifier
) {
    var isLongPressing by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Drag state for this card
    var dragState by remember { mutableStateOf(DragState()) }
    
    // Bookmark animation state
    var isBookmarkAnimating by remember { mutableStateOf(false) }
    val bookmarkScale by animateFloatAsState(
        targetValue = if (isBookmarkAnimating) 1.3f else 1.0f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "bookmarkScale"
    )
    
    // Respect reduced motion preferences
    val shouldReduce = com.example.klarity.presentation.utils.shouldReduceMotion()
    
    // Organic lift animations
    val elevation by animateDpAsState(
        targetValue = if (isHovered && !shouldReduce && dragOffset == 0f && !dragState.isDragging) 6.dp else 2.dp,
        animationSpec = KlarityMotion.springBouncy(),
        label = "contextCardElevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (dragState.isDragging) 0.95f else if (isHovered && !shouldReduce && dragOffset == 0f) 1.02f else 1.0f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "contextCardScale"
    )
    
    val rotationX by animateFloatAsState(
        targetValue = if (isHovered && !shouldReduce && dragOffset == 0f && !dragState.isDragging) -2f else 0f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "contextCardRotationX"
    )
    
    val rotationZ by animateFloatAsState(
        targetValue = if (dragState.isDragging) 5f else 0f,
        animationSpec = KlarityMotion.springBouncy(),
        label = "contextCardRotationZ"
    )
    
    KlarityNeumorphicCard(
        modifier = modifier
            .alpha(if (dragOffset != 0f || dragState.isDragging) 0.6f else 1f)
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.rotationX = rotationX
                this.rotationZ = rotationZ
                this.shadowElevation = elevation.toPx()
                if (dragState.isDragging) {
                    this.translationX = dragState.dragOffset.x
                    this.translationY = dragState.dragOffset.y
                }
            }
            .hoverable(interactionSource)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        isLongPressing = true
                        onLongPress()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        dragState = dragState.copy(isDragging = true)
                        onDragStart(item)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragState = dragState.copy(
                            dragOffset = dragState.dragOffset + dragAmount
                        )
                    },
                    onDragEnd = {
                        val finalOffset = dragState.dragOffset
                        onDragEnd(item, finalOffset)
                        dragState = DragState()
                    },
                    onDragCancel = {
                        dragState = DragState()
                    }
                )
            }
            .semantics {
                role = Role.Button
                contentDescription = "${item.type.name}: ${item.title}, relevance ${(item.relevance * 100).roundToInt()}%"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KlarityTheme.spacing.medium)
        ) {
            // Relevance indicator
            RelevanceIndicator(
                relevance = item.relevance,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            )
            
            Spacer(modifier = Modifier.height(KlarityTheme.spacing.small))
            
            // Item header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = item.type.getIcon(),
                        contentDescription = null,
                        tint = item.type.getColor(),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bookmark button
                    IconButton(
                        onClick = {
                            isBookmarkAnimating = true
                            onBookmarkToggle()
                            // Reset animation after delay
                            coroutineScope.launch {
                                delay(300)
                                isBookmarkAnimating = false
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .semantics {
                                contentDescription = if (item.isBookmarked) {
                                    "Remove bookmark"
                                } else {
                                    "Bookmark this conversation"
                                }
                            }
                    ) {
                        Icon(
                            imageVector = if (item.isBookmarked) Icons.Default.Star else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (item.isBookmarked) {
                                KlarityTheme.extendedColors.sentioPurple
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer {
                                    scaleX = bookmarkScale
                                    scaleY = bookmarkScale
                                }
                                .then(
                                    if (item.isBookmarked && !shouldReduce) {
                                        Modifier.pulsingGlow(
                                            color = KlarityTheme.extendedColors.sentioPurple,
                                            intensity = 0.35f,
                                            pulseSpeed = 1500
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Drag to reorder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(KlarityTheme.spacing.extraSmall))
            
            // Preview text
            Text(
                text = item.preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(KlarityTheme.spacing.small))
            
            // Timestamp
            Text(
                text = formatRelativeTime(item.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Relevance indicator with gradient progress bar
 */
@Composable
private fun RelevanceIndicator(
    relevance: Float,
    modifier: Modifier = Modifier
) {
    val gradientColors = if (relevance >= 0.5f) {
        listOf(
            KlarityTheme.extendedColors.electricMint,
            KlarityTheme.extendedColors.luminousTeal
        )
    } else {
        listOf(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.tertiary
        )
    }
    
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(relevance.coerceIn(0f, 1f))
                .background(
                    brush = Brush.horizontalGradient(gradientColors)
                )
        )
    }
}

/**
 * Resize handle for desktop
 */
@Composable
private fun ResizeHandle(
    onDrag: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(4.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
            .background(Color.Transparent)
    )
}

/**
 * Extension function to get icon for item type
 */
private fun AIContextItemType.getIcon(): ImageVector = when (this) {
    AIContextItemType.NOTE -> Icons.Default.Create
    AIContextItemType.TASK -> Icons.Default.CheckCircle
    AIContextItemType.LINK -> Icons.Default.Add
    AIContextItemType.FILE -> Icons.Default.Info
}

/**
 * Extension function to get color for item type
 */
@Composable
private fun AIContextItemType.getColor(): Color = when (this) {
    AIContextItemType.NOTE -> KlarityTheme.extendedColors.sentioPurple
    AIContextItemType.TASK -> KlarityTheme.extendedColors.electricMint
    AIContextItemType.LINK -> KlarityTheme.extendedColors.luminousTeal
    AIContextItemType.FILE -> MaterialTheme.colorScheme.tertiary
}

/**
 * Format timestamp as relative time
 */
private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "${diff / 1000}s ago"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> "${diff / 604_800_000}w ago"
    }
}
