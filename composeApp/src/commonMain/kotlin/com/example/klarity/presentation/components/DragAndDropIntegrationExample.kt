package com.example.klarity.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

/**
 * Example demonstrating drag-and-drop integration between AIContextPanel and KnowledgeGraph.
 * 
 * This example shows how to:
 * 1. Make AIContextPanel items draggable
 * 2. Accept drops on the KnowledgeGraph canvas
 * 3. Create new nodes from dropped context items
 * 4. Coordinate drag state across components
 */
@Composable
fun DragAndDropIntegrationExample() {
    // Shared drag state
    var draggedItem by remember { mutableStateOf<AIContextItem?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Context panel state
    var contextState by remember {
        mutableStateOf(
            AIContextState(
                items = listOf(
                    AIContextItem(
                        id = "1",
                        title = "Meeting Notes",
                        type = AIContextItemType.NOTE,
                        relevance = 0.9f,
                        preview = "Discussed project timeline and deliverables",
                        timestamp = System.currentTimeMillis(),
                        isBookmarked = false
                    ),
                    AIContextItem(
                        id = "2",
                        title = "Complete Design Review",
                        type = AIContextItemType.TASK,
                        relevance = 0.85f,
                        preview = "Review and approve final design mockups",
                        timestamp = System.currentTimeMillis() - 3600000,
                        isBookmarked = true
                    ),
                    AIContextItem(
                        id = "3",
                        title = "API Documentation",
                        type = AIContextItemType.LINK,
                        relevance = 0.75f,
                        preview = "https://docs.example.com/api/v2",
                        timestamp = System.currentTimeMillis() - 7200000,
                        isBookmarked = false
                    )
                ),
                isExpanded = true,
                bookmarkedItems = listOf()
            )
        )
    }
    
    // Knowledge graph state
    var graphState by remember {
        mutableStateOf(
            KnowledgeGraphState(
                nodes = listOf(
                    GraphNode(
                        id = "node1",
                        label = "Project",
                        type = NodeType.CONCEPT,
                        position = Offset.Zero
                    ),
                    GraphNode(
                        id = "node2",
                        label = "Implementation",
                        type = NodeType.TASK,
                        position = Offset(100f, 100f)
                    )
                ),
                edges = listOf(
                    GraphEdge(
                        fromId = "node1",
                        toId = "node2",
                        weight = 1.0f,
                        type = EdgeType.RELATED
                    )
                ),
                scale = 1.0f,
                offset = Offset.Zero
            )
        )
    }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Main content area with knowledge graph
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            KnowledgeGraph(
                state = graphState,
                onNodeClick = { node ->
                    println("Node clicked: ${node.label}")
                },
                onNodeLongPress = { node ->
                    println("Node long-pressed: ${node.label}")
                },
                onDropReceived = { item, position ->
                    // Convert screen position to graph coordinates
                    val graphPosition = position // In real implementation, convert coordinates
                    
                    // Create new node from dropped item
                    val newNode = contextItemToGraphNode(item, graphPosition)
                    
                    // Add node to graph
                    graphState = graphState.copy(
                        nodes = graphState.nodes + newNode
                    )
                    
                    println("Created node from dropped item: ${item.title} at $position")
                },
                enablePhysics = true,
                showLabels = true,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // AI Context Panel (right side)
        AIContextPanel(
            state = contextState,
            onItemClick = { item ->
                println("Context item clicked: ${item.title}")
            },
            onToggleExpanded = {
                contextState = contextState.copy(isExpanded = !contextState.isExpanded)
            },
            onReorder = { newOrder ->
                contextState = contextState.copy(items = newOrder)
            },
            onBookmarkToggle = { item ->
                val updatedItems = contextState.items.map {
                    if (it.id == item.id) it.copy(isBookmarked = !it.isBookmarked)
                    else it
                }
                val updatedBookmarks = updatedItems.filter { it.isBookmarked }
                contextState = contextState.copy(
                    items = updatedItems,
                    bookmarkedItems = updatedBookmarks
                )
            },
            onDragStart = { item ->
                draggedItem = item
                isDragging = true
                println("Drag started: ${item.title}")
            },
            onDragEnd = { item, offset ->
                println("Drag ended: ${item.title} at offset $offset")
                
                // Check if dropped on graph (in real implementation, calculate bounds)
                // For now, simulate that any drag is a drop on the graph
                val graphPosition = offset // Convert to graph coordinates
                
                // Create new node from dropped item
                val newNode = contextItemToGraphNode(item, graphPosition)
                
                // Add node to graph
                graphState = graphState.copy(
                    nodes = graphState.nodes + newNode
                )
                
                draggedItem = null
                isDragging = false
            },
            width = 320.dp,
            modifier = Modifier.fillMaxHeight()
        )
    }
}

/**
 * Keyboard shortcuts for accessibility (alternative to drag-and-drop).
 * 
 * These should be implemented in the actual application:
 * - Ctrl+Shift+G: Add selected context item to graph
 * - Ctrl+Shift+C: Add selected context item to chat
 */
@Composable
fun KeyboardShortcutsExample() {
    // Implementation would use platform-specific keyboard event handlers
    // For example:
    // - detectKey(Key.G, modifiers = KeyModifiers.Ctrl + KeyModifiers.Shift)
    // - Announce via screen reader: "Context item added to knowledge graph"
}
