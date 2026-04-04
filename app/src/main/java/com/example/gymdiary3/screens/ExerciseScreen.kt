package com.example.gymdiary3.screens

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(nav: NavHostController, muscle: String, viewModel: WorkoutViewModel) {

    val exercises by remember(viewModel) { viewModel.exercisesByMuscle }.collectAsState()

    LaunchedEffect(muscle) {
        viewModel.selectMuscle(muscle)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(muscle, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Text("+", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            )
        }
    ) { padding ->
        if (exercises.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No exercises yet", color = androidx.compose.ui.graphics.Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = exercises,
                    key = { it.name }
                ) { exercise ->
                var showMenu by remember { mutableStateOf(false) }

                Box {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    nav.navigate("set/$muscle/${Uri.encode(exercise.name)}")
                                },
                                onLongClick = { showMenu = true }
                            ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                exercise.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Exercise") },
                            onClick = {
                                viewModel.deleteExercise(exercise)
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (newExerciseName.isNotBlank()) {
                        viewModel.addExercise(newExerciseName, muscle)
                    }
                    showAddDialog = false
                    newExerciseName = ""
                }) { Text("ADD") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    newExerciseName = "" 
                }) { Text("CANCEL") }
            },
            title = { Text("Add $muscle Exercise") },
            text = {
                OutlinedTextField(
                    value = newExerciseName,
                    onValueChange = { newExerciseName = it },
                    label = { Text("Exercise Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}
