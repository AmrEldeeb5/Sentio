package com.example.klarity.presentation.screen.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityColors

/**
 * BoardControls - Header component for the Kanban board
 * 
 * Displays board title, subtitle, "Add Column" button, and filter chips.
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KlarityColors.BgSecondary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                    color = KlarityColors.TextPrimary
                )
                Text(
                    text = boardSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = KlarityColors.TextSecondary
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
            onTagsClick = onTagsClick
        )
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
                if (isHovered) KlarityColors.AccentPrimary.copy(alpha = 0.2f)
                else KlarityColors.BgTertiary
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
            tint = if (isHovered) KlarityColors.AccentPrimary else KlarityColors.TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "Add Column",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isHovered) KlarityColors.AccentPrimary else KlarityColors.TextSecondary
        )
    }
}

/**
 * Filter chip row containing Filter, Sort By, Assignee, and Tags chips
 * 
 * **Requirements: 5.1, 5.2, 5.3, 5.4**
 */
@Composable
private fun FilterChipRow(
    onFilterClick: () -> Unit,
    onSortByClick: () -> Unit,
    onAssigneeClick: () -> Unit,
    onTagsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Filter chip (Requirement 5.1)
        FilterDropdownChip(
            label = "Filter",
            onClick = onFilterClick
        )
        
        // Sort By chip (Requirement 5.2)
        FilterDropdownChip(
            label = "Sort By",
            onClick = onSortByClick
        )
        
        // Assignee chip (Requirement 5.3)
        FilterDropdownChip(
            label = "Assignee",
            onClick = onAssigneeClick
        )
        
        // Tags chip (Requirement 5.4)
        FilterDropdownChip(
            label = "Tags",
            onClick = onTagsClick
        )
    }
}

/**
 * Individual filter chip with dropdown icon and hover state
 */
@Composable
private fun FilterDropdownChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isHovered) KlarityColors.BgElevated
                else KlarityColors.BgTertiary
            )
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (isHovered) KlarityColors.TextPrimary else KlarityColors.TextSecondary
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Dropdown",
            tint = if (isHovered) KlarityColors.TextPrimary else KlarityColors.TextTertiary,
            modifier = Modifier.size(16.dp)
        )
    }
}
