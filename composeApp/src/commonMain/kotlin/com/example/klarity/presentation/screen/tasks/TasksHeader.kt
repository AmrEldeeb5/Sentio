package com.example.klarity.presentation.screen.tasks

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityColors

/**
 * TasksHeader - Main header component for the Kanban board
 * 
 * Displays logo, app name, view mode tabs (Kanban, List, Calendar),
 * Deep Work Mode button, notifications, and user avatar.
 * 
 * **Requirements: 10.1, 10.2**
 */
@Composable
fun TasksHeader(
    currentViewMode: TaskViewMode,
    onViewModeChange: (TaskViewMode) -> Unit,
    onDeepWorkModeClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onUserAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(KlarityColors.BgSecondary)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left section: Logo and app name
        LogoSection()
        
        // Center section: View mode tabs (Requirement 10.1, 10.2)
        ViewModeTabs(
            currentViewMode = currentViewMode,
            onViewModeChange = onViewModeChange
        )
        
        // Right section: Deep Work Mode, notifications, user avatar
        UserActionsSection(
            onDeepWorkModeClick = onDeepWorkModeClick,
            onNotificationsClick = onNotificationsClick,
            onUserAvatarClick = onUserAvatarClick
        )
    }
}


/**
 * Logo section with app icon and name.
 */
@Composable
private fun LogoSection(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo icon (using emoji as placeholder for SVG)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            KlarityColors.AccentPrimary,
                            KlarityColors.AccentSecondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "K",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KlarityColors.BgPrimary
            )
        }
        
        // App name
        Text(
            text = "Klarity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = KlarityColors.TextPrimary
        )
    }
}

/**
 * View mode tabs for switching between Kanban, List, and Calendar views.
 * 
 * **Requirement 10.1**: WHEN viewing the header navigation THEN the System 
 * SHALL display view options for Kanban, List, and Calendar
 * 
 * **Requirement 10.2**: WHEN a user selects a view option THEN the System 
 * SHALL highlight the active view in the navigation
 */
@Composable
private fun ViewModeTabs(
    currentViewMode: TaskViewMode,
    onViewModeChange: (TaskViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show Kanban, List, and Calendar (not Timeline)
    val displayModes = listOf(TaskViewMode.KANBAN, TaskViewMode.LIST, TaskViewMode.CALENDAR)
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(KlarityColors.BgTertiary)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayModes.forEach { mode ->
            ViewModeTab(
                mode = mode,
                isSelected = currentViewMode == mode,
                onClick = { onViewModeChange(mode) }
            )
        }
    }
}

/**
 * Individual view mode tab with selection highlighting.
 * 
 * The active view mode is highlighted with accent color background.
 */
@Composable
private fun ViewModeTab(
    mode: TaskViewMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> KlarityColors.AccentPrimary.copy(alpha = 0.2f)
            isHovered -> KlarityColors.BgElevated
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 150)
    )
    
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> KlarityColors.AccentPrimary
            isHovered -> KlarityColors.TextPrimary
            else -> KlarityColors.TextSecondary
        },
        animationSpec = tween(durationMillis = 150)
    )
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mode.emoji,
            fontSize = 14.sp
        )
        Text(
            text = mode.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}


/**
 * User actions section with Deep Work Mode button, notifications, and avatar.
 * 
 * **Requirement 10.1**: Header displays Deep Work Mode button and user actions
 */
@Composable
private fun UserActionsSection(
    onDeepWorkModeClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onUserAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Deep Work Mode button with glow effect
        DeepWorkModeButton(onClick = onDeepWorkModeClick)
        
        // Notifications icon button
        NotificationsButton(onClick = onNotificationsClick)
        
        // User avatar with hover ring
        UserAvatar(onClick = onUserAvatarClick)
    }
}

/**
 * Deep Work Mode button with animated glow effect.
 */
@Composable
private fun DeepWorkModeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Animated glow effect
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier
            .shadow(
                elevation = if (isHovered) 8.dp else 4.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = KlarityColors.AccentAI.copy(alpha = glowAlpha),
                spotColor = KlarityColors.AccentAI.copy(alpha = glowAlpha)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        KlarityColors.AccentAI.copy(alpha = 0.15f),
                        KlarityColors.AccentPrimary.copy(alpha = 0.15f)
                    )
                )
            )
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🧘",
                fontSize = 16.sp
            )
            Text(
                text = "Deep Work Mode",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = KlarityColors.AccentAI
            )
        }
    }
}

/**
 * Notifications icon button.
 */
@Composable
private fun NotificationsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                if (isHovered) KlarityColors.BgElevated else Color.Transparent
            )
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = if (isHovered) KlarityColors.TextPrimary else KlarityColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * User avatar with hover ring effect.
 */
@Composable
private fun UserAvatar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val ringColor by animateColorAsState(
        targetValue = if (isHovered) KlarityColors.AccentPrimary else Color.Transparent,
        animationSpec = tween(durationMillis = 200)
    )
    
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(ringColor)
            .padding(2.dp)
            .clip(CircleShape)
            .background(KlarityColors.BgTertiary)
            .hoverable(interactionSource = interactionSource)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "User Profile",
            tint = KlarityColors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ============================================================================
// Helper function for view mode highlight testing
// ============================================================================

/**
 * Determines if a view mode should be highlighted based on the current selection.
 * 
 * This function is used for property testing to verify that the correct view mode
 * is highlighted in the navigation.
 * 
 * **Property 13: View mode highlight consistency**
 * **Validates: Requirements 10.2**
 * 
 * @param viewMode The view mode to check
 * @param currentViewMode The currently selected view mode
 * @return true if the viewMode should be highlighted (i.e., it equals currentViewMode)
 */
fun isViewModeHighlighted(viewMode: TaskViewMode, currentViewMode: TaskViewMode): Boolean {
    return viewMode == currentViewMode
}
