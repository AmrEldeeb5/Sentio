package com.example.klarity.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityColors

/**
 * Navigation destinations for the app
 */
enum class NavDestination(val icon: String, val label: String) {
    HOME("🏠", "Home"),
    NOTES("📝", "Notes"),
    GRAPH("🕸️", "Graph"),
    TASKS("🧩", "Tasks"),
    SETTINGS("⚙️", "Settings")
}

/**
 * Side Navigation Rail - Minimal 72px left navigation
 * 
 * Features:
 * - Luminous Teal active state
 * - Electric Mint hover glow
 * - Subtle click pulse animation
 */
@Composable
fun NavigationRail(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    // Use centralized colors from theme
    val luminousTeal = KlarityColors.LuminousTeal
    val electricMint = KlarityColors.ElectricMint
    val glowColor = KlarityColors.GlowColor

    Surface(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight(),
        color = KlarityColors.BgPrimary // Darkest panel
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section - Main navigation items
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Logo/Brand at top
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = KlarityColors.BgElevated
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "S",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = luminousTeal
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Main nav items (Home, Notes, Tasks)
                NavDestination.entries
                    .filter { it != NavDestination.SETTINGS }
                    .forEach { destination ->
                        NavRailItem(
                            destination = destination,
                            isSelected = currentDestination == destination,
                            luminousTeal = luminousTeal,
                            electricMint = electricMint,
                            glowColor = glowColor,
                            onClick = { onDestinationSelected(destination) }
                        )
                    }
            }
            
            // Bottom section - Settings
            NavRailItem(
                destination = NavDestination.SETTINGS,
                isSelected = currentDestination == NavDestination.SETTINGS,
                luminousTeal = luminousTeal,
                electricMint = electricMint,
                glowColor = glowColor,
                onClick = { onDestinationSelected(NavDestination.SETTINGS) }
            )
        }
    }
}

@Composable
private fun NavRailItem(
    destination: NavDestination,
    isSelected: Boolean,
    luminousTeal: Color,
    electricMint: Color,
    glowColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Animations
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    val iconColor by animateColorAsState(
        targetValue = when {
            isSelected -> luminousTeal
            isHovered -> electricMint
            else -> KlarityColors.TextTertiary
        },
        animationSpec = tween(150),
        label = "iconColor"
    )
    
    val bgAlpha by animateFloatAsState(
        targetValue = when {
            isSelected -> 0.15f
            isHovered -> 0.1f
            else -> 0f
        },
        animationSpec = tween(150),
        label = "bgAlpha"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (isHovered && !isSelected) 1f else 0f,
        animationSpec = tween(150),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .size(56.dp) // Larger clickable area
            .scale(scale)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (isHovered || isSelected) {
                    Modifier.background(
                        color = luminousTeal.copy(alpha = bgAlpha),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else Modifier
            )
            .then(
                // Glow ring on hover
                if (glowAlpha > 0f) {
                    Modifier.border(
                        width = 1.5.dp,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                electricMint.copy(alpha = glowAlpha * 0.7f),
                                electricMint.copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = destination.icon,
                fontSize = 24.sp // Larger icons
            )
            
            // Show label only when selected or hovered
            if (isSelected) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = destination.label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = iconColor,
                    maxLines = 1
                )
            }
        }
        
        // Active indicator dot
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-2).dp)
                    .size(4.dp, 20.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                luminousTeal.copy(alpha = 0.3f),
                                luminousTeal,
                                luminousTeal.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
