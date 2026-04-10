package com.example.gymdiary3.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.gymdiary3.domain.WorkoutAnalyzer
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    Log.d("PERF", "ProgressScreen recomposing")
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val grouped = remember(workouts) { workouts.groupBy { it.exercise } }
    val sdf = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = { Text("PROGRESS & PRS", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        val exercisesList = remember(grouped) { grouped.keys.toList().sorted() }
        
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                WeeklyVolumeAnalysisCard(sessions)
            }

            if (exercisesList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No exercise data available.\nStart a workout to track your progress!",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            items(
                items = exercisesList,
                key = { it }
            ) { exercise ->
                val sets = grouped[exercise] ?: emptyList()
                ExerciseProgressCard(exercise, sets, sdf)
            }
            
            item {
                Spacer(Modifier.height(16.dp))
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

@Composable
fun WeeklyVolumeAnalysisCard(sessions: List<SessionWithSets>) {
    val weeklyVolume: Map<String, Double> = remember(sessions) { WorkoutAnalyzer.getWeeklyVolume(sessions) }
    val sortedWeeks = remember(weeklyVolume) { weeklyVolume.keys.toList().sortedDescending() }

    if (sortedWeeks.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "WEEKLY VOLUME ANALYSIS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(12.dp))

                val currentWeekKey = sortedWeeks.first()
                val currentVolume = weeklyVolume[currentWeekKey] ?: 0.0
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Week", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("${currentVolume.toInt()} kg", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    if (sortedWeeks.size >= 2) {
                        val prevWeekKey = sortedWeeks[1]
                        val prevVolume = weeklyVolume[prevWeekKey] ?: 0.0
                        val diff = currentVolume - prevVolume
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("vs Last Week", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(
                                text = (if (diff >= 0) "+" else "") + "${diff.toInt()} kg",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseProgressCard(exercise: String, sets: List<WorkoutSet>, sdf: SimpleDateFormat) {
    Log.d("PERF", "ExerciseProgressCard recomposing: $exercise")
    
    val sortedSets = remember(sets) { sets.sortedByDescending { it.date } }
    val prSet = remember(sets) { sets.maxByOrNull { it.weight } }
    
    val latestWeight = remember(sortedSets) {
        val sessionGroups = sortedSets.groupBy { 
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date)) 
        }.values.toList()
        if (sessionGroups.isNotEmpty()) sessionGroups[0].maxOf { it.weight } else 0.0
    }

    val previousWeight = remember(sortedSets) {
        val sessionGroups = sortedSets.groupBy { 
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date)) 
        }.values.toList()
        if (sessionGroups.size >= 2) sessionGroups[1].maxOf { it.weight } else 0.0
    }
    
    val diff = if (previousWeight > 0) latestWeight - previousWeight else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    exercise,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (latestWeight >= (prSet?.weight ?: 0.0) && latestWeight > 0) {
                    Text(
                        "NEW PR",
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Best Weight: ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "${prSet?.weight ?: 0.0} kg",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            Text("RECENT TREND", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            diff?.let { d ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        d > 0.0 -> Text(
                            text = "+${d}kg since last session",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        d < 0.0 -> Text(
                            text = "${d}kg since last session",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                        else -> Text(
                            text = "Same weight as last session",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            } ?: Text("Not enough data for trend", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(8.dp))
            
            sortedSets.take(3).forEach { set ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(sdf.format(Date(set.date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${set.weight}kg × ${set.reps}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
