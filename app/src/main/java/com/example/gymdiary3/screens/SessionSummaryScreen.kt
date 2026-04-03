package com.example.gymdiary3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.SessionSummary
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(nav: NavHostController, viewModel: WorkoutViewModel, sessionId: Int) {
    var summary by remember { mutableStateOf<SessionSummary?>(null) }

    LaunchedEffect(sessionId) {
        summary = viewModel.getSessionSummary(sessionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Workout Summary") })
        }
    ) { padding ->
        summary?.let { s ->
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                item {
                    Text("Total Sets: ${s.totalSets}", style = MaterialTheme.typography.titleLarge)
                    Text("Total Volume: ${s.totalVolume} kg", style = MaterialTheme.typography.titleMedium)
                    val minutes = s.duration / 60000
                    Text("Duration: $minutes min", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                }

                s.exercises.forEach { (exercise, sets) ->
                    item {
                        Text(exercise, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    items(sets) { set ->
                        Text("Set ${set.set}: ${set.weight}kg x ${set.reps}", modifier = Modifier.padding(start = 8.dp))
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                item {
                    Button(
                        onClick = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("Back to Home")
                    }
                }
            }
        }
    }
}
