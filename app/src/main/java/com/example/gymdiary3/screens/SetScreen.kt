package com.example.gymdiary3.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .imePadding()
            .padding(20.dp)
    ) {

        Text(
            text = exercise,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )

        Text(
            text = "Set $currentSet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        TextField(
            value = weight,
            onValueChange = { weight = it },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            ),
            label = {
                Text(
                    "Weight (${userSettings.weightUnit})",
                    color = MaterialTheme.colorScheme.primary
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().focusRequester(weightFocusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        TextField(
            value = reps,
            onValueChange = { reps = it },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            ),
            label = {
                Text(
                    "Reps",
                    color = MaterialTheme.colorScheme.primary
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth().focusRequester(repsFocusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            lastSet?.let {
                Text(
                    text = "Last: ${it.weight}${userSettings.weightUnit} × ${it.reps}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            suggestedWeight?.let { suggestion ->
                TextButton(
                    onClick = { weight = suggestion.toString() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    val unit = userSettings.weightUnit
                    Text(
                        text = "Suggest: ${suggestion}${unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = support,
                onCheckedChange = { support = it },
                modifier = Modifier.size(48.dp),
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                "Support / Assisted", 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(visible = isTimerRunning) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "REST TIMER",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "%d:%02d".format(timerSeconds / 60, timerSeconds % 60),
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 48.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(16.dp))

                    val progress = if (timerInitialSeconds > 0) {
                        timerSeconds.toFloat() / timerInitialSeconds.toFloat()
                    } else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = { viewModel.skipRestTimer() },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            "SKIP", 
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val r = reps.toIntOrNull() ?: return@Button
                    val w = weight.toDoubleOrNull() ?: return@Button

                    viewModel.insertWorkout(
                        muscle = muscle,
                        exercise = exercise,
                        setNumber = currentSet,
                        reps = r,
                        weight = w,
                        support = support
                    )

                    reps = ""
                    repsFocusRequester.requestFocus()
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                enabled = canLogSet,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "LOG SET",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { nav.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    "FINISH EXERCISE",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
