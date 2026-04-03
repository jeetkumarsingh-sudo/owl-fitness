package com.example.gymdiary3.screens

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

@Composable
fun HomeScreen(
    nav: NavHostController,
    viewModel: WorkoutViewModel,
    context: Context
) {

    val scope = rememberCoroutineScope()
    val workouts by viewModel.allWorkouts.collectAsState()

    Column(Modifier.padding(20.dp).fillMaxSize()) {

        Text("Gym Diary", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { nav.navigate("muscle") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Start Workout") }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { nav.navigate("history") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Workout History") }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { nav.navigate("progress") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Progress") }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { nav.navigate("weight") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Body Weight Tracker") }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                scope.launch {
                    val file = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                        "gymdiary_export.csv"
                    )

                    try {
                        val writer = FileWriter(file)
                        writer.append("Date,Muscle,Exercise,Set,Reps,Weight,Support\n")

                        workouts.forEach { workout ->
                            writer.append(
                                "${workout.date}," +
                                        "${workout.muscle}," +
                                        "${workout.exercise}," +
                                        "${workout.set}," +
                                        "${workout.reps}," +
                                        "${workout.weight}," +
                                        "${workout.support}\n"
                            )
                        }

                        writer.flush()
                        writer.close()

                        Toast.makeText(
                            context,
                            "Exported to ${file.absolutePath}",
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) { Text("Export Data to CSV") }
    }
}
