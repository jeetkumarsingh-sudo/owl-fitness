package com.example.gymdiary3.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.example.gymdiary3.ui.theme.Motion

/**
 * Animated line chart — draws in left-to-right on first composition, with an area
 * fill, a faint baseline grid and an emphasized endpoint. Canvas-based so it is
 * cheap and theme-correct. Replaces the old alpha charting dependency.
 */
@Composable
fun ApexLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    lineColor: Color = Apex.Accent,
    gridLines: Int = 3
) {
    val reduced = LocalReducedMotion.current
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(values) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started || reduced) 1f else 0f,
        animationSpec = tween(if (reduced) 0 else Motion.draw, easing = Motion.emphasized),
        label = "chartDraw"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        if (values.size < 2) return@Canvas
        val maxV = values.max()
        val minV = values.min()
        val range = (maxV - minV).takeIf { it > 0f } ?: 1f
        val padY = size.height * 0.12f
        val usableH = size.height - padY * 2
        val stepX = size.width / (values.size - 1)

        fun pointAt(i: Int): Offset {
            val x = stepX * i
            val norm = (values[i] - minV) / range
            val y = padY + (1f - norm) * usableH
            return Offset(x, y)
        }

        // Faint grid
        for (g in 0..gridLines) {
            val y = padY + usableH * g / gridLines
            drawLine(
                color = Apex.Hairline,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        val linePath = Path()
        val fillPath = Path()
        for (i in values.indices) {
            val p = pointAt(i)
            if (i == 0) {
                linePath.moveTo(p.x, p.y)
                fillPath.moveTo(p.x, size.height - padY)
                fillPath.lineTo(p.x, p.y)
            } else {
                linePath.lineTo(p.x, p.y)
                fillPath.lineTo(p.x, p.y)
            }
        }
        fillPath.lineTo(pointAt(values.size - 1).x, size.height - padY)
        fillPath.close()

        clipRect(right = size.width * progress) {
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(lineColor.copy(alpha = 0.28f), Color.Transparent)
                )
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Endpoint dot fades/pops in as the draw completes
        val tip = pointAt(values.size - 1)
        drawCircle(color = lineColor, radius = 5.dp.toPx() * progress, center = tip)
        drawCircle(color = Color.White, radius = 2.dp.toPx() * progress, center = tip)
    }
}

/**
 * Animated bar chart — bars grow from the baseline. The last bar is highlighted
 * as "current".
 */
@Composable
fun ApexBars(
    values: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    highlightLast: Boolean = true
) {
    val reduced = LocalReducedMotion.current
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(values) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started || reduced) 1f else 0f,
        animationSpec = tween(if (reduced) 0 else Motion.draw, easing = Motion.emphasized),
        label = "barsDraw"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        if (values.isEmpty()) return@Canvas
        val maxV = values.max().takeIf { it > 0f } ?: 1f
        val gap = 6.dp.toPx()
        val barW = (size.width - gap * (values.size - 1)) / values.size
        values.forEachIndexed { i, v ->
            val h = (v / maxV) * size.height * progress
            val x = i * (barW + gap)
            val isLast = i == values.size - 1
            val color = if (highlightLast && isLast) Apex.AccentBright else Apex.AccentDeep
            drawRoundRectCompat(
                color = color,
                topLeft = Offset(x, size.height - h),
                size = Size(barW, h),
                radius = 4.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawRoundRectCompat(color: Color, topLeft: Offset, size: Size, radius: Float) {
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
    )
}

/** Optional min/max/value caption row to pair under a chart. */
@Composable
fun ChartCaption(leftLabel: String, rightLabel: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        androidx.compose.material3.Text(
            leftLabel,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = Apex.TextMuted
        )
        androidx.compose.material3.Text(
            rightLabel,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = Apex.TextMuted
        )
    }
}

/** Empty placeholder to keep chart areas from collapsing before data exists. */
@Composable
fun ChartEmpty(height: Dp = 200.dp) {
    Box(Modifier.fillMaxWidth().height(height)) {
        ShimmerBox(Modifier.fillMaxWidth().height(height))
    }
}
