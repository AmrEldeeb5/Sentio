package com.example.klarity.presentation.screen.graph

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.domain.models.Note
import com.example.klarity.presentation.theme.KlarityColors
import kotlin.math.*

/**
 * Graph Node representing a note in the knowledge graph
 */
data class GraphNode(
    val id: String,
    val title: String,
    val linkCount: Int,
    var position: Offset,
    var velocity: Offset = Offset.Zero,
    val isPinned: Boolean = false
)

/**
 * Graph Edge representing a link between notes
 */
data class GraphEdge(
    val sourceId: String,
    val targetId: String
)

/**
 * Knowledge Graph View - Visualizes note connections
 */
@Composable
fun GraphScreen(
    notes: List<Note>,
    selectedNoteId: String?,
    onNoteSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val luminousTeal = Color(0xFF1FDBC8)
    val electricMint = Color(0xFF3DD68C)

    // Parse wiki links from notes to build graph
    val (nodes, edges) = remember(notes) {
        buildGraph(notes)
    }

    // Mutable node positions for physics simulation
    var nodePositions by remember(nodes) {
        mutableStateOf(nodes.associateWith { it.position })
    }

    // Selected node for highlighting
    var hoveredNodeId by remember { mutableStateOf<String?>(null) }

    // Zoom and pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Physics simulation
    var isSimulating by remember { mutableStateOf(true) }

    // Run force-directed layout simulation
    LaunchedEffect(isSimulating, nodes, edges) {
        if (isSimulating && nodes.isNotEmpty()) {
            while (isSimulating) {
                nodePositions = applyForces(nodes, edges, nodePositions)
                kotlinx.coroutines.delay(16) // ~60fps
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KlarityColors.BgPrimary)
    ) {
        // Graph Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Find clicked node
                        val adjustedTap = (tapOffset - offset) / scale
                        val clickedNode = nodes.find { node ->
                            val pos = nodePositions[node] ?: node.position
                            (pos - adjustedTap).getDistance() < 30f
                        }
                        clickedNode?.let { onNoteSelected(it.id) }
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Draw edges
            edges.forEach { edge ->
                val sourceNode = nodes.find { it.id == edge.sourceId }
                val targetNode = nodes.find { it.id == edge.targetId }

                if (sourceNode != null && targetNode != null) {
                    val sourcePos = nodePositions[sourceNode] ?: sourceNode.position
                    val targetPos = nodePositions[targetNode] ?: targetNode.position

                    val isHighlighted = edge.sourceId == selectedNoteId ||
                            edge.targetId == selectedNoteId ||
                            edge.sourceId == hoveredNodeId ||
                            edge.targetId == hoveredNodeId

                    val edgeColor = if (isHighlighted) {
                        luminousTeal.copy(alpha = 0.8f)
                    } else {
                        KlarityColors.BorderPrimary.copy(alpha = 0.3f)
                    }

                    drawLine(
                        color = edgeColor,
                        start = Offset(centerX + sourcePos.x, centerY + sourcePos.y),
                        end = Offset(centerX + targetPos.x, centerY + targetPos.y),
                        strokeWidth = if (isHighlighted) 2f else 1f
                    )
                }
            }

            // Draw nodes
            nodes.forEach { node ->
                val pos = nodePositions[node] ?: node.position
                val isSelected = node.id == selectedNoteId
                val isHovered = node.id == hoveredNodeId

                // Node size based on link count
                val baseRadius = 12f + (node.linkCount * 4f).coerceAtMost(20f)
                val radius = if (isSelected || isHovered) baseRadius * 1.2f else baseRadius

                // Node color
                val nodeColor = when {
                    isSelected -> luminousTeal
                    isHovered -> electricMint
                    node.isPinned -> Color(0xFFFFA500)
                    else -> KlarityColors.TextSecondary
                }

                // Glow effect for selected/hovered
                if (isSelected || isHovered) {
                    drawCircle(
                        color = nodeColor.copy(alpha = 0.3f),
                        radius = radius * 1.5f,
                        center = Offset(centerX + pos.x, centerY + pos.y)
                    )
                }

                // Node circle
                drawCircle(
                    color = nodeColor,
                    radius = radius,
                    center = Offset(centerX + pos.x, centerY + pos.y)
                )

                // Inner circle
                drawCircle(
                    color = KlarityColors.BgPrimary,
                    radius = radius * 0.6f,
                    center = Offset(centerX + pos.x, centerY + pos.y)
                )
            }
        }


        // Node labels overlay
        nodes.forEach { node ->
            val pos = nodePositions[node] ?: node.position
            val isSelected = node.id == selectedNoteId
            val isHovered = node.id == hoveredNodeId

            if (isSelected || isHovered || scale > 0.8f) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = ((pos.x * scale + offset.x) / 2 + 200).dp,
                            y = ((pos.y * scale + offset.y) / 2 + 200).dp
                        )
                ) {
                    Surface(
                        color = KlarityColors.BgElevated.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = node.title.take(20),
                            color = if (isSelected) luminousTeal else KlarityColors.TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Controls overlay
        GraphControls(
            scale = scale,
            onZoomIn = { scale = (scale * 1.2f).coerceAtMost(3f) },
            onZoomOut = { scale = (scale / 1.2f).coerceAtLeast(0.3f) },
            onResetView = {
                scale = 1f
                offset = Offset.Zero
            },
            isSimulating = isSimulating,
            onToggleSimulation = { isSimulating = !isSimulating },
            nodeCount = nodes.size,
            edgeCount = edges.size,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        )

        // Legend
        GraphLegend(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        )

        // Empty state
        if (nodes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🕸️",
                        fontSize = 64.sp
                    )
                    Text(
                        text = "No connections yet",
                        color = KlarityColors.TextSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Link notes using [[note-name]] syntax",
                        color = KlarityColors.TextTertiary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GraphControls(
    scale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetView: () -> Unit,
    isSimulating: Boolean,
    onToggleSimulation: () -> Unit,
    nodeCount: Int,
    edgeCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Stats
        Surface(
            color = KlarityColors.BgSecondary.copy(alpha = 0.9f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Knowledge Graph",
                    color = KlarityColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$nodeCount notes • $edgeCount links",
                    color = KlarityColors.TextTertiary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Zoom: ${(scale * 100).toInt()}%",
                    color = KlarityColors.TextTertiary,
                    fontSize = 11.sp
                )
            }
        }

        // Zoom controls
        Surface(
            color = KlarityColors.BgSecondary.copy(alpha = 0.9f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(onClick = onZoomIn, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Zoom in",
                        tint = KlarityColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onZoomOut, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Zoom out",
                        tint = KlarityColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onResetView, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset view",
                        tint = KlarityColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onToggleSimulation, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (isSimulating) Icons.Default.Clear else Icons.Default.PlayArrow,
                        contentDescription = if (isSimulating) "Pause" else "Play",
                        tint = if (isSimulating) Color(0xFF1FDBC8) else KlarityColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun GraphLegend(modifier: Modifier = Modifier) {
    Surface(
        color = KlarityColors.BgSecondary.copy(alpha = 0.9f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Legend",
                color = KlarityColors.TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
            LegendItem(color = Color(0xFF1FDBC8), label = "Selected")
            LegendItem(color = Color(0xFF3DD68C), label = "Hovered")
            LegendItem(color = Color(0xFFFFA500), label = "Pinned")
            LegendItem(color = KlarityColors.TextSecondary, label = "Note")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = color,
            shape = CircleShape,
            modifier = Modifier.size(8.dp)
        ) {}
        Text(
            text = label,
            color = KlarityColors.TextTertiary,
            fontSize = 10.sp
        )
    }
}

// ============================================================================
// Graph Building & Physics
// ============================================================================

/**
 * Parse wiki links from notes and build graph structure
 */
private fun buildGraph(notes: List<Note>): Pair<List<GraphNode>, List<GraphEdge>> {
    val wikiLinkRegex = """\[\[([^\]|]+)(?:\|[^\]]+)?\]\]""".toRegex()
    val edges = mutableListOf<GraphEdge>()
    val linkCounts = mutableMapOf<String, Int>()

    // Find all wiki links
    notes.forEach { note ->
        val matches = wikiLinkRegex.findAll(note.content)
        matches.forEach { match ->
            val linkedNoteName = match.groupValues[1].trim()
            // Find the target note by title
            val targetNote = notes.find {
                it.title.equals(linkedNoteName, ignoreCase = true)
            }
            if (targetNote != null && targetNote.id != note.id) {
                edges.add(GraphEdge(note.id, targetNote.id))
                linkCounts[note.id] = (linkCounts[note.id] ?: 0) + 1
                linkCounts[targetNote.id] = (linkCounts[targetNote.id] ?: 0) + 1
            }
        }
    }

    // Create nodes with initial positions in a circle
    val notesWithLinks = notes.filter { note ->
        edges.any { it.sourceId == note.id || it.targetId == note.id }
    }

    val nodes = notesWithLinks.mapIndexed { index, note ->
        val angle = (2 * PI * index / notesWithLinks.size.coerceAtLeast(1)).toFloat()
        val radius = 150f + (notesWithLinks.size * 10f).coerceAtMost(300f)
        GraphNode(
            id = note.id,
            title = note.title.ifEmpty { "Untitled" },
            linkCount = linkCounts[note.id] ?: 0,
            position = Offset(cos(angle) * radius, sin(angle) * radius),
            isPinned = note.isPinned
        )
    }

    return nodes to edges.distinctBy { setOf(it.sourceId, it.targetId) }
}

/**
 * Apply force-directed layout forces
 */
private fun applyForces(
    nodes: List<GraphNode>,
    edges: List<GraphEdge>,
    positions: Map<GraphNode, Offset>
): Map<GraphNode, Offset> {
    if (nodes.isEmpty()) return positions

    val newPositions = positions.toMutableMap()
    val forces = mutableMapOf<GraphNode, Offset>()

    // Initialize forces
    nodes.forEach { forces[it] = Offset.Zero }

    // Repulsion between all nodes
    val repulsionStrength = 5000f
    nodes.forEach { node1 ->
        nodes.forEach { node2 ->
            if (node1.id != node2.id) {
                val pos1 = newPositions[node1] ?: node1.position
                val pos2 = newPositions[node2] ?: node2.position
                val delta = pos1 - pos2
                val distance = delta.getDistance().coerceAtLeast(1f)
                val force = delta * (repulsionStrength / (distance * distance))
                forces[node1] = (forces[node1] ?: Offset.Zero) + force
            }
        }
    }

    // Attraction along edges
    val attractionStrength = 0.05f
    val idealDistance = 120f
    edges.forEach { edge ->
        val node1 = nodes.find { it.id == edge.sourceId }
        val node2 = nodes.find { it.id == edge.targetId }
        if (node1 != null && node2 != null) {
            val pos1 = newPositions[node1] ?: node1.position
            val pos2 = newPositions[node2] ?: node2.position
            val delta = pos2 - pos1
            val distance = delta.getDistance().coerceAtLeast(1f)
            val force = delta * attractionStrength * (distance - idealDistance)
            forces[node1] = (forces[node1] ?: Offset.Zero) + force
            forces[node2] = (forces[node2] ?: Offset.Zero) - force
        }
    }

    // Center gravity
    val centerGravity = 0.01f
    nodes.forEach { node ->
        val pos = newPositions[node] ?: node.position
        val force = -pos * centerGravity
        forces[node] = (forces[node] ?: Offset.Zero) + force
    }

    // Apply forces with damping
    val damping = 0.85f
    val maxSpeed = 10f
    nodes.forEach { node ->
        val force = forces[node] ?: Offset.Zero
        var velocity = force * damping
        val speed = velocity.getDistance()
        if (speed > maxSpeed) {
            velocity = velocity * (maxSpeed / speed)
        }
        val currentPos = newPositions[node] ?: node.position
        newPositions[node] = currentPos + velocity
    }

    return newPositions
}
