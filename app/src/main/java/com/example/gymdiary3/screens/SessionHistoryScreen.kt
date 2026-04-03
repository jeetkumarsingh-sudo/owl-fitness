package com.example.gymdiary3.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionHistoryScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    val sessions by viewModel.sessions.collectAsState()
    val sdf = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Session History") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            items(sessions) { session ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        .clickable { nav.navigate("summary/${session.id}") }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Session #${session.id}", style = MaterialTheme.typography.titleMedium)
                        Text("Start: ${sdf.format(Date(session.startTime))}")
                        session.endTime?.let {
                            val duration = (it - session.startTime) / 60000
                            Text("End: ${sdf.format(Date(it))}")
                            Text("Duration: $duration min", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}
