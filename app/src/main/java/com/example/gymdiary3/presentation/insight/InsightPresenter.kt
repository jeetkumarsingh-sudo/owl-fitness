package com.example.gymdiary3.presentation.insight

import com.example.gymdiary3.domain.progression.ExerciseProgression
import com.example.gymdiary3.domain.progression.ProgressionStatus
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.InsightType
import com.example.gymdiary3.presentation.format.Fmt
import kotlin.math.abs
import kotlin.math.roundToInt

enum class Tone { POSITIVE, WARNING, INFO, DANGER, NEUTRAL }

/**
 * A two-second insight: TAG / subject / current state / next action.
 * No sentences. [exercise] is set when the row can open exercise detail.
 */
data class InsightRow(
    val tag: String,
    val tone: Tone,
    val title: String,
    val state: String,
    val action: String?,
    val exercise: String? = null,
    val priority: Int
)

object InsightPresenter {

    /** Only exercises trained this recently produce insights. */
    const val RECENT_DAYS = 21

    fun fromProgression(p: ExerciseProgression, unit: String): InsightRow? {
        val c = p.latest ?: return null
        val b = p.previous
        val rec = p.recommendation
        val next = rec?.let {
            if (it.weightKg > 0) "Next: ${Fmt.weight(it.weightKg, unit)} × ${it.repsLabel}" else "Next: ${it.repsLabel} reps"
        }
        val w = Fmt.weightUnit(c.topWeight, unit)
        return when (p.status) {
            ProgressionStatus.NEW -> null
            ProgressionStatus.PROGRESSING -> {
                val state = if (b != null && c.topWeight > b.topWeight) {
                    "${Fmt.weight(b.topWeight, unit)} → $w"
                } else if (b != null) {
                    "$w · ${b.topReps} → ${c.topReps} reps"
                } else w
                InsightRow("Progressing", Tone.POSITIVE, p.exercise, state, next, p.exercise, 40)
            }
            ProgressionStatus.STABLE ->
                InsightRow("Stable", Tone.INFO, p.exercise, "$w · ${sessions(p.streakAtWeight)}", next, p.exercise, 15)
            ProgressionStatus.STALLING ->
                InsightRow(
                    "Stalling", Tone.WARNING, p.exercise,
                    "$w · ${sessions(p.streakAtWeight)}",
                    rec?.action, p.exercise, 80 + p.streakAtWeight
                )
            ProgressionStatus.REGRESSING ->
                InsightRow(
                    "Regressing", Tone.DANGER, p.exercise,
                    if (b != null) "${Fmt.weight(b.topWeight, unit)} → $w" else w,
                    rec?.action, p.exercise, 75
                )
        }
    }

    /**
     * Non-exercise insights from the intelligence engine, rendered from their
     * structured data. Exercise-level types (plateau, progress) are skipped:
     * the progression engine is the single source for those, so the app never
     * says "stalling" and "progressing" about the same lift.
     */
    fun fromEngine(insight: FitnessInsight, unit: String): InsightRow? {
        val d = insight.dataPoints
        return when (insight.type) {
            InsightType.RECOVERY_CONCERN -> InsightRow(
                "Recovery", Tone.WARNING, "Training load",
                "${d["sessions_last_7_days"]?.toInt() ?: "6+"} sessions in 7 days",
                "Take a rest day", priority = 90
            )
            InsightType.FATIGUE_ACCUMULATION -> InsightRow(
                "Fatigue", Tone.WARNING, insight.exerciseName ?: "Fatigue",
                "${Fmt.weight(d["peak_max_weight"] ?: 0.0, unit)} → ${Fmt.weightUnit(d["last_max_weight"] ?: 0.0, unit)}",
                "Consider a lighter week", insight.exerciseName, 85
            )
            InsightType.VOLUME_SPIKE -> InsightRow(
                "Volume", Tone.WARNING, "Weekly volume",
                "${Fmt.percent(d["volume_change_percent"] ?: 0.0)} vs last week",
                "Hold this level next week", priority = 70
            )
            InsightType.OPTIMAL_VOLUME -> InsightRow(
                "Volume", Tone.POSITIVE, "Weekly volume",
                "${Fmt.percent(d["volume_change_percent"] ?: 0.0)} vs last week", null, priority = 30
            )
            InsightType.VOLUME_DECLINE -> InsightRow(
                "Volume", Tone.INFO, "Weekly volume",
                "${Fmt.percent(d["volume_change_percent"] ?: 0.0)} vs last week", null, priority = 35
            )
            InsightType.TRAINING_FREQUENCY_LOW -> InsightRow(
                "Consistency", Tone.INFO, "Training frequency",
                "${oneDecimal(d["avg_sessions_per_week"] ?: 0.0)} sessions / week", "Aim for 3", priority = 50
            )
            InsightType.TRAINING_FREQUENCY_OPTIMAL -> InsightRow(
                "Consistency", Tone.POSITIVE, "Training frequency",
                "${oneDecimal(d["avg_sessions_per_week"] ?: 0.0)} sessions / week", null, priority = 20
            )
            else -> null
        }
    }

    fun build(
        progressions: List<ExerciseProgression>,
        engineInsights: List<FitnessInsight>,
        unit: String,
        now: Long = System.currentTimeMillis()
    ): List<InsightRow> {
        val cutoff = now - RECENT_DAYS * 24L * 60 * 60 * 1000
        val exerciseRows = progressions
            .filter { (it.latest?.date ?: 0L) >= cutoff }
            .mapNotNull { fromProgression(it, unit) }
        val engineRows = engineInsights.mapNotNull { fromEngine(it, unit) }
        return (exerciseRows + engineRows).sortedByDescending { it.priority }
    }

    private fun sessions(n: Int) = if (n == 1) "1 session" else "$n sessions"

    private fun oneDecimal(v: Double): String {
        val r = (v * 10).roundToInt() / 10.0
        return if (abs(r % 1.0) < 1e-9) r.toInt().toString() else r.toString()
    }
}
