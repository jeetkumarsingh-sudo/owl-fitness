package com.example.gymdiary3.screens

import android.util.Log
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
import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.presentation.state.ExerciseUiState
import com.example.gymdiary3.domain.analyzer.WorkoutAnalyzer
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
fun ProgressGraphScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    Log.d("PERF", "ProgressGraphScreen recomposing")
    val allSessions by viewModel.sessions.collectAsStateWithLifecycle()

    var selectedMuscle by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf("") }
    var showMuscleDropdown by remember { mutableStateOf(false) }
    var showExerciseDropdown by remember { mutableStateOf(false) }
    var graphType by remember { mutableStateOf("1RM") } // "1RM" or "Volume"

    val recentExercises = remember(allSessions) { viewModel.getRecentExercises() }
    val muscles = remember(allSessions) { viewModel.getMuscleGroups() }
    val exercisesForMuscle = remember(allSessions, selectedMuscle) {
        if (selectedMuscle.isNotEmpty()) viewModel.getExercisesByMuscle(selectedMuscle)
        else emptyList()
    }

    val latestBodyWeight by viewModel.latestBodyWeight.collectAsStateWithLifecycle()
    val weeklyVolumeMap = remember(allSessions) { WorkoutAnalyzer.getWeeklyVolume(allSessions) }
    val currentWeeklyVolume = remember(weeklyVolumeMap) {
        weeklyVolumeMap.values.firstOrNull() ?: 0.0
    }

    val userSettings by if (viewModel.settingsRepository != null) {
        viewModel.settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())
    } else {
        remember { mutableStateOf(com.example.gymdiary3.domain.settings.UserSettings()) }
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

    val lastSessionDate = remember(allSessions) {
        allSessions.maxOfOrNull { it.session.startTime }?.let { Date(it) }
    }
    val sessionCount = remember(allSessions) { allSessions.size }

    val volumeData = remember(allSessions) {
        WorkoutAnalyzer.getVolumeHistory(allSessions)
            .map { LineChartData.Point(it.second.toFloat(), it.first) }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = { Text("PERFORMANCE DASHBOARD", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 1.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp)
        ) {
            item {
                Text(
                    text = "OVERVIEW",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val unit = userSettings.weightUnit
                    QuickStatCard("Body Weight", "${latestBodyWeight ?: "--"} $unit", Modifier.weight(1f))
                    QuickStatCard("Best 1RM", if (exerciseStats != null) "${exerciseStats.best1RM.toInt()} $unit" else "--", Modifier.weight(1f))
                    QuickStatCard("Weekly Vol", "${currentWeeklyVolume.toInt()} $unit", Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total Sessions: $sessionCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (lastSessionDate != null) {
                        val sdfLast = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Text(
                            "Last Active: ${sdfLast.format(lastSessionDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "RECENT EXERCISES",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (recentExercises.isEmpty()) {
                    Text(
                        "No recent exercises found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
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
                                label = { Text(exercise, style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedExercise == exercise,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            item {
                Text(
                    text = "BROWSE BY MUSCLE",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showMuscleDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                selectedMuscle.ifEmpty { "Muscle" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(
                            expanded = showMuscleDropdown,
                            onDismissRequest = { showMuscleDropdown = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            muscles.forEach { muscle ->
                                DropdownMenuItem(
                                    text = { Text(muscle, style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        selectedMuscle = muscle
                                        selectedExercise = ""
                                        showMuscleDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showExerciseDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedMuscle.isNotEmpty(),
                            border = BorderStroke(1.dp, if (selectedMuscle.isNotEmpty()) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                selectedExercise.ifEmpty { "Exercise" },
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedMuscle.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                maxLines = 1
                            )
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = if (selectedMuscle.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        }
                        DropdownMenu(
                            expanded = showExerciseDropdown,
                            onDismissRequest = { showExerciseDropdown = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            exercisesForMuscle.forEach { exercise ->
                                DropdownMenuItem(
                                    text = { Text(exercise, style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        selectedExercise = exercise
                                        showExerciseDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                if (selectedExercise.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$graphType PROGRESS",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        SingleChoiceSegmentedButtonRow {
                            SegmentedButton(
                                selected = graphType == "1RM",
                                onClick = { graphType = "1RM" },
                                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primary,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text("1RM", style = MaterialTheme.typography.labelSmall)
                            }
                            SegmentedButton(
                                selected = graphType == "Volume",
                                onClick = { graphType = "Volume" },
                                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primary,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text("Vol", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    val exerciseData = remember(selectedExercise, allSessions, graphType) {
                        if (graphType == "1RM") {
                            WorkoutAnalyzer.get1RMHistory(selectedExercise, allSessions).map { (date, rm) ->
                                Pair(date, rm)
                            }
                        } else {
                            WorkoutAnalyzer.getExerciseVolumeHistory(selectedExercise, allSessions).map { (dateStr, vol) ->
                                Pair(0L, vol)
                            }
                        }
                    }
                    
                    if (exerciseData.size >= 2) {
                        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        val accentColor = MaterialTheme.colorScheme.primary
                        LineChart(
                            linesChartData = listOf(
                                LineChartData(
                                    points = if (graphType == "1RM") {
                                        exerciseData.map { (date, value) ->
                                            LineChartData.Point(value.toFloat(), dateFormat.format(Date(date)))
                                        }
                                    } else {
                                        val volData = WorkoutAnalyzer.getExerciseVolumeHistory(selectedExercise, allSessions)
                                        volData.map { (dateStr, vol) ->
                                            LineChartData.Point(vol.toFloat(), dateStr)
                                        }
                                    },
                                    lineDrawer = SolidLineDrawer(color = accentColor, thickness = 2.dp)
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            pointDrawer = FilledCircularPointDrawer(color = accentColor),
                            xAxisDrawer = SimpleXAxisDrawer(labelTextColor = Color.Gray),
                            yAxisDrawer = SimpleYAxisDrawer(
                                labelTextColor = Color.Gray,
                                labelValueFormatter = { v -> 
                                    if (v >= 1000f) "${(v/1000).toInt()}k" else "${v.toInt()}${if(graphType=="1RM") userSettings.weightUnit else ""}" 
                                }
                            )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Log $selectedExercise in at least 2 sessions to see progress",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // INSIGHTS PANEL
                    exerciseStats?.let { stats ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "INSIGHTS",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelMedium,
                                        letterSpacing = 1.sp
                                    )
                                    if (stats.isPR) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "🔥 NEW PR",
                                                color = MaterialTheme.colorScheme.tertiary,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = stats.recommendation,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Status: ${stats.trendLabel}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if(stats.trend > 0) Color(0xFF4CAF50) else if(stats.trend < 0) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // HISTORY SNAPSHOT
                    if (lastThreeSets.isNotEmpty()) {
                        Text(
                            text = "LAST 3 SESSIONS",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        val sdfHistory = SimpleDateFormat("MMM dd", Locale.getDefault())
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            lastThreeSets.forEach { set ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = sdfHistory.format(Date(set.date)),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${set.weight}${userSettings.weightUnit} x ${set.reps}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuickStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
