package com.example.gymdiary3.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProgressGraphScreen(nav: NavHostController, viewModel: WorkoutViewModel, preselectedExercise: String = "") {
    val allSessions by viewModel.sessions.collectAsStateWithLifecycle()
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val latestBodyWeight by viewModel.latestBodyWeight.collectAsStateWithLifecycle()

    var selectedMuscle by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf(preselectedExercise) }
    var showMuscleDropdown by remember { mutableStateOf(false) }
    var showExerciseDropdown by remember { mutableStateOf(false) }
    var graphType by remember { mutableStateOf("1RM") }

    val recentExercises = remember(allSessions) { viewModel.getRecentExercises() }
    val muscles = remember(allSessions) { viewModel.getMuscleGroups() }
    val exercisesForMuscle = remember(allSessions, selectedMuscle) {
        if (selectedMuscle.isNotEmpty()) viewModel.getExercisesByMuscle(selectedMuscle)
        else emptyList()
    }

    val userSettings by if (viewModel.settingsRepository != null) {
        viewModel.settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())
    } else {
        remember { mutableStateOf(com.example.gymdiary3.domain.settings.UserSettings()) }
    }

    LaunchedEffect(preselectedExercise, allSessions) {
        if (preselectedExercise.isNotEmpty()) {
            val muscle = allSessions.flatMap { it.sets }
                .firstOrNull { it.exercise == preselectedExercise }?.muscle ?: ""
            selectedMuscle = muscle
        }
    }

    val exerciseStats = remember(selectedExercise, allSessions) {
        if (selectedExercise.isNotEmpty()) viewModel.getExerciseUiState(selectedExercise)
        else null
    }

    val lastThreeSets by if (selectedExercise.isNotEmpty()) {
        viewModel.getLastThreeSets(selectedExercise).collectAsStateWithLifecycle(emptyList())
    } else {
        remember { mutableStateOf(emptyList<com.example.gymdiary3.data.WorkoutSet>()) }
    }

    Scaffold(
        containerColor = OwlColors.DeepBg,
        topBar = {
            TopAppBar(
                title = { Text("ANALYTICS", fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp) },
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
            contentPadding = PaddingValues(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val unit = userSettings.weightUnit
                    AnalyticsStatCard("Weight", "${latestBodyWeight ?: "--"} $unit", Modifier.weight(1f))
                    AnalyticsStatCard("Best 1RM", if (exerciseStats != null) "${exerciseStats.best1RM.toInt()} $unit" else "--", Modifier.weight(1f))
                }
            }

            item {
                val volumeHistory = remember(allSessions) {
                    WorkoutAnalyzer.getVolumeHistory(allSessions)
                }

                if (volumeHistory.size >= 2) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "VOLUME HISTORY",
                        color = OwlColors.PurpleSoft,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(12.dp))

                    Surface(
                        color = OwlColors.CardBg,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, OwlColors.BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            // Show last 8 data points max so chart is readable
                            val displayData = if (volumeHistory.size > 8) volumeHistory.takeLast(8) else volumeHistory

                            LineChart(
                                linesChartData = listOf(
                                    LineChartData(
                                        points = displayData.map { (label, volume) ->
                                            LineChartData.Point(volume.toFloat(), label)
                                        },
                                        lineDrawer = SolidLineDrawer(color = OwlColors.PurpleSoft, thickness = 2.dp)
                                    )
                                ),
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                pointDrawer = FilledCircularPointDrawer(color = OwlColors.Purple),
                                xAxisDrawer = SimpleXAxisDrawer(
                                    labelTextColor = OwlColors.TextMuted,
                                    axisLineColor = OwlColors.BorderSubtle
                                ),
                                yAxisDrawer = SimpleYAxisDrawer(
                                    labelTextColor = OwlColors.TextMuted,
                                    axisLineColor = OwlColors.BorderSubtle,
                                    labelValueFormatter = { v -> "${v.toInt()}kg" }
                                )
                            )

                            Spacer(Modifier.height(8.dp))
                            val totalVol = displayData.lastOrNull()?.second ?: 0.0
                            Text(
                                "Latest session: ${totalVol.toInt()} kg total volume",
                                color = OwlColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }

            item {
                Text("QUICK SELECT", color = OwlColors.PurpleSoft, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentExercises.forEach { (exercise, muscle) ->
                        FilterChip(
                            selected = selectedExercise == exercise,
                            onClick = { 
                                selectedExercise = exercise
                                selectedMuscle = muscle
                            },
                            label = { Text(exercise, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OwlColors.Purple,
                                selectedLabelColor = Color.White,
                                containerColor = OwlColors.CardBg,
                                labelColor = OwlColors.TextSecondary
                            ),
                            border = BorderStroke(1.dp, if(selectedExercise == exercise) OwlColors.Purple else OwlColors.BorderSubtle)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Text("EXPLORE ALL", color = OwlColors.PurpleSoft, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showMuscleDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, OwlColors.BorderSubtle),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OwlColors.TextPrimary)
                        ) {
                            Text(selectedMuscle.ifEmpty { "Muscle" }, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = showMuscleDropdown, onDismissRequest = { showMuscleDropdown = false }, modifier = Modifier.background(OwlColors.CardBg)) {
                            muscles.forEach { muscle ->
                                DropdownMenuItem(text = { Text(muscle, color = OwlColors.TextPrimary) }, onClick = {
                                    selectedMuscle = muscle
                                    selectedExercise = ""
                                    showMuscleDropdown = false
                                })
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showExerciseDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedMuscle.isNotEmpty(),
                            border = BorderStroke(1.dp, OwlColors.BorderSubtle),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OwlColors.TextPrimary)
                        ) {
                            Text(selectedExercise.ifEmpty { "Exercise" }, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = showExerciseDropdown, onDismissRequest = { showExerciseDropdown = false }, modifier = Modifier.background(OwlColors.CardBg)) {
                            exercisesForMuscle.forEach { exercise ->
                                DropdownMenuItem(text = { Text(exercise, color = OwlColors.TextPrimary) }, onClick = {
                                    selectedExercise = exercise
                                    showExerciseDropdown = false
                                })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }

            if (selectedExercise.isNotEmpty()) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedExercise.uppercase(), style = MaterialTheme.typography.titleMedium, color = OwlColors.TextPrimary, fontWeight = FontWeight.Bold)
                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(selected = graphType == "1RM", onClick = { graphType = "1RM" }, shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)) { Text("1RM") }
                            SegmentedButton(selected = graphType == "Volume", onClick = { graphType = "Volume" }, shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)) { Text("VOL") }
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))

                    val exerciseData = remember(selectedExercise, workouts, graphType) {
                        val filteredSets = workouts.filter { it.exercise == selectedExercise }
                        if (graphType == "1RM") WorkoutAnalyzer.get1RMHistory(selectedExercise, filteredSets)
                        else WorkoutAnalyzer.getExerciseVolumeHistory(selectedExercise, filteredSets).map { Pair(0L, it.second) }
                    }
                    
                    if (exerciseData.size >= 2) {
                        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        LineChart(
                            linesChartData = listOf(LineChartData(
                                points = if (graphType == "1RM") {
                                    exerciseData.map { LineChartData.Point(it.second.toFloat(), dateFormat.format(Date(it.first))) }
                                } else {
                                    val filteredSets = workouts.filter { it.exercise == selectedExercise }
                                    WorkoutAnalyzer.getExerciseVolumeHistory(selectedExercise, filteredSets).map { LineChartData.Point(it.second.toFloat(), it.first) }
                                },
                                lineDrawer = SolidLineDrawer(color = OwlColors.Purple, thickness = 3.dp)
                            )),
                            modifier = Modifier.fillMaxWidth().height(240.dp).padding(vertical = 8.dp),
                            pointDrawer = FilledCircularPointDrawer(color = OwlColors.Purple),
                            xAxisDrawer = SimpleXAxisDrawer(labelTextColor = OwlColors.TextMuted, axisLineColor = OwlColors.BorderSubtle),
                            yAxisDrawer = SimpleYAxisDrawer(labelTextColor = OwlColors.TextMuted, axisLineColor = OwlColors.BorderSubtle, labelValueFormatter = { v -> "${v.toInt()}" })
                        )
                    } else {
                        Box(Modifier.fillMaxWidth().height(140.dp).background(OwlColors.CardBg, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text("Need 2+ sessions for progress graph", color = OwlColors.TextMuted, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    exerciseStats?.let { stats ->
                        Surface(color = OwlColors.CardBg, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, OwlColors.BorderSubtle)) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("AI INSIGHTS", color = OwlColors.PurpleSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    if (stats.isPR) {
                                        Spacer(Modifier.width(12.dp))
                                        Text("NEW PR 🔥", color = OwlColors.GreenPositive, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(stats.recommendation, color = OwlColors.TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(8.dp))
                                Text("Trend: ${stats.trendLabel}", color = if(stats.trend > 0) OwlColors.GreenPositive else if(stats.trend < 0) OwlColors.RedNegative else OwlColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
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
            Text(value, style = MaterialTheme.typography.titleMedium, color = OwlColors.Purple, fontWeight = FontWeight.Bold)
        }
    }
}
