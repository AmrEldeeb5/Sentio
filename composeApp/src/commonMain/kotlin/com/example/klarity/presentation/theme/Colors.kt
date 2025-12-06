package com.example.klarity.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Klarity color palette - Dark teal theme matching the UI mockups.
 * Beautiful dark green/teal aesthetic with bright cyan accents.
 */
object KlarityColors {
    // ══════════════════════════════════════════════════════════════
    // BACKGROUNDS - Dark Teal/Green Palette with Elevation
    // Panel Hierarchy: NavRail (darkest) → NotesList → Editor (lightest)
    // ══════════════════════════════════════════════════════════════
    
    /** Darkest background - navigation rail, deepest panels */
    val BgPrimary = Color(0xFF0A1514)
    
    /** Secondary background - notes list panel */
    val BgSecondary = Color(0xFF0E1C1A)
    
    /** Tertiary background - cards, elevated surfaces */
    val BgTertiary = Color(0xFF142420)
    
    /** Editor panel background - slightly lighter for focus */
    val BgEditor = Color(0xFF121F1D)
    
    /** Elevated surfaces - hover states, active cards */
    val BgElevated = Color(0xFF1A302B)
    
    /** Card backgrounds with subtle transparency */
    val BgCard = Color(0xFF162B26)
    
    /** Note list item background - breathable cards */
    val BgNoteCard = Color(0xFF111E1B)

    /** Selected/Active item background */
    val BgSelected = Color(0xFF1F3D38)
    
    /** Selected item border - bright cyan */
    val BorderSelected = Color(0xFF1FDBC8)

    /** Code block background */
    val BgCode = Color(0xFF0A1614)
    
    /** Pill button background - for Sort/Group/Filter buttons */
    val BgPill = Color(0xFF1A2A27)

    // ══════════════════════════════════════════════════════════════
    // ACCENT COLORS - Bright Cyan/Teal
    // ══════════════════════════════════════════════════════════════
    
    /** Primary accent - buttons, links, active states */
    val AccentPrimary = Color(0xFF3DD68C)
    
    /** Secondary accent - hover states */
    val AccentSecondary = Color(0xFF2FB874)
    
    /** Tertiary accent - subtle highlights */
    val AccentTertiary = Color(0xFF25A066)
    
    /** AI-specific accent - AI features, suggestions */
    val AccentAI = Color(0xFF2DD4BF)
    
    /** AI glow effect */
    val AccentAIGlow = Color(0xFF2DD4BF).copy(alpha = 0.2f)
    
    /** Highlight color for text selection, AI suggestions */
    val Highlight = Color(0xFF2DD4BF).copy(alpha = 0.3f)

    // ══════════════════════════════════════════════════════════════
    // TEXT COLORS - Improved Contrast
    // ══════════════════════════════════════════════════════════════
    
    /** Primary text - headings, important content */
    val TextPrimary = Color(0xFFE8F0ED)
    
    /** Secondary text - body text, descriptions */
    val TextSecondary = Color(0xFFB0C4BC)
    
    /** Tertiary text - timestamps, hints (improved contrast) */
    val TextTertiary = Color(0xFF7FA598)
    
    /** Muted text - less important hints */
    val TextMuted = Color(0xFF5C7A6E)
    
    /** Disabled text */
    val TextDisabled = Color(0xFF4A6358)
    
    /** Link text color */
    val TextLink = AccentPrimary

    // ══════════════════════════════════════════════════════════════
    // BORDER COLORS
    // ══════════════════════════════════════════════════════════════
    
    /** Primary border - cards, inputs */
    val BorderPrimary = Color(0xFF1F3D35)

    /** Secondary border - subtle dividers */
    val BorderSecondary = Color(0xFF2A4F45)
    
    /** Focus border - focused inputs */
    val BorderFocus = AccentPrimary
    
    /** Dashed border for drop zones */
    val BorderDashed = Color(0xFF3D5A50)

    // ══════════════════════════════════════════════════════════════
    // SEMANTIC COLORS
    // ══════════════════════════════════════════════════════════════
    
    /** Success - completed tasks, saved states */
    val Success = Color(0xFF3DD68C)
    
    /** Warning - due soon, attention needed */
    val Warning = Color(0xFFFFC107)
    
    /** Error - overdue, failed states */
    val Error = Color(0xFFFF6B6B)
    
    /** Info - informational messages */
    val Info = Color(0xFF2DD4BF)

    // ══════════════════════════════════════════════════════════════
    // PRIORITY COLORS (for tasks/kanban)
    // ══════════════════════════════════════════════════════════════
    
    /** High priority - red dot */
    val PriorityHigh = Color(0xFFFF6B6B)
    
    /** Medium priority - yellow dot */
    val PriorityMedium = Color(0xFFFFC107)
    
