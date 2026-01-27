package com.example.klarity.presentation.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.navigation.NavDestination
import com.example.klarity.presentation.theme.KlarityMotion

/**
 * Workspace layout modes
 */
enum class WorkspaceLayoutMode {
    SINGLE_PANE,    // Full focus on one panel
    DUAL_PANE,      // Two panels side by side
    TRI_PANE,       // Three panels (list, editor, context)
    FOCUS           // Minimal UI, just editor
}

/**
 * Pane types that can be displayed in the workspace
 */
enum class PaneType {
    NOTES_LIST,
    EDITOR,
    GRAPH,
    TASKS,
    FILE_EXPLORER
}

/**
 * Configuration for a workspace layout.
 * Marked as @Immutable for Compose recomposition optimization.
 */
@Immutable
data class WorkspaceConfig(
    val mode: WorkspaceLayoutMode,
    val leftPane: PaneType? = null,
    val centerPane: PaneType? = null,
    val rightPane: PaneType? = null,
    val leftPaneWidth: Dp = 280.dp,
    val rightPaneWidth: Dp = 320.dp
)

/**
 * Predefined workspace configurations
 */
object WorkspacePresets {
    val notesDefault = WorkspaceConfig(
        mode = WorkspaceLayoutMode.TRI_PANE,
        leftPane = PaneType.FILE_EXPLORER,
        centerPane = PaneType.EDITOR,
        rightPane = PaneType.NOTES_LIST,
        leftPaneWidth = 260.dp,
        rightPaneWidth = 320.dp
    )
    
    val notesDual = WorkspaceConfig(
        mode = WorkspaceLayoutMode.DUAL_PANE,
        centerPane = PaneType.EDITOR,
        rightPane = PaneType.NOTES_LIST,
        rightPaneWidth = 320.dp
    )
    
    val notesFocus = WorkspaceConfig(
        mode = WorkspaceLayoutMode.SINGLE_PANE,
        centerPane = PaneType.EDITOR
    )
    
    val tasksWithNotes = WorkspaceConfig(
        mode = WorkspaceLayoutMode.DUAL_PANE,
        centerPane = PaneType.EDITOR,
        rightPane = PaneType.TASKS,
        rightPaneWidth = 320.dp
    )
    
    val tasksFull = WorkspaceConfig(
        mode = WorkspaceLayoutMode.SINGLE_PANE,
        centerPane = PaneType.TASKS
    )
}

/**
 * Adaptive workspace container that manages pane layouts
 */
@Composable
fun AdaptiveWorkspace(
    config: WorkspaceConfig,
    onConfigChange: (WorkspaceConfig) -> Unit,
    leftPaneContent: @Composable (Modifier) -> Unit,
    centerPaneContent: @Composable (Modifier) -> Unit,
    rightPaneContent: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier
) {
    val leftWidth by animateDpAsState(
        targetValue = if (config.leftPane != null) config.leftPaneWidth else 0.dp,
        animationSpec = KlarityMotion.standardEnter(),
        label = "leftWidth"
    )
    
    val rightWidth by animateDpAsState(
        targetValue = if (config.rightPane != null) config.rightPaneWidth else 0.dp,
        animationSpec = KlarityMotion.standardEnter(),
        label = "rightWidth"
    )

    Row(modifier = modifier.fillMaxSize()) {
        // Left Pane
        AnimatedVisibility(
            visible = config.leftPane != null,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        ) {
            WorkspacePane(
                paneType = config.leftPane,
                position = PanePosition.LEFT,
                onDetach = { /* TODO: Detach window */ },
                onClose = { 
                    onConfigChange(config.copy(leftPane = null))
                },
                modifier = Modifier.width(leftWidth)
            ) {
                leftPaneContent(Modifier.fillMaxSize())
            }
        }
        
        // Center Pane (always visible, takes remaining space)
        WorkspacePane(
            paneType = config.centerPane,
            position = PanePosition.CENTER,
            onDetach = { /* TODO: Detach window */ },
            onClose = null, // Center pane cannot be closed
            modifier = Modifier.weight(1f)
        ) {
            centerPaneContent(Modifier.fillMaxSize())
        }
        
        // Right Pane
        AnimatedVisibility(
            visible = config.rightPane != null,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        ) {
            WorkspacePane(
                paneType = config.rightPane,
                position = PanePosition.RIGHT,
                onDetach = { /* TODO: Detach window */ },
                onClose = { 
                    onConfigChange(config.copy(rightPane = null))
                },
                modifier = Modifier.width(rightWidth)
            ) {
                rightPaneContent(Modifier.fillMaxSize())
            }
        }
    }
}

enum class PanePosition { LEFT, CENTER, RIGHT }

/**
 * Individual workspace pane with header and controls
 */
@Composable
private fun WorkspacePane(
    paneType: PaneType?,
    position: PanePosition,
    onDetach: () -> Unit,
    onClose: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = when (position) {
            PanePosition.LEFT -> MaterialTheme.colorScheme.surfaceVariant
            PanePosition.CENTER -> MaterialTheme.colorScheme.background
            PanePosition.RIGHT -> MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        content()
    }
}

/**
 * Layout mode selector toolbar
 */
