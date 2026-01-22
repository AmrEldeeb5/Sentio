package com.example.klarity.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityMotion

/**
 * Navigation destinations for the app with Material icons
 */
enum class NavDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
) {
    HOME(Icons.Filled.Home, Icons.Outlined.Home, "Home"),
    NOTES(Icons.Filled.Edit, Icons.Outlined.Edit, "Notes"),
    GRAPH(Icons.Filled.Share, Icons.Outlined.Share, "Graph"),
    TASKS(Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "Tasks"),
    SETTINGS(Icons.Filled.Settings, Icons.Outlined.Settings, "Settings")
}

/**
 * Material 3 Navigation Rail - Minimal 80dp left navigation
 *
 * Features:
 * - M3 NavigationRailItem with proper states
 * - Animated selection indicator
 * - Proper accessibility support
 * - Optional FAB header
 */
@Composable
fun NavigationRail(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = {
            // Logo/Brand at top
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "K",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    ) {
        // Main navigation items
        NavDestination.entries
            .filter { it != NavDestination.SETTINGS }
            .forEach { destination ->
                KlarityNavRailItem(
                    destination = destination,
                    isSelected = currentDestination == destination,
                    onClick = { onDestinationSelected(destination) }
                )
            }

        Spacer(Modifier.weight(1f))

        // Settings at bottom
        KlarityNavRailItem(
            destination = NavDestination.SETTINGS,
            isSelected = currentDestination == NavDestination.SETTINGS,
            onClick = { onDestinationSelected(NavDestination.SETTINGS) }
        )

        Spacer(Modifier.height(16.dp))
    }
}

/**
 * Custom Navigation Rail Item with Klarity styling
 * Uses M3 NavigationRailItem as base with custom colors
 */
@Composable
private fun KlarityNavRailItem(
    destination: NavDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Animation for icon color
    val iconColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isHovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = KlarityMotion.standardEnter(),
        label = "iconColor"
    )
    
    NavigationRailItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = destination.label,
                modifier = Modifier.size(24.dp),
                tint = iconColor
            )
        },
        label = {
            Text(
                text = destination.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = iconColor
            )
        },
        alwaysShowLabel = false, // Show label only when selected
        interactionSource = interactionSource,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}
