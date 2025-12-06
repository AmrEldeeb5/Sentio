package com.example.klarity.presentation.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityColors

/**
 * Common UI components used across the home screen
 */

@Composable
fun ViewModeButton(
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = Modifier
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(6.dp),
        color = when {
            isSelected -> KlarityColors.BgSecondary
            isHovered -> Color.White.copy(alpha = 0.05f)
            else -> Color.Transparent
        },
        border = if (isSelected) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 12.sp, color = if (isSelected) Color.White else KlarityColors.TextTertiary)
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else KlarityColors.TextTertiary
            )
        }
    }
}

@Composable
fun IconActionButton(
    icon: String,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = Modifier
            .size(36.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(8.dp),
        color = when {
            isActive -> KlarityColors.AccentAI.copy(alpha = 0.2f)
            isHovered -> KlarityColors.BgElevated
            else -> KlarityColors.BgElevated
        },
        border = BorderStroke(1.dp, if (isActive) KlarityColors.AccentAI.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 16.sp, color = if (isActive) KlarityColors.AccentAI else Color.White)
        }
    }
}

@Composable
fun SmallIconButton(icon: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = Modifier
            .size(24.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .hoverable(interactionSource),
        shape = RoundedCornerShape(4.dp),
        color = if (isHovered) KlarityColors.BgElevated else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 14.sp, color = KlarityColors.TextTertiary)
        }
    }
}

@Composable
fun NavItem(
    icon: String,
    label: String,
    shortcut: String? = null,
    isActive: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) { }
            .hoverable(interactionSource),
        shape = RoundedCornerShape(8.dp),
        color = when {
            isActive -> KlarityColors.BgElevated
            isHovered -> KlarityColors.BgElevated.copy(alpha = 0.8f)
            else -> Color.Transparent
        },
        border = if (isActive) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    icon,
                    fontSize = 18.sp,
                    color = if (isActive) KlarityColors.AccentAI else KlarityColors.TextTertiary
                )
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive || isHovered) Color.White else KlarityColors.TextSecondary
                )
            }
            if (shortcut != null && (isActive || isHovered)) {
                Text(
                    shortcut,
                    fontSize = 10.sp,
                    color = KlarityColors.TextTertiary
                )
            }
        }
    }
}

