package com.example.gymdiary3.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymdiary3.data.BodyWeight
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.line.renderer.line.SolidLineDrawer
import com.github.tehras.charts.line.renderer.point.FilledCircularPointDrawer
import com.github.tehras.charts.line.renderer.xaxis.SimpleXAxisDrawer
import com.github.tehras.charts.line.renderer.yaxis.SimpleYAxisDrawer
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyWeightScreen(nav: NavHostController, viewModel: BodyWeightViewModel) {

    var weightInput by remember { mutableStateOf("") }
    val weights by viewModel.allWeights.collectAsStateWithLifecycle()
    
    val userSettings by if (viewModel.settingsRepository != null) {
        viewModel.settingsRepository.userSettingsFlow.collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())
    } else {
        remember { mutableStateOf(com.example.gymdiary3.domain.settings.UserSettings()) }
    }

    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        topBar = { 
            TopAppBar(
                title = { Text("BODY WEIGHT", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            ) 
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Current Weight (${userSettings.weightUnit})", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.headlineSmall,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        val logScale = remember { Animatable(1f) }
                        Button(
                            onClick = {
                                scope.launch {
                                    logScale.animateTo(0.95f, tween(100))
                                    logScale.animateTo(1f, tween(100))
                                }
                                val w = weightInput.toDoubleOrNull() ?: return@Button
                                viewModel.insertWeight(w)
                                weightInput = ""
                            },
                            modifier = Modifier.fillMaxWidth().height(64.dp).scale(logScale.value),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("LOG WEIGHT", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            BodyWeightChart(weights, userSettings.weightUnit)

            Text("HISTORY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = weights,
                    key = { it.id }
                ) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteWeight(item)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFB71C1C), RoundedCornerShape(12.dp))
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text("DELETE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    ) {
                        WeightCard(item, sdf, userSettings.weightUnit)
                    }
                }
            }

            val backScale = remember { Animatable(1f) }
            OutlinedButton(
                onClick = { 
                    scope.launch {
                        backScale.animateTo(0.95f, tween(100))
                        backScale.animateTo(1f, tween(100))
                    }
                    nav.popBackStack() 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).scale(backScale.value),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("BACK", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun BodyWeightChart(weights: List<BodyWeight>, unit: String) {
    if (weights.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Log at least 2 entries to see your trend",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val stats = com.example.gymdiary3.domain.BodyWeightAnalyzer.getStats(weights) ?: return
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("CURRENT", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Text("${stats.latestWeight}$unit", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CHANGE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                val changeColor = if (stats.totalChange <= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                Text(
                    "${if (stats.totalChange >= 0) "+" else ""}${"%.1f".format(stats.totalChange)}$unit",
                    color = changeColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("LOWEST", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Text("${stats.minWeight}$unit", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        LineChart(
            linesChartData = listOf(
                LineChartData(
                    points = weights.sortedBy { it.date }.map { bw ->
                        LineChartData.Point(bw.weight.toFloat(), dateFormat.format(Date(bw.date)))
                    },
                    lineDrawer = SolidLineDrawer(color = MaterialTheme.colorScheme.primary, thickness = 2.dp)
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            pointDrawer = FilledCircularPointDrawer(color = MaterialTheme.colorScheme.primary, diameter = 6.dp),
            xAxisDrawer = SimpleXAxisDrawer(labelTextColor = Color.Gray, axisLineColor = Color.Gray),
            yAxisDrawer = SimpleYAxisDrawer(
                labelTextColor = Color.Gray,
                axisLineColor = Color.Gray,
                labelValueFormatter = { value -> "%.1f".format(value) }
            ),
            horizontalOffset = 5f
        )
    }
}

@Composable
fun WeightCard(item: com.example.gymdiary3.data.BodyWeight, sdf: SimpleDateFormat, unit: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(sdf.format(Date(item.date)), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "${item.weight} $unit",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
