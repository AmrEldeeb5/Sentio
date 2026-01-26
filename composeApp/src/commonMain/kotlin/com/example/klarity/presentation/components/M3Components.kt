package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.klarity.presentation.theme.KlarityShapes
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityMotion
import com.example.klarity.presentation.theme.KlarityTheme
import com.example.klarity.presentation.theme.KlarityColors
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.zIndex
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.onFocusChanged

// ══════════════════════════════════════════════════════════════════════════════
// AI SUGGESTION DATA STRUCTURES
// ══════════════════════════════════════════════════════════════════════════════

/**
 * AI suggestion data class for command palette suggestions.
 * 
 * @param text Suggestion text content
 * @param category Category of the suggestion
 * @param confidence AI confidence score (0.0 to 1.0)
 * @param action Action to execute when suggestion is selected
 */
data class AISuggestion(
    val text: String,
    val category: AISuggestionCategory,
    val confidence: Float, // 0.0 to 1.0
    val action: () -> Unit
)

/**
 * Categories for AI suggestions with associated icons.
 */
enum class AISuggestionCategory {
    NOTE, TASK, LINK, COMMAND
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 SEARCH BAR
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 styled Search Bar with Klarity theming and AI suggestions.
 * Supports expanded/collapsed states, voice input hint, AI-powered suggestions, and keyboard navigation.
 * 
 * @param query Current search query text
 * @param onQueryChange Callback when query text changes
 * @param onSearch Callback when search is submitted
 * @param modifier Modifier for the component
 * @param enabled Whether the search bar is enabled
 * @param placeholder Placeholder text when empty
 * @param leadingIcon Leading icon (default: Search icon)
 * @param trailingIcon Optional trailing icon
 * @param onTrailingIconClick Callback for trailing icon click
 * @param expanded Whether search bar is in expanded state
 * @param onExpandedChange Callback when expanded state changes
 * @param keyboardShortcutHint Optional keyboard shortcut hint (e.g., "⌘K")
 * @param width Optional custom width in Dp. If null, uses fillMaxWidth()
 * @param height Custom height in Dp (default: 48.dp for M3 touch target)
 * @param shape Custom shape (default: MaterialTheme.shapes.small)
 * @param animationsEnabled Enable/disable animations for performance (default: true)
 * @param aiSuggestions List of AI-powered suggestions to display
 * @param onSuggestionClick Callback when an AI suggestion is clicked
 * @param showAISuggestions Whether to show AI suggestions dropdown
 */
@Composable
fun KlaritySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "Search notes, tasks, or type / for commands...",
    leadingIcon: ImageVector = Icons.Default.Search,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    keyboardShortcutHint: String? = "⌘K",
    width: androidx.compose.ui.unit.Dp? = null,
    height: androidx.compose.ui.unit.Dp = 48.dp,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.small,
    animationsEnabled: Boolean = true,
    aiSuggestions: List<AISuggestion> = emptyList(),
    onSuggestionClick: (AISuggestion) -> Unit = {},
    showAISuggestions: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val focusRequester = remember { FocusRequester() }
    
    // Keyboard navigation state for AI suggestions
    val selectedSuggestionIndex = remember { mutableIntStateOf(-1) }
    val visibleSuggestions = aiSuggestions.take(5) // Max 5 suggestions visible
    val showSuggestions = showAISuggestions && visibleSuggestions.isNotEmpty() && (isFocused || expanded)
    
    // Reset selection when suggestions change
    LaunchedEffect(aiSuggestions) {
        selectedSuggestionIndex.intValue = -1
    }

