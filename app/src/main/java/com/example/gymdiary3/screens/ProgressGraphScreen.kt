package com.example.gymdiary3.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import io.github.tehras.charts.line.LineChart
import io.github.tehras.charts.line.LineChartData
import io.github.tehras.charts.line.renderer.line.SolidLineDrawer
import io.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import io.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import io.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressGraphScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    Log.d("PERF", "ProgressGraphScreen recomposing")
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    
    val volumeData = remember(sessions) {
        sessions.sortedBy { it.session.startTime }.map { sessionWithSets ->
            val volume = sessionWithSets.totalVolume.toFloat()
            val dateLabel = SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(sessionWithSets.session.startTime))
            LineChartData.Point(volume, dateLabel)
        }
    }

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
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
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
                        
                        if (volumeData.size >= 2) {
                            Box(modifier = Modifier.fillMaxSize().padding(bottom = 16.dp)) {
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
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Add more sessions to see trend", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
