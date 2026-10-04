package com.example.gymdiary3.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.presentation.workout.ActiveWorkoutUiState
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.example.gymdiary3.viewmodel.ActiveWorkoutViewModel
import com.example.gymdiary3.viewmodel.RestUi

data class ActiveWorkoutActions(
    val onBack: () -> Unit = {},
    val onFinish: () -> Unit = {},
    val onStart: () -> Unit = {},
    val onAddExercise: () -> Unit = {},
    val onOpenExercise: (name: String, muscle: String) -> Unit = { _, _ -> },
    val onAdjustRest: (Int) -> Unit = {},
    val onSkipRest: () -> Unit = {}
)

fun loggerRoute(muscle: String, exercise: String) = "set/${Uri.encode(muscle)}/${Uri.encode(exercise)}"

@Composable
fun ActiveWorkoutRoute(nav: NavHostController, vm: ActiveWorkoutViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val elapsed by vm.elapsedSeconds.collectAsStateWithLifecycle()
    val rest by vm.rest.collectAsStateWithLifecycle()
    val unit by vm.unit.collectAsStateWithLifecycle()
    val s = state ?: return
    ActiveWorkoutScreen(
        state = s, elapsedSeconds = elapsed, rest = rest, unit = unit,
        actions = ActiveWorkoutActions(
            onBack = { nav.popBackStack() },
            onFinish = {
                vm.finish { id ->
                    if (id > 0) nav.navigate("summary/$id") { popUpTo("home") }
                    else nav.popBackStack("home", inclusive = false)
                }
            },
            onStart = vm::start,
            onAddExercise = { nav.navigate("picker") },
            onOpenExercise = { name, muscle -> nav.navigate(loggerRoute(muscle, name)) },
            onAdjustRest = vm::adjustRest,
            onSkipRest = vm::skipRest
        )
    )
}

@Composable
fun ActiveWorkoutScreen(
    state: ActiveWorkoutUiState,
    elapsedSeconds: Long,
    rest: RestUi,
    unit: String,
    actions: ActiveWorkoutActions,
    modifier: Modifier = Modifier
) {
    val reduced = LocalReducedMotion.current
    val active = state.sessionId != null
    Column(modifier.fillMaxSize()) {
        DetailTopBar(
            title = state.title,
            onBack = actions.onBack,
            actions = { if (active) TextAction("Finish", onClick = actions.onFinish, color = Gd.AccentText) }
        )

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = Gd.s8)) {
            if (!active) {
                item(key = "none") {
                    EmptyMessage(
                        title = "No workout in progress",
                        body = "Start one to log sets, or open an exercise from History.",
                        action = { PrimaryButton("Start workout", onClick = actions.onStart) }
                    )
                }
                return@LazyColumn
            }

            item(key = "clock") {
                Column(Modifier.gutter().padding(top = Gd.s2, bottom = Gd.s4)) {
                    Text(Fmt.clock(elapsedSeconds), style = GdType.hero, color = Gd.Text)
                    if (state.totalSets > 0) {
                        Spacer(Modifier.height(Gd.s1))
                        InlineStats(
                            listOf(
                                "${state.exercises.size}" to if (state.exercises.size == 1) "exercise" else "exercises",
                                "${state.totalSets}" to "sets",
                                Fmt.volume(state.totalVolumeKg, unit) to Fmt.unitLabel(unit)
                            )
                        )
                    }
                }
            }

            if (state.exercises.isNotEmpty()) {
                item(key = "exHeader") { Hairline(inset = 0.dp) }
                itemsIndexed(state.exercises, key = { _, r -> "ex_${r.exercise}" }) { i, row ->
                    ListRow(
                        title = row.exercise,
                        subtitle = row.setsLine,
                        overline = if (row.hasPr) "PR" else null,
                        overlineColor = Gd.AccentText,
                        titleStrong = true,
                        trailing = { Chevron() },
                        onClick = { actions.onOpenExercise(row.exercise, row.muscle) },
                        modifier = itemMotion()
                    )
                    Hairline(inset = if (i == state.exercises.lastIndex) 0.dp else Gd.Gutter)
                }
            }

            item(key = "add") {
                SecondaryButton(
                    "Add exercise",
                    onClick = actions.onAddExercise,
                    modifier = Modifier.fillMaxWidth().gutter().padding(top = Gd.s5)
                )
            }

            if (state.upNext.isNotEmpty() && state.upNextTitle != null) {
                item(key = "upNextHeader") { SectionHeader(state.upNextTitle) }
                itemsIndexed(state.upNext, key = { _, r -> "next_${r.exercise}" }) { i, row ->
                    ListRow(
                        title = row.exercise,
                        subtitle = row.detail,
                        trailing = {
                            Icon(Icons.Default.Add, contentDescription = "Log ${row.exercise}", tint = Gd.TextMuted)
                        },
                        onClick = { actions.onOpenExercise(row.exercise, row.muscle) },
                        modifier = itemMotion()
                    )
                    if (i < state.upNext.lastIndex) Hairline()
                }
            }
        }

        AnimatedVisibility(
            visible = rest.running,
            enter = if (reduced) fadeIn() else slideInVertically { it } + fadeIn(),
            exit = if (reduced) fadeOut() else slideOutVertically { it } + fadeOut()
        ) {
            RestTimerBar(rest.secondsLeft, rest.totalSeconds, actions.onAdjustRest, actions.onSkipRest)
        }
    }
}