    // Apply animation spec based on animationsEnabled parameter
    val animationSpec = if (animationsEnabled) KlarityMotion.standardEnter() else tween<Color>(0)

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.primary
            isHovered -> MaterialTheme.colorScheme.outlineVariant
            else -> Color.Transparent
        },
        animationSpec = animationSpec,
        label = "borderColor"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.surfaceContainerHigh
            isHovered -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = animationSpec,
        label = "bgColor"
    )

    // Apply custom width modifier if specified
    val widthModifier = if (width != null) {
        Modifier.width(width)
    } else {
        Modifier.fillMaxWidth()
    }
    
    // Main container with search bar and suggestions dropdown
    Column(
        modifier = modifier.then(widthModifier)
    ) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp) // M3 minimum touch target
            .height(height)
            .clip(shape)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .semantics {
                contentDescription = "Search bar: $placeholder"
                role = Role.Button
            },
        color = bgColor,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = KlarityTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small)
        ) {
            // Leading icon
            Icon(
                imageVector = leadingIcon,
                contentDescription = "Search icon",
                modifier = Modifier
                    .size(20.dp)
                    .semantics { contentDescription = "Search" },
                tint = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Search input
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyUp) {
                            when (event.key) {
                                Key.Enter -> {
                                    if (selectedSuggestionIndex.intValue >= 0 && selectedSuggestionIndex.intValue < visibleSuggestions.size) {
                                        // Execute selected suggestion
                                        visibleSuggestions[selectedSuggestionIndex.intValue].action()
                                        onSuggestionClick(visibleSuggestions[selectedSuggestionIndex.intValue])
                                        selectedSuggestionIndex.intValue = -1
                                    } else {
                                        // Normal search
                                        onSearch(query)
                                    }
                                    true
                                }
                                Key.Escape -> {
                                    if (selectedSuggestionIndex.intValue >= 0) {
                                        // Clear selection first
                                        selectedSuggestionIndex.intValue = -1
                                    } else if (showSuggestions) {
                                        // Hide suggestions
                                        onExpandedChange(false)
                                    } else {
                                        // Clear query
                                        onQueryChange("")
                                        onExpandedChange(false)
                                    }
                                    true
                                }
                                Key.DirectionDown -> {
                                    if (showSuggestions) {
                                        selectedSuggestionIndex.intValue = 
                                            (selectedSuggestionIndex.intValue + 1).coerceAtMost(visibleSuggestions.size - 1)
                                        true
                                    } else false
                                }
                                Key.DirectionUp -> {
                                    if (showSuggestions && selectedSuggestionIndex.intValue > 0) {
                                        selectedSuggestionIndex.intValue -= 1
                                        true
                                    } else false
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    }
                    .semantics {
                        contentDescription = "Search input field: $placeholder"
                    },
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                interactionSource = interactionSource,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Keyboard shortcut hint
            if (keyboardShortcutHint != null && query.isEmpty() && !isFocused) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.semantics { 
                        contentDescription = "Keyboard shortcut: $keyboardShortcutHint"
                    }
                ) {
                    Text(
                        text = keyboardShortcutHint,
                        modifier = Modifier.padding(horizontal = KlarityTheme.spacing.small, vertical = KlarityTheme.spacing.extraSmall),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Clear button when there's text
            AnimatedVisibility(
                visible = query.isNotEmpty() && animationsEnabled,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .size(48.dp) // M3 minimum touch target
                        .semantics {
                            contentDescription = "Clear search text"
                            role = Role.Button
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Non-animated clear button when animations are disabled
            if (query.isNotEmpty() && !animationsEnabled) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .size(48.dp) // M3 minimum touch target
                        .semantics {
                            contentDescription = "Clear search text"
                            role = Role.Button
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Trailing icon (clear, filter, etc.) - Material 3 48dp touch target
            if (trailingIcon != null && onTrailingIconClick != null) {
                IconButton(
                    onClick = onTrailingIconClick,
                    modifier = Modifier
                        .size(48.dp) // M3 minimum touch target
                        .semantics {
                            contentDescription = "Additional action"
                            role = Role.Button
                        }
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = "Action icon",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 FILLED TONAL BUTTON
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Filled Tonal Button with Klarity styling.
 */
@Composable
fun KlarityTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.38f),
            disabledContentColor = contentColor.copy(alpha = 0.38f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(KlarityTheme.spacing.small))
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 SEGMENTED BUTTON
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Segmented Button Row
 */
@Composable
fun <T> KlaritySegmentedButtonRow(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (T) -> String,
    icon: (@Composable (T) -> ImageVector)? = null
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(MaterialTheme.shapes.large)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.large
            )
            .background(MaterialTheme.colorScheme.surface),
        horizontalArrangement = Arrangement.Center
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                animationSpec = KlarityMotion.standardEnter(),
                label = "segmentBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = KlarityMotion.standardEnter(),
                label = "segmentText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(bgColor)
                    .clickable { onOptionSelected(option) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = textColor
                        )
                        Spacer(Modifier.width(KlarityTheme.spacing.extraSmall))
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon(option),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = textColor
                        )
                        Spacer(Modifier.width(KlarityTheme.spacing.extraSmall))
                    }
                    Text(
                        text = label(option),
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            // Divider between segments (except last)
            if (index < options.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                    .fillMaxHeight()
                    .padding(vertical = KlarityTheme.spacing.small)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 CHIP COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Filter Chip with Klarity styling
 */
@Composable
fun KlarityFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 13.sp
            )
        },
        modifier = modifier.height(KlarityTheme.spacing.extraLarge),
        enabled = enabled,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
            enabled = enabled,
            selected = selected
        ),
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Material 3 Input Chip with Klarity styling (for tags, etc.)
 */
@Composable
fun KlarityInputChip(
    label: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    InputChip(
        selected = false,
        onClick = { },
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                color = color
            )
        },
        modifier = modifier.height(28.dp),
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = color
                )
            }
        } else null,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onDismiss() },
                tint = color.copy(alpha = 0.7f)
            )
        },
        colors = InputChipDefaults.inputChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        ),
        border = InputChipDefaults.inputChipBorder(
            borderColor = color.copy(alpha = 0.3f),
            borderWidth = 1.dp,
            enabled = true,
            selected = false
        ),
        shape = MaterialTheme.shapes.large
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 FAB VARIANTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Small FAB with Klarity styling
 */
