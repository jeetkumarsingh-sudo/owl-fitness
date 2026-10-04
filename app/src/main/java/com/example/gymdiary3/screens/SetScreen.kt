package com.example.gymdiary3.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.ui.components.ApexCta
import com.example.gymdiary3.ui.components.ApexPanel
import com.example.gymdiary3.ui.components.ApexScaffold
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gymdiary3.domain.settings.UserSettings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetScreen(
    nav: NavHostController,
    muscle: String,
    exercise: String,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    var reps by remember { mutableIntStateOf(0) }
    var weight by remember { mutableDoubleStateOf(0.0) }
    var isAssisted by remember { mutableStateOf(false) }
    var rpe by remember { mutableStateOf<Float?>(null) }
    var setNotes by remember { mutableStateOf("") }
    var showNotesInput by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val lastSet by viewModel.lastSet.collectAsStateWithLifecycle()
    val suggestedWeight by viewModel.suggestedWeight.collectAsStateWithLifecycle()
    val currentSet by viewModel.currentSet.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isRestTimerRunning.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.restTimerSeconds.collectAsStateWithLifecycle()
    val userSettings by viewModel.settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(UserSettings())
    val weightUnit = userSettings.weightUnit

    var timerInitialSeconds by remember { mutableIntStateOf(userSettings.defaultRestSeconds) }
    LaunchedEffect(isTimerRunning) { if (isTimerRunning) timerInitialSeconds = userSettings.defaultRestSeconds }

    val canLogSet = remember(reps, weight) { reps > 0 && weight >= 0 }

    LaunchedEffect(exercise) { viewModel.loadLastSet(exercise) }
    LaunchedEffect(lastSet, weightUnit) {
        lastSet?.let {
            if (weight == 0.0) weight = WeightFormatter.fromKilograms(it.weight, weightUnit)
            if (reps == 0) reps = it.reps
            isAssisted = it.isAssisted
        }
    }

    var showPlates by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showRepsDialog by remember { mutableStateOf(false) }
    var loggedFlash by remember { mutableStateOf(false) }

    if (showWeightDialog) {
        NumberDialog("Enter weight", weight.toString(), KeyboardType.Number, onConfirm = { it.toDoubleOrNull()?.let { v -> weight = v }; showWeightDialog = false }, onDismiss = { showWeightDialog = false })
    }
    if (showRepsDialog) {
        NumberDialog("Enter reps", reps.toString(), KeyboardType.Number, onConfirm = { it.toIntOrNull()?.let { v -> reps = v }; showRepsDialog = false }, onDismiss = { showRepsDialog = false })
    }

    ApexScaffold(title = exercise, onBack = { nav.popBackStack() }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState).imePadding().padding(horizontal = 20.dp)
            ) {
                LastSessionSection(exercise, viewModel, weightUnit)
                Spacer(Modifier.height(16.dp))

                ApexPanel {
                    Column(Modifier.padding(24.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("SET $currentSet", style = MaterialTheme.typography.labelLarge, color = Apex.AccentSoft, letterSpacing = 1.sp)
                            TextButton(onClick = { showPlates = !showPlates }, contentPadding = PaddingValues(0.dp)) {
                                Text(if (showPlates) "HIDE PLATES" else "SHOW PLATES", style = MaterialTheme.typography.labelMedium, color = Apex.TextSecondary)
                            }
                        }

                        if (showPlates) {
                            PlateCalculatorCard(WeightFormatter.toKilograms(weight, weightUnit), userSettings.barWeight)
                            Spacer(Modifier.height(16.dp))
                        }

                        WeightStepper(weight, { weight = it }, WeightFormatter.label(weightUnit), WeightFormatter.step(weightUnit)) { showWeightDialog = true }
                        Spacer(Modifier.height(16.dp))
                        RepsStepper(reps, { reps = it }) { showRepsDialog = true }

                        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            lastSet?.let {
                                Text("Last: ${WeightFormatter.formatFromKilograms(it.weight, weightUnit)} x ${it.reps}", style = MaterialTheme.typography.bodyMedium, color = Apex.TextSecondary)
                            }
                            suggestedWeight?.let { suggestion ->
                                val displaySuggestion = WeightFormatter.fromKilograms(suggestion, weightUnit)
                                TextButton(onClick = { weight = displaySuggestion }, contentPadding = PaddingValues(0.dp)) {
                                    Text("Next: ${WeightFormatter.formatFromKilograms(suggestion, weightUnit)}", style = MaterialTheme.typography.bodyMedium, color = Apex.AccentSoft, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(isAssisted, { isAssisted = it }, colors = CheckboxDefaults.colors(checkedColor = Apex.Accent))
                            Text("Support / Assisted", style = MaterialTheme.typography.bodyMedium, color = Apex.TextSecondary)
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("RPE (EFFORT: 1-10)", style = MaterialTheme.typography.labelMedium, color = Apex.AccentSoft, modifier = Modifier.padding(bottom = 12.dp))
                        RpeSelector(rpe) { rpe = it }

                        Spacer(Modifier.height(24.dp))
                        if (!showNotesInput) {
                            TextButton(onClick = { showNotesInput = true }, contentPadding = PaddingValues(0.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Notes, null, modifier = Modifier.size(16.dp), tint = Apex.AccentSoft)
                                Spacer(Modifier.width(8.dp))
                                Text("ADD SET NOTES", style = MaterialTheme.typography.labelLarge, color = Apex.AccentSoft)
                            }
                        } else {
                            OutlinedTextField(
                                value = setNotes,
                                onValueChange = { setNotes = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Set notes...", color = Apex.TextMuted) },
                                textStyle = TextStyle(color = Apex.TextPrimary),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Apex.Accent,
                                    unfocusedBorderColor = Apex.Hairline,
                                    cursorColor = Apex.Accent
                                ),
                                maxLines = 2,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                AnimatedVisibility(visible = isTimerRunning) {
                    ApexPanel(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("REST TIMER", style = MaterialTheme.typography.labelLarge, color = Apex.AccentSoft)
                            Spacer(Modifier.height(8.dp))
                            Text("%d:%02d".format(timerSeconds / 60, timerSeconds % 60), style = MaterialTheme.typography.displaySmall, color = Apex.TextPrimary)
                            Spacer(Modifier.height(16.dp))
                            val progress = when {
                                timerInitialSeconds <= 0 -> 0f
                                timerSeconds <= 0 -> 1f
                                else -> timerSeconds.toFloat() / timerInitialSeconds.toFloat()
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = Apex.Accent,
                                trackColor = Apex.Surface4,
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.skipRestTimer() }) {
                                Text("SKIP TIMER", color = Apex.TextSecondary, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                ApexCta(
                    text = "LOG SET",
                    enabled = canLogSet,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.insertWorkout(
                            muscle = muscle,
                            exercise = exercise,
                            setNumber = currentSet,
                            reps = reps,
                            weight = WeightFormatter.toKilograms(weight, weightUnit),
                            isAssisted = isAssisted,
                            rpe = rpe,
                            notes = setNotes.takeIf { it.isNotBlank() }
                        )
                        reps = 0
                        rpe = null
                        setNotes = ""
                        showNotesInput = false
                        loggedFlash = true
                    }
                )

                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(100),
                    border = BorderStroke(1.dp, Apex.Hairline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Apex.TextSecondary)
                ) { Text("FINISH EXERCISE", style = MaterialTheme.typography.labelLarge) }

                Spacer(Modifier.height(40.dp))
            }

            SetLoggedFlash(loggedFlash) { loggedFlash = false }
        }
    }
}

@Composable
private fun SetLoggedFlash(visible: Boolean, onDone: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        scale.snapTo(0.6f); alpha.snapTo(0f)
        alpha.animateTo(1f, tween(120))
        scale.animateTo(1f, tween(160))
        kotlinx.coroutines.delay(500)
        alpha.animateTo(0f, tween(250))
        onDone()
    }
    if (!visible) return
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .scale(scale.value)
                .background(Apex.GlassStrong, RoundedCornerShape(100))
                .border(BorderStroke(1.dp, Apex.Positive.copy(alpha = 0.5f)), RoundedCornerShape(100))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(22.dp).background(Apex.Positive, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text("SET LOGGED", style = MaterialTheme.typography.labelLarge, color = Apex.TextPrimary, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun NumberDialog(title: String, initial: String, keyboard: KeyboardType, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var textValue by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Apex.Surface2,
        title = { Text(title, color = Apex.TextPrimary) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                keyboardOptions = KeyboardOptions(keyboardType = keyboard),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Apex.TextPrimary,
                    unfocusedTextColor = Apex.TextPrimary,
                    focusedBorderColor = Apex.Accent,
                    unfocusedBorderColor = Apex.Hairline,
                    cursorColor = Apex.Accent
                )
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(textValue) }) { Text("OK", color = Apex.AccentSoft) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = Apex.TextSecondary) } }
    )
}

@Composable
private fun RpeSelector(selectedRpe: Float?, onRpeSelected: (Float?) -> Unit) {
    val rpeValues = listOf(6.0f, 7.0f, 8.0f, 9.0f, 10.0f)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        rpeValues.forEach { value ->
            val isSelected = selectedRpe == value
            Surface(
                onClick = { onRpeSelected(if (isSelected) null else value) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Apex.Accent else Apex.Surface4,
                border = if (isSelected) null else BorderStroke(1.dp, Apex.Hairline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (value == 10f) "10" else value.toString(), color = if (isSelected) Color.White else Apex.TextPrimary, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun LastSessionSection(exerciseName: String, viewModel: WorkoutViewModel, unit: String) {
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val lastSessionSets by viewModel.getLastSessionSetsForExercise(exerciseName, currentSessionId ?: -1)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    if (lastSessionSets.isNotEmpty()) {
        ApexPanel(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), radius = Apex.radiusMd) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.History, null, tint = Apex.AccentSoft, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("LAST SESSION", color = Apex.AccentSoft, style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.height(16.dp))
                lastSessionSets.forEachIndexed { idx, set ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SET ${idx + 1}", color = Apex.TextSecondary, style = MaterialTheme.typography.labelLarge)
                        Text(
                            if (set.weight > 0) "${WeightFormatter.formatFromKilograms(set.weight, unit)} × ${set.reps}" else "BW × ${set.reps}",
                            color = Apex.TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeightStepper(value: Double, onValueChange: (Double) -> Unit, unit: String, step: Double, onLongClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(88.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        StepperButton("−", Apex.TextPrimary) { onValueChange((value - step).coerceAtLeast(0.0)) }
        Column(
            Modifier.weight(1f).combinedClickable(onClick = onLongClick, onLongClick = onLongClick),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("%.1f".format(value), style = MaterialTheme.typography.headlineLarge, color = Apex.TextPrimary)
            Text(unit.uppercase(), style = MaterialTheme.typography.labelMedium, color = Apex.TextSecondary)
        }
        StepperButton("+", Apex.AccentSoft) { onValueChange(value + step) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RepsStepper(value: Int, onValueChange: (Int) -> Unit, onLongClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(88.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        StepperButton("−", Apex.TextPrimary) { onValueChange((value - 1).coerceAtLeast(0)) }
        Column(
            Modifier.weight(1f).combinedClickable(onClick = onLongClick, onLongClick = onLongClick),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), style = MaterialTheme.typography.headlineLarge, color = Apex.TextPrimary)
            Text("REPS", style = MaterialTheme.typography.labelMedium, color = Apex.TextSecondary)
        }
        StepperButton("+", Apex.AccentSoft) { onValueChange(value + 1) }
    }
}

@Composable
private fun StepperButton(symbol: String, tint: Color, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Apex.Surface4)
    ) {
        Text(symbol, fontSize = 32.sp, fontWeight = FontWeight.Light, color = tint)
    }
}

@Composable
private fun PlateCalculatorCard(targetWeight: Double, barWeight: Double = 20.0) {
    val plates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    val sideLoad = (targetWeight - barWeight) / 2.0
    if (sideLoad > 0) {
        Column(
            Modifier.fillMaxWidth().background(Apex.Surface3, RoundedCornerShape(12.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("PLATES PER SIDE (${barWeight.toInt()}KG BAR)", color = Apex.AccentSoft, style = MaterialTheme.typography.labelSmall)
            var remaining = sideLoad
            for (plate in plates) {
                val count = (remaining / plate).toInt()
                if (count > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${plate}KG", color = Apex.TextSecondary, style = MaterialTheme.typography.labelLarge)
                        Text("× $count", color = Apex.TextPrimary, style = MaterialTheme.typography.titleMedium)
                    }
                    remaining -= count * plate
                }
            }
        }
    }
}
