package com.example.gymdiary3.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.ProgramViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramSessionLogScreen(
    nav: NavHostController,
    sessionId: Int,
    viewModel: ProgramViewModel = hiltViewModel()
) {
    val logs by viewModel.getLogsForSession(sessionId).collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LOG SESSION", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OwlColors.DeepBg,
                    titleContentColor = OwlColors.TextPrimary,
                    navigationIconContentColor = OwlColors.TextPrimary
                ),
                actions = {
                    TextButton(onClick = { nav.navigate("summary/$sessionId") }) {
                        Text("FINISH", color = OwlColors.Purple, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = OwlColors.DeepBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(logs) { log ->
                ExerciseLogRow(log) { updatedLog ->
                    viewModel.updateExerciseLog(updatedLog)
                }
            }
        }
    }
}

@Composable
fun ExerciseLogRow(
    log: SessionExerciseLog,
    onUpdate: (SessionExerciseLog) -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    Surface(
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    log.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OwlColors.TextPrimary
                )
                IconButton(onClick = { showDetails = !showDetails }) {
                    Icon(Icons.Default.Info, contentDescription = "Details", tint = OwlColors.PurpleDim)
                }
            }

            if (showDetails) {
                // Display progression rules and notes
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    if (!log.notes.isNullOrBlank()) {
                        Text(
                            text = "Notes: ${log.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OwlColors.TextSecondary
                        )
                    }
                    // We need to fetch the progression rule from the ProgramExercise model.
                    // For now, let's assume we want to show a generic placeholder if it's missing,
                    // but we should ideally pass it in the SessionExerciseLog if we want it visible here.
                    Text(
                        text = "Goal: Follow prescribed reps and RIR targets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OwlColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Set headers
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { i ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SET ${i + 1}", style = MaterialTheme.typography.labelSmall, color = OwlColors.TextMuted)
                        
                        val weight = when(i) {
                            0 -> log.set1Weight; 1 -> log.set2Weight; 2 -> log.set3Weight; 3 -> log.set4Weight; else -> log.set5Weight
                        }
                        val reps = when(i) {
                            0 -> log.set1Reps; 1 -> log.set2Reps; 2 -> log.set3Reps; 3 -> log.set4Reps; else -> log.set5Reps
                        }

                        OutlinedTextField(
                            value = weight?.toString() ?: "",
                            onValueChange = { val w = it.toDoubleOrNull(); onUpdate(updateSetWeight(log, i + 1, w)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = { Text("kg", fontSize = 10.sp) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = reps?.toString() ?: "",
                            onValueChange = { val r = it.toIntOrNull(); onUpdate(updateSetReps(log, i + 1, r)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("reps", fontSize = 10.sp) },
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

fun updateSetWeight(log: SessionExerciseLog, setNum: Int, weight: Double?): SessionExerciseLog {
    return when(setNum) {
        1 -> log.copy(set1Weight = weight)
        2 -> log.copy(set2Weight = weight)
        3 -> log.copy(set3Weight = weight)
        4 -> log.copy(set4Weight = weight)
        else -> log.copy(set5Weight = weight)
    }
}

fun updateSetReps(log: SessionExerciseLog, setNum: Int, reps: Int?): SessionExerciseLog {
    return when(setNum) {
        1 -> log.copy(set1Reps = reps)
        2 -> log.copy(set2Reps = reps)
        3 -> log.copy(set3Reps = reps)
        4 -> log.copy(set4Reps = reps)
        else -> log.copy(set5Reps = reps)
    }
}
