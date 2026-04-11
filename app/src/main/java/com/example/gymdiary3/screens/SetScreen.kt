package com.example.gymdiary3.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.WorkoutViewModel

@Composable
fun SetScreen(
    nav: NavHostController,
    muscle: String,
    exercise: String,
    viewModel: WorkoutViewModel
) {
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var support by remember { mutableStateOf(false) }

    val repsFocusRequester = remember { FocusRequester() }
    val weightFocusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    val lastSet by viewModel.lastSet.collectAsStateWithLifecycle()
    val suggestedWeight by viewModel.suggestedWeight.collectAsStateWithLifecycle()
    val currentSet by viewModel.currentSet.collectAsStateWithLifecycle()
    
    val isTimerRunning by viewModel.isRestTimerRunning.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.restTimerSeconds.collectAsStateWithLifecycle()
    
    val userSettings by if (viewModel.settingsRepository != null) {
        viewModel.settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())
    } else {
        remember { mutableStateOf(com.example.gymdiary3.domain.settings.UserSettings()) }
    }

    var timerInitialSeconds by remember { mutableIntStateOf(userSettings.defaultRestSeconds) }
    
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            timerInitialSeconds = userSettings.defaultRestSeconds
        }
    }

    val canLogSet = remember(reps, weight) {
        reps.isNotEmpty() && weight.isNotEmpty() && 
        reps.toIntOrNull() != null && weight.toDoubleOrNull() != null
    }

    LaunchedEffect(exercise) {
        viewModel.loadLastSet(exercise)
        viewModel.updateSetNumber(exercise)
    }

    LaunchedEffect(Unit) {
        repsFocusRequester.requestFocus()
    }

    LaunchedEffect(lastSet) {
        lastSet?.let {
            if (weight.isEmpty()) {
                weight = it.weight.toString()
            }
            support = it.support
        }
    }

    var showPlates by remember { mutableStateOf(false) }

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
        
        LastWeekSetsSection(exercise, viewModel)

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
                    PlateCalculatorCard(weight.toDoubleOrNull() ?: 0.0)
                    Spacer(Modifier.height(16.dp))
                }

                TextField(
                    value = weight,
                    onValueChange = { weight = it },
                    textStyle = TextStyle(color = OwlColors.TextPrimary, fontSize = 20.sp),
                    label = { Text("Weight (${userSettings.weightUnit})", color = OwlColors.TextSecondary) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = OwlColors.InputBg,
                        unfocusedContainerColor = OwlColors.InputBg,
                        focusedIndicatorColor = OwlColors.Purple,
                        unfocusedIndicatorColor = OwlColors.BorderSubtle,
                        cursorColor = OwlColors.Purple
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(weightFocusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                TextField(
                    value = reps,
                    onValueChange = { reps = it },
                    textStyle = TextStyle(color = OwlColors.TextPrimary, fontSize = 20.sp),
                    label = { Text("Reps", color = OwlColors.TextSecondary) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = OwlColors.InputBg,
                        unfocusedContainerColor = OwlColors.InputBg,
                        focusedIndicatorColor = OwlColors.Purple,
                        unfocusedIndicatorColor = OwlColors.BorderSubtle,
                        cursorColor = OwlColors.Purple
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(repsFocusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
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
                            onClick = { weight = suggestion.toString() },
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
                        checked = support,
                        onCheckedChange = { support = it },
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
                val r = reps.toIntOrNull() ?: return@Button
                val w = weight.toDoubleOrNull() ?: return@Button
                viewModel.insertWorkout(muscle, exercise, currentSet, r, w, support)
                reps = ""
                repsFocusRequester.requestFocus()
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
fun LastWeekSetsSection(exerciseName: String, viewModel: WorkoutViewModel) {
    val lastWeekSets by viewModel.getLastWeekSetsForExercise(exerciseName)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    if (lastWeekSets.isNotEmpty()) {
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
                    Text("LAST WEEK PERFORMANCE", color = OwlColors.PurpleSoft,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                Spacer(Modifier.height(12.dp))

                lastWeekSets.forEachIndexed { idx, set ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Set ${idx + 1}", color = OwlColors.TextMuted, fontSize = 13.sp)
                        Text(
                            if (set.weight > 0) "${"%.1f".format(set.weight)}kg x ${set.reps}" else "BW x ${set.reps}",
                            color = OwlColors.TextPrimary,
                            fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                        )
                        Text("${(set.weight * set.reps).toInt()}kg", color = OwlColors.TextMuted, fontSize = 12.sp)
                    }
                }
            }
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
