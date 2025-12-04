package com.example.sentio.presentation.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sentio.presentation.theme.SentioColors

/**
 * AI Toolbelt Sidebar - Left sidebar with AI actions
 */
@Composable
fun AIToolbeltSidebar() {
    Surface(
        modifier = Modifier.width(240.dp).fillMaxHeight(),
        color = SentioColors.BgSecondary,
        border = BorderStroke(1.dp, SentioColors.BorderPrimary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Header with Avatar
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar placeholder
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = SentioColors.BgElevated,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🤖", fontSize = 20.sp)
                        }
                    }
                    Column {
                        Text(
                            "AI Toolbelt",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Sentio Assistant",
                            color = SentioColors.TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Navigation Items
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    NavItem(icon = "≡", label = "Summarize", shortcut = "⌘⇧S", isActive = true)
                    NavItem(icon = "☑", label = "Suggest Tasks", shortcut = "⌘⇧T")
                    NavItem(icon = "🔗", label = "Find Links", shortcut = "⌘⇧L")

                    // Show more button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("⋯", fontSize = 18.sp, color = SentioColors.TextTertiary)
                        Text(
                            "Show more AI Actions...",
                            fontSize = 14.sp,
                            color = SentioColors.TextTertiary
                        )
                    }
                }
            }

            // Bottom Section
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                NavItem(icon = "⚙", label = "Settings")

                // User Profile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = SentioColors.BgElevated,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👤", fontSize = 12.sp)
                        }
                    }
                    Text("User Profile", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}

