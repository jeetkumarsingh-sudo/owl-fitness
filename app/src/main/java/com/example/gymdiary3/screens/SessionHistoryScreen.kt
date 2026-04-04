package com.example.gymdiary3.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SessionHistoryScreen(nav: NavHostController, viewModel: WorkoutViewModel) {
    val sessions by viewModel.sessions.collectAsState(initial = emptyList())
    val sdf = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    var showDeleteDialog by remember { mutableStateOf<Int?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Session") },
            text = { Text("Are you sure you want to delete this workout session?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { viewModel.deleteSession(it) }
                    showDeleteDialog = null
                }) {
                    Text("DELETE", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("CANCEL")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.deleteEmptySessions()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Session History", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        val filteredSessions = sessions.filter { session ->
            val duration = session.endTime?.let { (it - session.startTime) / 60000 } ?: 0
            // We can't easily check volume here without loading sets, but we can rely on duration
            // or better, rely on the fact that deleteEmptySessions() runs and endSession avoids empty ones.
            // For now, let's filter out sessions with 0 duration if they are finished.
            session.endTime == null || duration > 0
        }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredSessions) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { nav.navigate("summary/${session.id}") },
                            onLongClick = { showDeleteDialog = session.id }
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = sdf.format(Date(session.startTime)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            session.endTime?.let {
                                val duration = (it - session.startTime) / 60000
                                Text(
                                    text = "$duration min",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(4.dp))
                        
                        Text(
                            text = timeSdf.format(Date(session.startTime)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

