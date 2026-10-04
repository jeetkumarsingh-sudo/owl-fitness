package com.example.gymdiary3.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Confetto(
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val spin: Float,
    val drift: Float
)

/**
 * One-shot confetti burst for a new personal record. Place inside a Box over the
 * content; it does not intercept touches. Honors reduced motion by doing nothing
 * (pair it with a non-motion cue like the PR badge). Call [onFinished] to reset
 * the trigger.
 */
@Composable
fun PrCelebration(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {}
) {
    val reduced = LocalReducedMotion.current
    val progress = remember { Animatable(0f) }

    val palette = remember {
        listOf(Apex.AccentBright, Apex.Accent, Apex.Warning, Apex.Positive, Color.White)
    }
    val pieces = remember {
        List(90) {
            Confetto(
                angle = Random.nextFloat() * (2f * PI.toFloat()),
                speed = 0.6f + Random.nextFloat() * 0.9f,
                color = palette[Random.nextInt(palette.size)],
                size = 5f + Random.nextFloat() * 7f,
                spin = (Random.nextFloat() - 0.5f) * 720f,
                drift = (Random.nextFloat() - 0.5f) * 0.4f
            )
        }
    }

    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        if (reduced) { onFinished(); return@LaunchedEffect }
        progress.snapTo(0f)
        progress.animateTo(1f, tween(1500))
        onFinished()
    }

    if (!visible || reduced) return
    val t = progress.value

    Canvas(modifier = modifier.fillMaxSize()) {
        val origin = Offset(size.width / 2f, size.height * 0.34f)
        val spread = size.minDimension * 1.1f
        val gravity = size.height * 0.9f
        val alpha = (1f - ((t - 0.65f) / 0.35f)).coerceIn(0f, 1f)

        pieces.forEach { c ->
            val x = origin.x + cos(c.angle) * c.speed * spread * t + c.drift * size.width * t
            val y = origin.y + sin(c.angle) * c.speed * spread * t + gravity * t * t
            val s = c.size.dp.toPx()
            rotate(degrees = c.spin * t, pivot = Offset(x, y)) {
                drawRect(
                    color = c.color.copy(alpha = alpha),
                    topLeft = Offset(x - s / 2f, y - s / 2f),
                    size = Size(s, s * 0.5f)
                )
            }
        }
    }
}