@Composable
fun LayoutModeSelector(
    currentMode: WorkspaceLayoutMode,
    onModeSelected: (WorkspaceLayoutMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        LayoutModeButton(
            icon = "▢",
            label = "Single",
            isSelected = currentMode == WorkspaceLayoutMode.SINGLE_PANE,
            onClick = { onModeSelected(WorkspaceLayoutMode.SINGLE_PANE) }
        )
        LayoutModeButton(
            icon = "▢▢",
            label = "Dual",
            isSelected = currentMode == WorkspaceLayoutMode.DUAL_PANE,
            onClick = { onModeSelected(WorkspaceLayoutMode.DUAL_PANE) }
        )
        LayoutModeButton(
            icon = "▢▢▢",
            label = "Tri",
            isSelected = currentMode == WorkspaceLayoutMode.TRI_PANE,
            onClick = { onModeSelected(WorkspaceLayoutMode.TRI_PANE) }
        )
        LayoutModeButton(
            icon = "◎",
            label = "Focus",
            isSelected = currentMode == WorkspaceLayoutMode.FOCUS,
            onClick = { onModeSelected(WorkspaceLayoutMode.FOCUS) }
        )
    }
}

@Composable
private fun LayoutModeButton(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        animationSpec = KlarityMotion.quickExit(),
        label = "bgColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isHovered -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f) // Ghost buttons - much quieter
        },
        animationSpec = KlarityMotion.quickExit(),
        label = "contentColor"
    )
    
    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.hoverable(interactionSource)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 14.sp,
                color = contentColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = contentColor
            )
        }
    }
}

/**
 * Detached window wrapper with Klarity styling
 * Note: Actual window detachment requires platform-specific implementation
 */
@Composable
fun DetachedWindowStyle(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val luminousTeal = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = luminousTeal.copy(alpha = 0.1f),
                spotColor = luminousTeal.copy(alpha = 0.2f)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        luminousTeal.copy(alpha = 0.3f),
                        luminousTeal.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Window title bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

/**
 * Pane resizer handle
 */
@Composable
fun PaneResizer(
    onResize: (Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val width by animateDpAsState(
        targetValue = if (isDragging || isHovered) 6.dp else 2.dp,
        animationSpec = KlarityMotion.quickExit(),
        label = "resizerWidth"
    )
    
    val color by animateColorAsState(
        targetValue = when {
            isDragging -> MaterialTheme.colorScheme.primary
            isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        animationSpec = KlarityMotion.quickExit(),
        label = "resizerColor"
    )
    
    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .background(color)
            .hoverable(interactionSource)
            .clickable { /* Enable drag detection */ }
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// WORKSPACE TOP BAR
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Top bar with workspace controls and layout mode selector
 */
@Composable
fun WorkspaceTopBar(
    currentMode: WorkspaceLayoutMode,
    currentDestination: NavDestination,
    onLayoutModeChange: (WorkspaceLayoutMode) -> Unit,
    onToggleLeftPane: () -> Unit,
    onToggleRightPane: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left section - Current view title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = currentDestination.selectedIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = currentDestination.label,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
            
            // Center section - Layout mode selector
            LayoutModeSelector(
                currentMode = currentMode,
                onModeSelected = onLayoutModeChange
            )
            
            // Right section - Panel toggles
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle left panel
                PanelToggleButton(
                    icon = "◧",
                    label = "Left Panel",
                    onClick = onToggleLeftPane
                )
                
                // Toggle right panel  
                PanelToggleButton(
                    icon = "◨",
                    label = "Right Panel",
                    onClick = onToggleRightPane
                )
            }
        }
    }
}

@Composable
private fun PanelToggleButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Surface(
        onClick = onClick,
        color = if (isHovered) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.hoverable(interactionSource)
    ) {
        Box(
            modifier = Modifier.padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 18.sp,
                color = if (isHovered) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PLACEHOLDER PANES
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Graph visualization pane placeholder
 */
@Composable
fun GraphPane(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Graph icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔗",
                    fontSize = 36.sp
                )
            }
            
            Text(
                text = "Knowledge Graph",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            
            Text(
                text = "Visualize connections between your notes",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp)
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Placeholder stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StatItem(value = "0", label = "Nodes")
                StatItem(value = "0", label = "Connections")
                StatItem(value = "0", label = "Clusters")
            }
        }
    }
}

/**
 * Tasks pane placeholder
 */
@Composable
fun TasksPane(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tasks icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧩",
                    fontSize = 36.sp
                )
            }
            
            Text(
                text = "Tasks",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            
            Text(
                text = "Manage tasks extracted from your notes",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp)
            )
            
            Spacer(Modifier.height(8.dp))
            
            // Placeholder stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StatItem(value = "0", label = "To Do")
                StatItem(value = "0", label = "In Progress")
                StatItem(value = "0", label = "Done")
            }
        }
    }
}

/**
 * AI Chat pane placeholder
 */
@Composable
fun AIChatPane(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI icon with glow
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🤖",
                    fontSize = 36.sp
                )
            }
            
            Text(
                text = "AI Assistant",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            
            Text(
                text = "Chat with AI about your notes and ideas",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp)
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Start chat button
            Surface(
                onClick = { /* Start chat */ },
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✨", fontSize = 16.sp)
                    Text(
                        text = "Start a conversation",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}