@Composable
fun KlaritySmallFab(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
                    shape = MaterialTheme.shapes.medium,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 4.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Material 3 Extended FAB with Klarity styling
 */
@Composable
fun KlarityExtendedFab(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        expanded = expanded,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        text = {
            Text(
                text = text,
                fontWeight = FontWeight.Medium
            )
        },
        containerColor = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 BADGE
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Badge with count
 */
@Composable
fun KlarityBadge(
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = MaterialTheme.colorScheme.onError
) {
    if (count > 0) {
        Badge(
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 PROGRESS INDICATORS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Linear Progress with label
 */
@Composable
fun KlarityLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    showPercentage: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest
) {
    Column(modifier = modifier) {
        if (label != null || showPercentage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (showPercentage) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(KlarityTheme.spacing.extraSmall))
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(MaterialTheme.shapes.extraSmall),
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 CARD VARIANTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Elevated Card with Klarity styling
 */
@Composable
fun KlarityElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
                hoveredElevation = 4.dp
            ),
            content = content
        )
    } else {
        ElevatedCard(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp
            ),
            content = content
        )
    }
}

/**
 * Material 3 Outlined Card with Klarity styling
 */
@Composable
fun KlarityOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = KlarityMotion.standardEnter(),
        label = "cardBorder"
    )

    if (onClick != null) {
        OutlinedCard(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (selected) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, borderColor),
            content = content
        )
    } else {
        OutlinedCard(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (selected) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, borderColor),
            content = content
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 LIST ITEM
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 List Item with Klarity styling
 */
@Composable
fun KlarityListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent,
        animationSpec = KlarityMotion.standardEnter(),
        label = "listItemBg"
    )

    ListItem(
        headlineContent = headlineContent,
        modifier = modifier
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .background(bgColor),
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
            overlineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 BUTTON COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Primary Button - Main action button with primary accent color
 * 
 * Features:
 * - 40.dp height, 48.dp minimum touch target
 * - Hover state support
 * - Disabled state with 38% alpha
 * - Optional leading icon
 * - labelLarge typography (14sp, Medium weight)
 */
@Composable
fun KlarityPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Button(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.small,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = if (isHovered) 2.dp else 0.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Material 3 Secondary Button - Secondary action button with secondary accent color
 * 
 * Features:
 * - 40.dp height, 48.dp minimum touch target
 * - Hover state support
 * - Disabled state with 38% alpha
 * - Optional leading icon
 * - labelLarge typography (14sp, Medium weight)
 */
@Composable
fun KlaritySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Button(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.small,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = if (isHovered) 2.dp else 0.dp
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Material 3 Text Button - Text-only button without background container
 * 
 * Features:
 * - 40.dp height, 48.dp minimum touch target
 * - No container color (transparent background)
 * - Primary text color
 * - Hover state support
 * - Reduced horizontal padding (16.dp vs 20.dp)
 */
@Composable
fun KlarityTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Material 3 Icon Button - Icon-only button with circular background
 * 
 * Features:
 * - 40.dp size (48.dp minimum touch target)
 * - 24.dp icon size
 * - Circular shape
 * - Surface variant background
 * - Hover elevation
 * - Accessibility: content description support
 */
@Composable
fun KlarityIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        enabled = enabled,
        shape = androidx.compose.foundation.shape.CircleShape,
        color = if (enabled) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
        },
        contentColor = if (enabled) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        },
        shadowElevation = if (isHovered) 2.dp else 0.dp,
        interactionSource = interactionSource
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    this.contentDescription = contentDescription ?: ""
                    role = Role.Button
                }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 ENHANCED LIST ITEM (FOR NOTES)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Enhanced Material 3 List Item optimized for note lists with Klarity styling.
 * 
 * Features:
 * - 4dp accent bar when selected (left edge)
 * - Multi-select checkbox support (progressive disclosure)
 * - Hover state shows quick actions (progressive disclosure)
 * - Metadata/tags section at bottom
 * - 2-line note preview support
 * - Selected background: primary.copy(alpha = 0.12f)
 * - Padding: 16dp horizontal, 12dp vertical
 * - Typography: bodyLarge (headline), bodyMedium (supporting), labelSmall (metadata)
 * 
 * @param headline Note title
 * @param supporting Note preview (max 2 lines recommended)
 * @param metadata Bottom section for tags, timestamps, etc.
 * @param leading Leading content (checkbox, icon, etc.)
 * @param trailing Trailing quick actions (shown on hover/selection)
 * @param selected Whether the item is selected
 * @param multiSelected Whether the item is in multi-select mode
 * @param showCheckbox Whether to show the multi-select checkbox
 * @param showActionsOnHover Whether to show trailing actions only on hover/selection
 * @param onClick Click handler for the item
 * @param modifier Modifier for the component
 */
