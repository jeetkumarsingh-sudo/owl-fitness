package com.example.gymdiary3.presentation.history

import com.example.gymdiary3.domain.history.SessionSplit
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.progression.PrDetector
import com.example.gymdiary3.presentation.format.Fmt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SummaryUiState(
    val sessionId: Int,
    val title: String,
    val dateLine: String,
    val sets: Int,
    val duration: String,
    val volume: String,
    val prs: List<SummaryPr>,
    val exercises: List<SummaryExercise>,
    val muscles: List<MuscleShare>,
    val notes: String?
)

data class SummaryPr(val exercise: String, val set: String, val detail: String)
data class SummaryExercise(val name: String, val setsLine: String, val extra: String?, val isPr: Boolean)
data class MuscleShare(val muscle: String, val volume: String, val fraction: Float)

object SummaryStateBuilder {

    /**
     * [allSessions] supplies the history for PR detection: a PR is judged
     * against sessions before this one, never against later ones.
     */
    fun build(session: SessionWithSets, allSessions: List<SessionWithSets>, unit: String): SummaryUiState {
        val prEvents = PrDetector.events(allSessions.flatMap { it.sets })
            .filter { it.sessionKey == session.session.id.toLong() }
        val prNames = prEvents.map { it.exercise }.toSet()

        val exercises = session.sets.sortedBy { it.timestamp }.groupBy { it.exercise }.map { (name, sets) ->
            val rpes = sets.mapNotNull { it.rpe }
            val notes = sets.mapNotNull { it.notes?.takeIf { n -> n.isNotBlank() } }
            val extra = listOfNotNull(
                rpes.takeIf { it.isNotEmpty() }?.let { "RPE ${it.joinToString(", ") { r -> r.toInt().toString() }}" },
                notes.takeIf { it.isNotEmpty() }?.joinToString(" · ")
            ).joinToString(" · ").ifBlank { null }
            SummaryExercise(
                name = name,
                setsLine = sets.joinToString(" · ") { Fmt.set(it.weight, it.reps, unit) },
                extra = extra,
                isPr = name in prNames
            )
        }

        val byMuscle = session.volumePerMuscle.filter { it.value > 0 }.toList().sortedByDescending { it.second }
        val maxVol = byMuscle.maxOfOrNull { it.second } ?: 1.0

        return SummaryUiState(
            sessionId = session.session.id,
            title = session.session.name?.takeIf { it.isNotBlank() } ?: SessionSplit.label(session.sets),
            dateLine = SimpleDateFormat("EEEE, MMM d · HH:mm", Locale.getDefault()).format(Date(session.session.startTime)),
            sets = session.sets.size,
            duration = Fmt.duration(session.duration),
            volume = "${Fmt.volume(session.totalVolume, unit)} ${Fmt.unitLabel(unit)}",
            prs = prEvents.map {
                SummaryPr(
                    exercise = it.exercise,
                    set = Fmt.set(it.weightKg, it.reps, unit),
                    detail = "Previous best ${Fmt.weightUnit(it.previousBestKg, unit)}"
                )
            },
            exercises = exercises,
            muscles = byMuscle.map { (m, v) ->
                MuscleShare(m, "${Fmt.volume(v, unit)} ${Fmt.unitLabel(unit)}", (v / maxVol).toFloat())
            },
            notes = session.session.notes?.takeIf { it.isNotBlank() }
        )
    }
}
