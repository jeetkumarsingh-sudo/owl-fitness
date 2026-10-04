package com.example.gymdiary3.flows

import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.domain.model.WorkoutSession
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.system.export.ExportFormatter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/** Settings → Export CSV (feature "Export CSV"). Case IDs match replica/test-plan.md. */
class CsvExportTest {

    private val defaultLocale = Locale.getDefault()
    @After fun restoreLocale() = Locale.setDefault(defaultLocale)

    private val t = 1_780_000_000_000L
    private fun set(n: Int, exercise: String, kg: Double, reps: Int, at: Long) =
        WorkoutSet(n, at, "Chest", exercise, n, reps, kg, false, 1, null, null)

    private val session = SessionWithSets(
        WorkoutSession(1, t, t + 3_600_000, "Push", null),
        listOf(
            // Logged in this order: bench 1, bench 2, then fly 1.
            set(1, "Bench Press", 100.0, 5, t + 60_000),
            set(2, "Bench Press", 100.0, 5, t + 300_000),
            set(3, "Cable Fly, \"low\"", 22.5, 12, t + 600_000).copy(setNumber = 1),
        )
    )
    private val weights = listOf(BodyWeight(1, t, 80.0))

    /** Splits one CSV line into fields, honouring quotes. */
    private fun fields(line: String): List<String> {
        val out = mutableListOf<String>(); val cur = StringBuilder(); var quoted = false; var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && quoted && i + 1 < line.length && line[i + 1] == '"' -> { cur.append('"'); i++ }
                c == '"' -> quoted = !quoted
                c == ',' && !quoted -> { out += cur.toString(); cur.clear() }
                else -> cur.append(c)
            }
            i++
        }
        return out + cur.toString()
    }

    private fun setRows(csv: String) = csv.lines().drop(1).takeWhile { it.isNotBlank() }.map(::fields)

    @Test fun `CSV-H1 names with commas and quotes stay one field`() {
        val rows = setRows(ExportFormatter.buildCsv(listOf(session), weights))
        rows.forEach { assertEquals(it.toString(), 9, it.size) }
        assertEquals("Cable Fly, \"low\"", rows.last()[2])
    }

    @Test fun `CSV-E1 pounds users get pounds under the pounds header`() {
        val csv = ExportFormatter.buildCsv(listOf(session), weights, unit = "lbs")
        assertEquals("Weight (lbs)", fields(csv.lines().first())[5])
        val bench = setRows(csv).first()
        assertEquals(220.5, bench[5].toDouble(), 0.1)        // 100 kg
        assertEquals(5 * 220.5, bench[7].toDouble(), 1.0)    // volume
        val bodyRow = csv.lines().dropWhile { !it.startsWith("Date,Weight") }[1]
        assertEquals(176.4, fields(bodyRow)[1].toDouble(), 0.1) // 80 kg
    }

    @Test fun `CSV-E2 a comma-decimal phone still writes nine columns`() {
        Locale.setDefault(Locale.GERMANY)
        val rows = setRows(ExportFormatter.buildCsv(listOf(session), weights))
        rows.forEach { assertEquals(it.toString(), 9, it.size) }
        assertEquals(500.0, rows.first()[7].toDouble(), 0.0)
    }

    @Test fun `CSV-E3 sets are listed in the order they were performed`() {
        val rows = setRows(ExportFormatter.buildCsv(listOf(session), weights))
        assertEquals(listOf("Bench Press", "Bench Press", "Cable Fly, \"low\""), rows.map { it[2] })
    }
}
