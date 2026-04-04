package com.example.gymdiary3.screens

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
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    val workouts by remember(viewModel) { viewModel.workouts }.collectAsState()
    val grouped = remember(workouts) { workouts.groupBy { it.exercise } }
    val sdf = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            grouped.forEach { (exercise, sets) ->
                val sortedSets = sets.sortedByDescending { it.date }
                val prSet = sets.maxByOrNull { it.weight }
                
                item(key = exercise) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
            
            item(key = "back_button") {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("BACK", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
