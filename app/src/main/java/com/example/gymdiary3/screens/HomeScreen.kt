package com.example.gymdiary3.screens

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import com.example.gymdiary3.ui.components.LoadingOverlay
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
    val sessionsWithSets by viewModel.sessions.collectAsStateWithLifecycle()
    val currentSessionId by viewModel.sessionManager.currentSessionId.collectAsStateWithLifecycle()

    var isVisible by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OWL FITNESS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .alpha(animateFloatAsState(if (isVisible) 1f else 0f, animationSpec = tween(200)).value)
            )

            IconButton(
                onClick = { nav.navigate("settings") },
                modifier = Modifier.alpha(animateFloatAsState(if (isVisible) 1f else 0f, animationSpec = tween(200)).value)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = if (currentSessionId != null) "WORKOUT IN PROGRESS" else "READY FOR GYM?",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (currentSessionId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
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
                            modifier = Modifier.fillMaxWidth().height(64.dp).scale(finishScale.value),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = MaterialTheme.shapes.medium
                        ) { 
                            Text("FINISH SESSION", style = MaterialTheme.typography.titleLarge) 
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
                            modifier = Modifier.fillMaxWidth().height(64.dp).scale(startScale.value),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = MaterialTheme.shapes.medium
                        ) { 
                            Text("START NEW SESSION", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) {
                    MenuButton(
                        text = "LOG EXERCISES",
                        modifier = Modifier.height(140.dp),
                        onClick = {
                            if (currentSessionId != null) {
                                nav.navigate("muscle")
                            } else {
                                Toast.makeText(context, "Start a session first!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) {
                    MenuButton(
                        text = "PROGRESS & PRs",
                        modifier = Modifier.height(140.dp),
                        onClick = { nav.navigate("progress") }
                    )
                }
            }
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) {
                    MenuButton(
                        text = "VOLUME GRAPH",
                        modifier = Modifier.height(100.dp),
                        onClick = { nav.navigate("graph") }
                    )
                }
            }
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) {
                    MenuButton(
                        text = "HISTORY",
                        modifier = Modifier.height(100.dp),
                        onClick = { nav.navigate("history") }
                    )
                }
            }
            item {
                AnimatedVisibility(visible = isVisible, enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }) {
                    MenuButton(
                        text = "BODY WEIGHT",
                        modifier = Modifier.height(100.dp),
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
                    
                    isExporting = true
                    val uri = viewModel.exportAllDataToCsv(context)
                    isExporting = false
                    
                    if (uri != null) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Export CSV"))
                        Log.d("CSV_EXPORT", "Export success")
                    } else {
                        Log.d("CSV_EXPORT", "No data to export or export failed")
                        Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp).scale(exportScale.value),
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) { Text("EXPORT DATA (CSV)", style = MaterialTheme.typography.labelLarge) }
    }

    if (isExporting) {
        LoadingOverlay(message = "GENERATING CSV...")
    }
}
}

@Composable
fun MenuButton(
    text: String, 
    onClick: () -> Unit, 
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Surface(
        onClick = {
            scope.launch {
                scale.animateTo(0.95f, tween(100))
                scale.animateTo(1f, tween(100))
            }
            onClick()
        },
        modifier = modifier.scale(scale.value),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
