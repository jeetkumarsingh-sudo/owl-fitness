package com.example.gymdiary3.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.gymdiary3.domain.WorkoutAnalyzer
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressGraphScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    Log.d("PERF", "ProgressGraphScreen recomposing")
    val allSessions by viewModel.sessions.collectAsStateWithLifecycle()
    
    val exercises = remember(allSessions) {
        allSessions
            .flatMap { it.sets }
            .map { it.exercise }
            .distinct()
            .sorted()
    }
    
    var selectedExercise by remember { mutableStateOf(exercises.firstOrNull() ?: "") }
    var showExerciseDropdown by remember { mutableStateOf(false) }

    val volumeData = remember(allSessions) {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        
        allSessions.sortedBy { it.session.startTime }
            .groupBy { dateFormat.format(Date(it.session.startTime)) }
            .map { (date, sessionList) ->
                val totalVolume = sessionList.sumOf { it.totalVolume }
                val sortTime = sessionList.first().session.startTime
                Triple(date, totalVolume.toFloat(), sortTime)
            }
            .sortedBy { it.third }
            .map { LineChartData.Point(it.second, it.first) }
    }

    val plateauStatus: List<Pair<String, com.example.gymdiary3.viewmodel.ExerciseUiState>> = remember(allSessions) {
        val exList = allSessions.flatMap { it.sets }.map { it.exercise }.distinct()
        exList.map { exercise ->
            val uiState = viewModel.getExerciseUiState(exercise)
            Pair(exercise, uiState)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D1A)),
        topBar = {
            TopAppBar(
                title = { Text("PROGRESS ANALYTICS", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0D1A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF0D0D1A)),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    "TOTAL VOLUME",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (volumeData.size >= 2) {
                    LineChart(
                        linesChartData = listOf(
                            LineChartData(
                                points = volumeData,
                                lineDrawer = SolidLineDrawer(color = Color(0xFF7B68EE), thickness = 3.dp)
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        pointDrawer = FilledCircularPointDrawer(color = Color(0xFF7B68EE), diameter = 8.dp),
                        xAxisDrawer = SimpleXAxisDrawer(labelTextColor = Color.Gray),
                        yAxisDrawer = SimpleYAxisDrawer(
                            labelTextColor = Color.Gray,
                            labelValueFormatter = { value -> 
                                if (value >= 1000f) "${(value / 1000).toInt()}k" else value.toInt().toString()
                            }
                        )
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(150.dp).background(Color(0xFF1C1C2E), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add more sessions to see trend", color = Color.Gray)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }

            item {
                Text(
                    "STRENGTH PROGRESS (1RM)",
                    color = Color(0xFF7B68EE),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showExerciseDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFF7B68EE))
                    ) {
                        Text(selectedExercise.ifEmpty { "Select exercise" }, color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Text("▾", color = Color(0xFF7B68EE))
                    }
                    DropdownMenu(
                        expanded = showExerciseDropdown,
                        onDismissRequest = { showExerciseDropdown = false },
                        modifier = Modifier.background(Color(0xFF1C1C2E))
                    ) {
                        exercises.forEach { exercise ->
                            DropdownMenuItem(
                                text = { Text(exercise, color = Color.White) },
                                onClick = {
                                    selectedExercise = exercise
                                    showExerciseDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                if (selectedExercise.isNotEmpty()) {
                    val exerciseData = allSessions
                        .sortedBy { it.session.startTime }
                        .mapNotNull { sessionWithSets ->
                            val setsForExercise = sessionWithSets.sets.filter { it.exercise == selectedExercise }
                            if (setsForExercise.isEmpty()) return@mapNotNull null
                            
                            // Using WorkoutAnalyzer for 1RM calculation consistency
                            val stats = WorkoutAnalyzer.getExerciseStats(selectedExercise, listOf(sessionWithSets))
                            Pair(sessionWithSets.session.startTime, stats.best1RM)
                        }
                    
                    if (exerciseData.size >= 2) {
                        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        LineChart(
                            linesChartData = listOf(
                                LineChartData(
                                    points = exerciseData.map { (date, rm) ->
                                        LineChartData.Point(rm.toFloat(), dateFormat.format(Date(date)))
                                    },
                                    lineDrawer = SolidLineDrawer(color = Color(0xFF7B68EE), thickness = 2.dp)
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            pointDrawer = FilledCircularPointDrawer(color = Color(0xFF7B68EE)),
                            xAxisDrawer = SimpleXAxisDrawer(labelTextColor = Color.Gray),
                            yAxisDrawer = SimpleYAxisDrawer(
                                labelTextColor = Color.Gray,
                                labelValueFormatter = { v -> "${v.toInt()}kg" }
                            )
                        )
                        
                        val best = exerciseData.maxOf { it.second }
                        val latest = exerciseData.last().second
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Best 1RM: ${"%.1f".format(best)}kg", color = Color.Gray, fontSize = 13.sp)
                            Text("Latest: ${"%.1f".format(latest)}kg", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFF1C1C2E), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Log $selectedExercise in at least 2 sessions to see progress",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(32.dp))
            }

            if (plateauStatus.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C2E)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "PROGRESS FLAGS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B68EE)
                            )
                            Spacer(Modifier.height(12.dp))
                            for ((exercise, uiState) in plateauStatus) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        exercise,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    
                                    Text(
                                        uiState.trendLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = uiState.trendColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            item {
                Button(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C2E))
                ) {
                    Text("BACK", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
