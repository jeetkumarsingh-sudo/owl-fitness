package com.example.gymdiary3.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.core.util.WorkoutCalculations
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.presentation.workout.LoggedSetRow
import com.example.gymdiary3.presentation.workout.LoggerUiState
import com.example.gymdiary3.presentation.workout.PendingSetRow
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.example.gymdiary3.viewmodel.LoggerViewModel
import com.example.gymdiary3.viewmodel.RestUi
import kotlin.math.max

data class LoggerActions(
    val onBack: () -> Unit = {},
    val onDone: () -> Unit = {},
    val onOpenHistory: () -> Unit = {},
    val onLog: (weightKg: Double, reps: Int, rpe: Float?, assisted: Boolean, notes: String?) -> Unit = { _, _, _, _, _ -> },
    val onDeleteSet: (Int) -> Unit = {},
    val onAdjustRest: (Int) -> Unit = {},
    val onSkipRest: () -> Unit = {}
)

@Composable
fun LoggerRoute(nav: NavHostController, vm: LoggerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val rest by vm.rest.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val s = state ?: return
    LoggerScreen(
        state = s, rest = rest, unit = settings.weightUnit, barWeightKg = settings.barWeight,
        actions = LoggerActions(
            onBack = { nav.popBackStack() },
            onDone = { nav.popBackStack() },
            onOpenHistory = { nav.navigate("analytics/${android.net.Uri.encode(s.exercise)}") },
            onLog = { w, r, rpe, assisted, notes -> vm.log(w, r, s.nextSetNumber, rpe, assisted, notes) },
            onDeleteSet = vm::deleteSet,
            onAdjustRest = vm::adjustRest,
            onSkipRest = vm::skipRest
        )
    )
}

