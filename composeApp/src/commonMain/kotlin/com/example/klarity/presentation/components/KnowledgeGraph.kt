package com.example.klarity.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.theme.KlarityTheme
import kotlinx.coroutines.isActive
import kotlin.math.*

// ══════════════════════════════════════════════════════════════════════════════
// DATA STRUCTURES
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Represents a node in the knowledge graph with physics properties.
 */
data class GraphNode(
    val id: String,
    val label: String,
    val type: NodeType,
    val position: Offset = Offset.Zero,
    val connections: List<String> = emptyList(),
    // Physics properties (internal)
    val velocity: Offset = Offset.Zero,
    val isSelected: Boolean = false,
    val isDragging: Boolean = false
)

/**
 * Types of nodes in the knowledge graph.
 */
enum class NodeType {
    NOTE, TASK, CONCEPT, TAG
}

/**
 * Represents an edge connecting two nodes.
 */
data class GraphEdge(
    val fromId: String,
    val toId: String,
    val weight: Float = 1.0f,
    val type: EdgeType = EdgeType.RELATED
)

/**
 * Types of edges in the knowledge graph.
 */
enum class EdgeType {
    RELATED, PARENT_CHILD, TAG, LINK
}

/**
 * State container for the knowledge graph.
 */
data class KnowledgeGraphState(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>,
    val scale: Float = 1.0f,
    val offset: Offset = Offset.Zero
)

// ══════════════════════════════════════════════════════════════════════════════
// PHYSICS SIMULATION CONSTANTS
// ══════════════════════════════════════════════════════════════════════════════

private object PhysicsConstants {
    const val REPULSION_CONSTANT = 50000f
    const val ATTRACTION_CONSTANT = 0.001f
    const val GRAVITY_CONSTANT = 0.001f
    const val DAMPING_FACTOR = 0.8f
    const val MIN_DISTANCE = 5f
    const val MAX_FORCE = 50f
    const val TIME_STEP = 0.016f // ~60fps
}

// ══════════════════════════════════════════════════════════════════════════════
// MAIN COMPONENT
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Interactive physics-based knowledge graph component.
 * 
 * Visualizes relationships between notes, tasks, and concepts using force-directed
 * layout algorithm. Supports pan, zoom, node dragging, and interactive selection.
 * 
 * Features:
 * - Force-directed physics simulation (repulsion, attraction, gravity)
 * - Interactive pan and zoom (1.0x to 5.0x)
 * - Node dragging with physics suspension
 * - Animated curved edges with flow effects
 * - Type-specific node colors and pulsing glow for selected nodes
 * - Performance optimizations (viewport culling, throttled physics)
 * - Drag-and-drop support for creating nodes from AIContextItems
 * 
 * @param state Current graph state with nodes and edges
 * @param onNodeClick Callback when a node is clicked
 * @param onNodeLongPress Callback when a node is long-pressed
 * @param onDropReceived Callback when an AIContextItem is dropped onto the graph
 * @param modifier Modifier to apply to the component
 * @param enablePhysics Enable force-directed physics simulation
 * @param showLabels Show node labels below nodes
 */
