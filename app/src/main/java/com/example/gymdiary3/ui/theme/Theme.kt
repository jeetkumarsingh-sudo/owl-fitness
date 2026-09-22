package com.example.gymdiary3.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = OwlColors.Crimson,
    onPrimary = OwlColors.TextPrimary,
    primaryContainer = OwlColors.CrimsonDim,
    onPrimaryContainer = OwlColors.CrimsonSoft,

    secondary = OwlColors.CrimsonSoft,
    onSecondary = OwlColors.TextPrimary,
    secondaryContainer = OwlColors.CardBgAlt,
    onSecondaryContainer = OwlColors.TextPrimary,

    tertiary = OwlColors.AmberWarn,
    onTertiary = OwlColors.DeepBg,
    tertiaryContainer = OwlColors.CardBgAlt,
    onTertiaryContainer = OwlColors.AmberWarn,

    background = OwlColors.DeepBg,
    onBackground = OwlColors.TextPrimary,

    surface = OwlColors.CardBg,
    onSurface = OwlColors.TextPrimary,
    surfaceVariant = OwlColors.CardBgAlt,
    onSurfaceVariant = OwlColors.TextSecondary,

    surfaceContainerLowest = OwlColors.DeepBg,
    surfaceContainerLow = OwlColors.CardBg,
    surfaceContainer = OwlColors.CardBg,
    surfaceContainerHigh = OwlColors.CardBgAlt,
    surfaceContainerHighest = OwlColors.CardBgAlt,

    outline = OwlColors.BorderActive,
    outlineVariant = OwlColors.BorderSubtle,

    error = OwlColors.RedNegative,
    onError = OwlColors.TextPrimary,
)

val ApexShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun OwlFitnessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = ApexShapes,
        content = content
    )
}
