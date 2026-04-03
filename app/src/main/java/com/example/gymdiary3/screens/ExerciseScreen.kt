package com.example.gymdiary3.screens

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseScreen(nav: NavHostController, muscle: String) {

    val exerciseMap = remember {
        mutableStateMapOf(

            "Back" to mutableStateListOf(
                "Deadlift",
                "Rack Pull",
                "Pull Up",
                "Lat Pulldown",
                "Barbell Row",
                "Dumbbell Row",
                "Face Pull",
                "T-Bar Row",
                "Seated Cable Row"
            ),

            "Chest" to mutableStateListOf(
                "Bench Press",
                "Incline Bench Press",
                "Incline Dumbbell Press",
                "Cable Fly",
                "Pec Deck",
                "Flat Dumbbell Fly",
                "Machine Chest Press"
            ),

            "Legs" to mutableStateListOf(
                "Squat",
                "Romanian Deadlift",
                "Leg Press",
                "Walking Lunges",
                "Leg Curl",
                "Calf Raise"
            ),

            "Shoulders" to mutableStateListOf(
                "Overhead Press",
                "Dumbbell Lateral Raise",
                "Rear Delt Fly",
                "Upright Row"
            ),

            "Biceps" to mutableStateListOf(
                "Barbell Curl",
                "Hammer Curl",
                "Alt Dumbbell Curl",
                "Preacher Curl"
            ),

            "Triceps" to mutableStateListOf(
                "Overhead Triceps Extension",
                "Triceps Pushdown",
                "Rope Pushdown",
                "Close Grip Bench Press"
            ),

            "Abs" to mutableStateListOf(
                "Hanging Leg Raise",
                "Cable Crunch"
            )
        )
    }

    val exercises = exerciseMap[muscle] ?: mutableStateListOf()

    var showAddDialog by remember { mutableStateOf(false) }
    var newExercise by remember { mutableStateOf("") }

    Column(Modifier.padding(20.dp)) {

        Text("$muscle Exercises", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add Exercise") }

        Spacer(Modifier.height(20.dp))

        LazyColumn {

            items(exercises) { exercise ->

                var showMenu by remember { mutableStateOf(false) }

                Text(
                    exercise,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                nav.navigate("set/$muscle/${Uri.encode(exercise)}")
                            },
                            onLongClick = { showMenu = true }
                        )
                        .padding(12.dp)
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {

                    DropdownMenuItem(
                        text = { Text("Delete Exercise") },
                        onClick = {
                            exercises.remove(exercise)
                            showMenu = false
                        }
                    )
                }

                Divider()
            }
        }
    }

    if (showAddDialog) {

        AlertDialog(
            onDismissRequest = { showAddDialog = false },

            confirmButton = {
                Button(onClick = {

                    if (newExercise.isNotBlank())
                        exercises.add(newExercise)

                    newExercise = ""
                    showAddDialog = false

                }) { Text("Add") }
            },

            dismissButton = {
                Button(onClick = { showAddDialog = false }) { Text("Cancel") }
            },

            title = { Text("Add Exercise") },

            text = {
                OutlinedTextField(
                    value = newExercise,
                    onValueChange = { newExercise = it },
                    label = { Text("Exercise name") }
                )
            }
        )
    }
}