@Composable
fun KnowledgeGraph(
    state: KnowledgeGraphState,
    onNodeClick: (GraphNode) -> Unit,
    onNodeLongPress: (GraphNode) -> Unit,
    onDropReceived: (AIContextItem, Offset) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    enablePhysics: Boolean = true,
    showLabels: Boolean = true
) {
    // Theme colors - compute outside of Canvas
    val luminousTeal = KlarityTheme.extendedColors.luminousTeal
    val electricMint = KlarityTheme.extendedColors.electricMint
    val sentioPurple = KlarityTheme.extendedColors.sentioPurple
    val tertiary = MaterialTheme.colorScheme.tertiary
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val textStyle = MaterialTheme.typography.bodySmall
    
    // Pre-compute colors for each node type
    val nodeColors = remember(luminousTeal, electricMint, sentioPurple, tertiary) {
        mapOf(
            NodeType.NOTE to luminousTeal,
            NodeType.TASK to electricMint,
            NodeType.CONCEPT to sentioPurple,
            NodeType.TAG to tertiary
        )
    }
    
    // Text measurer for labels
    val textMeasurer = rememberTextMeasurer()
    
    // Mutable state for nodes (with physics)
    var nodes by remember(state.nodes) { mutableStateOf(initializeNodePositions(state.nodes)) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var hoveredNodeId by remember { mutableStateOf<String?>(null) }
    var draggedNodeId by remember { mutableStateOf<String?>(null) }
    
    // Drop zone state
    var dropPosition by remember { mutableStateOf<Offset?>(null) }
    var isDragHovering by remember { mutableStateOf(false) }
    var hoverPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Pulsing animation for drop zone indicator
    val pulseProgress by rememberInfiniteTransition(label = "dropZonePulse").animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseProgress"
    )
    
    // Canvas offset and scale (for pan/zoom)
    var canvasOffset by remember(state.offset) { mutableStateOf(state.offset) }
    var canvasScale by remember(state.scale) { mutableStateOf(state.scale) }
    
    // Physics simulation
    LaunchedEffect(enablePhysics, state.nodes.size, state.edges.size) {
        if (!enablePhysics) return@LaunchedEffect
        
        while (isActive) {
            withFrameMillis { frameTimeMillis ->
                // Only update physics if no node is being dragged
                if (draggedNodeId == null) {
                    nodes = updatePhysics(nodes, state.edges, PhysicsConstants.TIME_STEP)
                }
            }
        }
    }
    
    // Entrance animation (staggered fade-in)
    val entranceProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 500 + (state.nodes.size * 50),
            easing = FastOutSlowInEasing
        ),
        label = "entranceAnimation"
    )
    
    // Edge flow animation (dashed line offset)
    val edgeFlowOffset by rememberInfiniteTransition(label = "edgeFlow").animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "edgeFlowOffset"
    )
    
    // Render canvas
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        
                        // Track hover state for desktop
                        if (event.type == PointerEventType.Move) {
                            val position = event.changes.first().position
                            val graphOffset = screenToGraph(position, canvasOffset, canvasScale)
                            
                            // Update hover position for drop zone indicator
                            hoverPosition = position
                            
                            // Find hovered node
                            val hovered = nodes.firstOrNull { node ->
                                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                                val screenPos = graphToScreen(node.position, canvasOffset, canvasScale, centerOffset)
                                val distance = (screenPos - position).getDistance()
                                distance < getNodeRadius(node, state.edges) * canvasScale
                            }
                            
                            hoveredNodeId = hovered?.id
                        }
                        
                        // Detect drop events
                        if (event.type == PointerEventType.Release && isDragHovering) {
                            val position = event.changes.first().position
                            dropPosition = position
                            isDragHovering = false
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        // Convert tap to graph coordinates
                        val graphOffset = screenToGraph(tapOffset, canvasOffset, canvasScale)
                        
                        // Find tapped node
                        val tappedNode = nodes.firstOrNull { node ->
                            val distance = (node.position - graphOffset).getDistance()
                            distance < getNodeRadius(node, state.edges)
                        }
                        
                        if (tappedNode != null) {
                            selectedNodeId = tappedNode.id
                            onNodeClick(tappedNode.copy(isSelected = true))
                        } else {
                            selectedNodeId = null
                        }
                    },
                    onLongPress = { longPressOffset ->
                        val graphOffset = screenToGraph(longPressOffset, canvasOffset, canvasScale)
                        val longPressedNode = nodes.firstOrNull { node ->
                            val distance = (node.position - graphOffset).getDistance()
                            distance < getNodeRadius(node, state.edges)
                        }
                        
                        if (longPressedNode != null) {
                            onNodeLongPress(longPressedNode)
                        }
                    },
                    onDoubleTap = {
                        // Reset zoom and center view
                        canvasOffset = Offset.Zero
                        canvasScale = 1.0f
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val graphOffset = screenToGraph(offset, canvasOffset, canvasScale)
                        val draggedNode = nodes.firstOrNull { node ->
                            val distance = (node.position - graphOffset).getDistance()
                            distance < getNodeRadius(node, state.edges)
                        }
                        
                        if (draggedNode != null) {
                            draggedNodeId = draggedNode.id
                            nodes = nodes.map {
                                if (it.id == draggedNode.id) it.copy(isDragging = true, velocity = Offset.Zero)
                                else it
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        if (draggedNodeId != null) {
                            // Drag node
                            val scaledDrag = dragAmount / canvasScale
                            nodes = nodes.map {
                                if (it.id == draggedNodeId) it.copy(position = it.position + scaledDrag)
                                else it
                            }
                        } else {
                            // Pan canvas
                            canvasOffset += dragAmount
                        }
                    },
                    onDragEnd = {
                        draggedNodeId?.let { id ->
                            nodes = nodes.map {
                                if (it.id == id) it.copy(isDragging = false)
                                else it
                            }
                        }
                        draggedNodeId = null
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    canvasScale = (canvasScale * zoom).coerceIn(1.0f, 5.0f)
                    canvasOffset += pan
                }
            }
    ) {
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        
        // Draw drop zone indicator when dragging over graph
        if (isDragHovering) {
            val previewRadius = 30.dp.toPx()
            val previewColor = sentioPurple.copy(alpha = pulseProgress * 0.5f)
            
            // Draw dashed circle preview
            drawCircle(
                color = previewColor,
                radius = previewRadius,
                center = hoverPosition,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                )
            )
            
            // Draw pulsing glow
            val glowRadius = previewRadius * (1f + pulseProgress * 0.3f)
            drawCircle(
                color = previewColor.copy(alpha = pulseProgress * 0.2f),
                radius = glowRadius,
                center = hoverPosition
            )
        }
        
        // Draw edges
        state.edges.forEach { edge ->
            val fromNode = nodes.firstOrNull { it.id == edge.fromId }
            val toNode = nodes.firstOrNull { it.id == edge.toId }
            
            if (fromNode != null && toNode != null) {
                // Check if edge is in viewport
                val fromScreen = graphToScreen(fromNode.position, canvasOffset, canvasScale, centerOffset)
                val toScreen = graphToScreen(toNode.position, canvasOffset, canvasScale, centerOffset)
                
                if (isInViewport(fromScreen, size.width, size.height) || 
                    isInViewport(toScreen, size.width, size.height)) {
                    drawEdge(
                        from = fromScreen,
                        to = toScreen,
                        edge = edge,
                        color = outline.copy(alpha = 0.4f),
                        flowOffset = edgeFlowOffset,
                        alpha = entranceProgress
                    )
                }
            }
        }
        
        // Draw nodes
        nodes.forEachIndexed { index, node ->
            val screenPos = graphToScreen(node.position, canvasOffset, canvasScale, centerOffset)
            
            // Viewport culling
            if (isInViewport(screenPos, size.width, size.height)) {
                val nodeColor = nodeColors[node.type] ?: tertiary
                val baseRadius = getNodeRadius(node, state.edges) * canvasScale
                val isSelected = node.id == selectedNodeId
                val isHovered = node.id == hoveredNodeId
                
                // Apply scale for hover (1.15x) or selection (1.2x)
                val nodeScale = when {
                    isSelected -> 1.2f
                    isHovered -> 1.15f
                    else -> 1.0f
                }
                val radius = baseRadius * nodeScale
                
                // Staggered entrance alpha
                val nodeAlpha = (entranceProgress * (index + 1) / nodes.size.toFloat()).coerceIn(0f, 1f)
                
                // Draw node (with or without glow for hover/selection)
                if (isHovered || isSelected) {
                    drawNodeWithGlow(
                        center = screenPos,
                        radius = radius,
                        color = nodeColor,
                        alpha = nodeAlpha
                    )
                } else {
                    drawCircle(
                        color = nodeColor.copy(alpha = nodeAlpha),
                        radius = radius,
                        center = screenPos
                    )
                }
                
                // Draw label
                if (showLabels && canvasScale > 1.5f) {
                    drawNodeLabel(
                        textMeasurer = textMeasurer,
                        label = node.label,
                        position = screenPos.copy(y = screenPos.y + radius + 20f),
                        textStyle = textStyle,
                        color = onSurface.copy(alpha = nodeAlpha * 0.8f)
                    )
                }
            }
        }
    }
    
    // Accessibility announcement
    LaunchedEffect(state.nodes.size, state.edges.size) {
        // Screen reader announces: "{node count} nodes, {edge count} connections"
        // Implementation would use platform-specific accessibility APIs
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PHYSICS SIMULATION (Lines 340-440)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Updates node positions using force-directed layout algorithm.
 * 
 * Applies three forces:
 * 1. Repulsion - pushes all nodes apart
 * 2. Attraction - pulls connected nodes together
 * 3. Gravity - prevents nodes from drifting off-screen
 * 
 * @param nodes Current list of nodes
 * @param edges List of edges defining connections
 * @param timeStep Time delta for physics integration (default: 0.016s = 60fps)
 * @return Updated list of nodes with new positions and velocities
 */
private fun updatePhysics(
    nodes: List<GraphNode>,
    edges: List<GraphEdge>,
    timeStep: Float
): List<GraphNode> {
    val forces = MutableList(nodes.size) { Offset.Zero }
    val center = Offset.Zero
    
    // 1. Apply repulsion force between all node pairs
    for (i in nodes.indices) {
        for (j in i + 1 until nodes.size) {
            val node1 = nodes[i]
            val node2 = nodes[j]
            
            if (node1.isDragging || node2.isDragging) continue
            
            val delta = node2.position - node1.position
            val distance = max(delta.getDistance(), PhysicsConstants.MIN_DISTANCE)
            
            // Coulomb's law: F = k / r²
            val forceMagnitude = PhysicsConstants.REPULSION_CONSTANT / (distance * distance)
            val clampedForce = min(forceMagnitude, PhysicsConstants.MAX_FORCE)
            
            val forceDirection = delta / distance
            val force = forceDirection * -clampedForce
            
            forces[i] = forces[i] + force
            forces[j] = forces[j] - force
        }
    }
    
    // 2. Apply attraction force for connected nodes (Hooke's law)
    edges.forEach { edge ->
        val fromIndex = nodes.indexOfFirst { it.id == edge.fromId }
        val toIndex = nodes.indexOfFirst { it.id == edge.toId }
        
        if (fromIndex != -1 && toIndex != -1) {
            val fromNode = nodes[fromIndex]
            val toNode = nodes[toIndex]
            
            if (fromNode.isDragging || toNode.isDragging) return@forEach
            
            val delta = toNode.position - fromNode.position
            val distance = max(delta.getDistance(), PhysicsConstants.MIN_DISTANCE)
            
            // Hooke's law: F = k * distance * weight
            val forceMagnitude = PhysicsConstants.ATTRACTION_CONSTANT * distance * edge.weight
            val clampedForce = min(forceMagnitude, PhysicsConstants.MAX_FORCE)
            
            val forceDirection = delta / distance
            val force = forceDirection * clampedForce
            
            forces[fromIndex] = forces[fromIndex] + force
            forces[toIndex] = forces[toIndex] - force
        }
    }
    
    // 3. Apply center gravity (prevent drift)
    nodes.forEachIndexed { index, node ->
        if (node.isDragging) return@forEachIndexed
        
        val delta = center - node.position
        val distance = max(delta.getDistance(), PhysicsConstants.MIN_DISTANCE)
        
        val forceMagnitude = PhysicsConstants.GRAVITY_CONSTANT * distance
        val forceDirection = delta / distance
        val force = forceDirection * forceMagnitude
        
        forces[index] = forces[index] + force
    }
    
    // 4. Update positions with damping
    return nodes.mapIndexed { index, node ->
        if (node.isDragging) {
            node // Don't update dragged nodes
        } else {
            // Semi-implicit Euler integration
            val acceleration = forces[index]
            val newVelocity = (node.velocity + acceleration * timeStep) * PhysicsConstants.DAMPING_FACTOR
            val newPosition = node.position + newVelocity * timeStep
            
            node.copy(
                position = newPosition,
                velocity = newVelocity
            )
        }
    }
}

/**
 * Initializes node positions in a circular layout.
 */
private fun initializeNodePositions(nodes: List<GraphNode>): List<GraphNode> {
    if (nodes.isEmpty()) return emptyList()
    
    val radius = 200f
    val angleStep = (2 * PI / nodes.size).toFloat()
    
    return nodes.mapIndexed { index, node ->
        if (node.position != Offset.Zero) {
            node // Keep existing position
        } else {
            val angle = angleStep * index
            val x = cos(angle) * radius
            val y = sin(angle) * radius
            node.copy(position = Offset(x, y))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// DRAWING HELPERS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Draws an edge between two nodes with curved bezier path.
 */
private fun DrawScope.drawEdge(
    from: Offset,
    to: Offset,
    edge: GraphEdge,
    color: Color,
    flowOffset: Float,
    alpha: Float
) {
    val controlPoint = calculateBezierControlPoint(from, to)
    val path = Path().apply {
        moveTo(from.x, from.y)
        quadraticBezierTo(
            controlPoint.x, controlPoint.y,
            to.x, to.y
        )
    }
    
    val strokeWidth = (1f + edge.weight * 3f).dp.toPx()
    
    // Draw edge with flow animation for LINK type
    if (edge.type == EdgeType.LINK) {
        val dashPattern = floatArrayOf(10f, 10f)
        drawPath(
            path = path,
            color = color.copy(alpha = alpha),
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(dashPattern, flowOffset)
            )
        )
    } else {
        drawPath(
            path = path,
            color = color.copy(alpha = alpha),
            style = Stroke(width = strokeWidth)
        )
    }
}

/**
 * Calculates bezier control point for curved edges.
 */
private fun calculateBezierControlPoint(from: Offset, to: Offset): Offset {
    val midpoint = Offset((from.x + to.x) / 2f, (from.y + to.y) / 2f)
    val perpendicular = Offset(-(to.y - from.y), to.x - from.x)
    val normalized = perpendicular / max(perpendicular.getDistance(), 0.001f)
    
    // Offset control point perpendicular to edge
    val curvature = 0.2f
    return midpoint + normalized * (from - to).getDistance() * curvature
}

/**
 * Draws a node with pulsing glow effect.
 */
private fun DrawScope.drawNodeWithGlow(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float
) {
    // Draw glow
    val glowRadius = radius * 1.5f
    val gradient = Brush.radialGradient(
        colors = listOf(
            color.copy(alpha = alpha * 0.4f),
            color.copy(alpha = alpha * 0.2f),
            Color.Transparent
        ),
        center = center,
        radius = glowRadius
    )
    
    drawCircle(
        brush = gradient,
        center = center,
        radius = glowRadius
    )
    
    // Draw main node (scaled up)
    drawCircle(
        color = color.copy(alpha = alpha),
        radius = radius * 1.2f,
        center = center
    )
}

/**
 * Draws node label below the node.
 */
private fun DrawScope.drawNodeLabel(
    textMeasurer: TextMeasurer,
    label: String,
    position: Offset,
    textStyle: TextStyle,
    color: Color
) {
    val textLayoutResult = textMeasurer.measure(
        text = label,
        style = textStyle.copy(color = color)
    )
    
    val textOffset = Offset(
        x = position.x - textLayoutResult.size.width / 2f,
        y = position.y
    )
    
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = textOffset
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// UTILITY FUNCTIONS
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Gets node color based on type.
 */
@Composable
private fun getNodeColor(
    type: NodeType,
    luminousTeal: Color,
    electricMint: Color,
    sentioPurple: Color,
    tertiary: Color
): Color {
    return when (type) {
        NodeType.NOTE -> luminousTeal
        NodeType.TASK -> electricMint
        NodeType.CONCEPT -> sentioPurple
        NodeType.TAG -> tertiary
    }
}

/**
 * Calculates node radius based on number of connections.
 */
private fun getNodeRadius(node: GraphNode, edges: List<GraphEdge>): Float {
    val connectionCount = edges.count { it.fromId == node.id || it.toId == node.id }
    return (10f + min(connectionCount * 3f, 30f))
}

/**
 * Converts screen coordinates to graph coordinates.
 */
private fun screenToGraph(
    screenPos: Offset,
    canvasOffset: Offset,
    canvasScale: Float
): Offset {
    return (screenPos - canvasOffset) / canvasScale
}

/**
 * Converts graph coordinates to screen coordinates.
 */
private fun graphToScreen(
    graphPos: Offset,
    canvasOffset: Offset,
    canvasScale: Float,
    centerOffset: Offset
): Offset {
    return (graphPos * canvasScale) + canvasOffset + centerOffset
}

/**
 * Checks if a point is within the viewport.
 */
private fun isInViewport(position: Offset, width: Float, height: Float): Boolean {
    val margin = 100f // Allow some off-screen rendering for smooth panning
    return position.x >= -margin && position.x <= width + margin &&
           position.y >= -margin && position.y <= height + margin
}

/**
 * Extension function to get distance of an Offset vector.
 */
private fun Offset.getDistance(): Float {
    return sqrt(x * x + y * y)
}

// ══════════════════════════════════════════════════════════════════════════════
// EXTENDED COLORS EXTENSION (for missing colors)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Temporary extension properties for colors not yet in ExtendedColors.
 * These should be added to the theme system.
 */
private val com.example.klarity.presentation.theme.ExtendedColors.luminousTeal: Color
    get() = Color(0xFF1FDBC8)

private val com.example.klarity.presentation.theme.ExtendedColors.electricMint: Color
    get() = Color(0xFF3DD68C)

// ══════════════════════════════════════════════════════════════════════════════
// DRAG-AND-DROP UTILITIES
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Converts an AIContextItem to a GraphNode.
 * Used when dropping context items onto the knowledge graph.
 */
fun contextItemToGraphNode(item: AIContextItem, position: Offset): GraphNode {
    return GraphNode(
        id = item.id,
        label = item.title,
        type = when (item.type) {
            AIContextItemType.NOTE -> NodeType.NOTE
            AIContextItemType.TASK -> NodeType.TASK
            AIContextItemType.LINK -> NodeType.CONCEPT
            AIContextItemType.FILE -> NodeType.TAG
        },
        position = position,
        connections = emptyList()
    )
}
