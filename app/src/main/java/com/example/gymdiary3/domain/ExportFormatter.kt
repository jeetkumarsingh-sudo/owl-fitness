package com.example.gymdiary3.domain

import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.data.BodyWeight
import com.example.gymdiary3.utils.WorkoutCalculations
import java.text.SimpleDateFormat
import java.util.*

object ExportFormatter {
    fun buildCsv(sessions: List<SessionWithSets>, bodyWeights: List<BodyWeight>): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

        // Section 1: Workout Sessions
        sb.appendLine("=== WORKOUT SESSIONS ===")
        sb.appendLine("Session ID,Date,Duration (min),Total Sets,Total Volume (kg)")

        for (sessionWithSets in sessions.sortedByDescending { it.session.startTime }) {
            val session = sessionWithSets.session
            val sets = sessionWithSets.sets
            val totalVolume = sessionWithSets.totalVolume
            val durationMin = sessionWithSets.duration / 60_000
            sb.appendLine(
                "${session.id}," +
                "\"${dateFormat.format(Date(session.startTime))}\"," +
                "$durationMin," +
                "${sets.size}," +
                "%.1f".format(totalVolume)
            )
        }

        sb.appendLine()

        // Section 2: All Sets
        sb.appendLine("=== ALL SETS ===")
        sb.appendLine("Date,Session ID,Exercise,Muscle Group,Set Number,Weight (kg),Reps,Volume (kg),Est. 1RM (kg)")

        for (sessionWithSets in sessions.sortedByDescending { it.session.startTime }) {
            val dateStr = dateFormat.format(Date(sessionWithSets.session.startTime))
            for (set in sessionWithSets.sets.sortedBy { it.setNumber }) {
                val volume = WorkoutCalculations.calculateVolume(set.weight, set.reps)
                val est1rmVal = WorkoutCalculations.calculate1RM(set.weight, set.reps)
                val est1rm = if (est1rmVal > 0) "%.1f".format(est1rmVal) else "N/A"
                sb.appendLine(
                    "\"$dateStr\"," +
                    "${set.sessionId}," +
                    "\"${set.exercise}\"," +
                    "\"${set.muscle}\"," +
                    "${set.setNumber}," +
                    "${set.weight}," +
                    "${set.reps}," +
                    "%.1f".format(volume) + "," +
                    est1rm
                )
            }
        }

        sb.appendLine()

        // Section 3: Body Weight History
        sb.appendLine("=== BODY WEIGHT HISTORY ===")
        sb.appendLine("Date,Weight (kg)")
        
        for (bw in bodyWeights.sortedByDescending { it.date }) {
            sb.appendLine("\"${dateFormat.format(Date(bw.date))}\",${bw.weight}")
        }

        return sb.toString()
    }
}
