package com.example.gymdiary3.ui.theme

import androidx.compose.ui.graphics.Color

object OwlColors {
    // Apex Fitness Design System - Carbon & Crimson (Palette 1)
    val DeepBg      = Color(0xFF000000)   // True OLED black page background
    val CardBg      = Color(0xFF18181B)   // Zinc 900 main surface
    val CardBgAlt   = Color(0xFF27272A)   // Zinc 800 elevated / inner card
    val InputBg     = Color(0xFF27272A)   // Text field background

    val Crimson      = Color(0xFFE11D48)   // Crimson red primary brand accent
    val CrimsonSoft  = Color(0xFFF43F5E)   // Rose red secondary/highlights
    val CrimsonDim   = Color(0xFF9F1239)   // Darker red for inactive borders/backgrounds (Rose 800)

    val GreenPositive = Color(0xFF10B981) // Emerald green success/gains
    val AmberWarn     = Color(0xFFF59E0B) // Amber warnings
    val RedNegative   = Color(0xFFEF4444) // Red error/regression

    val TextPrimary   = Color(0xFFFFFFFF) // Pure white text
    val TextSecondary = Color(0xFFA1A1AA) // Zinc 400 secondary text
    val TextMuted     = Color(0xFF71717A) // Zinc 500 muted text
    val BorderSubtle  = Color(0xFF27272A) // Very subtle borders
    val BorderActive  = Color(0xFF3F3F46) // Slightly lighter for active elements

    // --- Compatibility bridge: legacy "Purple" accent names used across screens.
    // The palette was migrated to Carbon & Crimson but 77 call sites still reference
    // these names. Alias them to the crimson accent so the app compiles and the
    // accent stays consistent. (To be consolidated in the design-system pass.)
    val Purple     = Crimson      // primary accent
    val PurpleSoft = CrimsonSoft  // secondary/highlight accent
    val PurpleDim  = CrimsonDim   // dim/inactive accent
    val GreenBulk  = GreenPositive // legacy name for positive/bulk green
}
