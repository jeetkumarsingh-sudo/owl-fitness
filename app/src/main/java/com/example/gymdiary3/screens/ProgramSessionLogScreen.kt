package com.example.gymdiary3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.model.SessionExerciseLog
import com.example.gymdiary3.ui.components.ApexPanel
import com.example.gymdiary3.ui.components.ApexScaffold
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.viewmodel.ProgramViewModel

@Composable
fun ProgramSessionLogScreen(
    nav: NavHostController,
    sessionId: Int,
    viewModel: ProgramViewModel = hiltViewModel()
) {
    val logs by viewModel.getLogsForSession(sessionId).collectAsStateWithLifecycle(emptyList())

    ApexScaffold(
        title = "Log Session",
        onBack = { nav.navigateUp() },
        actions = {
            TextButton(onClick = { nav.navigate("summary/$sessionId") }) {
                Text("FINISH", color = Apex.AccentSoft, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
        ) {
            items(logs) { log ->
                ExerciseLogRow(log) { updatedLog -> viewModel.updateExerciseLog(updatedLog) }
            }
        }
    }
}

@Composable
private fun ExerciseLogRow(
    log: SessionExerciseLog,
    onUpdate: (SessionExerciseLog) -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    val localWeights = remember(log.id) {
        mutableStateMapOf<Int, String>().apply {
            put(1, log.set1Weight?.toString() ?: "")
            put(2, log.set2Weight?.toString() ?: "")
            put(3, log.set3Weight?.toString() ?: "")
            put(4, log.set4Weight?.toString() ?: "")
            put(5, log.set5Weight?.toString() ?: "")
        }
    }
    val localReps = remember(log.id) {
        mutableStateMapOf<Int, String>().apply {
            put(1, log.set1Reps?.toString() ?: "")
            put(2, log.set2Reps?.toString() ?: "")
            put(3, log.set3Reps?.toString() ?: "")
            put(4, log.set4Reps?.toString() ?: "")
            put(5, log.set5Reps?.toString() ?: "")
        }
    }

    LaunchedEffect(log) {
        if (localWeights[1] != (log.set1Weight?.toString() ?: "")) localWeights[1] = log.set1Weight?.toString() ?: ""
        if (localWeights[2] != (log.set2Weight?.toString() ?: "")) localWeights[2] = log.set2Weight?.toString() ?: ""
        if (localWeights[3] != (log.set3Weight?.toString() ?: "")) localWeights[3] = log.set3Weight?.toString() ?: ""
        if (localWeights[4] != (log.set4Weight?.toString() ?: "")) localWeights[4] = log.set4Weight?.toString() ?: ""
        if (localWeights[5] != (log.set5Weight?.toString() ?: "")) localWeights[5] = log.set5Weight?.toString() ?: ""
        if (localReps[1] != (log.set1Reps?.toString() ?: "")) localReps[1] = log.set1Reps?.toString() ?: ""
        if (localReps[2] != (log.set2Reps?.toString() ?: "")) localReps[2] = log.set2Reps?.toString() ?: ""
        if (localReps[3] != (log.set3Reps?.toString() ?: "")) localReps[3] = log.set3Reps?.toString() ?: ""
        if (localReps[4] != (log.set4Reps?.toString() ?: "")) localReps[4] = log.set4Reps?.toString() ?: ""
        if (localReps[5] != (log.set5Reps?.toString() ?: "")) localReps[5] = log.set5Reps?.toString() ?: ""
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Apex.TextPrimary,
        unfocusedTextColor = Apex.TextPrimary,
        focusedBorderColor = Apex.Accent,
        unfocusedBorderColor = Apex.Hairline,
        cursorColor = Apex.Accent,
        focusedContainerColor = Apex.Surface3,
        unfocusedContainerColor = Apex.Surface3
    )

    ApexPanel(radius = Apex.radiusMd) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(log.exerciseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Apex.TextPrimary)
                IconButton(onClick = { showDetails = !showDetails }) {
                    Icon(Icons.Default.Info, contentDescription = "Details", tint = Apex.AccentDeep)
                }
            }

            if (showDetails) {
                Column(Modifier.padding(bottom = 12.dp)) {
                    if (!log.notes.isNullOrBlank()) {
                        Text("Notes: ${log.notes}", style = MaterialTheme.typography.bodySmall, color = Apex.TextSecondary)
                    }
                    Text("Goal: Follow prescribed reps and RIR targets.", style = MaterialTheme.typography.bodySmall, color = Apex.TextSecondary, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { i ->
                    val setNum = i + 1
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SET $setNum", style = MaterialTheme.typography.labelSmall, color = Apex.TextMuted)
                        OutlinedTextField(
                            value = localWeights[setNum] ?: "",
                            onValueChange = { localWeights[setNum] = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = { Text("kg", fontSize = 10.sp) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true,
                            colors = fieldColors
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = localReps[setNum] ?: "",
                            onValueChange = { localReps[setNum] = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("reps", fontSize = 10.sp) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true,
                            colors = fieldColors
                        )
                    }
                }
            }

            Button(
                onClick = {
                    var updated = log
                    for (i in 1..5) {
                        updated = updateSetWeight(updated, i, localWeights[i]?.toDoubleOrNull())
                        updated = updateSetReps(updated, i, localReps[i]?.toIntOrNull())
                    }
                    onUpdate(updated)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Apex.AccentWash, contentColor = Apex.AccentSoft)
            ) {
                Text("SAVE EXERCISE", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun updateSetWeight(log: SessionExerciseLog, setNum: Int, weight: Double?): SessionExerciseLog = when (setNum) {
    1 -> log.copy(set1Weight = weight)
    2 -> log.copy(set2Weight = weight)
    3 -> log.copy(set3Weight = weight)
    4 -> log.copy(set4Weight = weight)
    else -> log.copy(set5Weight = weight)
}

private fun updateSetReps(log: SessionExerciseLog, setNum: Int, reps: Int?): SessionExerciseLog = when (setNum) {
    1 -> log.copy(set1Reps = reps)
    2 -> log.copy(set2Reps = reps)
    3 -> log.copy(set3Reps = reps)
    4 -> log.copy(set4Reps = reps)
    else -> log.copy(set5Reps = reps)
}
