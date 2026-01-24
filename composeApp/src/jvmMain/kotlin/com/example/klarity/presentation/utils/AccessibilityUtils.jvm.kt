package com.example.klarity.presentation.utils

import androidx.compose.runtime.Composable

/**
 * Desktop (JVM) implementation of shouldReduceMotion
 * Checks Java system properties for accessibility settings
 */
@Composable
actual fun shouldReduceMotion(): Boolean {
    return try {
        // Check Java accessibility properties
        // On Windows: reads from Windows Settings > Accessibility > Visual effects
        val property = System.getProperty("javax.accessibility.assistive_technologies")
        
        // If accessibility tools are active, assume reduced motion preference
        // This is a conservative approach - better implementations would read OS-specific settings
        property != null && property.isNotBlank()
    } catch (e: Exception) {
        false // Default to allowing animations
    }
}
