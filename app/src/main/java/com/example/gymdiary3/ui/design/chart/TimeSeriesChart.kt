package com.example.gymdiary3.ui.design.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.gymdiary3.ui.design.Gd
import com.example.gymdiary3.ui.design.GdMotion
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/** One observation. [lines] is what the tooltip shows, e.g. "15 kg × 10", "Est. 1RM 20 kg". */
data class ChartPoint(val time: Long, val value: Double, val lines: List<String> = emptyList())

enum class ChartKind { Line, Bars }

/**
 * A time-scaled chart that says what it shows: a titled value axis with units,
 * date ticks along a real time axis, straight segments between real data points,
 * and tap-or-drag inspection of any point. Bars always start at zero; lines get a
 * minimum span ([minSpan]) so small changes are not visually inflated.
 */
@Composable
fun TimeSeriesChart(
    points: List<ChartPoint>,
    rangeStart: Long,
    rangeEnd: Long,
    yAxisTitle: String,
    formatTick: (Double) -> String,
    minSpan: Double,
    summary: String,
    modifier: Modifier = Modifier,
    kind: ChartKind = ChartKind.Line,
    xAxisTitle: String? = null,
    height: Dp = 200.dp,
    emptyText: String = "No sessions in this range"
) {
    val reduced = LocalReducedMotion.current
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    val sorted = remember(points) { points.sortedBy { it.time } }
    var selected by remember(sorted, rangeStart, rangeEnd) { mutableStateOf<Int?>(null) }

    val progress = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(sorted, rangeStart, rangeEnd, reduced) {
        if (reduced) progress.snapTo(1f) else {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(GdMotion.Chart, easing = GdMotion.Ease))
        }
    }

    val scale = remember(sorted, minSpan, kind) {
        ChartScale.valueScale(sorted.map { it.value }, minSpan, includeZero = kind == ChartKind.Bars)
    }
    val spanDays = (rangeEnd - rangeStart) / ChartScale.DAY_MS
    val tickFormat = remember(spanDays) {
        SimpleDateFormat(if (spanDays > 400) "MMM yy" else "MMM d", Locale.getDefault())
    }
    val tipFormat = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }

    val tickStyle = GdType.metaNum.copy(color = Gd.TextFaint)
    val yLabels = scale.ticks.map { formatTick(it) }

    Column(modifier.fillMaxWidth().semantics { contentDescription = summary }) {
        Text(yAxisTitle, style = GdType.meta, color = Gd.TextMuted)
        Spacer(Modifier.height(6.dp))

        if (sorted.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().height(height),
                contentAlignment = Alignment.CenterStart
            ) { Text(emptyText, style = GdType.label, color = Gd.TextMuted) }
            return@Column
        }

        BoxWithConstraints(Modifier.fillMaxWidth().height(height)) {
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val yLabelWidth = yLabels.maxOf { measurer.measure(it, tickStyle).size.width }
            val xLabelHeight = measurer.measure("Aug 1", tickStyle).size.height
            val gap = with(density) { 6.dp.toPx() }
            val left = yLabelWidth + gap * 1.5f
            val right = widthPx - gap
            val top = gap
            val bottom = heightPx - xLabelHeight - gap
            val plotW = right - left
            val plotH = bottom - top

            fun xOf(t: Long) = left + ChartScale.fraction(t, rangeStart, rangeEnd) * plotW
            fun yOf(v: Double) = (bottom - ((v - scale.min) / scale.span) * plotH).toFloat()
            fun nearest(px: Float): Int = sorted.indices.minBy { abs(xOf(sorted[it].time) - px) }

            Canvas(
                Modifier
                    .fillMaxSize()
                    .pointerInput(sorted, rangeStart, rangeEnd) {
                        detectTapGestures { pos ->
                            val i = nearest(pos.x)
                            selected = if (selected == i) null else i
                        }
                    }
                    .pointerInput(sorted, rangeStart, rangeEnd) {
                        detectHorizontalDragGestures(
                            onDragStart = { selected = nearest(it.x) },
                            onHorizontalDrag = { change, _ -> selected = nearest(change.position.x) }
                        )
                    }
            ) {
                // Grid: one faint line per tick; the baseline a step stronger.
                scale.ticks.forEachIndexed { i, tick ->
                    val y = yOf(tick)
                    drawLine(
                        color = if (i == 0) Gd.BorderStrong else Gd.Border,
                        start = Offset(left, y), end = Offset(right, y), strokeWidth = 1f
                    )
                    val layout = measurer.measure(yLabels[i], tickStyle)
                    drawText(
                        layout,
                        topLeft = Offset(left - gap - layout.size.width, y - layout.size.height / 2f)
                    )
                }
                // Date ticks along the real time axis.
                ChartScale.timeTicks(rangeStart, rangeEnd, 4).forEach { t ->
                    val layout = measurer.measure(tickFormat.format(Date(t)), tickStyle)
                    val x = (xOf(t) - layout.size.width / 2f)
                        .coerceIn(left, right - layout.size.width)
                    drawText(layout, topLeft = Offset(x, bottom + gap * 0.8f))
                }

                val sel = selected
                if (sel != null) {
                    val sx = xOf(sorted[sel].time)
                    drawLine(Gd.BorderStrong, Offset(sx, top), Offset(sx, bottom), strokeWidth = 1.5f)
                }

                when (kind) {
                    ChartKind.Line -> {
                        if (sorted.size >= 2) {
                            val path = Path()
                            sorted.forEachIndexed { i, p ->
                                val x = xOf(p.time); val y = yOf(p.value)
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            clipRect(right = left + plotW * progress.value + 4f) {
                                drawPath(
                                    path, color = Gd.Text,
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                        val dotR = if (sorted.size > 40) 0f else 2.5.dp.toPx()
                        sorted.forEachIndexed { i, p ->
                            val x = xOf(p.time)
                            if (x > left + plotW * progress.value + 4f) return@forEachIndexed
                            val last = i == sorted.lastIndex
                            if (last || dotR > 0f) {
                                drawCircle(
                                    color = if (last) Gd.Text else Gd.TextMuted,
                                    radius = if (last) 4.dp.toPx() else dotR,
                                    center = Offset(x, yOf(p.value))
                                )
                            }
                        }
                    }
                    ChartKind.Bars -> {
                        val xs = sorted.map { xOf(it.time) }
                        val minGap = if (xs.size < 2) plotW / 4f
                        else xs.zipWithNext { a, b -> b - a }.filter { it > 0f }.minOrNull() ?: (plotW / 4f)
                        val barW = (minGap * 0.6f).coerceIn(3.dp.toPx(), 18.dp.toPx())
                        sorted.forEachIndexed { i, p ->
                            val h = (bottom - yOf(p.value)) * progress.value
                            val x = (xs[i] - barW / 2f).coerceIn(left, right - barW)
                            drawRoundRect(
                                color = when {
                                    i == sel -> Gd.Accent
                                    i == sorted.lastIndex -> Gd.TextMuted
                                    else -> Gd.DataNeutral
                                },
                                topLeft = Offset(x, bottom - h),
                                size = Size(barW, h),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                            )
                        }
                    }
                }

                if (sel != null && kind == ChartKind.Line) {
                    val c = Offset(xOf(sorted[sel].time), yOf(sorted[sel].value))
                    drawCircle(Gd.Bg, radius = 7.dp.toPx(), center = c)
                    drawCircle(Gd.Accent, radius = 5.dp.toPx(), center = c)
                }
            }

            // Tooltip for the selected point, clamped inside the chart.
            val sel = selected
            if (sel != null) {
                val p = sorted[sel]
                var tipWidth by remember { mutableIntStateOf(0) }
                val anchor = xOf(p.time)
                val tipX = (anchor - tipWidth / 2f).coerceIn(0f, (widthPx - tipWidth).coerceAtLeast(0f))
                Column(
                    Modifier
                        .offset { IntOffset(tipX.roundToInt(), 0) }
                        .onSizeChanged { tipWidth = it.width }
                        .background(Gd.SurfaceRaised, RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, Gd.Border), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(tipFormat.format(Date(p.time)), style = GdType.meta, color = Gd.TextMuted)
                    val lines = p.lines.ifEmpty { listOf(formatTick(p.value)) }
                    lines.forEachIndexed { i, line ->
                        Text(
                            line,
                            style = if (i == 0) GdType.labelNum.copy(color = Gd.Text) else GdType.metaNum.copy(color = Gd.TextMuted)
                        )
                    }
                }
            }
        }

        if (xAxisTitle != null) {
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
                Text(xAxisTitle, style = GdType.meta, color = Gd.TextFaint)
            }
        }
    }
}
