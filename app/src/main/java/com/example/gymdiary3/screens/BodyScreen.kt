package com.example.gymdiary3.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.analytics.TimeRange
import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.domain.recovery.RecoveryStatus
import com.example.gymdiary3.presentation.body.BodyCharts
import com.example.gymdiary3.presentation.body.BodyUiState
import com.example.gymdiary3.presentation.body.MuscleRecoveryRow
import com.example.gymdiary3.presentation.body.WeightEntryRow
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.design.chart.TimeSeriesChart
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.example.gymdiary3.viewmodel.BodyViewModel

data class BodyActions(
    val onLog: (displayValue: Double) -> Unit = {},
    val onDelete: (BodyWeight) -> Unit = {}
)

private const val ENTRIES_PREVIEW = 5

@Composable
fun BodyRoute(nav: NavHostController, vm: BodyViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val unit by vm.unit.collectAsStateWithLifecycle()
    val s = state ?: return
    BodyScreen(s, unit, System.currentTimeMillis(), BodyActions(onLog = vm::log, onDelete = vm::delete))
}

@Composable
fun BodyScreen(
    state: BodyUiState,
    unit: String,
    now: Long,
    actions: BodyActions,
    modifier: Modifier = Modifier,
    initialRange: TimeRange = TimeRange.D30
) {
    var range by rememberSaveable { mutableStateOf(initialRange) }
    var input by rememberSaveable { mutableStateOf("") }
    var showAll by rememberSaveable { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<WeightEntryRow?>(null) }
    val summary = remember(state.weights, range, unit, now) { BodyCharts.summary(state.weights, range, unit, now) }
    val unitLabel = Fmt.unitLabel(unit)

    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = Gd.s8)) {
        item(key = "header") { ScreenHeader("Body") }

        item(key = "weight") {
            Column(Modifier.gutter()) {
                if (state.current != null) {
                    Text(state.current, style = GdType.hero, color = Gd.Text)
                    Text(
                        listOfNotNull(summary.change, summary.period).joinToString(" · "),
                        style = GdType.labelNum, color = Gd.TextMuted
                    )
                } else {
                    Text("Track your body weight", style = GdType.metric, color = Gd.Text)
                    Text("Log it below; the trend appears after two entries.", style = GdType.label, color = Gd.TextMuted)
                }

                Spacer(Modifier.height(Gd.s4))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { v -> input = v.filter { it.isDigit() || it == '.' || it == ',' } },
                        singleLine = true,
                        placeholder = { Text(if (state.loggedToday) "Update today" else "Today's weight", style = GdType.body, color = Gd.TextFaint) },
                        suffix = { Text(unitLabel, style = GdType.label, color = Gd.TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = GdType.bodyStrong.copy(color = Gd.Text),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gd.Accent, unfocusedBorderColor = Gd.BorderInput, cursorColor = Gd.Accent,
                            focusedContainerColor = Gd.Surface, unfocusedContainerColor = Gd.Surface
                        )
                    )
                    Spacer(Modifier.width(Gd.s3))
                    PrimaryButton(
                        if (state.loggedToday) "Update" else "Log",
                        enabled = input.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true,
                        onClick = {
                            input.replace(',', '.').toDoubleOrNull()?.let { actions.onLog(it) }
                            input = ""
                        }
                    )
                }
            }
        }

        if (state.weights.isNotEmpty()) {
            item(key = "chart") {
                Column(Modifier.gutter().padding(top = Gd.s6)) {
                    SegmentedControl(TimeRange.BODY_WEIGHT.map { it.label }, TimeRange.BODY_WEIGHT.indexOf(range), { range = TimeRange.BODY_WEIGHT[it] })
                    Spacer(Modifier.height(Gd.s4))
                    TimeSeriesChart(
                        points = summary.points,
                        rangeStart = range.start(now, state.weights.firstOrNull()?.timestamp),
                        rangeEnd = now,
                        yAxisTitle = "Weight ($unitLabel)",
                        xAxisTitle = "Date",
                        formatTick = { Fmt.trim(it) },
                        // Daily weight wobbles ±0.5 kg; a floor of max(3 kg, 6%) shows the trend without inflating the noise.
                        minSpan = maxOf(if (unitLabel == "kg") 3.0 else 6.0, (summary.points.maxOfOrNull { it.value } ?: 0.0) * 0.06),
                        height = 180.dp,
                        summary = "Body weight, ${summary.points.size} entries, ${summary.period}",
                        emptyText = "No entries in this range"
                    )
                }
            }
        }

        if (state.hasTraining) {
            item(key = "recovery") {
                SectionHeader("Recovery")
                state.today?.let { t ->
                    Row(Modifier.gutter().padding(bottom = Gd.s4), verticalAlignment = Alignment.Bottom) {
                        Text("Today: ${t.title}", style = GdType.bodyStrong, color = Gd.Text)
                        Spacer(Modifier.width(Gd.s2))
                        Text(t.detail.replaceFirstChar { it.lowercase() }, style = GdType.label, color = Gd.TextMuted)
                    }
                }
                Column(Modifier.gutter(), verticalArrangement = Arrangement.spacedBy(Gd.s3)) {
                    state.recovery.forEach { RecoveryBar(it) }
                }
            }
        }

        if (state.entries.isNotEmpty()) {
            val entries = if (showAll) state.entries else state.entries.take(ENTRIES_PREVIEW)
            item(key = "entriesHeader") {
                SectionHeader(
                    "Entries",
                    action = if (state.entries.size > ENTRIES_PREVIEW) (if (showAll) "Fewer" else "All ${state.entries.size}") else null,
                    onAction = { showAll = !showAll }
                )
            }
            itemsIndexed(entries, key = { _, e -> "w_${e.entry.id}_${e.entry.timestamp}" }) { i, e ->
                ListRow(
                    title = e.value,
                    subtitle = e.date,
                    trailing = { e.delta?.let { Text(it, style = GdType.labelNum, color = Gd.TextMuted) } },
                    onLongClick = { deleting = e },
                    modifier = Modifier.animateItem()
                )
                if (i < entries.lastIndex) Hairline()
            }
        }
    }

    deleting?.let { e ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            containerColor = Gd.SurfaceRaised,
            title = { Text("Delete entry?", style = GdType.section, color = Gd.Text) },
            text = { Text("${e.value} on ${e.date} will be removed.", style = GdType.body, color = Gd.TextMuted) },
            confirmButton = { TextAction("Delete", onClick = { actions.onDelete(e.entry); deleting = null }, color = Gd.Danger) },
            dismissButton = { TextAction("Cancel", onClick = { deleting = null }) }
        )
    }
}

@Composable
private fun RecoveryBar(row: MuscleRecoveryRow) {
    val reduced = LocalReducedMotion.current
    var shown by remember { mutableStateOf(reduced) }
    LaunchedEffect(Unit) { shown = true }
    val f by animateFloatAsState(if (shown) row.fraction else 0f, tween(if (reduced) 0 else 500, easing = GdMotion.Ease), label = "rec")
    val (fill, labelColor) = when (row.status) {
        RecoveryStatus.READY -> Gd.Positive to Gd.Positive
        RecoveryStatus.NEARLY -> Gd.DataNeutral to Gd.TextMuted
        RecoveryStatus.RECOVERING -> Gd.DataNeutral to Gd.Warning
        RecoveryStatus.UNTRAINED -> Gd.Surface to Gd.TextFaint
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(row.muscle, style = GdType.label, color = Gd.Text, modifier = Modifier.width(84.dp))
        Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Gd.Surface)) {
            Box(Modifier.fillMaxWidth(f).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(fill))
        }
        Text(row.label, style = GdType.labelNum, color = labelColor, modifier = Modifier.width(104.dp).padding(start = Gd.s3))
    }
}