@Composable
fun KlarityNoteListItem(
    headline: String,
    supporting: String?,
    metadata: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    selected: Boolean = false,
    multiSelected: Boolean = false,
    showCheckbox: Boolean = false,
    showActionsOnHover: Boolean = true,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val bgColor by animateColorAsState(
        targetValue = when {
            selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            multiSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        animationSpec = KlarityMotion.standardEnter(),
        label = "noteListItemBg"
    )
    
    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar for selected state (4dp width)
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
                verticalAlignment = Alignment.Top
            ) {
                // Leading content (checkbox or icon) with progressive disclosure
                AnimatedVisibility(
                    visible = showCheckbox || (leading != null && !showActionsOnHover) || (leading != null && isHovered),
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                        leading?.invoke()
                    }
                }
                
                // Main content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Headline
                    Text(
                        text = headline,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Supporting text (preview)
                    if (supporting != null) {
                        Text(
                            text = supporting,
                            color = if (selected)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Metadata (tags, timestamp, etc.)
                    if (metadata != null) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            metadata()
                        }
                    }
                }
                
                // Trailing actions with progressive disclosure
                AnimatedVisibility(
                    visible = !showActionsOnHover || isHovered || selected,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                        trailing?.invoke()
                    }
                }
            }
        }
    }
    
    // AI Suggestions Dropdown
    AnimatedVisibility(
        visible = showSuggestions && animationsEnabled,
        enter = fadeIn(animationSpec = KlarityMotion.standardEnter()) + 
                expandVertically(animationSpec = KlarityMotion.standardEnter()),
        exit = fadeOut(animationSpec = KlarityMotion.standardExit()) + 
               shrinkVertically(animationSpec = KlarityMotion.standardExit()),
        modifier = Modifier.fillMaxWidth()
    ) {
        AISuggestionsDropdown(
            suggestions = visibleSuggestions,
            selectedIndex = selectedSuggestionIndex.intValue,
            onSuggestionClick = { suggestion, index ->
                suggestion.action()
                onSuggestionClick(suggestion)
                selectedSuggestionIndex.intValue = -1
            },
            animationsEnabled = animationsEnabled
        )
    }
    
    // Non-animated suggestions dropdown
    if (showSuggestions && !animationsEnabled) {
        AISuggestionsDropdown(
            suggestions = visibleSuggestions,
            selectedIndex = selectedSuggestionIndex.intValue,
            onSuggestionClick = { suggestion, index ->
                suggestion.action()
                onSuggestionClick(suggestion)
                selectedSuggestionIndex.intValue = -1
            },
            animationsEnabled = false
        )
    }
    } // End Column
}

