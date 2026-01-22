package com.example.klarity.presentation.screen.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * BoardControls - Header component for the Kanban board
 * 
 * Displays AI suggestion banner, board title, subtitle, "Add Column" button, and filter chips.
 * 
 * **Requirements: 2.1, 5.1, 5.2, 5.3, 5.4**
 */
@Composable
fun BoardControls(
    boardTitle: String = "Q3 Product Launch",
    boardSubtitle: String = "Kanban Board",
    onAddColumn: () -> Unit,
    onFilterClick: () -> Unit,
    onSortByClick: () -> Unit,
    onAssigneeClick: () -> Unit,
    onTagsClick: () -> Unit,
    // AI Suggestion state
    showAiSuggestion: Boolean = true,
    aiSuggestionText: String = "Cluster 'Q3 Product Launch' tasks",
    aiSuggestionDescription: String = "AI has identified 4 conceptually related tasks. Review and confirm to group them.",
    onReviewSuggestions: () -> Unit = {},
    onDismissSuggestion: () -> Unit = {},
    // Filter state for showing selected values
    currentSortBy: TaskSortOption = TaskSortOption.PRIORITY,
    currentAssignee: String? = null,
    selectedTags: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // AI Suggestion Banner
        if (showAiSuggestion) {
            AiSuggestionBanner(
                title = "AI Suggestion: $aiSuggestionText",
                description = aiSuggestionDescription,
                onReviewClick = onReviewSuggestions,
                onDismiss = onDismissSuggestion
            )
        }
        
        // Title row with Add Column button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Board title and subtitle
            Column {
                Text(
                    text = boardTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = boardSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Add Column button (Requirement 2.1)
            AddColumnButton(onClick = onAddColumn)
        }
        
        // Filter chip row (Requirements 5.1, 5.2, 5.3, 5.4)
        FilterChipRow(
            onFilterClick = onFilterClick,
            onSortByClick = onSortByClick,
            onAssigneeClick = onAssigneeClick,
            onTagsClick = onTagsClick,
            currentSortBy = currentSortBy,
            currentAssignee = currentAssignee,
            selectedTags = selectedTags
        )
    }
}

/**
 * AI Suggestion Banner - Teal colored banner with suggestion and review button
 */
@Composable
private fun AiSuggestionBanner(
    title: String,
    description: String,
    onReviewClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI Icon (using star as placeholder for AutoAwesome)
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "AI Suggestion",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }
        
        // Review Suggestions Button
        Button(
            onClick = onReviewClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Review Suggestions",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Add Column button with icon
 * 
 * **Requirement 2.1**: WHEN a user clicks the "Add Column" button 
 * THEN the System SHALL create a new empty column with an editable title
 */
@Composable
private fun AddColumnButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isHovered) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Column",
            tint = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Add Column",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Filter chip row containing Filter, Sort By, Assignee, and Tags chips
 * Shows selected values when filters are active.
 * 
 * **Requirements: 5.1, 5.2, 5.3, 5.4**
 */
@Composable
private fun FilterChipRow(
    onFilterClick: () -> Unit,
    onSortByClick: () -> Unit,
    onAssigneeClick: () -> Unit,
    onTagsClick: () -> Unit,
    currentSortBy: TaskSortOption = TaskSortOption.PRIORITY,
    currentAssignee: String? = null,
    selectedTags: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Filter chip (Requirement 5.1)
        FilterDropdownChip(
            emoji = "⚙️",
            label = "Filter",
            onClick = onFilterClick
        )
        
        // Sort By chip with selected value (Requirement 5.2)
        FilterDropdownChip(
            emoji = "↕️",
            label = "Sort By: ${currentSortBy.label}",
            onClick = onSortByClick,
            isActive = true
        )
        
        // Assignee chip with selected value (Requirement 5.3)
        FilterDropdownChip(
            emoji = "👤",
            label = if (currentAssignee != null) "Assignee: $currentAssignee" else "Assignee",
            onClick = onAssigneeClick,
            isActive = currentAssignee != null
        )
        
        // Tags chip with count (Requirement 5.4)
        FilterDropdownChip(
            emoji = "🏷️",
            label = if (selectedTags.isNotEmpty()) "Tags (${selectedTags.size})" else "Tags",
            onClick = onTagsClick,
            isActive = selectedTags.isNotEmpty()
        )
    }
}

/**
 * Individual filter chip with emoji, dropdown indicator, and hover state.
 * Shows active state when a filter is selected.
 */
@Composable
private fun FilterDropdownChip(
    emoji: String? = null,
    label: String,
    onClick: () -> Unit,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isActive -> MaterialTheme.colorScheme.primaryContainer
            isHovered -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(150)
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            isActive -> MaterialTheme.colorScheme.primary
            isHovered -> MaterialTheme.colorScheme.onSurface
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(150)
    )
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        emoji?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Dropdown",
            tint = contentColor.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
    }
}
