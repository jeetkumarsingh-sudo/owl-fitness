package com.example.gymdiary3.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    val sessionsWithSets by viewModel.sessionsWithSets.collectAsState()
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
        val filteredSessions = remember(sessionsWithSets) {
            sessionsWithSets.filter { it.totalVolume > 0 }
        }

        if (filteredSessions.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No workouts yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredSessions,
                    key = { it.session.id }
                ) { sessionWithSets ->
                    SessionCard(sessionWithSets, nav, sdf, timeSdf) {
                        showDeleteDialog = it
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionCard(
    sessionWithSets: com.example.gymdiary3.data.SessionWithSets,
    nav: NavHostController,
    sdf: SimpleDateFormat,
    timeSdf: SimpleDateFormat,
    onLongClick: (Int) -> Unit
) {
    val session = sessionWithSets.session
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { nav.navigate("summary/${session.id}") },
                onLongClick = { onLongClick(session.id) }
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
                
                val duration = sessionWithSets.duration / 60000
                if (duration > 0) {
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
            
            Text(
                text = "${sessionWithSets.totalVolume.toInt()} kg total",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

