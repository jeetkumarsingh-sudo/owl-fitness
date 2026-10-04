package com.example.gymdiary3.screens

import android.net.Uri
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.model.WorkoutSet
import com.example.gymdiary3.domain.model.SessionWithSets
import com.example.gymdiary3.presentation.state.ExerciseUiState
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.ui.components.*
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.intelligence.model.FitnessInsight
import com.example.gymdiary3.intelligence.model.InsightSeverity
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.example.gymdiary3.viewmodel.ProgressViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProgressScreen(
    nav: NavHostController,
    viewModel: WorkoutViewModel = hiltViewModel(),
    progressViewModel: ProgressViewModel = hiltViewModel()
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val exerciseUiStates by viewModel.exerciseUiStates.collectAsStateWithLifecycle()
    val insights by progressViewModel.fitnessInsights.collectAsStateWithLifecycle()
    val grouped = remember(workouts) { workouts.groupBy { it.exercise } }
    val sdf = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val userSettings by viewModel.settingsRepository.userSettingsFlow
        .collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())

    val exercisesList = remember(grouped) {
        grouped.entries.asSequence()
            .sortedByDescending { (_, sets) -> sets.maxOfOrNull { it.timestamp } ?: 0L }
            .map { it.key }.toList()
    }

    ApexScaffold(title = "Progress & PRs") { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (insights.isNotEmpty()) {
                item {
                    Column {
                        SectionLabel("Intelligence", accent = true)
                        Spacer(Modifier.height(12.dp))
                        insights.take(5).forEach { insight ->
                            InsightCard(insight)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            item { WeeklyVolumeCard(sessions, userSettings.weightUnit) }

            if (exercisesList.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp)) {
                        EmptyState(message = "Start a workout to track your progress!", title = "NO PERFORMANCE DATA")
                    }
                }
            }

            items(exercisesList, key = { it }) { exercise ->
                val uiState = exerciseUiStates[exercise] ?: return@items
                val sets = grouped[exercise] ?: emptyList()
                ExerciseProgressCard(exercise, uiState, sets, sdf, userSettings.weightUnit) {
                    nav.navigate("analytics/${Uri.encode(exercise)}")
                }
            }
        }
    }
}

@Composable
private fun InsightCard(insight: FitnessInsight) {
    val accent = when (insight.severity) {
        InsightSeverity.POSITIVE -> Apex.Positive
        InsightSeverity.WARNING -> Apex.Warning
        InsightSeverity.ACTION_REQUIRED -> Apex.Negative
        InsightSeverity.INFO -> Apex.Info
    }
    ApexPanel(radius = Apex.radiusMd) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(3.dp).height(36.dp).background(accent, RoundedCornerShape(100)))
            Spacer(Modifier.width(12.dp))
            Column {
                insight.exerciseName?.let {
                    Text(it.uppercase(), color = accent, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                    Spacer(Modifier.height(3.dp))
                }
                Text(insight.message, color = Apex.TextPrimary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun WeeklyVolumeCard(sessions: List<SessionWithSets>, unit: String) {
    val weeklyVolume = remember(sessions) { WorkoutAnalyzer.getWeeklyVolume(sessions) }
    val sortedWeeks = remember(weeklyVolume) { weeklyVolume.keys.toList().sortedDescending() }
    if (sortedWeeks.isEmpty()) return

    ApexPanel(radius = Apex.radiusMd) {
        Column(Modifier.padding(20.dp)) {
            SectionLabel("Weekly volume", accent = true)
            Spacer(Modifier.height(16.dp))

            val currentVolume = weeklyVolume[sortedWeeks.first()] ?: 0.0
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("CURRENT WEEK", style = MaterialTheme.typography.labelSmall, color = Apex.TextMuted)
                    CountUpText(target = currentVolume.toInt(), style = MaterialTheme.typography.headlineSmall, color = Apex.TextPrimary, suffix = " $unit")
                }
                if (sortedWeeks.size >= 2) {
                    val prev = weeklyVolume[sortedWeeks[1]] ?: 0.0
                    val diff = currentVolume - prev
                    Column(horizontalAlignment = Alignment.End) {
                        Text("VS LAST WEEK", style = MaterialTheme.typography.labelSmall, color = Apex.TextMuted)
                        Text(
                            (if (diff >= 0) "+" else "") + "${diff.toInt()} $unit",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (diff >= 0) Apex.Positive else Apex.Negative,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val last6 = remember(weeklyVolume) { sortedWeeks.take(6).reversed().map { (weeklyVolume[it] ?: 0.0).toFloat() } }
            if (last6.size >= 2) {
                Spacer(Modifier.height(18.dp))
                ApexBars(values = last6, height = 60.dp)
                Spacer(Modifier.height(6.dp))
                Text("Last ${last6.size} weeks", color = Apex.TextMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ExerciseProgressCard(
    exercise: String,
    uiState: ExerciseUiState,
    sets: List<WorkoutSet>,
    sdf: SimpleDateFormat,
    unit: String,
    onClick: () -> Unit
) {
    val sortedSets = remember(sets.map { it.id }) { sets.sortedByDescending { it.timestamp } }
    ApexPanel(onClick = onClick, radius = Apex.radiusMd) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(exercise.uppercase(), style = MaterialTheme.typography.titleMedium, color = Apex.TextPrimary, fontWeight = FontWeight.Bold)
                if (uiState.isPR) PrBadge()
                else Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, tint = Apex.AccentSoft, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Best 1RM: ", style = MaterialTheme.typography.labelLarge, color = Apex.TextSecondary)
                Text("${uiState.best1RM.toInt()} $unit", style = MaterialTheme.typography.titleMedium, color = Apex.AccentSoft, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Text("RECENT TREND", style = MaterialTheme.typography.labelSmall, color = Apex.AccentSoft, letterSpacing = 1.sp)
            val trendColor = when {
                uiState.trend > 0 -> Apex.Positive
                uiState.trend < 0 -> Apex.Negative
                else -> Apex.TextSecondary
            }
            Text(
                text = if (uiState.trend != 0.0) (if (uiState.trend > 0) "+" else "") + "${uiState.trend}$unit since last session"
                else "Same weight as last session",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = trendColor
            )
            Text(uiState.recommendation, style = MaterialTheme.typography.bodySmall, color = Apex.TextMuted)
            Spacer(Modifier.height(12.dp))
            sortedSets.take(3).forEach { set ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(sdf.format(Date(set.timestamp)), style = MaterialTheme.typography.bodySmall, color = Apex.TextMuted)
                    Text("${set.weight}$unit × ${set.reps}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Apex.TextPrimary)
                }
            }
        }
    }
}
