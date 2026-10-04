package com.example.gymdiary3.screenshots

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.design.chart.ChartKind
import com.example.gymdiary3.ui.design.chart.ChartPoint
import com.example.gymdiary3.ui.design.chart.TimeSeriesChart
import com.example.gymdiary3.ui.theme.GdType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_TALL, application = Application::class)
class DesignGalleryTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun designGallery() = compose.snap("00_design_gallery") {
        val day = 24L * 60 * 60 * 1000
        val end = 1_788_600_000_000L
        val start = end - 56 * day
        val e1rm = listOf(17.5, 18.0, 18.0, 19.2, 19.2, 20.0, 20.0, 21.0)
        val points = e1rm.mapIndexed { i, v ->
            ChartPoint(start + (i * 7 + 2) * day, v, listOf("${v.toInt()} kg est.", "12 kg × 10"))
        }
        val volume = listOf(3200.0, 4100.0, 3800.0, 4600.0, 0.0, 5200.0, 4900.0, 5600.0)
        val bars = volume.mapIndexed { i, v -> ChartPoint(start + (i * 7 + 3) * day, v) }

        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 40.dp)) {
            ScreenHeader(title = "Design system", subtitle = "Gym Diary v2")

            SectionHeader("Type scale")
            Column(Modifier.gutter(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("62.5", style = GdType.hero, color = Gd.Text)
                Text("Screen title", style = GdType.title, color = Gd.Text)
                Text("4,820 kg", style = GdType.metric, color = Gd.Text)
                Text("Section title", style = GdType.section, color = Gd.Text)
                Text("Body strong — Bench Press", style = GdType.bodyStrong, color = Gd.Text)
                Text("Body — supporting copy reads at 15sp.", style = GdType.body, color = Gd.Text)
                Text("Label — muted secondary text", style = GdType.label, color = Gd.TextMuted)
                Text("Meta — 12sp metadata", style = GdType.meta, color = Gd.TextFaint)
                StatusLabel("Stalling", Gd.Warning)
            }

            SectionHeader("Actions")
            Column(Modifier.gutter(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton("Start workout", onClick = {}, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton("Add exercise", onClick = {}, modifier = Modifier.weight(1f))
                    SecondaryButton("Finish", onClick = {}, modifier = Modifier.weight(1f))
                }
                TextAction("View all", onClick = {})
            }

            SectionHeader("Selection")
            Column(Modifier.gutter()) {
                SegmentedControl(listOf("4W", "8W", "3M", "6M", "1Y", "All"), selectedIndex = 1, onSelect = {})
            }
            Spacer(Modifier.height(12.dp))
            UnderlineTabs(listOf("Strength", "Volume", "Reps"), selectedIndex = 0, onSelect = {})

            SectionHeader("This week")
            WeekStrip(
                days = listOf(
                    WeekDay("M", true, false, false), WeekDay("T", false, false, false),
                    WeekDay("W", true, false, false), WeekDay("T", true, false, false),
                    WeekDay("F", false, true, false), WeekDay("S", false, false, true),
                    WeekDay("S", false, false, true)
                ),
                modifier = Modifier.gutter()
            )
            Spacer(Modifier.height(12.dp))
            InlineStats(listOf("3" to "workouts", "12,480 kg" to "volume", "2" to "PRs"), Modifier.gutter())

            SectionHeader("Metrics")
            MetricRow {
                Metric("15 kg", "Best set", Modifier.weight(1f))
                Metric("20 kg", "Est. 1RM", Modifier.weight(1f), detail = "+8% · 6 wk", detailColor = Gd.Positive)
                Metric("12 × 10", "Last top set", Modifier.weight(1f))
            }

            SectionHeader("Rows", action = "View all", onAction = {})
            ListRow("Lat Pulldown", subtitle = "50 kg · 3 sessions", overline = "Stalling",
                overlineColor = Gd.Warning, titleStrong = true, trailing = { Chevron() }, onClick = {})
            Hairline()
            ListRow("Bench Press", subtitle = "45 → 47.5 kg", overline = "Progressing",
                overlineColor = Gd.Positive, titleStrong = true, trailing = { Chevron() }, onClick = {})

            SectionHeader("Charts")
            Column(Modifier.gutter()) {
                TimeSeriesChart(
                    points = points, rangeStart = start, rangeEnd = end,
                    yAxisTitle = "Estimated 1RM (kg)", formatTick = { it.toInt().toString() },
                    minSpan = 5.0, summary = "Estimated 1RM, 8 sessions"
                )
                Spacer(Modifier.height(24.dp))
                TimeSeriesChart(
                    points = bars, rangeStart = start, rangeEnd = end, kind = ChartKind.Bars,
                    yAxisTitle = "Volume per week (kg)", formatTick = { "%,d".format(it.toInt()) },
                    minSpan = 1.0, summary = "Weekly volume, 8 weeks", height = 160.dp
                )
            }

            SectionHeader("Empty state")
            EmptyMessage("No workouts yet", "Your sessions will appear here after your first workout.")
        }
    }
}
