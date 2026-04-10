package com.example.gymdiary3.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = Color(0xFF9E9E9E),
    tertiary = PRGold,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    error = ErrorRed,
    onError = Color.White,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = PrimaryText,
    secondaryContainer = Color(0xFF2C2C3E),
    onSecondaryContainer = Color.White,
    tertiaryContainer = Color(0xFF322E1E),
    onTertiaryContainer = PRGold
)

@Composable
fun OwlFitnessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