/**
 * AI Suggestions Dropdown component.
 * Displays AI-powered suggestions below the search bar with animations and keyboard support.
 * 
 * @param suggestions List of AI suggestions to display (max 5)
 * @param selectedIndex Currently selected suggestion index (-1 for none)
 * @param onSuggestionClick Callback when a suggestion is clicked
 * @param animationsEnabled Whether animations are enabled
 */
@Composable
private fun AISuggestionsDropdown(
    suggestions: List<AISuggestion>,
    selectedIndex: Int,
    onSuggestionClick: (AISuggestion, Int) -> Unit,
    animationsEnabled: Boolean
) {
    KlarityNeumorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = KlarityTheme.spacing.extraSmall)
            .semantics {
                contentDescription = "${suggestions.size} AI suggestions available"
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            state = rememberLazyListState()
        ) {
            itemsIndexed(suggestions) { index, suggestion ->
                // Staggered animation delay: 50ms per item
                val animationDelay = if (animationsEnabled) index * 50 else 0
                
                AISuggestionItem(
                    suggestion = suggestion,
                    isSelected = index == selectedIndex,
                    onClick = { onSuggestionClick(suggestion, index) },
                    animationDelay = animationDelay,
                    animationsEnabled = animationsEnabled
                )
                
                // Divider between items (except last)
                if (index < suggestions.lastIndex) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

/**
 * Individual AI Suggestion item with icon, text, confidence indicator, and category badge.
 * 
 * @param suggestion The AI suggestion to display
 * @param isSelected Whether this suggestion is currently selected via keyboard
 * @param onClick Callback when the suggestion is clicked
 * @param animationDelay Staggered animation delay in milliseconds
 * @param animationsEnabled Whether animations are enabled
 */
@Composable
private fun AISuggestionItem(
    suggestion: AISuggestion,
    isSelected: Boolean,
    onClick: () -> Unit,
    animationDelay: Int,
    animationsEnabled: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Respect reduced motion preferences
    val shouldReduce = com.example.klarity.presentation.utils.shouldReduceMotion()
    
    // High confidence items get pulsing glow
    val isHighConfidence = suggestion.confidence > 0.8f
    
    // Organic lift animations
    val elevation by animateDpAsState(
        targetValue = if (isHovered && !shouldReduce && animationsEnabled) 4.dp else 0.dp,
        animationSpec = if (animationsEnabled) KlarityMotion.springBouncy() else tween(0),
        label = "suggestionElevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered && !shouldReduce && animationsEnabled) 1.02f else 1.0f,
        animationSpec = if (animationsEnabled) KlarityMotion.springBouncy() else tween(0),
        label = "suggestionScale"
    )
    
    val rotationX by animateFloatAsState(
        targetValue = if (isHovered && !shouldReduce && animationsEnabled) -1.5f else 0f,
        animationSpec = if (animationsEnabled) KlarityMotion.springBouncy() else tween(0),
        label = "suggestionRotationX"
    )
    
    // Background color animation
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            isHovered -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> Color.Transparent
        },
        animationSpec = if (animationsEnabled) KlarityMotion.springBouncy() else tween(0),
        label = "suggestionBg"
    )
    
    // Get icon for category
    val categoryIcon = when (suggestion.category) {
        AISuggestionCategory.NOTE -> Icons.Default.Create
        AISuggestionCategory.TASK -> Icons.Default.CheckCircle
        AISuggestionCategory.LINK -> Icons.Default.Add // Using Add as placeholder for Link
        AISuggestionCategory.COMMAND -> Icons.Default.Star
    }
    
    // Get category label
    val categoryLabel = when (suggestion.category) {
        AISuggestionCategory.NOTE -> "Note"
        AISuggestionCategory.TASK -> "Task"
        AISuggestionCategory.LINK -> "Link"
        AISuggestionCategory.COMMAND -> "Command"
    }
    
    Surface(
        onClick = onClick,
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
                contentDescription = "${suggestion.text}, ${categoryLabel}, confidence ${(suggestion.confidence * 100).toInt()}%"
            }
            .then(
                if (isHighConfidence && animationsEnabled) {
                    Modifier.pulsingGlow(
                        color = KlarityColors.LuminousTeal,
                        intensity = 0.3f,
                        pulseSpeed = 2000
                    )
                } else {
                    Modifier
                }
            ),
        color = bgColor,
        shape = MaterialTheme.shapes.small,
        tonalElevation = if (shouldReduce) elevation else 0.dp,
        shadowElevation = if (shouldReduce) elevation else 0.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KlarityTheme.spacing.small),
            horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Icon(
                imageVector = categoryIcon,
                contentDescription = categoryLabel,
                modifier = Modifier.size(20.dp),
                tint = when (suggestion.category) {
                    AISuggestionCategory.NOTE -> MaterialTheme.colorScheme.primary
                    AISuggestionCategory.TASK -> MaterialTheme.colorScheme.tertiary
                    AISuggestionCategory.LINK -> KlarityColors.AccentAI
                    AISuggestionCategory.COMMAND -> MaterialTheme.colorScheme.secondary
                }
            )
            
            // Suggestion text
            Text(
                text = suggestion.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Confidence indicator (if high confidence, show pulsing badge)
            if (isHighConfidence) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = KlarityColors.AccentAI.copy(alpha = 0.15f),
                    modifier = Modifier.padding(start = KlarityTheme.spacing.extraSmall)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = KlarityTheme.spacing.small,
                            vertical = KlarityTheme.spacing.extraSmall
                        ),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "High confidence",
                            modifier = Modifier.size(12.dp),
                            tint = KlarityColors.AccentAI
                        )
                    }
                }
            }
            
            // Category badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = KlarityTheme.spacing.extraSmall)
            ) {
                Text(
                    text = categoryLabel,
                    modifier = Modifier.padding(
                        horizontal = KlarityTheme.spacing.small,
                        vertical = KlarityTheme.spacing.extraSmall
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 CHECKBOX COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Checkbox with Klarity styling.
 * Unified checkbox component to standardize all checkbox implementations.
 * 
 * Features:
 * - Base: Material3 Checkbox
 * - Size: 24.dp (checkbox itself)
 * - Touch target: 48.dp minimum
 * - States: checked, unchecked, indeterminate, disabled
 * - Ripple effect on interaction
 * - Focus and hover states for keyboard navigation
 * - Accessible with proper semantic roles
 * 
 * @param checked Whether the checkbox is checked
 * @param onCheckedChange Callback when checkbox state changes
 * @param modifier Modifier for the component
 * @param enabled Whether the checkbox is enabled
 * @param indeterminate Whether the checkbox is in indeterminate state
 */
@Composable
fun KlarityCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    indeterminate: Boolean = false
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
            .size(48.dp) // M3 minimum touch target
            .semantics {
                role = Role.Checkbox
                contentDescription = when {
                    indeterminate -> "Indeterminate checkbox"
                    checked -> "Checked checkbox"
                    else -> "Unchecked checkbox"
                }
            },
        enabled = enabled,
        colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
            uncheckedColor = MaterialTheme.colorScheme.outline,
            checkmarkColor = MaterialTheme.colorScheme.onPrimary,
            disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledUncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
            disabledIndeterminateColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
        ),
        interactionSource = remember { MutableInteractionSource() }
    )
}

