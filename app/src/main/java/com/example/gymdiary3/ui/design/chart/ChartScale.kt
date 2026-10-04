package com.example.gymdiary3.ui.design.chart

import java.util.Calendar
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** A value axis: the drawn range and the tick values inside it. */
data class AxisScale(val min: Double, val max: Double, val ticks: List<Double>) {
    val span: Double get() = max - min
}

/**
 * Axis maths for the v2 charts. Pure Kotlin so it is unit-tested.
 *
 * Honesty rules baked in:
 *  - a y axis never spans less than [minSpan], so 19.5 → 20.5 kg cannot fill
 *    the whole chart height and read as a big jump;
 *  - non-negative data never gets a negative axis;
 *  - bars (volume, counts) always start at zero.
 */
object ChartScale {

    /** Heckbert's "nice number": 1, 2, 2.5, 5 or 10 times a power of ten. */
    fun niceNumber(range: Double, round: Boolean): Double {
        if (range <= 0.0) return 1.0
        val exponent = floor(log10(range))
        val fraction = range / 10.0.pow(exponent)
        val nice = if (round) {
            when {
                fraction < 1.5 -> 1.0
                fraction < 2.25 -> 2.0
                fraction < 3.5 -> 2.5
                fraction < 7.5 -> 5.0
                else -> 10.0
            }
        } else {
            when {
                fraction <= 1.0 -> 1.0
                fraction <= 2.0 -> 2.0
                fraction <= 2.5 -> 2.5
                fraction <= 5.0 -> 5.0
                else -> 10.0
            }
        }
        return nice * 10.0.pow(exponent)
    }

    fun valueScale(
        values: List<Double>,
        minSpan: Double,
        includeZero: Boolean,
        targetTicks: Int = 4
    ): AxisScale {
        require(targetTicks >= 2)
        if (values.isEmpty()) return AxisScale(0.0, 1.0, listOf(0.0, 1.0))

        val dataMin = values.min()
        val dataMax = values.max()
        var lo = if (includeZero) min(0.0, dataMin) else dataMin
        var hi = if (includeZero) max(0.0, dataMax) else dataMax

        val needed = max(minSpan, 1e-9)
        if (hi - lo < needed) {
            val mid = (hi + lo) / 2.0
            lo = mid - needed / 2.0
            hi = mid + needed / 2.0
        }
        // Keep non-negative data off a negative axis by shifting the window up.
        if (dataMin >= 0.0 && lo < 0.0) {
            hi -= lo
            lo = 0.0
        }

        val step = niceNumber(niceNumber(hi - lo, false) / (targetTicks - 1), true)
        val niceMin = floor(lo / step) * step
        val niceMax = ceil(hi / step) * step
        val ticks = generateSequence(niceMin) { it + step }
            .takeWhile { it <= niceMax + step * 1e-6 }
            .map { cleanZero(it) }
            .toList()
        return AxisScale(cleanZero(niceMin), cleanZero(niceMax), ticks)
    }

    /**
     * [count] evenly spaced tick times across [start, end], each snapped to the
     * nearest local midnight so labels read as real dates.
     */
    fun timeTicks(start: Long, end: Long, count: Int = 4): List<Long> {
        if (end <= start || count < 2) return listOf(start)
        val step = (end - start).toDouble() / (count - 1)
        return (0 until count)
            .map { i ->
                // Snap to a midnight, but never outside the range: a tick labelled
                // tomorrow on a chart that ends today would misstate the data.
                var t = snapToMidnight((start + step * i).toLong())
                if (t > end) t -= DAY_MS
                if (t < start) t += DAY_MS
                t
            }
            .filter { it in start..end }
            .distinct()
    }

    fun snapToMidnight(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val down = cal.timeInMillis
        val up = down + DAY_MS
        return if (millis - down <= up - millis) down else up
    }

    /** Fractional position of [t] in [start, end], clamped to 0..1. */
    fun fraction(t: Long, start: Long, end: Long): Float {
        if (end <= start) return 0.5f
        return ((t - start).toDouble() / (end - start)).coerceIn(0.0, 1.0).toFloat()
    }

    /** Percent change from [from] to [to]; null when there is no meaningful base. */
    fun percentChange(from: Double, to: Double): Double? =
        if (abs(from) < 1e-9) null else (to - from) / from * 100.0

    private fun cleanZero(v: Double): Double = if (abs(v) < 1e-9) 0.0 else v

    const val DAY_MS = 24L * 60 * 60 * 1000
}
