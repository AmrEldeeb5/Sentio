package com.example.klarity.presentation.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.klarity.presentation.theme.KlarityColors

/**
 * Top Bar - Main navigation bar with view mode toggles and action buttons
 */
@Composable
fun TopBar(
    showContextSidebar: Boolean = true,
    onToggleContextSidebar: () -> Unit = {}
) {
    var selectedViewMode by remember { mutableStateOf("Tri-Pane") }

    Surface(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        color = KlarityColors.BgSecondary,
        border = BorderStroke(1.dp, KlarityColors.BorderPrimary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Klarity Logo (simplified SVG-like shape)
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("K", color = KlarityColors.AccentAI, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Text(
                    "Klarity",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            }

            // View Mode Toggles
            Row(
                modifier = Modifier
                    .background(KlarityColors.BgElevated, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ViewModeButton("Tri-Pane", "⫏", selectedViewMode == "Tri-Pane") { selectedViewMode = "Tri-Pane" }
                ViewModeButton("Dual-Pane", "⫐", selectedViewMode == "Dual-Pane") { selectedViewMode = "Dual-Pane" }
                ViewModeButton("Single-Pane", "▢", selectedViewMode == "Single-Pane") { selectedViewMode = "Single-Pane" }
                ViewModeButton("Zen Mode", "⛶", selectedViewMode == "Zen Mode") { selectedViewMode = "Zen Mode" }
            }

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Share Button
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = KlarityColors.AccentAI),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text("Share", color = KlarityColors.BgPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Icon Buttons
                IconActionButton("↗")
                IconActionButton(
                    icon = "✨",
                    isActive = showContextSidebar,
                    onClick = onToggleContextSidebar
                )
            }
        }
    }
}
