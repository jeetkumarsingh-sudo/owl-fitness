package com.example.gymdiary3.screens

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.analytics.TimeRange
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.presentation.progress.ProgressUiState
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.design.chart.ChartKind
import com.example.gymdiary3.ui.design.chart.TimeSeriesChart
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.example.gymdiary3.viewmodel.ProgressOverviewViewModel

data class ProgressActions(
    val onOpenExercise: (String) -> Unit = {},
    val onStart: () -> Unit = {}
)

private const val LIFTS_PREVIEW = 6
private val VOLUME_RANGES = listOf(TimeRange.W8, TimeRange.M3, TimeRange.M6, TimeRange.Y1)

@Composable
fun ProgressScreen(nav: NavHostController, vm: ProgressOverviewViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val unit by vm.unit.collectAsStateWithLifecycle()
    val s = state ?: return
    ProgressContent(
        s, unit, System.currentTimeMillis(),
        ProgressActions(
            onOpenExercise = { nav.navigate("analytics/${Uri.encode(it)}") },
            onStart = { nav.navigate("home") { launchSingleTop = true } }
        )
    )
}

/** Compact large numbers for axis ticks: 7,500 → "7.5k". */
internal fun compactTick(v: Double): String =
    if (v >= 1000) Fmt.trim(v / 1000) + "k" else Fmt.trim(v)

@Composable
fun ProgressContent(
    state: ProgressUiState,
    unit: String,
    now: Long,
    actions: ProgressActions,
    modifier: Modifier = Modifier
) {
    var rangeIndex by rememberSaveable { mutableIntStateOf(0) }
    var showAllLifts by rememberSaveable { mutableStateOf(false) }

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Gd.s8)) {
        item(key = "header") { ScreenHeader("Progress") }

        if (state.isEmpty) {
            item(key = "empty") {
                EmptyMessage(
                    title = "Nothing to chart yet",
                    body = "Log a few workouts and your volume, records and trends show up here.",
                    action = { SecondaryButton("Start a workout", onClick = actions.onStart, compact = true) }
                )
            }
            return@LazyColumn
        }

        item(key = "overview") {
            MetricRow(Modifier.padding(top = Gd.s2)) {
                Metric(state.weekVolume, "this week", Modifier.weight(1.3f), detail = state.weekVolumeDelta, detailColor = state.weekVolumeTone.color())
                Metric(state.perWeek, "workouts / week", Modifier.weight(1f), detail = "4-week average")
                Metric("${state.prCount30}", if (state.prCount30 == 1) "PR" else "PRs", Modifier.weight(0.8f), detail = "last 30 days")
            }
        }

        item(key = "volume") {
            SectionHeader("Weekly volume")
            Column(Modifier.gutter()) {
                SegmentedControl(VOLUME_RANGES.map { it.label }, rangeIndex, { rangeIndex = it })
                Spacer(Modifier.height(Gd.s4))
                val range = VOLUME_RANGES[rangeIndex]
                val start = range.start(now, state.firstSessionAt)
                val points = state.weeklyVolume.filter { it.time >= start }
                TimeSeriesChart(
                    points = points,
                    rangeStart = start,
                    rangeEnd = now,
                    yAxisTitle = "Volume per week (${Fmt.unitLabel(unit)})",
                    formatTick = ::compactTick,
                    minSpan = 1.0,
                    kind = ChartKind.Bars,
                    height = 180.dp,
                    summary = "Weekly training volume, ${points.size} weeks shown",
                    emptyText = "No workouts in this range"
                )
            }
        }

        if (state.lifts.isNotEmpty()) {
            val lifts = if (showAllLifts) state.lifts else state.lifts.take(LIFTS_PREVIEW)
            item(key = "liftsHeader") {
                SectionHeader(
                    "Lifts",
                    action = if (state.lifts.size > LIFTS_PREVIEW) (if (showAllLifts) "Fewer" else "All ${state.lifts.size}") else null,
                    onAction = { showAllLifts = !showAllLifts }
                )
            }
            itemsIndexed(lifts, key = { _, l -> "lift_${l.exercise}" }) { i, lift ->
                ListRow(
                    title = lift.exercise,
                    subtitle = lift.state,
                    overline = lift.status.label(),
                    overlineColor = lift.status.color(),
                    titleStrong = true,
                    detail = lift.action,
                    trailing = {
                        // The status label already carries direction in colour; only a real drop repeats it.
                        lift.change?.let {
                            Text(
                                it, style = GdType.labelNum,
                                color = if (lift.changeTone == com.example.gymdiary3.presentation.insight.Tone.DANGER) Gd.Danger else Gd.TextMuted
                            )
                        }
                    },
                    onClick = { actions.onOpenExercise(lift.exercise) },
                    modifier = itemMotion()
                )
                if (i < lifts.lastIndex) Hairline()
            }
        }

        if (state.records.isNotEmpty()) {
            item(key = "recordsHeader") { SectionHeader("Personal records") }
            itemsIndexed(state.records, key = { i, r -> "rec_${i}_${r.exercise}" }) { i, rec ->
                ListRow(
                    title = rec.exercise,
                    subtitle = rec.whenLabel,
                    trailing = { Text(rec.set, style = GdType.bodyStrong.copy(fontFeatureSettings = "tnum"), color = Gd.Text) },
                    onClick = { actions.onOpenExercise(rec.exercise) }
                )
                if (i < state.records.lastIndex) Hairline()
            }
        }

        if (state.balance.isNotEmpty()) {
            item(key = "balance") {
                SectionHeader("Training balance")
                Text("Sets per muscle · last 4 weeks", style = GdType.meta, color = Gd.TextMuted, modifier = Modifier.gutter())
                Spacer(Modifier.height(Gd.s3))
                Column(Modifier.gutter(), verticalArrangement = Arrangement.spacedBy(Gd.s3)) {
                    state.balance.forEach { BalanceBar(it.muscle, it.sets, it.fraction) }
                }
            }
        }

        if (state.insights.isNotEmpty()) {
            item(key = "insightsHeader") { SectionHeader("Insights") }
            itemsIndexed(state.insights, key = { i, r -> "ins_${i}_${r.tag}" }) { i, row ->
                InsightItem(row, onClick = row.exercise?.let { ex -> { actions.onOpenExercise(ex) } })
                if (i < state.insights.lastIndex) Hairline()
            }
        }
    }
}

@Composable
private fun BalanceBar(muscle: String, sets: Int, fraction: Float) =
    LabeledBar(muscle, fraction, "$sets\u00A0sets", valueWidth = 72.dp)
