package com.example.gymdiary3.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Apex Fitness design system — "Carbon & Crimson" (refined).
 *
 * A single, always-dark theme built on a layered carbon elevation ramp with a
 * punchy rose-crimson brand accent. Surfaces get lighter as they rise; the accent
 * is reserved for interactive/brand moments so data stays the hero.
 */
object OwlColors {
    // --- Backgrounds: carbon elevation ramp (page -> card -> elevated) ---
    val DeepBg      = Color(0xFF0A0A0C)   // near-black page background
    val CardBg      = Color(0xFF151519)   // primary surface / card
    val CardBgAlt   = Color(0xFF1F1F26)   // elevated / inner surface
    val InputBg     = Color(0xFF1F1F26)   // text-field background

    // --- Brand accent: rose-crimson family ---
    val Crimson      = Color(0xFFF43F5E)  // primary brand accent (Rose 500)
    val CrimsonSoft  = Color(0xFFFB7185)  // lighter highlight (Rose 400)
    val CrimsonDim   = Color(0xFF7F1D2E)  // deep muted (disabled / inactive)
    val CrimsonGlow  = Color(0x33F43F5E)  // 20% accent for glows / indicator fills

    // Gradient endpoints for hero surfaces (buttons, accents)
    val CrimsonGradTop = Color(0xFFFF5470)
    val CrimsonGradBot = Color(0xFFE11D48)

    // Subtle surface gradient for elevated cards
    val CardGradTop = Color(0xFF1A1A20)
    val CardGradBot = Color(0xFF131317)

    // --- Semantic ---
    val GreenPositive = Color(0xFF34D399) // gains / success (Emerald 400)
    val GreenBulk     = Color(0xFF34D399) // desirable weight gain / completed
    val AmberWarn     = Color(0xFFFBBF24) // warnings (Amber 400)
    val RedNegative   = Color(0xFFF87171) // regression / error (Red 400)

    // --- Text tiers (softened off-white for reduced glare on OLED) ---
    val TextPrimary   = Color(0xFFF4F4F6) // primary text
    val TextSecondary = Color(0xFF9A9AA6) // secondary text
    val TextMuted     = Color(0xFF64646F) // muted / captions

    // --- Borders / hairlines ---
    val BorderSubtle  = Color(0xFF26262E) // resting hairline
    val BorderActive  = Color(0xFF3A3A46) // hover / active hairline
}
