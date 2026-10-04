package com.example.gymdiary3.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.gymdiary3.ui.design.Gd

private val DarkColorScheme = darkColorScheme(
    primary = Gd.Accent,
    onPrimary = Gd.OnAccent,
    primaryContainer = Gd.SurfaceRaised,
    onPrimaryContainer = Gd.Text,
    secondary = Gd.TextMuted,
    onSecondary = Gd.Bg,
    // NavigationBar's selected-item indicator: a quiet neutral, not the accent.
    secondaryContainer = Gd.SurfaceRaised,
    onSecondaryContainer = Gd.Text,
    tertiary = Gd.Info,
    background = Gd.Bg,
    onBackground = Gd.Text,
    surface = Gd.Bg,
    onSurface = Gd.Text,
    surfaceVariant = Gd.Surface,
    onSurfaceVariant = Gd.TextMuted,
    surfaceContainerLowest = Gd.Bg,
    surfaceContainerLow = Gd.Surface,
    surfaceContainer = Gd.Surface,
    surfaceContainerHigh = Gd.SurfaceRaised,
    surfaceContainerHighest = Gd.SurfaceRaised,
    outline = Gd.BorderInput,
    outlineVariant = Gd.Border,
    error = Gd.Danger,
    onError = Gd.Bg,
)

private val GdShapes = Shapes(
    extraSmall = RoundedCornerShape(Gd.RadiusSm),
    small = RoundedCornerShape(Gd.RadiusSm),
    medium = RoundedCornerShape(Gd.RadiusMd),
    large = RoundedCornerShape(Gd.RadiusLg),
    extraLarge = RoundedCornerShape(Gd.RadiusLg),
)

@Composable
fun GymDiaryTheme(content: @Composable () -> Unit) {
    val reducedMotion = reducedMotionFromSystem()
    CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            shapes = GdShapes,
            content = content
        )
    }
}
