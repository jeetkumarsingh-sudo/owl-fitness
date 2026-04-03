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
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseScreen(nav: NavHostController, muscle: String, viewModel: WorkoutViewModel) {

    val exercises by viewModel.exercises.collectAsState()

    LaunchedEffect(muscle) {
        viewModel.selectMuscle(muscle)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newExerciseName by remember { mutableStateOf("") }

    Column(Modifier.padding(20.dp).fillMaxSize()) {

        Text("$muscle Exercises", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add Exercise") }

        Spacer(Modifier.height(20.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(exercises) { exercise ->
                var showMenu by remember { mutableStateOf(false) }

                Text(
                    exercise.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                nav.navigate("set/$muscle/${Uri.encode(exercise.name)}")
                            },
                            onLongClick = { showMenu = true }
                        )
                        .padding(16.dp)
                )

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
                HorizontalDivider()
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
                }) { Text("Add") }
            },
            dismissButton = {
                Button(onClick = { 
                    showAddDialog = false
                    newExerciseName = "" 
                }) { Text("Cancel") }
            },
            title = { Text("Add Exercise") },
            text = {
                OutlinedTextField(
                    value = newExerciseName,
                    onValueChange = { newExerciseName = it },
                    label = { Text("Exercise name") }
                )
            }
        )
    }
}
