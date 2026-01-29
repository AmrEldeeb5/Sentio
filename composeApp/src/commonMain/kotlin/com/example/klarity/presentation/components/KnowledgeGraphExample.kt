package com.example.klarity.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.klarity.presentation.theme.KlarityTheme

/**
 * Example usage and performance test for KnowledgeGraph component.
 * 
 * This demonstrates:
 * - 50+ node graph with various connections
 * - Different node types and edge types
 * - Interactive features (click, long press, pan, zoom)
 * - Physics simulation performance
 */
@Composable
fun KnowledgeGraphExample() {
    KlarityTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var selectedNode by remember { mutableStateOf<GraphNode?>(null) }
            
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with performance info
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Knowledge Graph - Physics Demo",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "50 nodes • Pan: drag canvas • Zoom: pinch/scroll • Drag: drag nodes • Double-tap: reset",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        selectedNode?.let { node ->
                            Text(
                                text = "Selected: ${node.label} (${node.type})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                
                // Knowledge graph
                KnowledgeGraph(
                    state = createSampleGraphState(),
                    onNodeClick = { node ->
                        selectedNode = node
                        println("Node clicked: ${node.label}")
                    },
                    onNodeLongPress = { node ->
                        println("Node long pressed: ${node.label}")
                    },
                    modifier = Modifier.weight(1f),
                    enablePhysics = true,
                    showLabels = true
                )
            }
        }
    }
}

/**
 * Creates a sample graph with 50+ nodes for performance testing.
 */
private fun createSampleGraphState(): KnowledgeGraphState {
    // Create 50 nodes of various types
    val nodes = mutableListOf<GraphNode>()
    
    // Notes (20)
    repeat(20) { i ->
        nodes.add(
            GraphNode(
                id = "note_$i",
                label = "Note ${i + 1}",
                type = NodeType.NOTE,
                position = Offset.Zero, // Will be positioned by physics
                connections = emptyList()
            )
        )
    }
    
    // Tasks (15)
    repeat(15) { i ->
        nodes.add(
            GraphNode(
                id = "task_$i",
                label = "Task ${i + 1}",
                type = NodeType.TASK,
                position = Offset.Zero,
                connections = emptyList()
            )
        )
    }
    
    // Concepts (10)
    repeat(10) { i ->
        nodes.add(
            GraphNode(
                id = "concept_$i",
                label = "Concept ${i + 1}",
                type = NodeType.CONCEPT,
                position = Offset.Zero,
                connections = emptyList()
            )
        )
    }
    
    // Tags (5)
    repeat(5) { i ->
        nodes.add(
            GraphNode(
                id = "tag_$i",
                label = "Tag ${i + 1}",
                type = NodeType.TAG,
                position = Offset.Zero,
                connections = emptyList()
            )
        )
    }
    
    // Create edges (100 connections for dense graph)
    val edges = mutableListOf<GraphEdge>()
    
    // Connect notes to tasks (20 edges)
    repeat(20) { i ->
        val noteId = "note_${i % 20}"
        val taskId = "task_${i % 15}"
        edges.add(
            GraphEdge(
                fromId = noteId,
                toId = taskId,
                weight = 0.5f + (i % 3) * 0.25f,
                type = EdgeType.RELATED
            )
        )
    }
    
    // Connect tasks to concepts (30 edges)
    repeat(30) { i ->
        val taskId = "task_${i % 15}"
        val conceptId = "concept_${i % 10}"
        edges.add(
            GraphEdge(
                fromId = taskId,
                toId = conceptId,
                weight = 0.6f + (i % 4) * 0.1f,
                type = EdgeType.PARENT_CHILD
            )
        )
    }
    
    // Connect notes to tags (25 edges)
    repeat(25) { i ->
        val noteId = "note_${i % 20}"
        val tagId = "tag_${i % 5}"
        edges.add(
            GraphEdge(
                fromId = noteId,
                toId = tagId,
                weight = 0.8f,
                type = EdgeType.TAG
            )
        )
    }
    
    // Connect concepts to concepts (15 edges for clustering)
    repeat(15) { i ->
        val concept1Id = "concept_${i % 10}"
        val concept2Id = "concept_${(i + 1) % 10}"
        edges.add(
            GraphEdge(
                fromId = concept1Id,
                toId = concept2Id,
                weight = 1.0f,
                type = EdgeType.LINK
            )
        )
    }
    
    // Connect notes to notes (10 edges)
    repeat(10) { i ->
        val note1Id = "note_${i * 2}"
        val note2Id = "note_${i * 2 + 1}"
        edges.add(
            GraphEdge(
                fromId = note1Id,
                toId = note2Id,
                weight = 0.7f,
                type = EdgeType.LINK
            )
        )
    }
    
    return KnowledgeGraphState(
        nodes = nodes,
        edges = edges,
        scale = 1.0f,
        offset = Offset.Zero
    )
}

/**
 * Smaller example for quick testing.
 */
private fun createSimpleGraphState(): KnowledgeGraphState {
    val nodes = listOf(
        GraphNode(id = "1", label = "Project Roadmap", type = NodeType.NOTE),
        GraphNode(id = "2", label = "Q1 Goals", type = NodeType.TASK),
        GraphNode(id = "3", label = "Planning", type = NodeType.TAG),
        GraphNode(id = "4", label = "Architecture", type = NodeType.CONCEPT),
        GraphNode(id = "5", label = "Design System", type = NodeType.NOTE)
    )
    
    val edges = listOf(
        GraphEdge(fromId = "1", toId = "2", weight = 0.8f, type = EdgeType.RELATED),
        GraphEdge(fromId = "1", toId = "3", weight = 0.5f, type = EdgeType.TAG),
        GraphEdge(fromId = "2", toId = "4", weight = 1.0f, type = EdgeType.PARENT_CHILD),
        GraphEdge(fromId = "4", toId = "5", weight = 0.7f, type = EdgeType.LINK)
    )
    
    return KnowledgeGraphState(
        nodes = nodes,
        edges = edges,
        scale = 1.0f,
        offset = Offset.Zero
    )
}
