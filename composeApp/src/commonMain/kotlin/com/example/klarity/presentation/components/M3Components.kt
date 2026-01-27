package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.SolidColor
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
import com.example.klarity.presentation.theme.KlarityColors
import com.example.klarity.presentation.theme.KlarityMotion
import com.example.klarity.presentation.theme.KlarityTheme

// ══════════════════════════════════════════════════════════════════════════════
// M3 SEARCH BAR
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 styled Search Bar with Klarity theming.
 * Supports expanded/collapsed states, voice input hint, and keyboard navigation.
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
    keyboardShortcutHint: String? = "⌘K"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val focusRequester = remember { FocusRequester() }

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.primary
            isHovered -> MaterialTheme.colorScheme.outlineVariant
            else -> Color.Transparent
        },
        animationSpec = KlarityMotion.standardEnter(),
        label = "borderColor"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isFocused -> MaterialTheme.colorScheme.surfaceContainerHigh
            isHovered -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = KlarityMotion.standardEnter(),
        label = "bgColor"
    )

    Surface(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            ),
        color = bgColor,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Leading icon
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
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
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                            onSearch(query)
                            true
                        } else if (event.key == Key.Escape && event.type == KeyEventType.KeyUp) {
                            onQueryChange("")
                            onExpandedChange(false)
                            true
                        } else {
                            false
                        }
                    },
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
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
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 14.sp
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
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = keyboardShortcutHint,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Clear button when there's text
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(32.dp)
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
                        .size(48.dp)
                        .semantics {
                            contentDescription = "Clear search"
                            role = Role.Button
                        }
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
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
        shape = RoundedCornerShape(20.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
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
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(20.dp)
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
                        Spacer(Modifier.width(4.dp))
                    } else if (icon != null) {
                        Icon(
                            imageVector = icon(option),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = textColor
                        )
                        Spacer(Modifier.width(4.dp))
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
                        .padding(vertical = 8.dp)
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
        modifier = modifier.height(32.dp),
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
        shape = RoundedCornerShape(16.dp)
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
        shape = RoundedCornerShape(14.dp)
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
        shape = RoundedCornerShape(12.dp),
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
        shape = RoundedCornerShape(16.dp)
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
            Spacer(Modifier.height(4.dp))
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
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
            shape = RoundedCornerShape(16.dp),
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
            shape = RoundedCornerShape(16.dp),
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
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (selected) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, borderColor),
            content = content
        )
    } else {
        OutlinedCard(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
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

/**
 * Material 3 Note List Item with Klarity styling.
 * Specialized list item for displaying notes with title, preview, and metadata.
 * 
 * Features:
 * - Left accent bar for selected state (4dp gradient)
 * - Progressive disclosure: hover-activated actions
 * - Multi-selection support with checkboxes
 * - Smooth animations for background color changes
 * - Support for metadata display (tags, timestamps)
 * - Hover and selected states
 * 
 * @param headline Note title
 * @param supporting Note preview text (optional)
 * @param metadata Composable for tags, timestamp, etc. (optional)
 * @param modifier Modifier for the component
 * @param leading Leading content (checkbox or icon)
 * @param trailing Trailing actions (visible on hover)
 * @param selected Whether the note is selected
 * @param multiSelected Whether the note is multi-selected
 * @param showCheckbox Whether to show the checkbox
 * @param showActionsOnHover Whether to show actions only on hover
 * @param onClick Callback when note is clicked
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
}

/**
 * Specialized Note Card for the Home Dashboard - matches the provided image spec.
 */
@Composable
fun KlarityNoteCard(
    headline: String,
    supporting: String?,
    timestamp: String,
    status: String? = null,
    statusColor: Color = MaterialTheme.colorScheme.primary,
    tag: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Edit,
    showAvatar: Boolean = false,
    avatar: String? = null, // Placeholder for avatar
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = KlarityColors.BgCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Icon + Title + Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF2DD4BF),
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            // Supporting text
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Bottom Row: Tags + Status + Avatars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (status != null) {
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = statusColor
                            )
                        }
                    }
                    
                    if (tag != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                if (showAvatar) {
                    // Avatar placeholder
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
