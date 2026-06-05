package com.example.gymdiary3.system.export

import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.core.util.WorkoutCalculations
import java.text.SimpleDateFormat
import java.util.*

object ExportFormatter {
    fun buildCsv(sessions: List<SessionWithSets>, bodyWeights: List<BodyWeight>, unit: String = "kg"): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // SECTION 1: Sets
        sb.appendLine("Date,Session ID,Exercise,Muscle Group,Set #,Weight ($unit),Reps,Volume ($unit),Est 1RM ($unit)")

        for (sessionWithSets in sessions.sortedByDescending { it.session.startTime }) {
            val dateStr = dateFormat.format(Date(sessionWithSets.session.startTime))
            for (set in sessionWithSets.sets.sortedBy { it.setNumber }) {
                val volume = WorkoutCalculations.calculateVolume(set.weight, set.reps)
                val est1rm = WorkoutCalculations.calculate1RM(set.weight, set.reps)
                val est1rmStr = if (est1rm > 0) "%.1f".format(est1rm) else "0"
                val exercise = set.exercise.replace("\"", "\"\"")
                val muscle = set.muscle.replace("\"", "\"\"")
                sb.appendLine(
                    "\"$dateStr\"," +
                    "${set.sessionId}," +
                    "\"$exercise\"," +
                    "\"$muscle\"," +
                    "${set.setNumber}," +
                    "${set.weight}," +
                    "${set.reps}," +
                    "%.1f".format(volume) + "," +
                    est1rmStr
                )
            }
        }

        sb.appendLine()

        // SECTION 2: Body weight
        sb.appendLine("Body Weight Log")
        sb.appendLine("Date,Weight ($unit)")
        for (bw in bodyWeights.sortedByDescending { it.timestamp }) {
            sb.appendLine("\"${dateFormat.format(Date(bw.timestamp))}\",${bw.weight}")
        }

        return sb.toString()
    }
}
