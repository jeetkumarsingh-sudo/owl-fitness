package com.example.gymdiary3.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.ui.components.ApexCard
import com.example.gymdiary3.ui.components.ApexPrimaryButton
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
    
    val userSettings by viewModel.settingsRepository.userSettingsFlow
        .collectAsStateWithLifecycle(UserSettings())
    val weightUnit = userSettings.weightUnit

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
    }

    LaunchedEffect(lastSet, weightUnit) {
        lastSet?.let {
            if (weight == 0.0) {
                weight = WeightFormatter.fromKilograms(it.weight, weightUnit)
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
            containerColor = OwlColors.CardBg,
            title = { Text("Enter Weight", color = OwlColors.TextPrimary) },
            text = {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OwlColors.TextPrimary,
                        unfocusedTextColor = OwlColors.TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    textValue.toDoubleOrNull()?.let { weight = it }
                    showWeightDialog = false
                }) { Text("OK", color = OwlColors.Purple) }
            }
        )
    }

    if (showRepsDialog) {
        var textValue by remember { mutableStateOf(reps.toString()) }
        AlertDialog(
            onDismissRequest = { showRepsDialog = false },
            containerColor = OwlColors.CardBg,
            title = { Text("Enter Reps", color = OwlColors.TextPrimary) },
            text = {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OwlColors.TextPrimary,
                        unfocusedTextColor = OwlColors.TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    textValue.toIntOrNull()?.let { reps = it }
                    showRepsDialog = false
                }) { Text("OK", color = OwlColors.Purple) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OwlColors.DeepBg)
            .verticalScroll(scrollState)
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = exercise.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = OwlColors.TextPrimary,
            maxLines = 2
        )
        
        Spacer(Modifier.height(16.dp))
        
        LastSessionSection(exercise, viewModel, weightUnit)

        Spacer(Modifier.height(16.dp))

        ApexCard {
            Column(Modifier.padding(24.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SET $currentSet",
                        style = MaterialTheme.typography.labelLarge,
                        color = OwlColors.Purple
                    )
                    
                    TextButton(onClick = { showPlates = !showPlates }, contentPadding = PaddingValues(0.dp)) {
                        Text(
                            if (showPlates) "HIDE PLATES" else "SHOW PLATES",
                            style = MaterialTheme.typography.labelMedium,
                            color = OwlColors.TextSecondary
                        )
                    }
                }

                if (showPlates) {
                    PlateCalculatorCard(WeightFormatter.toKilograms(weight, weightUnit), userSettings.barWeight)
                    Spacer(Modifier.height(16.dp))
                }

                WeightStepper(
                    value = weight,
                    onValueChange = { weight = it },
                    unit = WeightFormatter.label(weightUnit),
                    step = WeightFormatter.step(weightUnit),
                    onLongClick = { showWeightDialog = true }
                )

                Spacer(Modifier.height(16.dp))

                RepsStepper(
                    value = reps,
                    onValueChange = { reps = it },
                    onLongClick = { showRepsDialog = true }
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    lastSet?.let {
                        Text(
                            text = "Last: ${WeightFormatter.formatFromKilograms(it.weight, weightUnit)} x ${it.reps}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OwlColors.TextSecondary
                        )
                    }

                    suggestedWeight?.let { suggestion ->
                        val displaySuggestion = WeightFormatter.fromKilograms(suggestion, weightUnit)
                        TextButton(
                            onClick = { weight = displaySuggestion },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Next: ${WeightFormatter.formatFromKilograms(suggestion, weightUnit)}",
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

                Spacer(Modifier.height(24.dp))
                
                // RPE Selection
                Text(
                    "RPE (EFFORT: 1-10)", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = OwlColors.PurpleSoft,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                RpeSelector(
                    selectedRpe = rpe,
                    onRpeSelected = { rpe = it }
                )

                Spacer(Modifier.height(24.dp))

                // Notes toggle
                if (!showNotesInput) {
                    TextButton(
                        onClick = { showNotesInput = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Notes, null, modifier = Modifier.size(16.dp), tint = OwlColors.Purple)
                        Spacer(Modifier.width(8.dp))
                        Text("ADD SET NOTES", style = MaterialTheme.typography.labelLarge, color = OwlColors.Purple)
                    }
                } else {
                    OutlinedTextField(
                        value = setNotes,
                        onValueChange = { setNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Set notes...", color = OwlColors.TextMuted) },
                        textStyle = TextStyle(color = OwlColors.TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OwlColors.Purple,
                            unfocusedBorderColor = OwlColors.BorderSubtle
                        ),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(visible = isTimerRunning) {
            ApexCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("REST TIMER", style = MaterialTheme.typography.labelLarge, color = OwlColors.Purple)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "%d:%02d".format(timerSeconds / 60, timerSeconds % 60),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp),
                        color = OwlColors.TextPrimary
                    )
                    Spacer(Modifier.height(16.dp))
                    val progress = when {
                        timerInitialSeconds <= 0 -> 0f
                        timerSeconds <= 0 -> 1f
                        else -> timerSeconds.toFloat() / timerInitialSeconds.toFloat()
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = OwlColors.Purple,
                        trackColor = OwlColors.BorderSubtle,
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { viewModel.skipRestTimer() }) {
                        Text("SKIP TIMER", color = OwlColors.TextSecondary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        ApexPrimaryButton(
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
            }
        )

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(100),
            border = BorderStroke(1.dp, OwlColors.BorderSubtle),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OwlColors.TextSecondary)
        ) {
            Text("FINISH EXERCISE", style = MaterialTheme.typography.labelLarge)
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun RpeSelector(
    selectedRpe: Float?,
    onRpeSelected: (Float?) -> Unit
) {
    val rpeValues = listOf(6.0f, 7.0f, 8.0f, 9.0f, 10.0f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rpeValues.forEach { value ->
            val isSelected = selectedRpe == value
            Surface(
                onClick = { onRpeSelected(if (isSelected) null else value) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) OwlColors.Purple else OwlColors.CardBgAlt,
                border = if (isSelected) null else BorderStroke(1.dp, OwlColors.BorderSubtle)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (value == 10f) "10" else value.toString(),
                        color = if (isSelected) Color.White else OwlColors.TextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun LastSessionSection(exerciseName: String, viewModel: WorkoutViewModel, unit: String) {
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val lastSessionSets by viewModel.getLastSessionSetsForExercise(
        exerciseName, 
        currentSessionId ?: -1
    ).collectAsStateWithLifecycle(initialValue = emptyList())

    if (lastSessionSets.isNotEmpty()) {
        ApexCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.History, null, tint = OwlColors.PurpleSoft, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "LAST SESSION",
                        color = OwlColors.PurpleSoft,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(Modifier.height(16.dp))
                lastSessionSets.forEachIndexed { idx, set ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SET ${idx + 1}", color = OwlColors.TextSecondary, style = MaterialTheme.typography.labelLarge)
                        Text(
                            if (set.weight > 0) "${WeightFormatter.formatFromKilograms(set.weight, unit)} × ${set.reps}"
                            else "BW × ${set.reps}",
                            color = OwlColors.TextPrimary,
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
fun WeightStepper(
    value: Double,
    onValueChange: (Double) -> Unit,
    unit: String,
    modifier: Modifier = Modifier,
    step: Double = 2.5,
    onLongClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth().height(88.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = { onValueChange((value - step).coerceAtLeast(0.0)) },
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = OwlColors.CardBgAlt)
        ) {
            Text("−", fontSize = 32.sp, fontWeight = FontWeight.Light, color = OwlColors.TextPrimary)
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = { onLongClick() },
                    onLongClick = onLongClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%.1f".format(value),
                style = MaterialTheme.typography.headlineLarge,
                color = OwlColors.TextPrimary
            )
            Text(
                text = unit.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = OwlColors.TextSecondary
            )
        }
        
        FilledTonalButton(
            onClick = { onValueChange(value + step) },
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = OwlColors.CardBgAlt)
        ) {
            Text("+", fontSize = 32.sp, fontWeight = FontWeight.Light, color = OwlColors.Purple)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable  
fun RepsStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth().height(88.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = { onValueChange((value - 1).coerceAtLeast(0)) },
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = OwlColors.CardBgAlt)
        ) {
            Text("−", fontSize = 32.sp, fontWeight = FontWeight.Light, color = OwlColors.TextPrimary)
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = { onLongClick() },
                    onLongClick = onLongClick
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = OwlColors.TextPrimary
            )
            Text(
                text = "REPS",
                style = MaterialTheme.typography.labelMedium,
                color = OwlColors.TextSecondary
            )
        }
        
        FilledTonalButton(
            onClick = { onValueChange(value + 1) },
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = OwlColors.CardBgAlt)
        ) {
            Text("+", fontSize = 32.sp, fontWeight = FontWeight.Light, color = OwlColors.Purple)
        }
    }
}

@Composable
fun PlateCalculatorCard(targetWeight: Double, barWeight: Double = 20.0) {
    val plates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
    val sideLoad = (targetWeight - barWeight) / 2.0

    if (sideLoad > 0) {
        Column(
            modifier = Modifier.fillMaxWidth().background(OwlColors.CardBgAlt, RoundedCornerShape(12.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("PLATES PER SIDE (${barWeight.toInt()}KG BAR)", color = OwlColors.PurpleSoft, style = MaterialTheme.typography.labelSmall)
            var remaining = sideLoad
            for (plate in plates) {
                val count = (remaining / plate).toInt()
                if (count > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${plate}KG", color = OwlColors.TextSecondary, style = MaterialTheme.typography.labelLarge)
                        Text("× $count", color = OwlColors.TextPrimary, style = MaterialTheme.typography.titleMedium)
                    }
                    remaining -= count * plate
                }
            }
        }
    }
}
