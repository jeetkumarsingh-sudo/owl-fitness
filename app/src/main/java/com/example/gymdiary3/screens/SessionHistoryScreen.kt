package com.example.gymdiary3.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import com.example.gymdiary3.ui.theme.BackgroundDark
import com.example.gymdiary3.ui.theme.CardDark
import com.example.gymdiary3.ui.theme.PrimaryText
import com.example.gymdiary3.ui.theme.SecondaryText
import com.example.gymdiary3.ui.theme.Accent
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
            containerColor = CardDark,
            title = { Text("Delete Session", color = PrimaryText) },
            text = { Text("Are you sure you want to delete this workout session?", color = SecondaryText) },
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
                    Text("CANCEL", color = SecondaryText)
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.deleteEmptySessions()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(BackgroundDark),
        topBar = { 
            TopAppBar(
                title = { Text("SESSION HISTORY", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = PrimaryText
                )
            ) 
        }
    ) { padding ->
        val filteredSessions = remember(sessionsWithSets) {
            sessionsWithSets.filter { it.totalVolume > 0 }
        }

        Column(modifier = Modifier.padding(padding).fillMaxSize().background(BackgroundDark)) {
            if (filteredSessions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No workouts yet", color = SecondaryText)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
        colors = CardDefaults.cardColors(containerColor = CardDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = sdf.format(Date(session.startTime)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                
                val duration = sessionWithSets.duration / 60000
                if (duration > 0) {
                    Text(
                        text = "$duration min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText
                    )
                }
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = timeSdf.format(Date(session.startTime)),
                style = MaterialTheme.typography.bodyMedium,
                color = Accent
            )
            
            Text(
                text = "${sessionWithSets.totalVolume.toInt()} kg total",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
        }
    }
}
