package com.example.klarity.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Klarity Shape System - Consistent corner radii across the app
 * Following Material 3 shape scale guidelines
 */
object KlarityShapes {
    /**
     * Extra Small: 4dp
     * Usage: Small badges, priority dots, chips
     */
    val ExtraSmall = RoundedCornerShape(4.dp)
    
    /**
     * Small: 8dp
     * Usage: Buttons, small cards, tags
     */
    val Small = RoundedCornerShape(8.dp)
    
    /**
     * Medium: 12dp
     * Usage: Cards, input fields, task cards, columns (default)
     */
    val Medium = RoundedCornerShape(12.dp)
    
    /**
     * Large: 16dp
     * Usage: Panels, navigation rail, large cards
     */
    val Large = RoundedCornerShape(16.dp)
    
    /**
     * Extra Large: 28dp
     * Usage: Modals, bottom sheets, dialogs (top corners only)
     */
    val ExtraLarge = RoundedCornerShape(28.dp)
    
    /**
     * Extra Large (Top Only): 28dp on top, 0dp on bottom
     * Usage: Bottom sheets, modal dialogs
     */
    val ExtraLargeTop = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    
    /**
     * Full: Circular/Pill shape
     * Usage: Avatar, floating action buttons, pill buttons
     */
    val Full = RoundedCornerShape(50)
}
