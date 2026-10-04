package com.example.gymdiary3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.model.SessionExerciseLog
import com.example.gymdiary3.presentation.format.Fmt
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.viewmodel.ProgramViewModel

private const val SETS = 5

@Composable
fun ProgramSessionLogScreen(nav: NavHostController, sessionId: Int, viewModel: ProgramViewModel = hiltViewModel()) {
    val logs by viewModel.getLogsForSession(sessionId).collectAsStateWithLifecycle(emptyList())
    ProgramLogContent(
        logs,
        onBack = { nav.navigateUp() },
        onFinish = { nav.navigate("summary/$sessionId") },
        onSave = viewModel::updateExerciseLog
    )
}

@Composable
fun ProgramLogContent(
    logs: List<SessionExerciseLog>,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    onSave: (SessionExerciseLog) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize()) {
        DetailTopBar("Log session", onBack = onBack, actions = {
            TextAction("Finish", onClick = onFinish, color = Gd.AccentText)
        })
        LazyColumn(Modifier.weight(1f).imePadding(), contentPadding = PaddingValues(bottom = Gd.s8)) {
            if (logs.isEmpty()) {
                item(key = "empty") { EmptyMessage("No exercises in this session", "This program day has no exercises planned.") }
            }
            itemsIndexed(logs, key = { _, l -> "log_${l.id}" }) { i, log ->
                ExerciseGrid(log, onSave, Modifier.padding(top = if (i == 0) Gd.s3 else Gd.s6))
                if (i < logs.lastIndex) Hairline(Modifier.padding(top = Gd.s6))
            }
        }
    }
}

/**
 * One exercise as a compact 5-column grid: set numbers, then a kg row and a reps
 * row. Edits stay local until saved, so a half-typed value never hits the database.
 */
@Composable
private fun ExerciseGrid(log: SessionExerciseLog, onSave: (SessionExerciseLog) -> Unit, modifier: Modifier = Modifier) {
    val saved = remember(log) { (1..SETS).map { log.weight(it).orEmpty() to log.reps(it).orEmpty() } }
    val weights = remember(log.id) { mutableStateListOf(*saved.map { it.first }.toTypedArray()) }
    val reps = remember(log.id) { mutableStateListOf(*saved.map { it.second }.toTypedArray()) }
    // Pick up external changes (e.g. a restore) without clobbering in-progress edits elsewhere.
    LaunchedEffect(log) {
        saved.forEachIndexed { i, (w, r) -> weights[i] = w; reps[i] = r }
    }
    val dirty = (0 until SETS).any { weights[it] != saved[it].first || reps[it] != saved[it].second }
    val logged = (0 until SETS).count { saved[it].second.isNotEmpty() }

    Column(modifier.gutter()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(log.exerciseName, style = GdType.bodyStrong, color = Gd.Text)
                log.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = GdType.meta, color = Gd.TextMuted, modifier = Modifier.padding(top = 2.dp))
                }
            }
            when {
                dirty -> TextAction("Save", color = Gd.AccentText, onClick = {
                    var updated = log
                    for (n in 1..SETS) {
                        updated = updated.withSet(n, weights[n - 1].replace(',', '.').toDoubleOrNull(), reps[n - 1].toIntOrNull())
                    }
                    onSave(updated)
                })
                logged > 0 -> Text("$logged/$SETS", style = GdType.labelNum, color = Gd.TextMuted, modifier = Modifier.padding(start = Gd.s3))
            }
        }
        Spacer(Modifier.height(Gd.s3))
        GridRow(label = "Set") { n -> Text("$n", style = GdType.meta, color = Gd.TextFaint, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(Gd.s1))
        GridRow(label = "kg") { n ->
            GridCell(weights[n - 1], { v -> weights[n - 1] = v.filter { it.isDigit() || it == '.' || it == ',' } }, KeyboardType.Decimal, "${log.exerciseName} set $n weight")
        }
        Spacer(Modifier.height(Gd.s2))
        GridRow(label = "reps") { n ->
            GridCell(reps[n - 1], { v -> reps[n - 1] = v.filter { it.isDigit() }.take(3) }, KeyboardType.Number, "${log.exerciseName} set $n reps")
        }
    }
}

@Composable
private fun GridRow(label: String, cell: @Composable (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = GdType.meta, color = Gd.TextMuted, modifier = Modifier.width(34.dp))
        for (n in 1..SETS) Box(Modifier.weight(1f)) { cell(n) }
    }
}

@Composable
private fun GridCell(value: String, onChange: (String) -> Unit, keyboard: KeyboardType, description: String) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        // "102.5" must fit a ~50dp cell on a small phone at large text; long values step down a size.
        textStyle = (if (value.length > 4) GdType.label else GdType.bodyStrong).copy(color = Gd.Text, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(Gd.Accent),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard, imeAction = ImeAction.Next),
        modifier = Modifier
            .fillMaxWidth()
            .height(Gd.TouchMin)
            .onFocusChanged { focused = it.isFocused }
            .semantics { contentDescription = description },
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Gd.Surface, RoundedCornerShape(Gd.RadiusSm))
                    .border(1.dp, if (focused) Gd.Accent else Gd.BorderStrong, RoundedCornerShape(Gd.RadiusSm)),
                contentAlignment = Alignment.Center
            ) {
                if (value.isEmpty()) Text("–", style = GdType.body, color = Gd.TextFaint)
                inner()
            }
        }
    )
}

private fun SessionExerciseLog.weight(n: Int): String? = when (n) {
    1 -> set1Weight; 2 -> set2Weight; 3 -> set3Weight; 4 -> set4Weight; else -> set5Weight
}?.let { Fmt.trim(it) }

private fun SessionExerciseLog.reps(n: Int): String? = when (n) {
    1 -> set1Reps; 2 -> set2Reps; 3 -> set3Reps; 4 -> set4Reps; else -> set5Reps
}?.toString()

private fun SessionExerciseLog.withSet(n: Int, weight: Double?, reps: Int?): SessionExerciseLog = when (n) {
    1 -> copy(set1Weight = weight, set1Reps = reps)
    2 -> copy(set2Weight = weight, set2Reps = reps)
    3 -> copy(set3Weight = weight, set3Reps = reps)
    4 -> copy(set4Weight = weight, set4Reps = reps)
    else -> copy(set5Weight = weight, set5Reps = reps)
}
