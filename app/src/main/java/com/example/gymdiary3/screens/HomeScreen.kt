package com.example.gymdiary3.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    nav: NavHostController,
    viewModel: WorkoutViewModel,
    context: Context
) {

    val scope = rememberCoroutineScope()
    val sessionsWithSets by viewModel.sessionsWithSets.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Owl Fitness",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (currentSessionId != null) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = if (currentSessionId != null) "WORKOUT IN PROGRESS" else "READY FOR GYM?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (currentSessionId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(8.dp))

                if (currentSessionId != null) {
                    Button(
                        onClick = { 
                            viewModel.endSession { sessionId ->
                                nav.navigate("summary/$sessionId")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { 
                        Text("FINISH SESSION", fontWeight = FontWeight.Bold) 
                    }
                } else {
                    Button(
                        onClick = { viewModel.startSession() },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) { 
                        Text("START NEW SESSION", fontWeight = FontWeight.Bold) 
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MenuButton(
                text = "LOG EXERCISES",
                modifier = Modifier.weight(1f).height(120.dp),
                onClick = {
                    if (currentSessionId != null) {
                        nav.navigate("muscle")
                    } else {
                        Toast.makeText(context, "Start a session first!", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
            MenuButton(
                text = "PROGRESS & PRs",
                modifier = Modifier.weight(1f).height(120.dp),
                onClick = { nav.navigate("progress") },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MenuButton(
                text = "HISTORY",
                modifier = Modifier.weight(1f).height(80.dp),
                onClick = { nav.navigate("history") }
            )
            MenuButton(
                text = "BODY WEIGHT",
                modifier = Modifier.weight(1f).height(80.dp),
                onClick = { nav.navigate("weight") }
            )
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = {
                scope.launch {
                    Log.d("CSV_EXPORT", "Export started. Sessions: ${sessionsWithSets.size}")
                    if (sessionsWithSets.isEmpty()) {
                        Log.d("CSV_EXPORT", "No data to export")
                        Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    try {
                        val file = File(context.cacheDir, "workout_data.csv")
                        val writer = FileWriter(file)
                        writer.append("Date,Muscle,Exercise,Set,Reps,Weight,Support\n")

                        sessionsWithSets.forEach { sessionWithSets ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val dateStr = sdf.format(Date(sessionWithSets.date))
                            
                            sessionWithSets.sets.forEach { workout ->
                                writer.append("$dateStr,${workout.muscle},${workout.exercise},${workout.setNumber},${workout.reps},${workout.weight},${workout.support}\n")
                            }
                        }

                        writer.flush()
                        writer.close()

                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        context.startActivity(Intent.createChooser(intent, "Export CSV"))
                        Log.d("CSV_EXPORT", "Export success")
                    } catch (e: Exception) {
                        Log.e("CSV_EXPORT", "Export failed: ${e.message}")
                        e.printStackTrace()
                        Toast.makeText(context, "Export data failed", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) { Text("EXPORT DATA (CSV)", style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
fun MenuButton(
    text: String, 
    onClick: () -> Unit, 
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        }
    }
}
