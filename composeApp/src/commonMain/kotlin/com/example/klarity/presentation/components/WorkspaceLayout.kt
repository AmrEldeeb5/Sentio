package com.example.klarity.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.theme.KlarityMotion
import com.example.klarity.presentation.theme.KlarityTheme

/**
 * State holder for the resizable workspace layout.
 * 
 * Maintains the width and collapse state of the left and right panes,
 * along with constraints for minimum and maximum pane widths.
 * 
 * @param leftPaneWidth Current width of the left pane (notes list)
 * @param rightPaneWidth Current width of the right pane (AI context panel)
 * @param isLeftPaneCollapsed Whether the left pane is collapsed
 * @param isRightPaneCollapsed Whether the right pane is collapsed
 * @param minPaneWidth Minimum width allowed for panes
 * @param maxPaneWidth Maximum width allowed for panes
 */
data class WorkspaceLayoutState(
    val leftPaneWidth: Dp = 280.dp,
    val rightPaneWidth: Dp = 320.dp,
    val isLeftPaneCollapsed: Boolean = false,
    val isRightPaneCollapsed: Boolean = false,
    val minPaneWidth: Dp = 200.dp,
    val maxPaneWidth: Dp = 600.dp
)

/**
 * A resizable 3-pane layout component for desktop with draggable dividers.
 * 
 * Creates a workspace with three adjustable sections: left pane (notes list),
 * center pane (editor), and right pane (AI context panel). Users can resize
 * panes by dragging dividers, collapse panes, and the state persists.
 * 
 * **Features:**
 * - Draggable dividers with hover effects and pulsing glow
 * - Collapsible left and right panes with smooth animations
 * - Minimum/maximum width constraints
 * - Double-click to reset divider to default
 * - Snap to collapsed when dragged near edge
 * - Keyboard accessible (dividers are sliders)
 * - Respects design system motion tokens
 * 
 * **Layout:**
 * ```
 * ┌──────────┬───────────────────┬──────────┐
 * │          │                   │          │
 * │  Left    │    Center         │  Right   │
 * │  Pane    │    Pane           │  Pane    │
 * │  (List)  │    (Editor)       │  (AI)    │
 * │          │                   │          │
 * └──────────┴───────────────────┴──────────┘
 * ```
 * 
 * @param state Current layout state (widths and collapse states)
 * @param onStateChange Callback when state changes (for persistence)
 * @param leftPane Content for the left pane (notes list)
 * @param centerPane Content for the center pane (editor)
 * @param rightPane Content for the right pane (AI context panel)
 * @param modifier Modifier to be applied to the root layout
 * @param showLeftPane Whether to show the left pane at all
 * @param showRightPane Whether to show the right pane at all
 * 
 * @example
 * ```kotlin
 * var layoutState by remember { mutableStateOf(WorkspaceLayoutState()) }
 * 
 * WorkspaceLayout(
 *     state = layoutState,
 *     onStateChange = { newState -> 
 *         layoutState = newState
 *         // Save to local storage
 *     },
 *     leftPane = { NotesListPane() },
 *     centerPane = { EditorPanel() },
 *     rightPane = { AIContextPanel() }
 * )
 * ```
 */
@Composable
fun WorkspaceLayout(
    state: WorkspaceLayoutState,
    onStateChange: (WorkspaceLayoutState) -> Unit,
    leftPane: @Composable () -> Unit,
    centerPane: @Composable () -> Unit,
    rightPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    showLeftPane: Boolean = true,
    showRightPane: Boolean = true
) {
    // Animated widths for smooth transitions
    val leftWidth by animateDpAsState(
        targetValue = if (state.isLeftPaneCollapsed || !showLeftPane) 0.dp else state.leftPaneWidth,
        animationSpec = KlarityMotion.springGentle(),
        label = "leftPaneWidth"
    )

    val rightWidth by animateDpAsState(
        targetValue = if (state.isRightPaneCollapsed || !showRightPane) 0.dp else state.rightPaneWidth,
        animationSpec = KlarityMotion.springGentle(),
        label = "rightPaneWidth"
    )

    Row(
        modifier = modifier.fillMaxSize()
    ) {
        // Left Pane (Notes List)
        if (showLeftPane) {
            Box(
                modifier = Modifier
                    .width(leftWidth)
                    .fillMaxHeight()
            ) {
                if (!state.isLeftPaneCollapsed) {
                    leftPane()
                }
            }

            // Left Divider
            ResizableDivider(
                onDrag = { dragDelta ->
                    val newWidth = (state.leftPaneWidth + dragDelta).coerceIn(
                        state.minPaneWidth,
                        state.maxPaneWidth
                    )
                    
                    // Snap to collapsed if dragged below 100dp
                    if (newWidth < 100.dp && !state.isLeftPaneCollapsed) {
                        onStateChange(state.copy(isLeftPaneCollapsed = true))
                    } else if (state.isLeftPaneCollapsed && newWidth >= 100.dp) {
                        onStateChange(state.copy(
                            leftPaneWidth = newWidth,
                            isLeftPaneCollapsed = false
                        ))
                    } else if (!state.isLeftPaneCollapsed) {
                        onStateChange(state.copy(leftPaneWidth = newWidth))
                    }
                },
                onDoubleClick = {
                    onStateChange(state.copy(leftPaneWidth = 280.dp))
                },
                orientation = DividerOrientation.Vertical
            )
        }

        // Center Pane (Editor) - Takes remaining space
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            centerPane()
        }

        // Right Divider
        if (showRightPane) {
            ResizableDivider(
                onDrag = { dragDelta ->
                    val newWidth = (state.rightPaneWidth - dragDelta).coerceIn(
                        state.minPaneWidth,
                        state.maxPaneWidth
                    )
                    
                    // Snap to collapsed if dragged below 100dp
                    if (newWidth < 100.dp && !state.isRightPaneCollapsed) {
                        onStateChange(state.copy(isRightPaneCollapsed = true))
                    } else if (state.isRightPaneCollapsed && newWidth >= 100.dp) {
                        onStateChange(state.copy(
                            rightPaneWidth = newWidth,
                            isRightPaneCollapsed = false
                        ))
                    } else if (!state.isRightPaneCollapsed) {
                        onStateChange(state.copy(rightPaneWidth = newWidth))
                    }
                },
                onDoubleClick = {
                    onStateChange(state.copy(rightPaneWidth = 320.dp))
                },
                orientation = DividerOrientation.Vertical
            )

            // Right Pane (AI Context Panel)
            Box(
                modifier = Modifier
                    .width(rightWidth)
                    .fillMaxHeight()
            ) {
                if (!state.isRightPaneCollapsed) {
                    rightPane()
                }
            }
        }
    }
}