    /** Low priority - blue dot */
    val PriorityLow = Color(0xFF4ECDC4)
    
    /** No priority - gray dot */
    val PriorityNone = Color(0xFF6B8A7D)

    // ══════════════════════════════════════════════════════════════
    // TAG COLORS
    // ══════════════════════════════════════════════════════════════
    
    val TagResearch = Color(0xFF4ECDC4)
    val TagUIDesign = Color(0xFFFF6B9D)
    val TagBackend = Color(0xFF3DD68C)
    val TagMarketing = Color(0xFFFFB347)
    val TagAnalysis = Color(0xFF9B59B6)
    val TagHighEffort = Color(0xFFE74C3C)

    // ══════════════════════════════════════════════════════════════
    // OVERLAYS & MODALS
    // ══════════════════════════════════════════════════════════════
    
    /** Modal overlay - 50% opacity */
    val ModalOverlay = Color(0x800D1B1A)
    
    /** Light overlay - 25% opacity */
    val OverlayLight = Color(0x400D1B1A)

    // ══════════════════════════════════════════════════════════════
    // BUTTON COLORS
    // ══════════════════════════════════════════════════════════════
    
    /** Primary button background */
    val ButtonPrimary = AccentPrimary
    
    /** Primary button text */
    val ButtonPrimaryText = Color(0xFF0D1B1A)
    
    /** Secondary button background */
    val ButtonSecondary = Color(0xFF1A3530)
    
    /** Secondary button text */
    val ButtonSecondaryText = TextPrimary
    
    /** Ghost button (transparent) */
    val ButtonGhost = Color.Transparent
    
    /** Ghost button text */
    val ButtonGhostText = AccentPrimary

    // ══════════════════════════════════════════════════════════════
    // SIDEBAR COLORS
    // ══════════════════════════════════════════════════════════════
    
    /** Sidebar background */
    val SidebarBg = BgSecondary
    
    /** Sidebar item hover */
    val SidebarItemHover = Color(0xFF1A3530)
    
    /** Sidebar item active */
    val SidebarItemActive = AccentPrimary
    
    /** Sidebar section header */
    val SidebarSectionHeader = TextTertiary

    // ══════════════════════════════════════════════════════════════
    // EDITOR COLORS
    // ══════════════════════════════════════════════════════════════
    
    /** Editor background */
    val EditorBg = BgPrimary
    
    /** Editor cursor */
    val EditorCursor = AccentPrimary
    
    /** Editor selection */
    val EditorSelection = AccentPrimary.copy(alpha = 0.3f)
    
    /** Editor line highlight */
    val EditorLineHighlight = Color(0xFF152922)

    // ══════════════════════════════════════════════════════════════
    // SYNTAX HIGHLIGHTING
    // ══════════════════════════════════════════════════════════════
    
    val SyntaxKeyword = Color(0xFFFF79C6)
    val SyntaxString = Color(0xFFF1FA8C)
    val SyntaxComment = Color(0xFF6B8A7D)
    val SyntaxFunction = Color(0xFF3DD68C)
    val SyntaxNumber = Color(0xFFBD93F9)
    val SyntaxOperator = Color(0xFFFF6B6B)
    val SyntaxVariable = Color(0xFF8BE9FD)
    val SyntaxType = Color(0xFF4ECDC4)

    // ══════════════════════════════════════════════════════════════
    // TIMER COLORS (for task timers)
    // ══════════════════════════════════════════════════════════════
    
    val TimerBg = Color(0xFF2DD4BF).copy(alpha = 0.15f)
    val TimerText = Color(0xFF2DD4BF)
    val TimerActive = Color(0xFF3DD68C)

    // ══════════════════════════════════════════════════════════════
    // SWITCH/TOGGLE COLORS
    // ══════════════════════════════════════════════════════════════
    
    val SwitchTrackOn = AccentPrimary
    val SwitchTrackOff = Color(0xFF2A4F45)
    val SwitchThumb = Color(0xFFE8F0ED)

    // ══════════════════════════════════════════════════════════════
    // SCROLLBAR COLORS
    // ══════════════════════════════════════════════════════════════
    
    val ScrollbarTrack = Color.Transparent
    val ScrollbarThumb = Color(0xFF2A4F45)
    val ScrollbarThumbHover = Color(0xFF3D5A50)
}

/**
 * Helper for gradient colors.
 */
data class GradientColors(
    val start: Color,
    val end: Color
)

val AccentGradient = GradientColors(
    start = KlarityColors.AccentPrimary,
    end = KlarityColors.AccentSecondary
)

val AIGradient = GradientColors(
    start = KlarityColors.AccentAI,
    end = Color(0xFF764BA2)
)

val CardGradient = GradientColors(
    start = KlarityColors.BgCard,
    end = KlarityColors.BgTertiary
)


