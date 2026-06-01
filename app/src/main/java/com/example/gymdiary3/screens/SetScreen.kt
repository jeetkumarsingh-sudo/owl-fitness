package com.example.gymdiary3.screens

import androidx.compose.foundation.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.WorkoutViewModel

import androidx.hilt.navigation.compose.hiltViewModel

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

    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val lastSet by viewModel.lastSet.collectAsStateWithLifecycle()
    val suggestedWeight by viewModel.suggestedWeight.collectAsStateWithLifecycle()
    val currentSet by viewModel.currentSet.collectAsStateWithLifecycle()
    
    val isTimerRunning by viewModel.isRestTimerRunning.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.restTimerSeconds.collectAsStateWithLifecycle()
    
    val userSettings by viewModel.settingsRepository.userSettingsFlow
        .collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())

    var timerInitialSeconds by remember { mutableIntStateOf(userSettings.defaultRestSeconds) }
    
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            timerInitialSeconds = userSettings.defaultRestSeconds
        }
    }

    val canLogSet = remember(reps, weight) {
        reps > 0 && weight >= 0
    }

    LaunchedEffect(exercise) {
        viewModel.loadLastSet(exercise)
        viewModel.updateSetNumber(exercise)
    }

    LaunchedEffect(lastSet) {
        lastSet?.let {
            if (weight == 0.0) {
                weight = it.weight
            }
            if (reps == 0) {
                reps = it.reps
            }
            isAssisted = it.isAssisted
        }
    }

    var showPlates by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showRepsDialog by remember { mutableStateOf(false) }

    if (showWeightDialog) {
        var textValue by remember { mutableStateOf(weight.toString()) }
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text("Enter Weight") },
            text = {
                TextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    textValue.toDoubleOrNull()?.let { weight = it }
                    showWeightDialog = false
                }) { Text("OK") }
            }
        )
    }

    if (showRepsDialog) {
        var textValue by remember { mutableStateOf(reps.toString()) }
        AlertDialog(
            onDismissRequest = { showRepsDialog = false },
            title = { Text("Enter Reps") },
            text = {
                TextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    textValue.toIntOrNull()?.let { reps = it }
                    showRepsDialog = false
                }) { Text("OK") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OwlColors.DeepBg)
            .verticalScroll(scrollState)
            .imePadding()
            .padding(20.dp)
    ) {
        Text(
            text = exercise,
            style = MaterialTheme.typography.headlineMedium.copy(color = OwlColors.TextPrimary),
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
        
        Spacer(Modifier.height(8.dp))
        
        LastSessionSection(exercise, viewModel)

        Spacer(Modifier.height(16.dp))

        Surface(
            color = OwlColors.CardBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OwlColors.BorderSubtle)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SET $currentSet",
                        style = MaterialTheme.typography.labelLarge,
                        color = OwlColors.Purple,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TextButton(onClick = { showPlates = !showPlates }) {
                        Text(
                            if (showPlates) "HIDE PLATES" else "SHOW PLATES",
                            style = MaterialTheme.typography.labelMedium,
                            color = OwlColors.TextSecondary
                        )
                    }
                }

                if (showPlates) {
                    PlateCalculatorCard(weight)
                    Spacer(Modifier.height(16.dp))
                }

                WeightStepper(
                    value = weight,
                    onValueChange = { weight = it },
                    unit = userSettings.weightUnit,
                    onLongClick = { showWeightDialog = true }
                )

                Spacer(Modifier.height(16.dp))

                RepsStepper(
                    value = reps,
                    onValueChange = { reps = it },
                    onLongClick = { showRepsDialog = true }
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    lastSet?.let {
                        Text(
                            text = "Last: ${it.weight}${userSettings.weightUnit} × ${it.reps}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OwlColors.TextSecondary
                        )
                    }

                    suggestedWeight?.let { suggestion ->
                        TextButton(
                            onClick = { weight = suggestion },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Next: ${suggestion}${userSettings.weightUnit}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OwlColors.Purple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAssisted,
                        onCheckedChange = { isAssisted = it },
                        colors = CheckboxDefaults.colors(checkedColor = OwlColors.Purple)
                    )
                    Text("Support / Assisted", style = MaterialTheme.typography.bodyMedium, color = OwlColors.TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(visible = isTimerRunning) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                color = OwlColors.CardBg,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, OwlColors.PurpleDim)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("REST TIMER", style = MaterialTheme.typography.labelMedium, color = OwlColors.Purple, letterSpacing = 2.sp)
                    Text(
                        text = "%d:%02d".format(timerSeconds / 60, timerSeconds % 60),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp),
                        color = OwlColors.TextPrimary
                    )
                    val progress = if (timerInitialSeconds > 0) timerSeconds.toFloat() / timerInitialSeconds.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = OwlColors.Purple,
                        trackColor = OwlColors.BorderSubtle,
                        strokeCap = StrokeCap.Round
                    )
                    TextButton(onClick = { viewModel.skipRestTimer() }) {
                        Text("SKIP", color = OwlColors.TextSecondary)
                    }
                }
            }
        }

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.insertWorkout(muscle, exercise, currentSet, reps, weight, isAssisted)
                reps = 0
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            enabled = canLogSet,
            colors = ButtonDefaults.buttonColors(containerColor = OwlColors.Purple, disabledContainerColor = OwlColors.PurpleDim),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("LOG SET", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, OwlColors.BorderSubtle),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OwlColors.TextSecondary)
        ) {
            Text("FINISH EXERCISE", style = MaterialTheme.typography.bodyLarge)
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun LastSessionSection(exerciseName: String, viewModel: WorkoutViewModel) {
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val lastSessionSets by viewModel.getLastSessionSetsForExercise(
        exerciseName, 
        currentSessionId ?: -1
    ).collectAsStateWithLifecycle(initialValue = emptyList())

    if (lastSessionSets.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = OwlColors.CardBgAlt),
            border = BorderStroke(1.dp, OwlColors.BorderSubtle)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.History, null, tint = OwlColors.PurpleSoft, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "LAST SESSION",
                        color = OwlColors.PurpleSoft,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                lastSessionSets.forEachIndexed { idx, set ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Set ${idx + 1}", color = OwlColors.TextMuted, fontSize = 13.sp)
                        Text(
                            if (set.weight > 0) "${"%.1f".format(set.weight)}kg × ${set.reps}"
                            else "BW × ${set.reps}",
                            color = OwlColors.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeightStepper(
    value: Double,
    onValueChange: (Double) -> Unit,
    unit: String,
    step: Double = 2.5,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().height(72.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = { onValueChange((value - step).coerceAtLeast(0.0)) },
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = OwlColors.CardBgAlt)
        ) {
            Text("−", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OwlColors.TextPrimary)
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%.1f".format(value),
                style = MaterialTheme.typography.headlineMedium,
                color = OwlColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelMedium,
                color = OwlColors.TextSecondary
            )
        }
        
        FilledTonalButton(
            onClick = { onValueChange(value + step) },
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = OwlColors.CardBgAlt)
        ) {
            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OwlColors.Purple)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable  
fun RepsStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().height(72.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = { onValueChange((value - 1).coerceAtLeast(1)) },
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = OwlColors.CardBgAlt)
        ) {
            Text("−", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OwlColors.TextPrimary)
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = OwlColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "REPS",
                style = MaterialTheme.typography.labelMedium,
                color = OwlColors.TextSecondary
            )
        }
        
        FilledTonalButton(
            onClick = { onValueChange(value + 1) },
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = OwlColors.CardBgAlt)
        ) {
            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = OwlColors.Purple)
        }
    }
}

@Composable
fun PlateCalculatorCard(targetWeight: Double, barWeight: Double = 20.0) {
    val plates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    val sideLoad = (targetWeight - barWeight) / 2.0

    if (sideLoad > 0) {
        Column(
            modifier = Modifier.fillMaxWidth().background(OwlColors.CardBgAlt, RoundedCornerShape(8.dp)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("PLATES PER SIDE (${barWeight.toInt()}kg Bar)", color = OwlColors.PurpleSoft, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            var remaining = sideLoad
            for (plate in plates) {
                val count = (remaining / plate).toInt()
                if (count > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${plate}kg", color = OwlColors.TextSecondary, fontSize = 13.sp)
                        Text("x $count", color = OwlColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    remaining -= count * plate
                }
            }
        }
    }
}