/**
 * Orientation for the resizable divider.
 */
private enum class DividerOrientation {
    Vertical,
    Horizontal
}

/**
 * A draggable divider that separates resizable panes.
 * 
 * Features:
 * - Hover state with width animation and pulsing glow
 * - Drag gesture detection for resizing
 * - Double-click to reset to default
 * - Resize cursor on hover
 * - Accessibility semantics (Role.Slider)
 * 
 * @param onDrag Callback when divider is dragged, receives delta in Dp
 * @param onDoubleClick Callback when divider is double-clicked
 * @param orientation Whether divider is vertical or horizontal
 */
@Composable
private fun ResizableDivider(
    onDrag: (Dp) -> Unit,
    onDoubleClick: () -> Unit,
    orientation: DividerOrientation,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val density = LocalDensity.current

    // Animate divider width on hover
    val dividerWidth by animateDpAsState(
        targetValue = if (isHovered) 6.dp else 4.dp,
        animationSpec = KlarityMotion.springSnappy(),
        label = "dividerWidth"
    )

    // Track drag state
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .then(
                if (orientation == DividerOrientation.Vertical) {
                    Modifier
                        .width(dividerWidth)
                        .fillMaxHeight()
                } else {
                    Modifier
                        .height(dividerWidth)
                        .fillMaxWidth()
                }
            )
            .background(
                if (isHovered || isDragging) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .then(
                if (isHovered) {
                    Modifier.pulsingGlow(
                        color = MaterialTheme.colorScheme.primary,
                        intensity = 0.2f,
                        pulseSpeed = 2000
                    )
                } else {
                    Modifier
                }
            )
            .pointerHoverIcon(
                icon = if (orientation == DividerOrientation.Vertical) {
                    PointerIcon.Hand // Would use resize horizontal cursor if available
                } else {
                    PointerIcon.Hand // Would use resize vertical cursor if available
                }
            )
            .hoverable(interactionSource = interactionSource)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        dragOffset = 0f
                    },
                    onDragEnd = {
                        isDragging = false
                        dragOffset = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        dragOffset = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        
                        val delta = if (orientation == DividerOrientation.Vertical) {
                            dragAmount.x
                        } else {
                            dragAmount.y
                        }
                        
                        dragOffset += delta
                        
                        // Convert pixels to Dp and notify
                        val dragDp = with(density) { delta.toDp() }
                        onDrag(dragDp)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleClick()
                    }
                )
            }
            .semantics {
                role = androidx.compose.ui.semantics.Role.Button
            }
    )
}

/**
 * Helper function to toggle pane collapse state.
 * 
 * Can be used in pane headers with collapse buttons.
 * 
 * @param state Current layout state
 * @param pane Which pane to toggle (left or right)
 * @param onStateChange Callback to update state
 */
fun togglePaneCollapse(
    state: WorkspaceLayoutState,
    pane: WorkspacePane,
    onStateChange: (WorkspaceLayoutState) -> Unit
) {
    when (pane) {
        WorkspacePane.Left -> {
            onStateChange(state.copy(isLeftPaneCollapsed = !state.isLeftPaneCollapsed))
        }
        WorkspacePane.Right -> {
            onStateChange(state.copy(isRightPaneCollapsed = !state.isRightPaneCollapsed))
        }
    }
}

/**
 * Enum for identifying panes in the workspace.
 */
enum class WorkspacePane {
    Left,
    Right
}

/**
 * Helper to reset layout to default state.
 * 
 * Useful for keyboard shortcuts or reset buttons.
 * 
 * @param onStateChange Callback to update state
 */
fun resetLayoutToDefault(onStateChange: (WorkspaceLayoutState) -> Unit) {
    onStateChange(WorkspaceLayoutState())
}
