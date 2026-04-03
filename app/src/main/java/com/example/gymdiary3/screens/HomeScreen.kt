package com.example.gymdiary3.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
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
    val workouts by viewModel.workouts.collectAsState()

    Column(Modifier.padding(20.dp).fillMaxSize()) {

        Text("Gym Diary", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { nav.navigate("plan") },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Workout Plan") }

        Spacer(Modifier.height(10.dp))

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
                    val fileName = "GymDiary_Export_${System.currentTimeMillis()}.csv"
                    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

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

                        val uri: Uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "Gym Diary Export")
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        
                        val chooser = Intent.createChooser(intent, "Share CSV")
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)

                    } catch (e: Exception) {
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) { Text("Export & Share CSV") }
    }
}
