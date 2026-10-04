package com.example.gymdiary3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.presentation.home.HomeUiState
import com.example.gymdiary3.presentation.insight.InsightRow
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.viewmodel.HomeViewModel

data class HomeActions(
    val onStart: () -> Unit = {},
    val onLogYesterday: () -> Unit = {},
    val onResume: () -> Unit = {},
    val onFinish: () -> Unit = {},
    val onPrograms: () -> Unit = {},
    val onSettings: () -> Unit = {},
    val onHistory: () -> Unit = {},
    val onOpenSession: (Int) -> Unit = {},
    val onOpenInsight: (InsightRow) -> Unit = {},
    val onAllInsights: () -> Unit = {}
)

@Composable
fun HomeRoute(nav: NavHostController, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val elapsed by vm.elapsedSeconds.collectAsStateWithLifecycle()
    val s = state ?: return
    HomeScreen(
        state = s,
        elapsedSeconds = elapsed,
        actions = HomeActions(
            onStart = { vm.start(yesterday = false) { nav.navigate("workout") } },
            onLogYesterday = { vm.start(yesterday = true) { nav.navigate("workout") } },
            onResume = { nav.navigate("workout") },
            onFinish = { vm.finish { id -> if (id > 0) nav.navigate("summary/$id") } },
            onPrograms = { nav.navigate("program_tracker") },
            onSettings = { nav.navigate("settings") },
            onHistory = { nav.navigate("history") { launchSingleTop = true } },
            onOpenSession = { nav.navigate("summary/$it") },
            onOpenInsight = { row ->
                row.exercise?.let { nav.navigate("analytics/${android.net.Uri.encode(it)}") }
                    ?: nav.navigate("progress") { launchSingleTop = true }
            },
            onAllInsights = { nav.navigate("progress") { launchSingleTop = true } }
        )
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    elapsedSeconds: Long,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Gd.s8)) {
        item(key = "header") { Header(state, actions.onSettings) }
        item(key = "today") { TodayBlock(state, elapsedSeconds, actions) }

        if (!state.isNewUser) {
            item(key = "week") {
                SectionHeader("This week", action = "History", onAction = actions.onHistory)
                WeekStrip(state.week, Modifier.gutter().padding(top = Gd.s2))
                Spacer(Modifier.height(Gd.s5))
                MetricRow {
                    Metric(
                        value = "${state.workoutsThisWeek}",
                        label = if (state.workoutsThisWeek == 1) "workout" else "workouts",
                        modifier = Modifier.weight(1f)
                    )
                    Metric(
                        value = "${state.streakDays} ${if (state.streakDays == 1) "day" else "days"}",
                        label = "streak",
                        modifier = Modifier.weight(1f)
                    )
                    val pr = state.recentPr
                    if (pr != null) {
                        Metric(
                            value = pr.value, label = pr.exercise, modifier = Modifier.weight(1.3f),
                            valueColor = Gd.AccentText, detail = "PR · ${pr.whenLabel}"
                        )
                    } else {
                        Metric(value = "—", label = "no PR yet", modifier = Modifier.weight(1.3f))
                    }
                }
            }

            state.lastWorkout?.let { last ->
                item(key = "last") {
                    SectionHeader("Last workout")
                    ListRow(
                        title = last.title,
                        subtitle = last.subtitle,
                        titleStrong = true,
                        trailing = { Chevron() },
                        onClick = { actions.onOpenSession(last.sessionId) }
                    )
                }
            }

            state.insight?.let { insight ->
                item(key = "insight") {
                    SectionHeader("Worth knowing", action = "Progress", onAction = actions.onAllInsights)
                    InsightItem(insight, onClick = { actions.onOpenInsight(insight) })
                }
            }
        }
    }
}

@Composable
private fun Header(state: HomeUiState, onSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().gutter().padding(top = Gd.s4, bottom = Gd.s5),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Gym Diary · ${state.dateLine}", style = GdType.label, color = Gd.TextMuted)
            Spacer(Modifier.height(2.dp))
            Text(state.greeting, style = GdType.title, color = Gd.Text)
        }
        IconButton(onClick = onSettings, modifier = Modifier.offset(x = 12.dp)) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = Gd.TextMuted)
        }
    }
}

/** The primary action. The only container on Home, because it is the one thing to do. */
@Composable
private fun TodayBlock(state: HomeUiState, elapsedSeconds: Long, actions: HomeActions) {
    Column(
        Modifier
            .gutter()
            .fillMaxWidth()
            .clip(RoundedCornerShape(Gd.RadiusLg))
            .background(Gd.Surface)
            // The trailing text action brings its own 48dp touch height, so less bottom padding.
            .padding(start = Gd.s5, top = Gd.s5, end = Gd.s5, bottom = Gd.s2)
    ) {
        val active = state.active
        when {
            active != null -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(Gd.Positive, CircleShape))
                    Spacer(Modifier.width(Gd.s2))
                    StatusLabel("In progress", Gd.TextMuted)
                }
                Spacer(Modifier.height(Gd.s2))
                Text(Fmt.clock(elapsedSeconds), style = GdType.hero, color = Gd.Text)
                Text(
                    "${active.exerciseCount} ${if (active.exerciseCount == 1) "exercise" else "exercises"} · ${active.setCount} sets",
                    style = GdType.labelNum, color = Gd.TextMuted
                )
                Spacer(Modifier.height(Gd.s5))
                PrimaryButton("Resume workout", onClick = actions.onResume, modifier = Modifier.fillMaxWidth())
                TextAction("Finish workout", onClick = actions.onFinish, contentPadding = PaddingValues(0.dp))
            }

            state.isNewUser -> {
                StatusLabel("Today", Gd.TextMuted)
                Spacer(Modifier.height(Gd.s2))
                Text("Your first workout", style = GdType.metric, color = Gd.Text)
                Spacer(Modifier.height(Gd.s1))
                Text(
                    "Log your sets as you train. Progress and suggestions appear after a couple of sessions.",
                    style = GdType.label, color = Gd.TextMuted
                )
                Spacer(Modifier.height(Gd.s5))
                PrimaryButton("Start workout", onClick = actions.onStart, modifier = Modifier.fillMaxWidth())
                TextAction("Follow a program", onClick = actions.onPrograms, contentPadding = PaddingValues(0.dp))
            }

            else -> {
                StatusLabel("Today", Gd.TextMuted)
                Spacer(Modifier.height(Gd.s2))
                Text(state.today.title, style = GdType.metric, color = Gd.Text)
                Spacer(Modifier.height(2.dp))
                Text(state.today.detail, style = GdType.label, color = Gd.TextMuted)
                Spacer(Modifier.height(Gd.s5))
                PrimaryButton("Start workout", onClick = actions.onStart, modifier = Modifier.fillMaxWidth())
                Row {
                    TextAction("Log yesterday's workout", onClick = actions.onLogYesterday, contentPadding = PaddingValues(0.dp))
                    Spacer(Modifier.weight(1f))
                    TextAction("Programs", onClick = actions.onPrograms, contentPadding = PaddingValues(0.dp))
                }
            }
        }
    }
}
