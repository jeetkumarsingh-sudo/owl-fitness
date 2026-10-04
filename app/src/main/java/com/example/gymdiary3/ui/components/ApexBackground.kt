package com.example.gymdiary3.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Atmospheric page background for the Apex design system.
 *
 * Near-black base + a soft crimson radial glow in the upper area + a bottom
 * vignette for depth. The glow "breathes" very slowly; it holds still when the
 * OS reduced-motion setting is on. Rendering is a couple of gradient draws in a
 * single [drawBehind], so it stays cheap on low-end devices.
 */
@Composable
fun ApexBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    glow: Color = Apex.AccentGlow,
    content: @Composable BoxScope.() -> Unit
) {
    val reduced = LocalReducedMotion.current
    val doAnimate = animated && !reduced

    val transition = rememberInfiniteTransition(label = "atmosphere")
    val breathe by if (doAnimate) {
        transition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(9000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breathe"
        )
    } else {
        androidx.compose.runtime.mutableFloatStateOf(1f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Base
                drawRect(Apex.Base)
                // Upper crimson glow, offset toward the top-right
                val gx = size.width * 0.82f
                val gy = size.height * 0.06f
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(glow, Color.Transparent),
                        center = Offset(gx, gy),
                        radius = size.maxDimension * 0.75f * breathe
                    )
                )
                // Soft secondary glow lower-left for layered depth
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(glow.copy(alpha = glow.alpha * 0.4f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.5f),
                        radius = size.maxDimension * 0.6f
                    )
                )
                // Bottom vignette
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                        startY = size.height * 0.55f,
                        endY = size.height
                    )
                )
            }
    ) {
        // Keep reduced-motion flag honored for descendants
        CompositionLocalProvider(LocalReducedMotion provides reduced) {
            content()
        }
    }
}
