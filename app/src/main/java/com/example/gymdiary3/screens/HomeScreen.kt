package com.example.gymdiary3.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val sessionsWithSets by viewModel.sessionsWithSets.collectAsStateWithLifecycle()
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Owl Fitness",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .alpha(animateFloatAsState(if (isVisible) 1f else 0f, animationSpec = tween(200)).value)
        )

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = if (currentSessionId != null) "WORKOUT IN PROGRESS" else "READY FOR GYM?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (currentSessionId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                    
                    Spacer(Modifier.height(8.dp))

                    if (currentSessionId != null) {
                        val finishScale = remember { Animatable(1f) }
                        Button(
                            onClick = { 
                                scope.launch {
                                    finishScale.animateTo(0.95f, tween(100))
                                    finishScale.animateTo(1f, tween(100))
                                }
                                viewModel.endSession { sessionId ->
                                    nav.navigate("summary/$sessionId")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).scale(finishScale.value),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { 
                            Text("FINISH SESSION", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onError) 
                        }
                    } else {
                        val startScale = remember { Animatable(1f) }
                        Button(
                            onClick = { 
                                scope.launch {
                                    startScale.animateTo(0.95f, tween(100))
                                    startScale.animateTo(1f, tween(100))
                                }
                                viewModel.startSession() 
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).scale(startScale.value),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { 
                            Text("START NEW SESSION", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) {
                    MenuButton(
                        text = "LOG EXERCISES",
                        modifier = Modifier.height(120.dp),
                        onClick = {
                            if (currentSessionId != null) {
                                nav.navigate("muscle")
                            } else {
                                Toast.makeText(context, "Start a session first!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) {
                    MenuButton(
                        text = "PROGRESS & PRs",
                        modifier = Modifier.height(120.dp),
                        onClick = { nav.navigate("progress") },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
            }
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) {
                    MenuButton(
                        text = "VOLUME GRAPH",
                        modifier = Modifier.height(80.dp),
                        onClick = { nav.navigate("graph") }
                    )
                }
            }
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) {
                    MenuButton(
                        text = "HISTORY",
                        modifier = Modifier.height(80.dp),
                        onClick = { nav.navigate("history") }
                    )
                }
            }
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) {
                    MenuButton(
                        text = "BODY WEIGHT",
                        modifier = Modifier.height(80.dp),
                        onClick = { nav.navigate("weight") }
                    )
                }
            }
        }

        val exportScale = remember { Animatable(1f) }
        OutlinedButton(
            onClick = {
                scope.launch {
                    exportScale.animateTo(0.95f, tween(100))
                    exportScale.animateTo(1f, tween(100))
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
                                val supportStr = if (workout.support) "Yes" else "No"
                                writer.append("$dateStr,${workout.muscle},${workout.exercise},${workout.setNumber},${workout.reps},${workout.weight},$supportStr\n")
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
            modifier = Modifier.fillMaxWidth().height(48.dp).scale(exportScale.value)
        ) { Text("EXPORT DATA (CSV)", style = MaterialTheme.typography.labelLarge) }
    }
}

@Composable
fun MenuButton(
    text: String, 
    onClick: () -> Unit, 
    modifier: Modifier = Modifier,
    containerColor: Color? = null
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Card(
        onClick = {
            scope.launch {
                scale.animateTo(0.95f, tween(100))
                scale.animateTo(1f, tween(100))
            }
            onClick()
        },
        modifier = modifier.scale(scale.value),
        colors = CardDefaults.cardColors(containerColor = containerColor ?: MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
