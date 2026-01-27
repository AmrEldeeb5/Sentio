package com.example.klarity.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.theme.KlarityMotion
import com.example.klarity.presentation.theme.KlarityTheme

// ══════════════════════════════════════════════════════════════════════════════
// M3 CLICKABLE CARD
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Clickable Card with hover states and ripple effect.
 * Interactive card component with elevation changes on hover.
 * 
 * Features:
 * - Shape: MaterialTheme.shapes.medium (12dp)
 * - Default elevation: 2.dp, hover: 4.dp
 * - Hover state: subtle background change
 * - Click ripple effect
 * - Min touch target: 48.dp
 * 
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for the card
 * @param enabled Whether the card is clickable
 * @param shape Shape of the card (default: MaterialTheme.shapes.medium = 12dp)
 * @param elevation Default elevation (default: 2.dp)
 * @param content Content to display inside the card
 */
@Composable
fun KlarityClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    elevation: androidx.compose.ui.unit.Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val cardElevation by animateDpAsState(
        targetValue = if (isHovered) 4.dp else elevation,
        animationSpec = KlarityMotion.standardEnter(),
        label = "cardElevation"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isHovered) 
            MaterialTheme.colorScheme.surfaceContainerHigh 
        else 
            MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = KlarityMotion.standardEnter(),
        label = "cardBgColor"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .semantics { 
                role = Role.Button
            },
        enabled = enabled,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = bgColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.38f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation,
            pressedElevation = 4.dp,
            hoveredElevation = 4.dp,
            disabledElevation = 0.dp
        ),
        interactionSource = interactionSource,
        content = content
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 SELECTION CARD
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Selection Card with checkbox and accent bar.
 * Multi-selection card with visual feedback for selected state.
 * 
 * Features:
 * - Shape: MaterialTheme.shapes.medium (12dp)
 * - Selected: primary.copy(alpha = 0.12f) background + 4.dp accent bar
 * - Unselected: surfaceVariant background
 * - Accent bar color: MaterialTheme.colorScheme.primary
 * - Elevation: 2.dp
 * - Padding: 16.dp horizontal, 12.dp vertical
 * 
 * @param selected Whether the card is selected
 * @param onSelectedChange Callback when selection state changes
 * @param modifier Modifier for the card
 * @param showCheckbox Whether to show the leading checkbox (default: true)
 * @param shape Shape of the card (default: MaterialTheme.shapes.medium = 12dp)
 * @param content Content to display inside the card
 */
@Composable
fun KlaritySelectionCard(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showCheckbox: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    content: @Composable RowScope.() -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = KlarityMotion.standardEnter(),
        label = "selectionCardBg"
    )

    Card(
        onClick = { onSelectedChange(!selected) },
        modifier = modifier
            .semantics { 
                role = Role.Checkbox
            },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent bar on left edge when selected (4.dp width)
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small)
            ) {
                // Leading checkbox (optional)
                if (showCheckbox) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = onSelectedChange,
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                // Content
                content()
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// M3 DRAGGABLE CARD
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 Draggable Card with drag handle.
 * Extension of KlarityClickableCard with drag functionality.
 * 
 * Features:
 * - Shape: MaterialTheme.shapes.medium (12dp)
 * - Default elevation: 2.dp, hover: 4.dp, dragging: 8.dp
 * - Optional leading drag handle icon
 * - Padding: KlarityTheme.spacing.medium (16dp)
 * - Min touch target: 48.dp
 * 
 * @param onClick Callback when card is clicked
 * @param modifier Modifier for the card
 * @param enabled Whether the card is clickable
 * @param isDragging Whether the card is currently being dragged
 * @param dragHandleVisible Whether to show the drag handle icon (default: true)
 * @param shape Shape of the card (default: MaterialTheme.shapes.medium = 12dp)
 * @param content Content to display inside the card
 */
@Composable
fun KlarityDraggableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDragging: Boolean = false,
    dragHandleVisible: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val cardElevation by animateDpAsState(
        targetValue = when {
            isDragging -> 8.dp
            isHovered -> 4.dp
            else -> 2.dp
        },
        animationSpec = KlarityMotion.standardEnter(),
        label = "draggableCardElevation"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isDragging -> MaterialTheme.colorScheme.surfaceContainerHighest
            isHovered -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> MaterialTheme.colorScheme.surfaceContainer
        },
        animationSpec = KlarityMotion.standardEnter(),
        label = "draggableCardBg"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .semantics { 
                role = Role.Button
            },
        enabled = enabled,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = bgColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.38f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation,
            pressedElevation = 4.dp,
            hoveredElevation = 4.dp,
            disabledElevation = 0.dp
        ),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KlarityTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KlarityTheme.spacing.small)
        ) {
            // Leading drag handle (Menu icon)
            if (dragHandleVisible) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drag handle",
                    modifier = Modifier.size(20.dp),
                    tint = if (isDragging) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Content
            content()
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// NEUMORPHIC CARD WITH GLOW
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Neumorphic card with glowing accents, inspired by Sentio's design language.
 * Features dual-layer shadows and bottom edge glow effect.
 * 
 * Visual Design:
 * - Dual-layer shadow: dark bottom-right + subtle light top-left
 * - Bottom edge gradient glow with configurable intensity
 * - Background: MaterialTheme.colorScheme.surfaceContainer
 * - Shape: MaterialTheme.shapes.large (16dp rounded corners)
 * 
 * Elevation States:
 * - Resting: 2dp elevation
 * - Hovered (desktop): 4dp elevation + glow intensifies
 * - Pressed: 1dp elevation (appears "pressed in")
 * 
 * @param modifier Modifier for the card
 * @param onClick Optional callback when card is clicked (makes card interactive)
 * @param elevated Whether to use elevated state (4dp elevation)
 * @param glowIntensity Glow effect alpha intensity (0.0 to 1.0, default 0.3)
 * @param glowColor Color of the glow effect (default: MaterialTheme.colorScheme.primary)
 * @param content Content to display inside the card
 */
@Composable
fun KlarityNeumorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevated: Boolean = false,
    glowIntensity: Float = 0.3f,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    // Elevation animation: pressed (1dp) < resting (2dp) < hovered/elevated (4dp)
    val targetElevation = when {
        isPressed -> 1.dp
        isHovered || elevated -> 4.dp
        else -> 2.dp
    }

    val cardElevation by animateDpAsState(
        targetValue = targetElevation,
        animationSpec = KlarityMotion.standardEnter(),
        label = "neumorphicCardElevation"
    )

    // Glow intensity increases on hover
    val currentGlowIntensity by animateFloatAsState(
        targetValue = if (isHovered) (glowIntensity * 1.5f).coerceAtMost(1f) else glowIntensity,
        animationSpec = KlarityMotion.standardEnter(),
        label = "neumorphicGlowIntensity"
    )

    // Remember glow color with intensity
    val animatedGlowColor = remember(glowColor, currentGlowIntensity) {
        glowColor.copy(alpha = currentGlowIntensity)
    }

    Box(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .semantics { role = Role.Button }
                } else {
                    Modifier
                }
            )
    ) {
        // Main card with neumorphic styling
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = cardElevation,
            shadowElevation = cardElevation,
            onClick = onClick ?: {},
            enabled = onClick != null,
            interactionSource = interactionSource
        ) {
            Box {
                // Content column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(KlarityTheme.spacing.medium),
                    content = content
                )

                // Bottom edge glow gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    animatedGlowColor
                                )
                            )
                        )
                )
            }
        }
    }
}
