package com.example.gymdiary3.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
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
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.ui.components.EmptyState
import com.example.gymdiary3.ui.components.PrBadge
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val exerciseUiStates by viewModel.exerciseUiStates.collectAsStateWithLifecycle()
    val grouped = remember(workouts) { workouts.groupBy { it.exercise } }
    val sdf = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    val userSettings by if (viewModel.settingsRepository != null) {
        viewModel.settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())
    } else {
        remember { mutableStateOf(com.example.gymdiary3.domain.settings.UserSettings()) }
    }

    Scaffold(
        containerColor = OwlColors.DeepBg,
        topBar = {
            TopAppBar(
                title = { Text("PROGRESS & PRS", fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OwlColors.DeepBg,
                    titleContentColor = OwlColors.TextPrimary
                )
            )
        }
    ) { padding ->
        val exercisesList = remember(grouped) { grouped.keys.toList().sorted() }
        
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
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
                val uiState = exerciseUiStates[exercise] ?: return@items
                val sets = grouped[exercise] ?: emptyList()
                ExerciseProgressCard(exercise, uiState, sets, sdf, userSettings.weightUnit) {
                    nav.navigate("analytics/${Uri.encode(exercise)}")
                }
            }
            
            item {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OwlColors.BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OwlColors.TextSecondary)
                ) {
                    Text("BACK", style = MaterialTheme.typography.bodyLarge)
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
            color = OwlColors.CardBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OwlColors.BorderSubtle)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "WEEKLY VOLUME ANALYSIS",
                    style = MaterialTheme.typography.labelMedium,
                    color = OwlColors.PurpleSoft,
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
                        Text("Current Week", style = MaterialTheme.typography.labelSmall, color = OwlColors.TextSecondary)
                        Text("${currentVolume.toInt()} $unit", style = MaterialTheme.typography.headlineSmall, color = OwlColors.TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    if (sortedWeeks.size >= 2) {
                        val prevWeekKey = sortedWeeks[1]
                        val prevVolume = weeklyVolume[prevWeekKey] ?: 0.0
                        val diff = currentVolume - prevVolume
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("vs Last Week", style = MaterialTheme.typography.labelSmall, color = OwlColors.TextSecondary)
                            Text(
                                text = (if (diff >= 0) "+" else "") + "${diff.toInt()} $unit",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (diff >= 0) OwlColors.GreenPositive else OwlColors.RedNegative,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Mini bar chart showing last 6 weeks
                val weeklySorted = remember(weeklyVolume) { sortedWeeks.take(6).reversed() }
                if (weeklySorted.size >= 2) {
                    val maxVol = weeklySorted.mapNotNull { weeklyVolume[it] }.maxOrNull() ?: 1.0
                    Row(
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklySorted.forEach { weekKey ->
                            val vol = weeklyVolume[weekKey] ?: 0.0
                            val barHeightFraction = (vol / maxVol).coerceIn(0.05, 1.0).toFloat()
                            val isCurrentWeek = weekKey == sortedWeeks.first()
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(barHeightFraction)
                                        .background(
                                            color = if (isCurrentWeek) OwlColors.Purple else OwlColors.PurpleDim,
                                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Last ${weeklySorted.size} weeks",
                        color = OwlColors.TextMuted,
                        fontSize = 11.sp
                    )
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
    unit: String,
    onClick: () -> Unit
) {
    val sortedSets = remember(sets.map { it.id }) { sets.sortedByDescending { it.date } }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
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
                    color = OwlColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                if (uiState.isPR) {
                    PrBadge()
                } else {
                    Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = OwlColors.PurpleSoft, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Best 1RM: ", style = MaterialTheme.typography.labelLarge, color = OwlColors.TextSecondary)
                Text(
                    "${uiState.best1RM.toInt()} $unit",
                    style = MaterialTheme.typography.titleMedium,
                    color = OwlColors.Purple,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("RECENT TREND", style = MaterialTheme.typography.labelSmall, color = OwlColors.PurpleSoft, letterSpacing = 1.sp)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                val trendColor = when {
                    uiState.trend > 0 -> OwlColors.GreenPositive
                    uiState.trend < 0 -> OwlColors.RedNegative
                    else -> OwlColors.TextSecondary
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
                color = OwlColors.TextMuted
            )

            Spacer(Modifier.height(12.dp))
            
            sortedSets.take(3).forEach { set ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(sdf.format(Date(set.date)), style = MaterialTheme.typography.bodySmall, color = OwlColors.TextMuted)
                    Text("${set.weight}$unit × ${set.reps}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = OwlColors.TextPrimary)
                }
            }
        }
    }
}

