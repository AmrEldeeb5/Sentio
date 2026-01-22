package com.example.klarity.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared Breadcrumbs component for navigation hierarchy display.
 *
 * Used in:
 * - TopCommandBar for global navigation
 * - EditorPanel for note location
 *
 * Features:
 * - Hover effects on clickable segments
 * - Accessible with content descriptions
 * - Consistent styling with theme colors
 *
 * @param path List of breadcrumb segments from root to current
 * @param onSegmentClick Callback when a segment is clicked (null = non-clickable)
 * @param modifier Modifier for customization
 */
@Composable
fun KlarityBreadcrumbs(
    path: List<String>,
    onSegmentClick: ((index: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .semantics { contentDescription = "Navigation path: ${path.joinToString(" / ")}" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        path.forEachIndexed { index, segment ->
            if (index > 0) {
                BreadcrumbSeparator()
            }

            BreadcrumbSegment(
                text = segment,
                isLast = index == path.lastIndex,
                isClickable = onSegmentClick != null && index < path.lastIndex,
                onClick = { onSegmentClick?.invoke(index) }
            )
        }
    }
}

@Composable
private fun BreadcrumbSeparator() {
    Text(
        text = "/",
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        fontSize = 12.sp,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

@Composable
private fun BreadcrumbSegment(
    text: String,
    isLast: Boolean,
    isClickable: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Text(
        text = text,
        color = when {
            isLast -> MaterialTheme.colorScheme.onSurface
            isHovered -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        fontSize = 13.sp,
        fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal,
        modifier = Modifier
            .then(
                if (isClickable) {
                    Modifier
                        .hoverable(interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .then(
                            if (isHovered) {
                                Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            } else Modifier
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                } else {
                    Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                }
            )
            .semantics {
                contentDescription = if (isClickable) "Navigate to $text" else text
            }
    )
}

/**
 * Extended breadcrumbs for editor with folder context.
 *
 * @param projectName The root project name
 * @param folderName The folder containing the item
 * @param itemName The current item name
 * @param onProjectClick Callback when project is clicked
 * @param onFolderClick Callback when folder is clicked
 */
@Composable
fun EditorBreadcrumbs(
    projectName: String,
    folderName: String,
    itemName: String,
    onProjectClick: (() -> Unit)? = null,
    onFolderClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ClickableBreadcrumbItem(
            text = projectName,
            isClickable = onProjectClick != null,
            onClick = { onProjectClick?.invoke() }
        )

        Text("/", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)

        ClickableBreadcrumbItem(
            text = folderName,
            isClickable = onFolderClick != null,
            onClick = { onFolderClick?.invoke() }
        )

        Text("/", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)

        // Last item is never clickable - it's the current location
        Text(
            text = itemName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ClickableBreadcrumbItem(
    text: String,
    isClickable: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = when {
            isHovered -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        },
        modifier = if (isClickable) {
            Modifier
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .hoverable(interactionSource)
        } else Modifier
    )
}
