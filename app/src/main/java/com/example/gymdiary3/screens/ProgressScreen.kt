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

    Column(Modifier.padding(20.dp).fillMaxSize()) {

        Text(
            "Progress Tracker",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(20.dp))

        LazyColumn(Modifier.weight(1f)) {
            grouped.forEach { (exercise, sets) ->
                val best = sets.maxOfOrNull { it.weight } ?: 0.0
                val total = sets.size

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                exercise,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Best Weight: $best kg")
                            Text("Total Sets Logged: $total")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { nav.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}
