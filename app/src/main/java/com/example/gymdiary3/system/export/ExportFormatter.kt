package com.example.gymdiary3.system.export

import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.core.util.WorkoutCalculations
import com.example.gymdiary3.domain.settings.WeightFormatter
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

object ExportFormatter {
    /**
     * Sets and body weight as CSV. Weights are stored in kg and converted to
     * [unit] so values match their headers; numbers are written with a dot
     * whatever the phone's locale, because a decimal comma would split a column.
     */
    fun buildCsv(sessions: List<SessionWithSets>, bodyWeights: List<BodyWeight>, unit: String = "kg"): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        fun w(kg: Double) = num(WeightFormatter.fromKilograms(kg, unit))

        // SECTION 1: Sets
        sb.appendLine("Date,Session ID,Exercise,Muscle Group,Set #,Weight ($unit),Reps,Volume ($unit),Est 1RM ($unit)")

        for (sessionWithSets in sessions.sortedByDescending { it.session.startTime }) {
            val dateStr = dateFormat.format(Date(sessionWithSets.session.startTime))
            // The order they were performed, so one exercise's sets stay together.
            for (set in sessionWithSets.sets.sortedWith(compareBy({ it.timestamp }, { it.setNumber }))) {
                val volume = WorkoutCalculations.calculateVolume(set.weight, set.reps)
                val est1rm = WorkoutCalculations.calculate1RM(set.weight, set.reps)
                val exercise = set.exercise.replace("\"", "\"\"")
                val muscle = set.muscle.replace("\"", "\"\"")
                sb.appendLine(
                    "\"$dateStr\"," +
                    "${set.sessionId}," +
                    "\"$exercise\"," +
                    "\"$muscle\"," +
                    "${set.setNumber}," +
                    "${w(set.weight)}," +
                    "${set.reps}," +
                    "${w(volume)}," +
                    if (est1rm > 0) w(est1rm) else "0"
                )
            }
        }

        sb.appendLine()

        // SECTION 2: Body weight
        sb.appendLine("Body Weight Log")
        sb.appendLine("Date,Weight ($unit)")
        for (bw in bodyWeights.sortedByDescending { it.timestamp }) {
            sb.appendLine("\"${dateFormat.format(Date(bw.timestamp))}\",${w(bw.weight)}")
        }

        return sb.toString()
    }

    /** Up to two decimals, no trailing zeros, always a dot: 100, 22.5, 220.46. */
    private fun num(v: Double): String =
        BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
}
