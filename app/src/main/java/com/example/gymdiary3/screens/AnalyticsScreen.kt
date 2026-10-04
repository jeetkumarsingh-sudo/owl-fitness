package com.example.gymdiary3.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.example.gymdiary3.presentation.exercise.ExerciseCharts
import com.example.gymdiary3.presentation.exercise.ExerciseDetailUiState
import com.example.gymdiary3.presentation.exercise.ExerciseTab
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.design.chart.TimeSeriesChart
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.example.gymdiary3.viewmodel.ExerciseDetailViewModel

data class ExerciseDetailActions(val onBack: () -> Unit = {}, val onLog: () -> Unit = {})

@Composable
fun AnalyticsScreen(nav: NavHostController, vm: ExerciseDetailViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val unit by vm.unit.collectAsStateWithLifecycle()
    val s = state ?: return
    ExerciseDetailScreen(
        s, unit, System.currentTimeMillis(),
        ExerciseDetailActions(
            onBack = { nav.popBackStack() },
            onLog = { nav.navigate(loggerRoute(vm.muscle, s.exercise)) }
        )
    )
}

@Composable
fun ExerciseDetailScreen(
    state: ExerciseDetailUiState,
    unit: String,
    now: Long,
    actions: ExerciseDetailActions,
    modifier: Modifier = Modifier,
    initialTab: ExerciseTab = ExerciseTab.STRENGTH,
    initialRange: TimeRange = TimeRange.W8
) {
    val reduced = LocalReducedMotion.current
    var tab by rememberSaveable { mutableStateOf(initialTab) }
    var range by rememberSaveable { mutableStateOf(initialRange) }
    val spec = remember(state.sessions, tab, range, unit, now) { ExerciseCharts.spec(state.sessions, tab, range, unit, now) }

    Column(modifier.fillMaxSize()) {
        DetailTopBar(
            title = state.exercise,
            onBack = actions.onBack,
            actions = { TextAction("Log", onClick = actions.onLog, color = Gd.Text) }
        )
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = Gd.s8)) {
            if (state.sessions.isEmpty()) {
                item(key = "empty") {
                    EmptyMessage("No sessions yet", "Log this exercise once and its history starts here.")
                }
                return@LazyColumn
            }

            item(key = "status") {
                if (state.status != null) {
                    Row(Modifier.gutter(), verticalAlignment = Alignment.CenterVertically) {
                        StatusLabel(state.status.label(), state.status.color())
                        state.statusText?.let {
                            Spacer(Modifier.width(Gd.s2))
                            Text(it, style = GdType.labelNum, color = Gd.TextMuted)
                        }
                    }
                    Spacer(Modifier.height(Gd.s4))
                }
                MetricRow {
                    Metric(state.bestWeight, "heaviest", Modifier.weight(1f), detail = state.bestWeightDetail)
                    Metric(state.e1rm, "est. 1RM", Modifier.weight(1f))
                    Metric(state.lastSession ?: "—", "last session", Modifier.weight(1.25f), detail = state.lastSessionDetail)
                }
            }

            item(key = "chart") {
                Spacer(Modifier.height(Gd.s6))
                UnderlineTabs(ExerciseTab.entries.map { it.label }, tab.ordinal, { tab = ExerciseTab.entries[it] })
                Column(Modifier.gutter().padding(top = Gd.s4)) {
                    SegmentedControl(TimeRange.EXERCISE.map { it.label }, TimeRange.EXERCISE.indexOf(range), { range = TimeRange.EXERCISE[it] })
                    Spacer(Modifier.height(Gd.s4))
                    AnimatedContent(
                        targetState = spec.headline,
                        transitionSpec = { fadeIn(tween(if (reduced) 0 else GdMotion.Base)) togetherWith fadeOut(tween(if (reduced) 0 else GdMotion.Fast)) },
                        label = "headline"
                    ) { headline ->
                        Text(
                            headline ?: " ",
                            style = GdType.bodyStrong.copy(fontFeatureSettings = "tnum"),
                            color = spec.headlineTone.color().takeIf { spec.headlineTone != com.example.gymdiary3.presentation.insight.Tone.NEUTRAL } ?: Gd.Text
                        )
                    }
                    Spacer(Modifier.height(Gd.s3))
                    TimeSeriesChart(
                        points = spec.points,
                        rangeStart = range.start(now, state.firstDate),
                        rangeEnd = now,
                        yAxisTitle = spec.yAxisTitle,
                        formatTick = ::compactTick,
                        minSpan = spec.minSpan,
                        kind = spec.kind,
                        height = 200.dp,
                        summary = spec.summary,
                        emptyText = spec.emptyText
                    )
                    Text("Tap or drag the chart to see a session", style = GdType.meta, color = Gd.TextFaint, modifier = Modifier.padding(top = Gd.s2))
                }
            }

            state.next?.let { next ->
                item(key = "next") {
                    Spacer(Modifier.height(Gd.s6))
                    Column(
                        Modifier
                            .gutter()
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Gd.RadiusLg))
                            .background(Gd.Surface)
                            .padding(Gd.s5)
                    ) {
                        StatusLabel("Next session", Gd.TextMuted)
                        Spacer(Modifier.height(Gd.s2))
                        Text(next.headline, style = GdType.metric, color = Gd.Text)
                        Spacer(Modifier.height(2.dp))
                        Text(next.action, style = GdType.label, color = Gd.Text)
                        next.reason?.let { Text(it, style = GdType.meta, color = Gd.TextMuted) }
                    }
                }
            }

            item(key = "recentHeader") { SectionHeader("Recent sessions") }
            itemsIndexed(state.recent, key = { _, r -> "r_${r.key}" }) { i, row ->
                ListRow(title = row.date, subtitle = row.setsLine)
                if (i < state.recent.lastIndex) Hairline()
            }
        }
    }
}
