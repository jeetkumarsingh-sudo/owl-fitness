package com.example.gymdiary3.presentation.format

import com.example.gymdiary3.domain.settings.WeightFormatter
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** Display formatting. Weights are stored in kg and converted to the user's unit here. */
object Fmt {

    /** "62.5" / "60" — drops a trailing .0. */
    fun weight(kg: Double, unit: String): String = trim(WeightFormatter.fromKilograms(kg, unit))

    /** "62.5 kg" */
    fun weightUnit(kg: Double, unit: String): String = "${weight(kg, unit)} ${WeightFormatter.label(unit)}"

    /** "60 × 8" (bodyweight sets read "BW × 12"). */
    fun set(kg: Double, reps: Int, unit: String): String =
        if (kg <= 0.0) "BW × $reps" else "${weight(kg, unit)} × $reps"

    /** "4,820" in the user's unit, no decimals — for volume. */
    fun volume(kg: Double, unit: String): String =
        NumberFormat.getIntegerInstance(Locale.getDefault())
            .format(WeightFormatter.fromKilograms(kg, unit).roundToLong())

    fun unitLabel(unit: String): String = WeightFormatter.label(unit)

    /** "+8%" / "−3%" / "0%". */
    fun percent(p: Double): String {
        val r = p.roundToInt()
        return when {
            r > 0 -> "+$r%"
            r < 0 -> "−${abs(r)}%"
            else -> "0%"
        }
    }

    /** "+1.2 kg" / "−0.5 kg" with one decimal, in the user's unit. */
    fun signedWeight(deltaKg: Double, unit: String): String {
        val v = WeightFormatter.fromKilograms(deltaKg, unit)
        val r = (v * 10).roundToInt() / 10.0
        val body = trim(abs(r))
        val sign = when {
            r > 0 -> "+"
            r < 0 -> "−"
            else -> ""
        }
        return "$sign$body ${WeightFormatter.label(unit)}"
    }

    /** "68 min" / "1 h 05 min" */
    fun duration(ms: Long): String {
        val minutes = (ms / 60_000).toInt()
        return if (minutes < 60) "$minutes min" else "${minutes / 60} h ${"%02d".format(minutes % 60)} min"
    }

    /** "32:08" for a running clock. */
    fun clock(totalSeconds: Long): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    fun trim(v: Double): String {
        val r = (v * 100).roundToInt() / 100.0
        return if (r % 1.0 == 0.0) r.toLong().toString() else {
            val one = (r * 10).roundToInt() / 10.0
            if (abs(one - r) < 1e-9) one.toString() else r.toString()
        }
    }
}
