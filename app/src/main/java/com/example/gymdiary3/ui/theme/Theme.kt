package com.example.gymdiary3.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    background = BackgroundDark,
    surface = CardDark,
    onPrimary = PrimaryText,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    secondary = SecondaryText,
    onSurfaceVariant = SecondaryText
)

@Composable
fun GymDiaryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
