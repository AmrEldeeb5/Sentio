package com.example.sentio.presentation.screen.home

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
import com.example.sentio.presentation.theme.KlarityColors

/**
 * Slash Menu - Floating command menu for quick actions
 */
@Composable
fun SlashMenu(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.width(320.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1A342F),
        border = BorderStroke(1.dp, KlarityColors.BorderPrimary),
        shadowElevation = 16.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                "COMMANDS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = KlarityColors.TextTertiary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )

            SlashMenuItem(
                icon = "✨",
                title = "Ask AI",
                subtitle = "Generate content with AI",
                isActive = true
            )
            SlashMenuItem(
                icon = "☑",
                title = "Task",
                subtitle = "Create a new task item"
            )
            SlashMenuItem(
                icon = "</>",
                title = "Code Block",
                subtitle = "Add a formatted code block"
            )
            SlashMenuItem(
                icon = "🔗",
                title = "Wiki Link",
                subtitle = "Link to another note [[note-name]]"
            )
            SlashMenuItem(
                icon = "📝",
                title = "Heading",
                subtitle = "Add a section heading"
            )
            SlashMenuItem(
                icon = "•",
                title = "Bullet List",
                subtitle = "Create a bulleted list"
            )
        }
    }
}

@Composable
fun SlashMenuItem(
    icon: String,
    title: String,
    subtitle: String,
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
        color = if (isActive || isHovered) Color.White.copy(alpha = 0.05f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(6.dp),
                color = if (isActive) KlarityColors.AccentAI.copy(alpha = 0.1f) else Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        icon,
                        fontSize = 16.sp,
                        color = if (isActive) KlarityColors.AccentAI else KlarityColors.TextTertiary
                    )
                }
            }
            Column {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) Color.White else KlarityColors.TextSecondary
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = KlarityColors.TextTertiary
                )
            }
        }
    }
}

