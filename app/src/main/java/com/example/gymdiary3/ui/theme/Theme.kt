package com.example.gymdiary3.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = OwlColors.Crimson,
    onPrimary = OwlColors.TextPrimary,
    secondary = OwlColors.CrimsonSoft,
    tertiary = OwlColors.AmberWarn,
    background = OwlColors.DeepBg,
    surface = OwlColors.CardBg,
    onBackground = OwlColors.TextPrimary,
    onSurface = OwlColors.TextPrimary,
    error = OwlColors.RedNegative,
    onError = OwlColors.TextPrimary,
    primaryContainer = OwlColors.CardBg,
    onPrimaryContainer = OwlColors.TextPrimary,
    secondaryContainer = OwlColors.CardBgAlt,
    onSecondaryContainer = OwlColors.TextPrimary,
    tertiaryContainer = OwlColors.CardBgAlt,
    onTertiaryContainer = OwlColors.AmberWarn
)

val ApexShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun OwlFitnessTheme(content: @Composable () -> Unit) {
    val reducedMotion = reducedMotionFromSystem()
    CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            shapes = ApexShapes,
            content = content
        )
    }
}
