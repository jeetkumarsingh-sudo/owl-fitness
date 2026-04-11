package com.example.gymdiary3.ui.theme

import androidx.compose.ui.graphics.Color

object OwlColors {
    val DeepBg      = Color(0xFF0A0A15)   // page background
    val CardBg      = Color(0xFF13131F)   // card surface
    val CardBgAlt   = Color(0xFF1A1A2E)   // elevated / inner card
    val InputBg     = Color(0xFF1E1E32)   // text field background

    val Purple      = Color(0xFF7B68EE)   // primary accent
    val PurpleSoft  = Color(0xFF9D8FFF)   // secondary labels
    val PurpleDim   = Color(0xFF3D3570)   // inactive borders

    // IMPORTANT: user is bulking — weight gain is GOOD
    val GreenPositive = Color(0xFF4CAF93) // gains, progress up (teal-green, not harsh)
    val GreenBulk     = Color(0xFF56C596) // body weight increase = good for bulking
    val RedNegative   = Color(0xFFE05C6C) // regression, loss
    val AmberWarn     = Color(0xFFF0A500) // warnings, "no progress" flags

    val TextPrimary   = Color(0xFFEEEEFF)
    val TextSecondary = Color(0xFF9090B0)
    val TextMuted     = Color(0xFF5A5A7A)
    val BorderSubtle  = Color(0xFF252538)
    val BorderActive  = Color(0xFF4A4870)
}