/**
 * Material 3 Checkbox with text label.
 * Entire row is clickable, improving usability.
 * 
 * Features:
 * - Combines KlarityCheckbox + Text in a Row
 * - Entire row is clickable
 * - Min touch target: 48.dp height
 * - Spacing: 12.dp between checkbox and label
 * - Typography: MaterialTheme.typography.bodyLarge
 * - Hover state for better visual feedback
 * 
 * @param checked Whether the checkbox is checked
 * @param onCheckedChange Callback when checkbox state changes
 * @param label Text label for the checkbox
 * @param modifier Modifier for the component
 * @param enabled Whether the checkbox is enabled
 */
@Composable
fun KlarityCheckboxWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgColor by animateColorAsState(
        targetValue = if (isHovered && enabled) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        },
        animationSpec = KlarityMotion.standardEnter(),
        label = "checkboxRowBg"
    )

    Row(
        modifier = modifier
            .heightIn(min = 48.dp) // M3 minimum touch target
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .clickable(
                enabled = enabled,
                onClick = { onCheckedChange(!checked) },
                interactionSource = interactionSource,
                indication = null // Custom bg animation instead of ripple
            )
            .padding(horizontal = KlarityTheme.spacing.small)
            .semantics {
                role = Role.Checkbox
                contentDescription = "$label checkbox"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KlarityCheckbox(
            checked = checked,
            onCheckedChange = null, // Handled by row click
            enabled = enabled,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 SWITCH COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Switch with Klarity styling.
 * Toggle switch for on/off states.
 * 
 * Features:
 * - Base: Material3 Switch
 * - Size: 52x32dp (Material3 default)
 * - Touch target: 48.dp minimum
 * - Animated transition between states
 * - Colors: primary (checked thumb), primaryContainer (checked track), outline (unchecked)
 * - Focus and hover states for accessibility
 * 
 * @param checked Whether the switch is on
 * @param onCheckedChange Callback when switch state changes
 * @param modifier Modifier for the component
 * @param enabled Whether the switch is enabled
 */
@Composable
fun KlaritySwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
            .heightIn(min = 48.dp) // M3 minimum touch target
            .semantics {
                role = Role.Switch
                contentDescription = if (checked) "Switch on" else "Switch off"
            },
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = MaterialTheme.colorScheme.primary,
            checkedIconColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            uncheckedIconColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledCheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledCheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledCheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledCheckedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledUncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
            disabledUncheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledUncheckedIconColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
        ),
        interactionSource = remember { MutableInteractionSource() }
    )
}

