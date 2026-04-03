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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(nav: NavHostController, viewModel: WorkoutViewModel) {

    val workouts by viewModel.allWorkouts.collectAsState()
    
    // Group workouts by exercise name
    val groupedWorkouts = workouts.groupBy { it.exercise }

    Column(Modifier.padding(20.dp).fillMaxSize()) {

        Text(
            "Workout History",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(20.dp))

        LazyColumn(Modifier.weight(1f)) {
            groupedWorkouts.forEach { (exercise, sets) ->
                item {
                    Text(
                        text = exercise,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(sets) { workout ->
                    val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(workout.date))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Set ${workout.set}", fontWeight = FontWeight.SemiBold)
                                Text(date, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${workout.weight}kg × ${workout.reps}")
                            if (workout.support) {
                                Text("Support used", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }
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
