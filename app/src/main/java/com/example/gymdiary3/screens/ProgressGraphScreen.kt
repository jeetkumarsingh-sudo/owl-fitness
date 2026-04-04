package com.example.gymdiary3.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.data.SessionWithSets
import com.example.gymdiary3.data.calculate1RM
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressGraphScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    Log.d("PERF", "ProgressGraphScreen recomposing")
    val sessions: List<SessionWithSets> by viewModel.sessions.collectAsStateWithLifecycle()
    
    val volumeData = remember(sessions) {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        
        sessions.sortedBy { it.session.startTime }
            .groupBy { dateFormat.format(Date(it.session.startTime)) }
            .map { (date, sessionList) ->
                val totalVolume = sessionList.sumOf { it.totalVolume }
                val sortTime = sessionList.first().session.startTime
                Triple(date, totalVolume.toFloat(), sortTime)
            }
            .sortedBy { it.third }
            .map { LineChartData.Point(it.second, it.first) }
    }

    val strengthData = remember(sessions) {
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        sessions.sortedBy { it.session.startTime }
            .groupBy { dateFormat.format(Date(it.session.startTime)) }
            .map { (date, sessionList) ->
                val best1RM = sessionList.maxOfOrNull { it.best1RM } ?: 0.0
                val sortTime = sessionList.first().session.startTime
                Triple(date, best1RM.toFloat(), sortTime)
            }
            .sortedBy { it.third }
            .map { LineChartData.Point(it.second, it.first) }
    }

    val plateauStatus = remember(sessions) {
        val exercises = sessions.flatMap { it.sets }.map { it.exercise }.distinct()
        exercises.map { exercise ->
            val bestSets = sessions
                .sortedBy { it.session.startTime }
                .mapNotNull { session ->
                    session.sets.filter { it.exercise == exercise }
                        .maxByOrNull { it.weight * it.reps }
                }
            val last3 = bestSets.takeLast(3)
            val isPlateau = last3.size == 3 &&
                    last3[0].weight == last3[1].weight && last3[1].weight == last3[2].weight &&
                    last3[0].reps == last3[1].reps && last3[1].reps == last3[2].reps
            
            val isImproving = last3.size >= 2 && 
                    (last3.last().weight * last3.last().reps > last3[last3.size - 2].weight * last3[last3.size - 2].reps)
            
            Triple(exercise, isPlateau, isImproving)
        }
    }

    val maxVolume = remember(volumeData) { volumeData.maxByOrNull { it.value } }
    val minVolume = remember(volumeData) { volumeData.minByOrNull { it.value } }
    
    val maxStrength = remember(strengthData) { strengthData.maxByOrNull { it.value } }
    val minStrength = remember(strengthData) { strengthData.minByOrNull { it.value } }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = { Text("VOLUME PROGRESS", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Total Volume (kg)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        // Using .isEmpty() and .size explicitly to avoid ambiguity with Modifier.size
                        if (volumeData.isNotEmpty() && volumeData.size >= 2) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 16.dp)) {
                                LineChart(
                                    linesChartData = listOf(
                                        LineChartData(
                                            points = volumeData,
                                            lineDrawer = SolidLineDrawer(
                                                color = MaterialTheme.colorScheme.primary,
                                                thickness = 3.dp
                                            )
                                        )
                                    ),
                                    pointDrawer = FilledCircularPointDrawer(
                                        color = MaterialTheme.colorScheme.secondary,
                                        diameter = 8.dp
                                    ),
                                    xAxisDrawer = SimpleXAxisDrawer(
                                        labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        axisLineColor = MaterialTheme.colorScheme.outline
                                    ),
                                    yAxisDrawer = SimpleYAxisDrawer(
                                        labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        axisLineColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                maxVolume?.let {
                                    Text(
                                        "Highest: ${it.value.toInt()} kg (${it.label})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                minVolume?.let {
                                    Text(
                                        "Lowest: ${it.value.toInt()} kg (${it.label})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Add more sessions to see trend", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Strength Progress (1RM kg)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        if (strengthData.isNotEmpty() && strengthData.size >= 2) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = 16.dp)) {
                                LineChart(
                                    linesChartData = listOf(
                                        LineChartData(
                                            points = strengthData,
                                            lineDrawer = SolidLineDrawer(
                                                color = MaterialTheme.colorScheme.secondary,
                                                thickness = 3.dp
                                            )
                                        )
                                    ),
                                    pointDrawer = FilledCircularPointDrawer(
                                        color = MaterialTheme.colorScheme.primary,
                                        diameter = 8.dp
                                    ),
                                    xAxisDrawer = SimpleXAxisDrawer(
                                        labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        axisLineColor = MaterialTheme.colorScheme.outline
                                    ),
                                    yAxisDrawer = SimpleYAxisDrawer(
                                        labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        axisLineColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                maxStrength?.let {
                                    Text(
                                        "Highest: ${it.value.toInt()} kg (${it.label})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                minStrength?.let {
                                    Text(
                                        "Lowest: ${it.value.toInt()} kg (${it.label})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Add more sessions to see trend", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (plateauStatus.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "PROGRESS FLAGS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(12.dp))
                            for ((exercise, isPlateau, isImproving) in plateauStatus) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        exercise,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    when {
                                        isPlateau -> {
                                            Text(
                                                "⚠ No progress in last 3 sessions",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Red,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        isImproving -> {
                                            Text(
                                                "📈 Progressing well",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF4CAF50), // Material Green
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        else -> {
                                            Text(
                                                "Steady",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text("BACK", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
