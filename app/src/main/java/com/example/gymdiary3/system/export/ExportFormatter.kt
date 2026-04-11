package com.example.gymdiary3.system.export

import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.data.BodyWeight
import com.example.gymdiary3.domain.util.WorkoutCalculations
import java.text.SimpleDateFormat
import java.util.*

object ExportFormatter {
    fun buildCsv(sessions: List<SessionWithSets>, bodyWeights: List<BodyWeight>): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // SECTION 1: Sets (the most important data — put first)
        sb.appendLine("Date,Session ID,Exercise,Muscle Group,Set #,Weight (kg),Reps,Volume (kg),Est 1RM (kg)")

        for (sessionWithSets in sessions.sortedByDescending { it.session.startTime }) {
            val dateStr = dateFormat.format(Date(sessionWithSets.session.startTime))
            for (set in sessionWithSets.sets.sortedBy { it.setNumber }) {
                val volume = WorkoutCalculations.calculateVolume(set.weight, set.reps)
                val est1rm = WorkoutCalculations.calculate1RM(set.weight, set.reps)
                val est1rmStr = if (est1rm > 0) "%.1f".format(est1rm) else "0"
                // Escape quotes in exercise/muscle names
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

        // Blank line separator — valid in CSV as a visual break
        sb.appendLine()

        // SECTION 2: Body weight — separate header block
        sb.appendLine("Body Weight Log")
        sb.appendLine("Date,Weight (kg)")
        for (bw in bodyWeights.sortedByDescending { it.date }) {
            sb.appendLine("\"${dateFormat.format(Date(bw.date))}\",${bw.weight}")
        }

        return sb.toString()
    }
}
