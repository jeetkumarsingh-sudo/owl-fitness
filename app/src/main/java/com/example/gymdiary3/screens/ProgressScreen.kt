package com.example.gymdiary3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    val workouts by viewModel.workouts.collectAsState(initial = emptyList())
    val grouped = workouts.groupBy { it.exercise }
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Progress & PRs", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            grouped.forEach { (exercise, sets) ->
                val sortedSets = sets.sortedByDescending { it.date }
                val prSet = sets.maxByOrNull { it.weight }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                exercise,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text("Best: ", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${prSet?.weight ?: 0} kg",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                            Text("RECENT TREND", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            
                            // Compare last session with the one before it
                            val sessionGroups = sortedSets.groupBy { 
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date)) 
                            }.values.toList()

                            if (sessionGroups.size >= 2) {
                                val latestWeight = sessionGroups[0].maxOf { it.weight }
                                val previousWeight = sessionGroups[1].maxOf { it.weight }
                                val diff = latestWeight - previousWeight
                                
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text(
                                        text = if (diff >= 0) "+${diff}kg" else "${diff}kg",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (diff >= 0) Color(0xFF4CAF50) else Color.Red
                                    )
                                    Text(
                                        " since last session",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            
                            sortedSets.take(3).forEach { set ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(sdf.format(Date(set.date)), style = MaterialTheme.typography.bodySmall)
                                    Text("${set.weight}kg × ${set.reps}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("BACK")
                }
            }
        }
    }
}
