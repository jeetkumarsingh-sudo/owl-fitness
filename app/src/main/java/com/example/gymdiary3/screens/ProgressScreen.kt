package com.example.gymdiary3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel

@Composable
fun ProgressScreen(nav: NavHostController, viewModel: WorkoutViewModel) {

    val workouts by viewModel.workouts.collectAsState()
    val grouped = workouts.groupBy { it.exercise }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Progress Tracker") })
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

            LazyColumn(Modifier.weight(1f)) {
                grouped.forEach { (exercise, sets) ->
                    val pr = sets.maxByOrNull { it.weight * it.reps }
                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    exercise,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                
                                pr?.let {
                                    Text("All-Time PR: ${it.weight}kg x ${it.reps}", style = MaterialTheme.typography.bodyLarge)
                                }
                                
                                Spacer(Modifier.height(8.dp))
                                Text("Recent History:", style = MaterialTheme.typography.labelLarge)
                                
                                sets.take(5).forEach { set ->
                                    Text("• ${set.weight}kg x ${set.reps}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { nav.popBackStack() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Back")
            }
        }
    }
}