/**
 * Material 3 Switch with text label.
 * Similar to CheckboxWithLabel but with Switch.
 * 
 * Features:
 * - Row with SpaceBetween arrangement (label left, switch right)
 * - Min touch target: 48.dp height
 * - Entire row is clickable
 * - Hover state for better visual feedback
 * 
 * @param checked Whether the switch is on
 * @param onCheckedChange Callback when switch state changes
 * @param label Text label for the switch
 * @param modifier Modifier for the component
 * @param enabled Whether the switch is enabled
 */
@Composable
fun KlaritySwitchWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bgColor by animateColorAsState(
        targetValue = if (isHovered && enabled) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            Color.Transparent
        },
        animationSpec = KlarityMotion.standardEnter(),
        label = "switchRowBg"
    )

    Row(
        modifier = modifier
            .heightIn(min = 48.dp) // M3 minimum touch target
            .clip(MaterialTheme.shapes.small)
            .background(bgColor)
            .clickable(
                enabled = enabled,
                onClick = { onCheckedChange(!checked) },
                interactionSource = interactionSource,
                indication = null // Custom bg animation instead of ripple
            )
            .padding(horizontal = KlarityTheme.spacing.medium)
            .semantics {
                role = Role.Switch
                contentDescription = "$label switch"
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = Modifier.weight(1f)
        )
        KlaritySwitch(
            checked = checked,
            onCheckedChange = null, // Handled by row click
            enabled = enabled
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 DROPDOWN MENU COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Dropdown Menu with Klarity styling.
 * Wrapper for Material3 DropdownMenu with standardized styling.
 * 
 * Features:
 * - Base: Material3 DropdownMenu
 * - Background: MaterialTheme.colorScheme.surface
 * - Elevation: 8.dp
 * - Shape: MaterialTheme.shapes.medium
 * - Padding: 8.dp vertical
 * - Animated entry/exit
 * 
 * @param expanded Whether the dropdown is expanded
 * @param onDismissRequest Callback when the dropdown is dismissed
 * @param modifier Modifier for the component
 * @param content Content of the dropdown menu (typically KlarityDropdownMenuItem)
 */
@Composable
fun KlarityDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        content = content
    )
}

/**
 * Material 3 Dropdown Menu Item with Klarity styling.
 * Menu item for use within KlarityDropdownMenu.
 * 
 * Features:
 * - Base: Material3 DropdownMenuItem
 * - Height: 48.dp minimum
 * - Padding: 16.dp horizontal, 12.dp vertical
 * - Typography: MaterialTheme.typography.bodyLarge
 * - Hover state: surfaceVariant background
 * - Selected state: primaryContainer background
 * - Optional leading and trailing icons
 * 
 * @param text Text label for the menu item
 * @param onClick Callback when menu item is clicked
 * @param modifier Modifier for the component
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param enabled Whether the menu item is enabled
 * @param selected Whether the menu item is selected
 */
@Composable
fun KlarityDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    selected: Boolean = false
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics {
                role = Role.Button
                contentDescription = "$text menu item"
            },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else null,
        trailingIcon = if (trailingIcon != null) {
            {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        enabled = enabled,
        colors = MenuDefaults.itemColors(
            textColor = MaterialTheme.colorScheme.onSurface,
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        interactionSource = remember { MutableInteractionSource() }
    )
}
