package com.example.gymdiary3.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.ProgressViewModel
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val unitLabel = WeightFormatter.label(unit)

    Scaffold(
        containerColor = OwlColors.DeepBg,
        topBar = {
            TopAppBar(
                title = { Text(exerciseName.uppercase(), fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OwlColors.DeepBg,
                    titleContentColor = OwlColors.TextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OwlColors.TextPrimary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val state = uiState
            if (state == null) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No data for this exercise", color = OwlColors.TextMuted)
                    }
                }
                return@LazyColumn
            }

            // Stat Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnalyticsStatCard("Best 1RM", "${WeightFormatter.fromKilograms(state.best1RM, unit).toInt()} $unitLabel", Modifier.weight(1f))
                    AnalyticsStatCard("Total Volume", "${WeightFormatter.fromKilograms(state.totalVolume, unit).toInt()} $unitLabel", Modifier.weight(1f))
                }
            }

            // RECOMMENDATION / Trend
            item {
                if (state.recommendation.isNotBlank()) {
                    Surface(
                        color = OwlColors.CardBg,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "RECOMMENDATION",
                                color = OwlColors.CrimsonSoft,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = state.recommendation,
                                color = OwlColors.TextPrimary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = (if (state.trend >= 0) "+" else "") + "${WeightFormatter.fromKilograms(state.trend, unit).toInt()} $unitLabel since last session",
                                color = if (state.trend > 0) OwlColors.GreenPositive 
                                        else if (state.trend < 0) OwlColors.RedNegative 
                                        else OwlColors.TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 1RM Progress Chart (values converted to the user's display unit)
            if (oneRMHistory.size >= 2) {
                item {
                    ChartSection("1RM PROGRESS (ESTIMATED)", oneRMHistory.map { WeightFormatter.fromKilograms(it.second, unit) })
                }
            }

            // Volume Progress Chart (values converted to the user's display unit)
            if (volumeHistory.size >= 2) {
                item {
                    ChartSection("VOLUME PROGRESS", volumeHistory.map { WeightFormatter.fromKilograms(it.second, unit) }, labels = volumeHistory.map { it.first })
                }
            }
        }
    }
}

@Composable
fun ChartSection(title: String, data: List<Double>, labels: List<String>? = null) {
    Column {
        Text(title, color = OwlColors.CrimsonSoft, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
        Spacer(Modifier.height(16.dp))
        
        Surface(
            color = OwlColors.CardBg,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OwlColors.BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.padding(16.dp)) {
                LineChart(
                    linesChartData = listOf(
                        LineChartData(
                            points = data.mapIndexed { index, value -> 
                                LineChartData.Point(value.toFloat(), labels?.getOrNull(index) ?: "")
                            },
                            lineDrawer = SolidLineDrawer(color = OwlColors.Crimson, thickness = 3.dp)
                        )
                    ),
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    pointDrawer = FilledCircularPointDrawer(color = OwlColors.Crimson),
                    xAxisDrawer = SimpleXAxisDrawer(labelTextColor = OwlColors.TextMuted, axisLineColor = OwlColors.BorderSubtle),
                    yAxisDrawer = SimpleYAxisDrawer(labelTextColor = OwlColors.TextMuted, axisLineColor = OwlColors.BorderSubtle, labelValueFormatter = { v -> v.toInt().toString() })
                )
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OwlColors.TextSecondary)
            Text(value, style = MaterialTheme.typography.titleMedium, color = OwlColors.Crimson, fontWeight = FontWeight.Bold)
        }
    }
}
