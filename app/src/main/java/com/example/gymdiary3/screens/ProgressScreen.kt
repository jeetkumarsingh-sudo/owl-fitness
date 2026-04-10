package com.example.gymdiary3.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.data.WorkoutSet
import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.presentation.state.ExerciseUiState
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.ui.components.EmptyState
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    Log.d("PERF", "ProgressScreen recomposing")
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val grouped = remember(workouts) { workouts.groupBy { it.exercise } }
    val sdf = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    val userSettings by if (viewModel.settingsRepository != null) {
        viewModel.settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())
    } else {
        remember { mutableStateOf(com.example.gymdiary3.domain.settings.UserSettings()) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = { Text("PROGRESS & PRS", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        val exercisesList = remember(grouped) { grouped.keys.toList().sorted() }
        
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WeeklyVolumeAnalysisCard(sessions, userSettings.weightUnit)
            }

            if (exercisesList.isEmpty()) {
                item {
                    EmptyState(
                        message = "Start a workout to track your progress!",
                        title = "NO PERFORMANCE DATA",
                        modifier = Modifier.padding(top = 64.dp)
                    )
                }
            }

            items(
                items = exercisesList,
                key = { it }
            ) { exercise ->
                val uiState = viewModel.getExerciseUiState(exercise)
                val sets = grouped[exercise] ?: emptyList()
                ExerciseProgressCard(exercise, uiState, sets, sdf, userSettings.weightUnit)
            }
            
            item {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("BACK", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun WeeklyVolumeAnalysisCard(sessions: List<SessionWithSets>, unit: String) {
    val weeklyVolume: Map<String, Double> = remember(sessions) { WorkoutAnalyzer.getWeeklyVolume(sessions) }
    val sortedWeeks = remember(weeklyVolume) { weeklyVolume.keys.toList().sortedDescending() }

    if (sortedWeeks.isNotEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "WEEKLY VOLUME ANALYSIS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(16.dp))

                val currentWeekKey = sortedWeeks.first()
                val currentVolume = weeklyVolume[currentWeekKey] ?: 0.0
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Week", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("${currentVolume.toInt()} $unit", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    if (sortedWeeks.size >= 2) {
                        val prevWeekKey = sortedWeeks[1]
                        val prevVolume = weeklyVolume[prevWeekKey] ?: 0.0
                        val diff = currentVolume - prevVolume
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("vs Last Week", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(
                                text = (if (diff >= 0) "+" else "") + "${diff.toInt()} $unit",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseProgressCard(
    exercise: String,
    uiState: ExerciseUiState,
    sets: List<WorkoutSet>,
    sdf: SimpleDateFormat,
    unit: String
) {
    Log.d("PERF", "ExerciseProgressCard recomposing: $exercise")
    
    val sortedSets = remember(sets) { sets.sortedByDescending { it.date } }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    exercise.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (uiState.isPR) {
                    Text(
                        "NEW PR",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFC107)
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Best 1RM: ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${uiState.best1RM.toInt()} $unit",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("RECENT TREND", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                val trendColor = when {
                    uiState.trend > 0 -> Color(0xFF4CAF50)
                    uiState.trend < 0 -> Color(0xFFFF5252)
                    else -> Color.Gray
                }
                
                Text(
                    text = if (uiState.trend != 0.0) {
                        (if (uiState.trend > 0) "+" else "") + "${uiState.trend}$unit since last session"
                    } else {
                        "Same weight as last session"
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = trendColor
                )
            }
            
            Text(
                text = uiState.recommendation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(12.dp))
            
            sortedSets.take(3).forEach { set ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(sdf.format(Date(set.date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${set.weight}$unit × ${set.reps}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
