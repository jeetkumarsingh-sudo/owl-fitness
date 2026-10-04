package com.example.gymdiary3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.ui.components.ApexLineChart
import com.example.gymdiary3.ui.components.ApexPanel
import com.example.gymdiary3.ui.components.ApexScaffold
import com.example.gymdiary3.ui.components.CountUpText
import com.example.gymdiary3.ui.components.SectionLabel
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.viewmodel.ProgressViewModel
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AnalyticsScreen(
    nav: NavHostController,
    viewModel: ProgressViewModel = hiltViewModel(),
    workoutViewModel: WorkoutViewModel = hiltViewModel(),
) {
    val uiState by viewModel.exerciseUiState.collectAsStateWithLifecycle()
    val oneRMHistory by viewModel.oneRMHistory.collectAsStateWithLifecycle()
    val volumeHistory by viewModel.volumeHistory.collectAsStateWithLifecycle()
    val userSettings by workoutViewModel.settingsRepository.userSettingsFlow
        .collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())

    val exerciseName = viewModel.exerciseName.ifEmpty { "Exercise" }
    val unit = userSettings.weightUnit

    ApexScaffold(title = exerciseName, onBack = { nav.popBackStack() }) { padding ->
        val state = uiState
        if (state == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No data for this exercise", color = Apex.TextMuted)
            }
            return@ApexScaffold
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    StatCard("Best 1RM", state.best1RM.toInt(), unit, Modifier.weight(1f))
                    StatCard("Total volume", state.totalVolume.toInt(), unit, Modifier.weight(1f))
                }
            }

            if (state.recommendation.isNotBlank()) {
                item {
                    ApexPanel(radius = Apex.radiusMd) {
                        Column(Modifier.padding(18.dp)) {
                            SectionLabel("Recommendation", accent = true)
                            Spacer(Modifier.height(8.dp))
                            Text(state.recommendation, color = Apex.TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                (if (state.trend >= 0) "+" else "") + "${state.trend.toInt()} $unit since last session",
                                color = if (state.trend > 0) Apex.Positive else if (state.trend < 0) Apex.Negative else Apex.TextSecondary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            if (oneRMHistory.size >= 2) {
                item { ChartCard("1RM Progress (estimated)", oneRMHistory.map { it.second.toFloat() }) }
            }
            if (volumeHistory.size >= 2) {
                item { ChartCard("Volume Progress", volumeHistory.map { it.second.toFloat() }) }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, unit: String, modifier: Modifier = Modifier) {
    ApexPanel(modifier = modifier, radius = Apex.radiusMd) {
        Column(Modifier.padding(16.dp)) {
            CountUpText(
                target = value,
                style = MaterialTheme.typography.headlineSmall,
                color = Apex.AccentSoft,
                suffix = " $unit"
            )
            Spacer(Modifier.height(4.dp))
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Apex.TextMuted)
        }
    }
}

@Composable
private fun ChartCard(title: String, values: List<Float>) {
    Column {
        SectionLabel(title, accent = true)
        Spacer(Modifier.height(14.dp))
        ApexPanel(radius = Apex.radiusMd) {
            Box(Modifier.padding(16.dp)) {
                ApexLineChart(values = values, height = 200.dp)
            }
        }
    }
}
