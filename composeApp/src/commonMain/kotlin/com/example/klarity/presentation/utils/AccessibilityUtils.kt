package com.example.klarity.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Accessibility utilities for Klarity app
 * Provides helpers for motion preferences, text sizing, and a11y features
 */

/**
 * Composable that checks if the user prefers reduced motion
 * Returns true if animations should be disabled or minimized
 * 
 * Usage:
 * ```kotlin
 * val shouldReduceMotion = shouldReduceMotion()
 * animationSpec = if (shouldReduceMotion) snap() else KlarityMotion.standardEnter()
 * ```
 */
@Composable
expect fun shouldReduceMotion(): Boolean

/**
 * Minimum touch target size in DP following Material 3 guidelines
 * Ensures all interactive elements meet accessibility standards
 */
const val MIN_TOUCH_TARGET_DP = 48

/**
 * Minimum text contrast ratio for WCAG AA compliance
 * - Normal text: 4.5:1
 * - Large text (18pt+): 3:1
 */
const val WCAG_AA_NORMAL_CONTRAST = 4.5f
const val WCAG_AA_LARGE_CONTRAST = 3.0f

/**
 * Recommended line height multiplier for body text
 * WCAG Success Criterion 1.4.12 (Text Spacing)
 */
const val RECOMMENDED_LINE_HEIGHT_MULTIPLIER = 1.75f

/**
 * Maximum line length in characters for optimal readability
 * Best practice: 65-75 characters per line
 */
const val MAX_LINE_LENGTH_CHARS = 75
