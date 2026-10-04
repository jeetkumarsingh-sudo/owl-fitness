package com.example.gymdiary3.ui.design

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Gym Diary v2 design tokens. Values mirror replica/design/tokens.json, which is
 * checked for WCAG AA with replica-design's contrast.py — change both together.
 *
 * Restraint is the point: neutral surfaces and text carry the hierarchy; [Accent]
 * is reserved for the primary action, the active state, PRs and a selected chart
 * point. Elevation is expressed by surface colour, never by shadow or glow.
 */
object Gd {
    // Surfaces
    val Bg = Color(0xFF0B0B0C)
    val Surface = Color(0xFF141416)
    val SurfaceRaised = Color(0xFF1C1C1F)

    // Lines. Border/BorderStrong are decorative hairlines; BorderInput meets 3:1
    // and is the only one used to identify a control's boundary.
    val Border = Color(0xFF242427)
    val BorderStrong = Color(0xFF34343A)
    val BorderInput = Color(0xFF686870)

    // Text
    val Text = Color(0xFFF4F4F5)
    val TextMuted = Color(0xFFA1A1A8)
    val TextFaint = Color(0xFF85858D)

    // Accent: Accent fills (white text on it), AccentText is accent-coloured text on dark.
    val Accent = Color(0xFFE11D48)
    val AccentText = Color(0xFFFB6A85)
    val OnAccent = Color(0xFFFFFFFF)
    val AccentWash = Color(0x24E11D48) // ~14%: selected-point band, PR row highlight

    // Semantic — each colour means one thing.
    val Positive = Color(0xFF3CCB7F) // progressing, gains
    val Warning = Color(0xFFF2A33A)  // stalling, attention
    val Info = Color(0xFF6AB0F5)     // neutral information, stable
    val Danger = Color(0xFFF26464)   // regressing, destructive

    // Neutral data marks (bars/lines that are not the selection)
    val DataNeutral = Color(0xFF4A4A52)

    // Spacing (4dp base)
    val s1 = 4.dp
    val s2 = 8.dp
    val s3 = 12.dp
    val s4 = 16.dp
    val s5 = 20.dp
    val s6 = 24.dp
    val s8 = 32.dp
    val s10 = 40.dp
    val s14 = 56.dp

    // Layout
    val Gutter = 20.dp
    val RowMin = 56.dp
    val TouchMin = 48.dp
    val ButtonHeight = 52.dp

    // Radius
    val RadiusSm = 6.dp
    val RadiusMd = 10.dp
    val RadiusLg = 14.dp
}

/** Motion durations (ms) and easing. Call sites check LocalReducedMotion first. */
object GdMotion {
    const val Fast = 120
    const val Base = 200
    const val Slow = 300
    const val Chart = 450
    val Ease: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