@Composable
fun LoggerScreen(
    state: LoggerUiState,
    rest: RestUi,
    unit: String,
    barWeightKg: Double,
    actions: LoggerActions,
    modifier: Modifier = Modifier
) {
    val reduced = LocalReducedMotion.current
    val haptic = LocalHapticFeedback.current

    // The next set is pre-filled from the suggestion and re-filled after each logged set.
    var weight by remember(state.exercise, state.nextSetNumber, unit) {
        mutableDoubleStateOf(WeightFormatter.fromKilograms(state.prefillWeightKg, unit))
    }
    var reps by remember(state.exercise, state.nextSetNumber) { mutableIntStateOf(state.prefillReps) }
    var rpe by remember(state.nextSetNumber) { mutableStateOf<Float?>(null) }
    var assisted by rememberSaveable { mutableStateOf(false) }
    var notes by remember(state.nextSetNumber) { mutableStateOf("") }
    var showMore by rememberSaveable { mutableStateOf(false) }
    var editWeight by remember { mutableStateOf(false) }
    var editReps by remember { mutableStateOf(false) }
    var deleteRow by remember { mutableStateOf<LoggedSetRow?>(null) }

    // A distinct buzz when rest runs out on its own.
    var restWasRunning by remember { mutableStateOf(rest.running) }
    LaunchedEffect(rest.running) {
        if (restWasRunning && !rest.running && rest.secondsLeft == 0) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        restWasRunning = rest.running
    }

    val step = WeightFormatter.step(unit)
    val unitLabel = Fmt.unitLabel(unit)

    Column(modifier.fillMaxSize()) {
        DetailTopBar(
            title = state.exercise,
            onBack = actions.onBack,
            actions = {
                IconButton(onClick = actions.onOpenHistory) {
                    Icon(Icons.AutoMirrored.Outlined.ShowChart, contentDescription = "Exercise history", tint = Gd.TextMuted)
                }
                TextAction("Done", onClick = actions.onDone, color = Gd.Text)
            }
        )

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = Gd.s6)) {
            item(key = "target") { TargetBlock(state) }

            if (state.todaySets.isNotEmpty() || state.pendingSets.isNotEmpty()) {
                item(key = "tableHeader") { TableHeader() }
                items(state.todaySets, key = { it.id }) { row ->
                    LoggedRow(row, unit, onLongClick = { deleteRow = row }, modifier = Modifier.animateItem())
                }
                items(state.pendingSets, key = { "pending_${it.setNumber}" }) { row ->
                    PendingRow(row, isNext = row.setNumber == state.nextSetNumber, modifier = Modifier.animateItem())
                }
            }

            item(key = "input") {
                Column(Modifier.gutter().padding(top = Gd.s5)) {
                    Text("Set ${state.nextSetNumber}", style = GdType.section, color = Gd.Text)
                    Spacer(Modifier.height(Gd.s3))
                    Row(horizontalArrangement = Arrangement.spacedBy(Gd.s3)) {
                        ValueStepper(
                            label = "Weight ($unitLabel)",
                            value = Fmt.trim(weight),
                            onMinus = { weight = max(0.0, weight - step) },
                            onPlus = { weight += step },
                            onEdit = { editWeight = true },
                            modifier = Modifier.weight(1.15f)
                        )
                        ValueStepper(
                            label = "Reps",
                            value = "$reps",
                            onMinus = { reps = max(0, reps - 1) },
                            onPlus = { reps += 1 },
                            onEdit = { editReps = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TextAction(
                        if (showMore) "Hide RPE, notes and plates" else "RPE, notes and plates",
                        onClick = { showMore = !showMore },
                        contentPadding = PaddingValues(0.dp)
                    )
                    AnimatedVisibility(
                        visible = showMore,
                        enter = if (reduced) fadeIn() else expandVertically() + fadeIn(),
                        exit = if (reduced) fadeOut() else shrinkVertically() + fadeOut()
                    ) {
                        MoreOptions(
                            rpe = rpe, onRpe = { rpe = it },
                            assisted = assisted, onAssisted = { assisted = it },
                            notes = notes, onNotes = { notes = it },
                            plates = plateLine(WeightFormatter.toKilograms(weight, unit), barWeightKg, unit)
                        )
                    }

                    Spacer(Modifier.height(Gd.s3))
                    PrimaryButton(
                        text = "Log set ${state.nextSetNumber}",
                        enabled = reps > 0,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val kg = WeightFormatter.toKilograms(weight, unit)
                            val isPr = kg > 0 && state.bestE1rmKg > 0 &&
                                WorkoutCalculations.calculate1RM(kg, reps) > state.bestE1rmKg + 0.01
                            haptic.performHapticFeedback(
                                if (isPr) HapticFeedbackType.LongPress else HapticFeedbackType.Confirm
                            )
                            actions.onLog(kg, reps, rpe, assisted, notes.takeIf { it.isNotBlank() })
                        }
                    )
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

    if (editWeight) {
        NumberEntryDialog("Weight ($unitLabel)", Fmt.trim(weight), decimal = true,
            onConfirm = { v -> v.toDoubleOrNull()?.let { weight = max(0.0, it) }; editWeight = false },
            onDismiss = { editWeight = false })
    }
    if (editReps) {
        NumberEntryDialog("Reps", "$reps", decimal = false,
            onConfirm = { v -> v.toIntOrNull()?.let { reps = max(0, it) }; editReps = false },
            onDismiss = { editReps = false })
    }
    deleteRow?.let { row ->
        AlertDialog(
            onDismissRequest = { deleteRow = null },
            containerColor = Gd.SurfaceRaised,
            title = { Text("Delete set ${row.setNumber}?", style = GdType.section, color = Gd.Text) },
            text = {
                Text(
                    "${Fmt.set(row.weightKg, row.reps, unit)} will be removed from this workout.",
                    style = GdType.body, color = Gd.TextMuted
                )
            },
            confirmButton = {
                TextAction("Delete", onClick = { actions.onDeleteSet(row.id); deleteRow = null }, color = Gd.Danger)
            },
            dismissButton = { TextAction("Cancel", onClick = { deleteRow = null }) }
        )
    }
}

@Composable
private fun TargetBlock(state: LoggerUiState) {
    Column(Modifier.fillMaxWidth().gutter().padding(top = Gd.s1, bottom = Gd.s4)) {
        state.status?.let {
            StatusLabel(it.label(), it.color())
            Spacer(Modifier.height(Gd.s2))
        }
        if (state.targetLine != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Target", style = GdType.meta, color = Gd.TextMuted)
                if (state.targetMet) {
                    Spacer(Modifier.width(Gd.s2))
                    StatusLabel("Met", Gd.Positive)
                }
            }
            Text(state.targetLine, style = GdType.metric, color = if (state.targetMet) Gd.TextMuted else Gd.Text)
            if (!state.targetMet) state.targetReason?.let { Text(it, style = GdType.label, color = Gd.TextMuted) }
        } else if (state.lastSessionLabel == null) {
            Text("First time logging this exercise", style = GdType.label, color = Gd.TextMuted)
        }
        state.lastSessionLabel?.let {
            Spacer(Modifier.height(Gd.s2))
            Text(it, style = GdType.meta, color = Gd.TextFaint)
        }
    }
}

@Composable
private fun PendingRow(row: PendingSetRow, isNext: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(if (isNext) Gd.Surface else androidx.compose.ui.graphics.Color.Transparent)
            .height(48.dp)
            .gutter(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${row.setNumber}", style = GdType.labelNum,
            color = if (isNext) Gd.Text else Gd.TextFaint, modifier = Modifier.width(40.dp)
        )
        Text(row.previous, style = GdType.labelNum, color = Gd.TextMuted, modifier = Modifier.weight(1f))
        Text("—", style = GdType.labelNum, color = Gd.TextFaint, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun TableHeader() {
    Column {
        Hairline(inset = 0.dp)
        Row(Modifier.fillMaxWidth().gutter().padding(vertical = Gd.s2)) {
            Text("Set", style = GdType.meta, color = Gd.TextFaint, modifier = Modifier.width(40.dp))
            Text("Last time", style = GdType.meta, color = Gd.TextFaint, modifier = Modifier.weight(1f))
            Text("Today", style = GdType.meta, color = Gd.TextFaint, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(32.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoggedRow(row: LoggedSetRow, unit: String, onLongClick: () -> Unit, modifier: Modifier = Modifier) {
    val reduced = LocalReducedMotion.current
    // A PR row starts lightly washed in the accent and settles to neutral.
    val wash = remember(row.id) { Animatable(if (row.isPr && !reduced) 1f else 0f) }
    LaunchedEffect(row.id) { if (row.isPr && !reduced) wash.animateTo(0f, tween(1600)) }
    val check = remember(row.id) { Animatable(if (reduced) 1f else 0.4f) }
    LaunchedEffect(row.id) { if (!reduced) check.animateTo(1f, tween(220)) }

    Row(
        modifier
            .fillMaxWidth()
            .background(Gd.AccentWash.copy(alpha = Gd.AccentWash.alpha * wash.value))
            .combinedClickable(onClick = {}, onLongClick = onLongClick)
            .height(48.dp)
            .gutter(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${row.setNumber}", style = GdType.labelNum, color = Gd.TextMuted, modifier = Modifier.width(40.dp))
        Text(row.previous ?: "—", style = GdType.labelNum, color = Gd.TextMuted, modifier = Modifier.weight(1f))
        Text(
            Fmt.set(row.weightKg, row.reps, unit) + (row.rpe?.let { "  @${it.toInt()}" } ?: ""),
            style = GdType.bodyStrong.copy(fontFeatureSettings = "tnum"),
            color = Gd.Text,
            modifier = Modifier.weight(1f)
        )
        Box(Modifier.width(32.dp), contentAlignment = Alignment.CenterEnd) {
            if (row.isPr) {
                StatusLabel("PR", Gd.AccentText, Modifier.scale(check.value))
            } else {
                Icon(
                    Icons.Default.Check, contentDescription = "Done", tint = Gd.Positive,
                    modifier = Modifier.size(18.dp).scale(check.value)
                )
            }
        }
    }
}

@Composable
private fun MoreOptions(
    rpe: Float?, onRpe: (Float?) -> Unit,
    assisted: Boolean, onAssisted: (Boolean) -> Unit,
    notes: String, onNotes: (String) -> Unit,
    plates: String
) {
    Column(Modifier.fillMaxWidth().padding(top = Gd.s2), verticalArrangement = Arrangement.spacedBy(Gd.s3)) {
        Column {
            Text("RPE", style = GdType.meta, color = Gd.TextMuted)
            Spacer(Modifier.height(6.dp))
            RpePicker(rpe, onRpe)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = assisted, onCheckedChange = onAssisted,
                colors = CheckboxDefaults.colors(checkedColor = Gd.Text, checkmarkColor = Gd.Bg, uncheckedColor = Gd.BorderInput)
            )
            Text("Assisted", style = GdType.label, color = Gd.Text)
            Spacer(Modifier.weight(1f))
            Text(plates, style = GdType.labelNum, color = Gd.TextMuted)
        }
        OutlinedTextField(
            value = notes, onValueChange = onNotes, singleLine = true,
            placeholder = { Text("Note for this set", style = GdType.label, color = Gd.TextFaint) },
            textStyle = GdType.label.copy(color = Gd.Text),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gd.Accent, unfocusedBorderColor = Gd.BorderInput, cursorColor = Gd.Accent
            )
        )
    }
}

/** "Per side 20 · 5 · 1.25" for the entered weight, or "Bar only". */
internal fun plateLine(totalKg: Double, barKg: Double, unit: String): String {
    val lbs = WeightFormatter.label(unit) != "kg"
    val total = WeightFormatter.fromKilograms(totalKg, unit)
    val bar = WeightFormatter.fromKilograms(barKg, unit).let { if (lbs) Math.round(it).toDouble() else it }
    val plates = if (lbs) listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5) else listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    var side = (total - bar) / 2.0
    if (side <= 0.01) return "Bar only"
    val used = mutableListOf<String>()
    for (p in plates) {
        while (side >= p - 1e-6) { used += Fmt.trim(p); side -= p }
    }
    return "Per side " + used.joinToString(" · ")
}
