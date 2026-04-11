package com.example.gymdiary3.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.ui.components.LoadingOverlay
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import com.example.gymdiary3.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    nav: NavHostController,
    viewModel: WorkoutViewModel,
    bodyViewModel: BodyWeightViewModel,
    context: Context
) {
    val scope = rememberCoroutineScope()
    val currentSessionId by viewModel.sessionManager.currentSessionId.collectAsStateWithLifecycle()
    val latestWeight by bodyViewModel.latestBodyWeight.collectAsStateWithLifecycle()
    val totalWorkouts by viewModel.totalWorkoutCount.collectAsStateWithLifecycle(0)

    var isVisible by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(Modifier.fillMaxSize().background(OwlColors.DeepBg)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OWL FITNESS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OwlColors.TextPrimary,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.alpha(animateFloatAsState(if (isVisible) 1f else 0f, tween(300)).value)
                )

                IconButton(onClick = { nav.navigate("settings") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = OwlColors.TextSecondary)
                }
            }

            // Session Card
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = OwlColors.CardBg,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OwlColors.BorderSubtle)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = if (currentSessionId != null) "WORKOUT IN PROGRESS" else "READY FOR GYM?",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (currentSessionId != null) OwlColors.Purple else OwlColors.TextSecondary,
                            letterSpacing = 1.sp
                        )
                        
                        Spacer(Modifier.height(12.dp))

                        if (currentSessionId != null) {
                            Button(
                                onClick = { 
                                    viewModel.endSession { sessionId ->
                                        nav.navigate("summary/$sessionId")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OwlColors.RedNegative),
                                shape = RoundedCornerShape(12.dp)
                            ) { 
                                Text("FINISH SESSION", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) 
                            }
                        } else {
                            Button(
                                onClick = { viewModel.startSession() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OwlColors.Purple),
                                shape = RoundedCornerShape(12.dp)
                            ) { 
                                Text("START NEW SESSION", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Stats
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
            ) {
                Row(
                    Modifier.fillMaxWidth().height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeQuickStatCard(
                        label = "WORKOUTS",
                        value = totalWorkouts.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    HomeQuickStatCard(
                        label = "BODY WEIGHT",
                        value = latestWeight?.let { "${it.weight}kg" } ?: "--",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Menu Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MenuButton(
                        text = "LOG WORKOUT",
                        icon = Icons.Default.Add,
                        color = OwlColors.Purple,
                        onClick = {
                            if (currentSessionId != null) nav.navigate("muscle")
                            else Toast.makeText(context, "Start a session first!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                item {
                    MenuButton(
                        text = "BODY WEIGHT",
                        icon = Icons.Default.MonitorWeight,
                        color = OwlColors.GreenBulk,
                        onClick = { nav.navigate("weight") }
                    )
                }
                item {
                    MenuButton(
                        text = "ANALYTICS",
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        color = OwlColors.PurpleSoft,
                        onClick = { nav.navigate("progress") }
                    )
                }
                item {
                    MenuButton(
                        text = "HISTORY",
                        icon = Icons.Default.History,
                        color = OwlColors.TextSecondary,
                        onClick = { nav.navigate("history") }
                    )
                }
            }

            // Export
            OutlinedButton(
                onClick = {
                    scope.launch {
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
                        } else {
                            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OwlColors.TextSecondary),
                border = BorderStroke(1.dp, OwlColors.BorderSubtle)
            ) { Text("EXPORT DATA (CSV)", style = MaterialTheme.typography.labelLarge) }
        }

        if (isExporting) {
            LoadingOverlay(message = "GENERATING CSV...")
        }
    }
}

@Composable
fun HomeQuickStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = OwlColors.TextSecondary)
            Text(value, style = MaterialTheme.typography.titleLarge, color = OwlColors.Purple, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Surface(
        onClick = {
            scope.launch {
                scale.animateTo(0.92f, tween(100))
                scale.animateTo(1f, tween(100))
                onClick()
            }
        },
        modifier = modifier.height(140.dp).scale(scale.value),
        color = OwlColors.CardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OwlColors.BorderSubtle)
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                color = OwlColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
