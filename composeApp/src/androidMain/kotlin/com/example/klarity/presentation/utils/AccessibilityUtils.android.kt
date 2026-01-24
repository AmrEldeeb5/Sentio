package com.example.klarity.presentation.utils

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of shouldReduceMotion
 * Checks system animation settings via Settings.Global
 */
@Composable
actual fun shouldReduceMotion(): Boolean {
    val context = LocalContext.current
    
    return try {
        // Check if animations are disabled at system level
        val animationScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )
        
        val windowAnimationScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.WINDOW_ANIMATION_SCALE,
            1f
        )
        
        // If either scale is 0 or very low, user prefers reduced motion
        animationScale < 0.1f || windowAnimationScale < 0.1f
    } catch (e: Exception) {
        false // Default to allowing animations if we can't read settings
    }
}
                )
            }
        }
    }
}

/**
 * Android-specific composable to get context for motion preferences
 */
@Composable
actual fun shouldReduceMotion(): Boolean {
    val context = LocalContext.current
    val preferences = MotionPreferences(context)
    return preferences.prefersReducedMotion()
}
